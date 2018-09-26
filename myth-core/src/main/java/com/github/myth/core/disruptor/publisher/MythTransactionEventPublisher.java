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
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadpool.MythTransactionThreadFactory;
import com.github.myth.core.disruptor.event.MythTransactionEvent;
import com.github.myth.core.disruptor.factory.MythTransactionEventFactory;
import com.github.myth.core.disruptor.handler.MythTransactionEventHandler;
import com.github.myth.core.disruptor.translator.MythTransactionEventTranslator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MythTransactionEventPublisher.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionEventPublisher implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MythTransactionEventPublisher.class);

    private static final int MAX_THREAD = Runtime.getRuntime().availableProcessors() << 1;

    private Executor executor;

    private Disruptor<MythTransactionEvent> disruptor;

    private final MythTransactionEventHandler mythTransactionEventHandler;

    @Autowired
    public MythTransactionEventPublisher(MythTransactionEventHandler mythTransactionEventHandler) {
        this.mythTransactionEventHandler = mythTransactionEventHandler;
    }

    /**
     * start disruptor.
     *
     * @param bufferSize bufferSize
     */
    public void start(final int bufferSize) {

        disruptor = new Disruptor<>(new MythTransactionEventFactory(), bufferSize, r -> {
            AtomicInteger index = new AtomicInteger(1);
            return new Thread(null, r, "disruptor-thread-" + index.getAndIncrement());
        }, ProducerType.MULTI, new BlockingWaitStrategy());

        executor = new ThreadPoolExecutor(MAX_THREAD, MAX_THREAD, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                MythTransactionThreadFactory.create("myth-log-disruptor", false),
                new ThreadPoolExecutor.AbortPolicy());
        disruptor.handleEventsWith(mythTransactionEventHandler);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<MythTransactionEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, MythTransactionEvent event) {
                LogUtil.error(LOGGER, () -> "Disruptor handleEventException:"
                        + event.getType() + event.getMythTransaction().toString() + ex.getMessage());
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                LogUtil.error(LOGGER, () -> "Disruptor start exception");
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                LogUtil.error(LOGGER, () -> "Disruptor close Exception ");
            }
        });

        disruptor.start();
    }


    /**
     * publish disruptor event.
     *
     * @param mythTransaction {@linkplain MythTransaction }
     * @param type            {@linkplain EventTypeEnum}
     */
    public void publishEvent(final MythTransaction mythTransaction, final int type) {
        executor.execute(() -> {
            final RingBuffer<MythTransactionEvent> ringBuffer = disruptor.getRingBuffer();
            ringBuffer.publishEvent(new MythTransactionEventTranslator(type), mythTransaction);
        });

    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }
}
