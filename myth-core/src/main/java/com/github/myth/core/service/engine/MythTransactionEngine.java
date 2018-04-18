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
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class MythTransactionEngine {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythTransactionEngine.class);

    /**
     * 将事务信息存放在threadLocal里面
     */
    private static final ThreadLocal<MythTransaction> CURRENT = new ThreadLocal<>();

    @Autowired
    private MythSendMessageService mythSendMessageService;

    @Autowired
    private MythTransactionEventPublisher publishEvent;


    public MythTransaction begin(ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "开始执行Myth分布式事务！start");
        MythTransaction mythTransaction =
                buildMythTransaction(point, MythRoleEnum.START.getCode(),
                        MythStatusEnum.BEGIN.getCode(), "");

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

        return mythTransaction;

    }


    public void failTransaction(String errorMsg) {
        MythTransaction mythTransaction = getCurrentTransaction();
        if (Objects.nonNull(mythTransaction)) {
            mythTransaction.setStatus(MythStatusEnum.FAILURE.getCode());
            mythTransaction.setErrorMsg(errorMsg);
            publishEvent.publishEvent(mythTransaction, EventTypeEnum.UPDATE_FAIR.getCode());
        }
    }


    public MythTransaction actorTransaction(ProceedingJoinPoint point, MythTransactionContext mythTransactionContext) {
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

        return mythTransaction;

    }


    public void updateStatus(int status) {
        MythTransaction mythTransaction = getCurrentTransaction();
        Optional.ofNullable(mythTransaction)
                .map(t -> {
                    t.setStatus(status);
                    return t;
                }).ifPresent(t -> publishEvent.publishEvent(t, EventTypeEnum.UPDATE_STATUS.getCode()));

        mythTransaction.setStatus(MythStatusEnum.COMMIT.getCode());
    }


    public void sendMessage() {
        MythTransaction mythTransaction = getCurrentTransaction();
        if (Objects.nonNull(mythTransaction)) {
            mythSendMessageService.sendMessage(mythTransaction);
        }
    }


    public boolean isBegin() {
        return CURRENT.get() != null;
    }


    public void cleanThreadLocal() {
        CURRENT.remove();
    }


    private MythTransaction getCurrentTransaction() {
        return CURRENT.get();
    }


    public void registerParticipant(MythParticipant participant) {
        final MythTransaction mythTransaction = this.getCurrentTransaction();
        mythTransaction.registerParticipant(participant);
        publishEvent.publishEvent(mythTransaction, EventTypeEnum.UPDATE_PARTICIPANT.getCode());

    }

    private MythTransaction buildMythTransaction(ProceedingJoinPoint point, int role, int status,
                                                 String transId) {
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
