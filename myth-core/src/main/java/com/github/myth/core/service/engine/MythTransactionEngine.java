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

package com.github.myth.core.service.engine;

import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.EventTypeEnum;
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import com.github.myth.core.service.MythSendMessageService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * MythTransactionEngine.
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

    @Autowired
    private MythSendMessageService mythSendMessageService;

    @Autowired
    private MythTransactionEventPublisher publishEvent;

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
        Optional.ofNullable(getCurrentTransaction()).ifPresent(c -> mythSendMessageService.sendMessage(c));
    }

    /**
     * transaction is begin.
     * @return true
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
