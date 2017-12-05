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

package com.github.myth.core.service.handler;

import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.service.MythTransactionHandler;
import com.github.myth.core.service.impl.MythTransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: .</p>
 * Myth分布式事务发起者， 即分布式事务接口的入口 会进入该handler
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/30 10:11
 * @since JDK 1.8
 */
@Component
public class StartMythTransactionHandler implements MythTransactionHandler {


    private static final Lock LOCK = new ReentrantLock();


    private final MythTransactionManager mythTransactionManager;


    @Autowired
    public StartMythTransactionHandler(MythTransactionManager mythTransactionManager) {
        this.mythTransactionManager = mythTransactionManager;
    }


    /**
     * Myth分布式事务处理接口
     *
     * @param point                  point 切点
     * @param mythTransactionContext myth事务上下文
     * @return Object
     * @throws Throwable 异常
     */
    @Override
    public Object handler(ProceedingJoinPoint point, MythTransactionContext mythTransactionContext) throws Throwable {

        try {
            MythTransaction begin;
            //主要防止并发问题，对事务日志的写造成压力，加了锁进行处理
            try {
                LOCK.lock();
                begin = mythTransactionManager.begin(point);
            } finally {
                LOCK.unlock();
            }
            //发起调用 执行try方法
            final Object proceed = point.proceed();

            try {
                //这里为什么要这么做呢？ 主要是为了防止在极端情况下，发起者执行过程中，突然自身down 机
                //造成消息未发送，新增一个状态标记，如果出现这种情况，通过定时任务发送消息
                LOCK.lock();
                mythTransactionManager.updateStatus(begin.getTransId(), MythStatusEnum.COMMIT.getCode());
            } finally {
                LOCK.unlock();
            }

            return proceed;

        } finally {
            //发送消息
            mythTransactionManager.sendMessage();
            mythTransactionManager.cleanThreadLocal();
            TransactionContextLocal.getInstance().remove();
        }
    }
}
