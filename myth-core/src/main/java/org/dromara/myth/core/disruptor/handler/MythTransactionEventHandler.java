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

package org.dromara.myth.core.disruptor.handler;

import com.lmax.disruptor.WorkHandler;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.enums.EventTypeEnum;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.disruptor.event.MythTransactionEvent;

import java.util.concurrent.Executor;

/**
 * MythTransactionEventHandler.
 *
 * @author xiaoyu(Myth)
 */
public class MythTransactionEventHandler implements WorkHandler<MythTransactionEvent> {

    private final MythCoordinatorService mythCoordinatorService;

    private Executor executor;

    public MythTransactionEventHandler(final MythCoordinatorService mythCoordinatorService,
                                       final Executor executor) {
        this.mythCoordinatorService = mythCoordinatorService;
        this.executor = executor;
    }

    @Override
    public void onEvent(final MythTransactionEvent event) {
        executor.execute(() -> {
            EventTypeEnum eventTypeEnum = EventTypeEnum.buildByCode(event.getType());

            switch (eventTypeEnum) {
                case SAVE:
                    mythCoordinatorService.save(event.getMythTransaction());
                    break;
                case UPDATE_FAIR:
                    mythCoordinatorService.updateFailTransaction(event.getMythTransaction());
                    break;
                case UPDATE_STATUS:
                    final MythTransaction mythTransaction = event.getMythTransaction();
                    mythCoordinatorService.updateStatus(mythTransaction.getTransId(), mythTransaction.getStatus());
                    break;
                case UPDATE_PARTICIPANT:
                    mythCoordinatorService.updateParticipant(event.getMythTransaction());
                    break;
                default:
                    break;
            }
            event.clear();
        });
    }
}
