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

package org.dromara.myth.core.service.handler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.service.MythTransactionHandler;
import org.dromara.myth.core.service.engine.MythTransactionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * this myth transaction actor.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class ActorMythTransactionHandler implements MythTransactionHandler {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActorMythTransactionHandler.class);

    private final MythTransactionEngine mythTransactionEngine;

    /**
     * Instantiates a new Actor myth transaction handler.
     *
     * @param mythTransactionEngine the myth transaction engine
     */
    @Autowired
    public ActorMythTransactionHandler(final MythTransactionEngine mythTransactionEngine) {
        this.mythTransactionEngine = mythTransactionEngine;
    }

    @Override
    public Object handler(final ProceedingJoinPoint point, final MythTransactionContext mythTransactionContext) throws Throwable {
        try {
            //save log.
            mythTransactionEngine.actorTransaction(point, mythTransactionContext);
            //execute try.
            final Object proceed = point.proceed();
            //update status commit.
            mythTransactionEngine.updateStatus(MythStatusEnum.COMMIT.getCode());
            return proceed;
        } catch (Throwable throwable) {
            LogUtil.error(LOGGER, "handler myth transaction exception,transId：{}", mythTransactionContext::getTransId);
            mythTransactionEngine.failTransaction(throwable.getMessage());
            throw throwable;
        } finally {
            TransactionContextLocal.getInstance().remove();
        }
    }
}
