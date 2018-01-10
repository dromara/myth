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
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.service.MythTransactionHandler;
import com.github.myth.core.service.impl.MythTransactionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Description: .</p>
 * Myth分布式事务参与者， 参与分布式事务的接口会进入该handler
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/11/30 10:11
 * @since JDK 1.8
 */
@Component
public class ActorMythTransactionHandler implements MythTransactionHandler {


    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActorMythTransactionHandler.class);

    private static final Lock LOCK = new ReentrantLock();


    private final MythTransactionManager mythTransactionManager;


    @Autowired
    public ActorMythTransactionHandler(MythTransactionManager mythTransactionManager) {
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
            //处理并发问题
            LOCK.lock();
            //先保存事务日志
            mythTransactionManager.actorTransaction(point, mythTransactionContext);

            //发起调用 执行try方法

            final Object proceed = point.proceed();

            //执行成功 更新状态为commit

            mythTransactionManager.updateStatus(mythTransactionContext.getTransId(),
                    MythStatusEnum.COMMIT.getCode());

            return proceed;

        } catch (Throwable throwable) {
            LogUtil.error(LOGGER, "执行分布式事务接口失败,事务id：{}", mythTransactionContext::getTransId);
            mythTransactionManager.failTransaction(throwable.getMessage());
            throw throwable;
        } finally {
            LOCK.unlock();
            TransactionContextLocal.getInstance().remove();

        }
    }
}
