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
package com.github.myth.core.service.impl;


import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.CoordinatorActionEnum;
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.coordinator.command.CoordinatorAction;
import com.github.myth.core.coordinator.command.CoordinatorCommand;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * @author xiaoyu
 */
@Component
@SuppressWarnings("unchecked")
public class MythTransactionManager {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythTransactionManager.class);


    /**
     * 将事务信息存放在threadLocal里面
     */
    private static final ThreadLocal<MythTransaction> CURRENT = new ThreadLocal<>();

    private final CoordinatorService coordinatorService;


    private final CoordinatorCommand coordinatorCommand;


    @Autowired
    public MythTransactionManager(CoordinatorCommand coordinatorCommand,
                                  CoordinatorService coordinatorService) {
        this.coordinatorCommand = coordinatorCommand;
        this.coordinatorService = coordinatorService;

    }


    public MythTransaction begin(ProceedingJoinPoint point) {
        LogUtil.debug(LOGGER, () -> "开始执行Myth分布式事务！start");
        MythTransaction mythTransaction = getCurrentTransaction();
        if (Objects.isNull(mythTransaction)) {

            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            Class<?> clazz = point.getTarget().getClass();

            mythTransaction = new MythTransaction();
            mythTransaction.setStatus(MythStatusEnum.BEGIN.getCode());
            mythTransaction.setRole(MythRoleEnum.START.getCode());
            mythTransaction.setTargetClass(clazz.getName());
            mythTransaction.setTargetMethod(method.getName());
        }
        //保存当前事务信息
        coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.SAVE, mythTransaction));

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
            coordinatorService.updateFailTransaction(mythTransaction);
        }
    }


    public MythTransaction actorTransaction(ProceedingJoinPoint point, MythTransactionContext mythTransactionContext) {
        MythTransaction mythTransaction =
                buildProviderTransaction(point, mythTransactionContext.getTransId(), MythStatusEnum.BEGIN.getCode());
        //保存当前事务信息
        coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.SAVE, mythTransaction));

        //当前事务保存到ThreadLocal
        CURRENT.set(mythTransaction);

        //设置提供者角色
        mythTransactionContext.setRole(MythRoleEnum.PROVIDER.getCode());

        TransactionContextLocal.getInstance().set(mythTransactionContext);

        return mythTransaction;

    }


    public void commitLocalTransaction(ProceedingJoinPoint point, String transId) {

        MythTransaction mythTransaction;
        if (StringUtils.isNoneBlank(transId)) {
            mythTransaction = coordinatorService.findByTransId(transId);
            if (Objects.nonNull(mythTransaction)) {
                updateStatus(transId, MythStatusEnum.COMMIT.getCode());
            } else {
                mythTransaction = buildProviderTransaction(point, transId, MythStatusEnum.COMMIT.getCode());
                //保存当前事务信息
                coordinatorCommand.execute(new CoordinatorAction(CoordinatorActionEnum.SAVE, mythTransaction));

            }
        }

    }


    public void sendMessage() {
        MythTransaction mythTransaction = getCurrentTransaction();
        if (Objects.nonNull(mythTransaction)) {
            coordinatorService.sendMessage(mythTransaction);
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


    public void updateStatus(String transId, Integer status) {
        coordinatorService.updateStatus(transId, status);
    }

    public void registerParticipant(MythParticipant participant) {
        final MythTransaction transaction = this.getCurrentTransaction();
        transaction.registerParticipant(participant);
        coordinatorService.updateParticipant(transaction);

    }

    private MythTransaction buildProviderTransaction(ProceedingJoinPoint point, String transId, Integer status) {
        MythTransaction mythTransaction = new MythTransaction(transId);

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Class<?> clazz = point.getTarget().getClass();

        mythTransaction.setStatus(status);
        mythTransaction.setRole(MythRoleEnum.PROVIDER.getCode());
        mythTransaction.setTargetClass(clazz.getName());
        mythTransaction.setTargetMethod(method.getName());
        return mythTransaction;
    }
}
