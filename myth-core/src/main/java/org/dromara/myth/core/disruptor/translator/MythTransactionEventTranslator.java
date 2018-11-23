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

package org.dromara.myth.core.disruptor.translator;

import com.lmax.disruptor.EventTranslatorOneArg;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.core.disruptor.event.MythTransactionEvent;

/**
 * MythTransactionEventTranslator.
 *
 * @author xiaoyu(Myth)
 */
public class MythTransactionEventTranslator implements EventTranslatorOneArg<MythTransactionEvent, MythTransaction> {

    private int type;

    /**
     * Instantiates a new Myth transaction event translator.
     *
     * @param type the type
     */
    public MythTransactionEventTranslator(final int type) {
        this.type = type;
    }

    @Override
    public void translateTo(final MythTransactionEvent mythTransactionEvent, final long l, final MythTransaction mythTransaction) {
        mythTransactionEvent.setMythTransaction(mythTransaction);
        mythTransactionEvent.setType(type);
    }
}
