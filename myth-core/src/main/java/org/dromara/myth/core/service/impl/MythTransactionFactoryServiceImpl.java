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

package org.dromara.myth.core.service.impl;

import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.core.service.MythTransactionFactoryService;
import org.dromara.myth.core.service.engine.MythTransactionEngine;
import org.dromara.myth.core.service.handler.ActorMythTransactionHandler;
import org.dromara.myth.core.service.handler.LocalMythTransactionHandler;
import org.dromara.myth.core.service.handler.StartMythTransactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * MythTransactionFactoryServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionFactoryServiceImpl implements MythTransactionFactoryService {

    private final MythTransactionEngine mythTransactionEngine;

    /**
     * Instantiates a new Myth transaction factory service.
     *
     * @param mythTransactionEngine the myth transaction engine
     */
    @Autowired
    public MythTransactionFactoryServiceImpl(final MythTransactionEngine mythTransactionEngine) {
        this.mythTransactionEngine = mythTransactionEngine;
    }

    @Override
    public Class factoryOf(final MythTransactionContext context) {
        if (Objects.isNull(context)) {
            return StartMythTransactionHandler.class;
        } else {
            //上下文不为空，并且当前事务存在threadLocal里面，那只可能是内嵌调用，或者走了多个切面
            if (mythTransactionEngine.isBegin()) {
                return LocalMythTransactionHandler.class;
            }
            return ActorMythTransactionHandler.class;
        }
    }
}
