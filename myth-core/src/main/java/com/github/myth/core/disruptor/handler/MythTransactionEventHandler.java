/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.github.myth.core.disruptor.handler;

import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.EventTypeEnum;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.disruptor.event.MythTransactionEvent;
import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MythTransactionEventHandler.
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionEventHandler implements EventHandler<MythTransactionEvent> {

    private final CoordinatorService coordinatorService;

    @Autowired
    public MythTransactionEventHandler(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @Override
    public void onEvent(final MythTransactionEvent mythTransactionEvent, final long sequence, final boolean endOfBatch) {
        if (mythTransactionEvent.getType() == EventTypeEnum.SAVE.getCode()) {
            coordinatorService.save(mythTransactionEvent.getMythTransaction());
        } else if (mythTransactionEvent.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
            coordinatorService.updateParticipant(mythTransactionEvent.getMythTransaction());
        } else if (mythTransactionEvent.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
            final MythTransaction mythTransaction = mythTransactionEvent.getMythTransaction();
            coordinatorService.updateStatus(mythTransaction.getTransId(), mythTransaction.getStatus());
        } else if (mythTransactionEvent.getType() == EventTypeEnum.UPDATE_FAIR.getCode()) {
            coordinatorService.updateFailTransaction(mythTransactionEvent.getMythTransaction());
        }
        mythTransactionEvent.clear();
    }
}
