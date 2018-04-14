/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.myth.core.coordinator.impl;


import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.bean.mq.MessageEntity;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.enums.CoordinatorActionEnum;
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.exception.MythException;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.serializer.ObjectSerializer;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.concurrent.threadpool.MythTransactionThreadFactory;
import com.github.myth.core.concurrent.threadpool.MythTransactionThreadPool;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.coordinator.command.CoordinatorAction;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.ApplicationService;
import com.github.myth.core.service.MythMqSendService;
import com.github.myth.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaoyu
 */
@Service("coordinatorService")
public class CoordinatorServiceImpl implements CoordinatorService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatorServiceImpl.class);

    private static BlockingQueue<CoordinatorAction> QUEUE;

    private MythConfig mythConfig;

    private CoordinatorRepository coordinatorRepository;

    private final ApplicationService applicationService;


    private static volatile MythMqSendService mythMqSendService;

    private static final Lock LOCK = new ReentrantLock();


    private ObjectSerializer serializer;

    @Autowired
    public CoordinatorServiceImpl(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }


    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }


    /**
     * 保存本地事务日志
     *
     * @param mythConfig 配置信息
     * @throws MythException 异常
     */
    @Override
    public void start(MythConfig mythConfig) {
        this.mythConfig = mythConfig;

        coordinatorRepository = SpringBeanUtils.getInstance().getBean(CoordinatorRepository.class);

        final String repositorySuffix = buildRepositorySuffix(mythConfig.getRepositorySuffix());
        //初始化spi 协调资源存储
        coordinatorRepository.init(repositorySuffix, mythConfig);


    }


    /**
     * 保存本地事务信息
     *
     * @param mythTransaction 实体对象
     * @return 主键 transId
     */
    @Override
    public String save(MythTransaction mythTransaction) {
        final int rows = coordinatorRepository.create(mythTransaction);
        if (rows > 0) {
            return mythTransaction.getTransId();
        }
        return null;
    }

    @Override
    public MythTransaction findByTransId(String transId) {
        return coordinatorRepository.findByTransId(transId);
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<MythTransaction>
     */
    @Override
    public List<MythTransaction> listAllByDelay(Date date) {
        return coordinatorRepository.listAllByDelay(date);
    }

    /**
     * 删除补偿事务信息
     *
     * @param transId 事务id
     * @return true成功 false 失败
     */
    @Override
    public boolean remove(String transId) {
        return coordinatorRepository.remove(transId) > 0;
    }

    /**
     * 更新
     *
     * @param mythTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int update(MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.update(mythTransaction);
    }

    /**
     * 更新事务失败日志
     *
     * @param mythTransaction 实体对象
     * @return rows 1 成功
     * @throws MythRuntimeException
     */
    @Override
    public int updateFailTransaction(MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.updateFailTransaction(mythTransaction);
    }

    /**
     * 更新 List<MythParticipant>  只更新这一个字段数据
     *
     * @param mythTransaction 实体对象
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int updateParticipant(MythTransaction mythTransaction) throws MythRuntimeException {
        return coordinatorRepository.updateParticipant(mythTransaction);
    }


    /**
     * 更新本地日志状态
     *
     * @param transId 事务id
     * @param status  状态
     * @return rows 1 rows 1 成功
     * @throws MythRuntimeException 异常信息
     */
    @Override
    public int updateStatus(String transId, Integer status) {
        return coordinatorRepository.updateStatus(transId, status);
    }

    /**
     * 接收到mq消息处理
     *
     * @param message 实体对象转换成byte[]后的数据
     * @return true 处理成功  false 处理失败
     */
    @Override
    public Boolean processMessage(byte[] message) {
        try {
            MessageEntity entity;
            try {
                entity = serializer.deSerialize(message, MessageEntity.class);
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
            final MythTransaction mythTransaction = findByTransId(transId);

            //第一次调用 也就是服务down机，或者没有调用到的时候， 通过mq执行
            if (Objects.isNull(mythTransaction)) {
                try {
                    handler(entity);
                    //执行成功 保存成功的日志
                    final MythTransaction log = buildTransactionLog(transId, "",
                            MythStatusEnum.COMMIT.getCode(),
                            entity.getMythInvocation().getTargetClass().getName(),
                            entity.getMythInvocation().getMethodName());
                    //submit(new CoordinatorAction(CoordinatorActionEnum.SAVE, log));
                } catch (Exception e) {
                    //执行失败保存失败的日志
                    final MythTransaction log = buildTransactionLog(transId, e.getMessage(),
                            MythStatusEnum.FAILURE.getCode(),
                            entity.getMythInvocation().getTargetClass().getName(),
                            entity.getMythInvocation().getMethodName());
                    //submit(new CoordinatorAction(CoordinatorActionEnum.SAVE, log));
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
                                + " ,执行接口为:" + entity.getMythInvocation().getTargetClass() + " ,方法为:" +
                                entity.getMythInvocation().getMethodName() + ",事务id为：" + entity.getTransId());
                        return Boolean.FALSE;
                    }
                    try {
                        handler(entity);
                        //执行成功 更新日志为成功
                        updateStatus(entity.getTransId(), MythStatusEnum.COMMIT.getCode());

                    } catch (Throwable e) {
                        //执行失败，设置失败原因和重试次数
                        mythTransaction.setErrorMsg(e.getCause().getMessage());
                        mythTransaction.setRetriedCount(mythTransaction.getRetriedCount() + 1);
                        updateFailTransaction(mythTransaction);
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


    private MythTransaction buildTransactionLog(String transId, String errorMsg, Integer status, String targetClass, String targetMethod) {
        MythTransaction logTransaction = new MythTransaction(transId);
        logTransaction.setRetriedCount(1);
        logTransaction.setStatus(status);
        logTransaction.setErrorMsg(errorMsg);
        logTransaction.setRole(MythRoleEnum.PROVIDER.getCode());
        logTransaction.setTargetClass(targetClass);
        logTransaction.setTargetMethod(targetMethod);
        return logTransaction;
    }



    private void handler(MessageEntity entity) throws Exception {
        //设置事务上下文，这个类会传递给远端
        MythTransactionContext context = new MythTransactionContext();
        //设置事务id
        context.setTransId(entity.getTransId());

        //设置为发起者角色
        context.setRole(MythRoleEnum.LOCAL.getCode());

        TransactionContextLocal.getInstance().set(context);

        executeLocalTransaction(entity.getMythInvocation());
    }

    private String buildRepositorySuffix(String repositorySuffix) {
        if (StringUtils.isNoneBlank(repositorySuffix)) {
            return repositorySuffix;
        } else {
            return applicationService.acquireName();
        }

    }

    @SuppressWarnings("unchecked")
    private void executeLocalTransaction(MythInvocation mythInvocation) throws Exception {
        if (Objects.nonNull(mythInvocation)) {
            final Class clazz = mythInvocation.getTargetClass();
            final String method = mythInvocation.getMethodName();
            final Object[] args = mythInvocation.getArgs();
            final Class[] parameterTypes = mythInvocation.getParameterTypes();
            final Object bean = SpringBeanUtils.getInstance().getBean(clazz);
            MethodUtils.invokeMethod(bean, method, args, parameterTypes);
            LogUtil.debug(LOGGER, "Myth执行本地协调事务:{}", () -> mythInvocation.getTargetClass()
                    + ":" + mythInvocation.getMethodName());
        }
    }



}
