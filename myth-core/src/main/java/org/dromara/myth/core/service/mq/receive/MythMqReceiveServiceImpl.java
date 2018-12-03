/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.myth.core.service.mq.receive;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.bean.entity.MythInvocation;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.bean.mq.MessageEntity;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.enums.EventTypeEnum;
import org.dromara.myth.common.enums.MythRoleEnum;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.exception.MythException;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.MythMqReceiveService;
import org.dromara.myth.core.service.mq.send.MythSendMessageServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MythMqReceiveServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
@Service("mythMqReceiveService")
public class MythMqReceiveServiceImpl implements MythMqReceiveService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythMqReceiveServiceImpl.class);

    private static final Lock LOCK = new ReentrantLock();

    private volatile ObjectSerializer serializer;

    private final MythCoordinatorService mythCoordinatorService;

    private final MythTransactionEventPublisher publisher;

    private final MythConfig mythConfig;

    @Autowired
    public MythMqReceiveServiceImpl(MythCoordinatorService mythCoordinatorService, MythTransactionEventPublisher publisher, MythConfig mythConfig) {
        this.mythCoordinatorService = mythCoordinatorService;
        this.publisher = publisher;
        this.mythConfig = mythConfig;
    }

    @Override
    public Boolean processMessage(final byte[] message) {
        try {
            MessageEntity entity;
            try {
                entity = getObjectSerializer().deSerialize(message, MessageEntity.class);
            } catch (MythException e) {
                e.printStackTrace();
                throw new MythRuntimeException(e.getMessage());
            }
            /*
             * 1 检查该事务有没被处理过，已经处理过的 则不处理
             * 2 发起发射调用，调用接口，进行处理
             * 3 记录本地日志
             */
            LOCK.lock();
            final String transId = entity.getTransId();
            final MythTransaction mythTransaction = mythCoordinatorService.findByTransId(transId);
            //第一次调用 也就是服务down机，或者没有调用到的时候， 通过mq执行
            if (Objects.isNull(mythTransaction)) {
                try {
                    execute(entity);
                    //执行成功 保存成功的日志
                    final MythTransaction log = buildTransactionLog(transId, "",
                            MythStatusEnum.COMMIT.getCode(),
                            entity.getMythInvocation().getTargetClass().getName(),
                            entity.getMythInvocation().getMethodName());
                    //submit(new CoordinatorAction(CoordinatorActionEnum.SAVE, log));
                    publisher.publishEvent(log, EventTypeEnum.SAVE.getCode());
                } catch (Exception e) {
                    //执行失败保存失败的日志
                    final MythTransaction log = buildTransactionLog(transId, e.getMessage(),
                            MythStatusEnum.FAILURE.getCode(),
                            entity.getMythInvocation().getTargetClass().getName(),
                            entity.getMythInvocation().getMethodName());
                    publisher.publishEvent(log, EventTypeEnum.SAVE.getCode());
                    throw new MythRuntimeException(e);
                } finally {
                    TransactionContextLocal.getInstance().remove();
                }
            } else {
                //如果是执行失败的话
                if (mythTransaction.getStatus() == MythStatusEnum.FAILURE.getCode()) {
                    //如果超过了最大重试次数 则不执行
                    if (mythTransaction.getRetriedCount() >= mythConfig.getRetryMax()) {
                        LogUtil.error(LOGGER, () -> "此事务已经超过了最大重试次数:" + mythConfig.getRetryMax()
                                + " ,执行接口为:" + entity.getMythInvocation().getTargetClass() + " ,方法为:"
                                + entity.getMythInvocation().getMethodName() + ",事务id为：" + entity.getTransId());
                        return Boolean.FALSE;
                    }
                    try {
                        execute(entity);
                        //执行成功 更新日志为成功
                        mythTransaction.setStatus(MythStatusEnum.COMMIT.getCode());
                        publisher.publishEvent(mythTransaction, EventTypeEnum.UPDATE_STATUS.getCode());

                    } catch (Throwable e) {
                        //执行失败，设置失败原因和重试次数
                        mythTransaction.setErrorMsg(e.getMessage());
                        mythTransaction.setRetriedCount(mythTransaction.getRetriedCount() + 1);
                        publisher.publishEvent(mythTransaction, EventTypeEnum.UPDATE_FAIR.getCode());
                        throw new MythRuntimeException(e);
                    } finally {
                        TransactionContextLocal.getInstance().remove();
                    }
                }
            }

        } finally {
            LOCK.unlock();
        }
        return Boolean.TRUE;
    }

    private void execute(final MessageEntity entity) throws Exception {
        //设置事务上下文，这个类会传递给远端
        MythTransactionContext context = new MythTransactionContext();
        //设置事务id
        context.setTransId(entity.getTransId());
        //设置为发起者角色
        context.setRole(MythRoleEnum.LOCAL.getCode());
        TransactionContextLocal.getInstance().set(context);
        executeLocalTransaction(entity.getMythInvocation());
    }

    @SuppressWarnings("unchecked")
    private void executeLocalTransaction(final MythInvocation mythInvocation) throws Exception {
        if (Objects.nonNull(mythInvocation)) {
            final Class clazz = mythInvocation.getTargetClass();
            final String method = mythInvocation.getMethodName();
            final Object[] args = mythInvocation.getArgs();
            final Class[] parameterTypes = mythInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "Myth执行本地协调事务:{}", () -> mythInvocation.getTargetClass() + ":" + mythInvocation.getMethodName());

        }
    }

    private MythTransaction buildTransactionLog(final String transId, final String errorMsg, final Integer status,
                                                final String targetClass, final String targetMethod) {
        MythTransaction logTransaction = new MythTransaction(transId);
        logTransaction.setRetriedCount(1);
        logTransaction.setStatus(status);
        logTransaction.setErrorMsg(errorMsg);
        logTransaction.setRole(MythRoleEnum.PROVIDER.getCode());
        logTransaction.setTargetClass(targetClass);
        logTransaction.setTargetMethod(targetMethod);
        return logTransaction;
    }

    private synchronized ObjectSerializer getObjectSerializer() {
        if (serializer == null) {
            synchronized (MythSendMessageServiceImpl.class) {
                if (serializer == null) {
                    serializer = SpringBeanUtils.getInstance().getBean(ObjectSerializer.class);
                }
            }
        }
        return serializer;
    }
}
