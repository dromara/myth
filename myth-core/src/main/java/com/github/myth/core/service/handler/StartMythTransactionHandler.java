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
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.service.MythTransactionHandler;
import com.github.myth.core.service.engine.MythTransactionEngine;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * this myth transaction starter.
 * @author xiaoyu(Myth)
 */
@Component
public class StartMythTransactionHandler implements MythTransactionHandler {

    private final MythTransactionEngine mythTransactionEngine;

    @Autowired
    public StartMythTransactionHandler(final MythTransactionEngine mythTransactionEngine) {
        this.mythTransactionEngine = mythTransactionEngine;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final MythTransactionContext mythTransactionContext) throws Throwable {
        try {
            mythTransactionEngine.begin(point);
            return point.proceed();
        } catch (Throwable throwable) {
            //更新失败的日志信息
            mythTransactionEngine.failTransaction(throwable.getMessage());
            throw throwable;
        } finally {
            //发送消息
            mythTransactionEngine.sendMessage();
            mythTransactionEngine.cleanThreadLocal();
            TransactionContextLocal.getInstance().remove();
        }
    }

}
