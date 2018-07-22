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

package com.github.myth.core.disruptor.publisher;

import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.EventTypeEnum;
import com.github.myth.core.disruptor.event.MythTransactionEvent;
import com.github.myth.core.disruptor.factory.MythTransactionEventFactory;
import com.github.myth.core.disruptor.handler.MythTransactionEventHandler;
import com.github.myth.core.disruptor.translator.MythTransactionEventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MythTransactionEventPublisher.
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionEventPublisher implements DisposableBean {

    private Disruptor<MythTransactionEvent> disruptor;

    @Autowired
    private MythTransactionEventHandler mythTransactionEventHandler;

    /**
     * start disruptor.
     *
     * @param bufferSize bufferSize
     */
    public void start(final int bufferSize) {
        disruptor = new Disruptor<>(new MythTransactionEventFactory(), bufferSize, r -> {
            AtomicInteger index = new AtomicInteger(1);
            return new Thread(null, r, "disruptor-thread-" + index.getAndIncrement());
        }, ProducerType.MULTI, new YieldingWaitStrategy());
        disruptor.handleEventsWith(mythTransactionEventHandler);
        disruptor.start();
    }


    /**
     * publish disruptor event.
     *
     * @param mythTransaction {@linkplain MythTransaction }
     * @param type            {@linkplain EventTypeEnum}
     */
    public void publishEvent(final MythTransaction mythTransaction, final int type) {
        final RingBuffer<MythTransactionEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(new MythTransactionEventTranslator(type), mythTransaction);
    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }
}
