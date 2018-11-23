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

package org.dromara.myth.core.service.engine;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.enums.EventTypeEnum;
import org.dromara.myth.common.enums.MythRoleEnum;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import org.dromara.myth.core.service.MythSendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * MythTransactionEngine.
 *
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class MythTransactionEngine {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythTransactionEngine.class);

    /**
     * save MythTransaction in threadLocal.
     */
    private static final ThreadLocal<MythTransaction> CURRENT = new ThreadLocal<>();

    private final MythSendMessageService mythSendMessageService;

    private final MythTransactionEventPublisher publishEvent;

    /**
     * Instantiates a new Myth transaction engine.
     *
     * @param mythSendMessageService the myth send message service
     * @param publishEvent           the publish event
     */
    @Autowired
    public MythTransactionEngine(MythSendMessageService mythSendMessageService, MythTransactionEventPublisher publishEvent) {
        this.mythSendMessageService = mythSendMessageService;
        this.publishEvent = publishEvent;
    }

    /**
     * this is stater begin MythTransaction.
     *
     * @param point cut point.
     */
    public void begin(final ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "开始执行Myth分布式事务！start");
        MythTransaction mythTransaction = buildMythTransaction(point, MythRoleEnum.START.getCode(), MythStatusEnum.BEGIN.getCode(), "");
        //发布事务保存事件，异步保存
        publishEvent.publishEvent(mythTransaction, EventTypeEnum.SAVE.getCode());
        //当前事务保存到ThreadLocal
        CURRENT.set(mythTransaction);
        //设置tcc事务上下文，这个类会传递给远端
        MythTransactionContext context = new MythTransactionContext();
        //设置事务id
        context.setTransId(mythTransaction.getTransId());
        //设置为发起者角色
        context.setRole(MythRoleEnum.START.getCode());
        TransactionContextLocal.getInstance().set(context);
    }

    /**
     * save errorMsg into MythTransaction .
     *
     * @param errorMsg errorMsg
     */
    public void failTransaction(final String errorMsg) {
        MythTransaction mythTransaction = getCurrentTransaction();
        if (Objects.nonNull(mythTransaction)) {
            mythTransaction.setStatus(MythStatusEnum.FAILURE.getCode());
            mythTransaction.setErrorMsg(errorMsg);
            publishEvent.publishEvent(mythTransaction, EventTypeEnum.UPDATE_FAIR.getCode());
        }
    }

    /**
     * this is actor begin transaction.
     *
     * @param point                  cut point
     * @param mythTransactionContext {@linkplain MythTransactionContext}
     */
    public void actorTransaction(final ProceedingJoinPoint point, final MythTransactionContext mythTransactionContext) {
        MythTransaction mythTransaction =
                buildMythTransaction(point, MythRoleEnum.PROVIDER.getCode(),
                        MythStatusEnum.BEGIN.getCode(), mythTransactionContext.getTransId());
        //发布事务保存事件，异步保存
        publishEvent.publishEvent(mythTransaction, EventTypeEnum.SAVE.getCode());
        //当前事务保存到ThreadLocal
        CURRENT.set(mythTransaction);
        //设置提供者角色
        mythTransactionContext.setRole(MythRoleEnum.PROVIDER.getCode());
        TransactionContextLocal.getInstance().set(mythTransactionContext);
    }

    /**
     * update transaction status.
     *
     * @param status {@linkplain MythStatusEnum}
     */
    public void updateStatus(final int status) {
        MythTransaction mythTransaction = getCurrentTransaction();
        Optional.ofNullable(mythTransaction)
                .map(t -> {
                    t.setStatus(status);
                    return t;
                }).ifPresent(t -> publishEvent.publishEvent(t, EventTypeEnum.UPDATE_STATUS.getCode()));
        mythTransaction.setStatus(MythStatusEnum.COMMIT.getCode());
    }

    /**
     * send message.
     */
    public void sendMessage() {
        Optional.ofNullable(getCurrentTransaction()).ifPresent(mythSendMessageService::sendMessage);
    }

    /**
     * transaction is begin.
     *
     * @return true boolean
     */
    public boolean isBegin() {
        return CURRENT.get() != null;
    }

    /**
     * help gc.
     */
    public void cleanThreadLocal() {
        CURRENT.remove();
    }

    private MythTransaction getCurrentTransaction() {
        return CURRENT.get();
    }

    /**
     * add participant into transaction.
     *
     * @param participant {@linkplain MythParticipant}
     */
    public void registerParticipant(final MythParticipant participant) {
        final MythTransaction mythTransaction = this.getCurrentTransaction();
        mythTransaction.registerParticipant(participant);
        publishEvent.publishEvent(mythTransaction, EventTypeEnum.UPDATE_PARTICIPANT.getCode());
    }

    private MythTransaction buildMythTransaction(final ProceedingJoinPoint point, final int role,
                                                 final int status, final String transId) {
        MythTransaction mythTransaction;
        if (StringUtils.isNoneBlank(transId)) {
            mythTransaction = new MythTransaction(transId);
        } else {
            mythTransaction = new MythTransaction();
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = point.getTarget().getClass();
        mythTransaction.setStatus(status);
        mythTransaction.setRole(role);
        mythTransaction.setTargetClass(clazz.getName());
        mythTransaction.setTargetMethod(method.getName());
        return mythTransaction;
    }

}
