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

package com.github.myth.core.schedule;

import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.bean.mq.MessageEntity;
import com.github.myth.common.config.MythConfig;
import com.github.myth.common.enums.EventTypeEnum;
import com.github.myth.common.enums.MythStatusEnum;
import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.concurrent.threadpool.MythTransactionThreadFactory;
import com.github.myth.core.coordinator.CoordinatorService;
import com.github.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import com.github.myth.core.service.MythSendMessageService;
import com.github.myth.core.spi.CoordinatorRepository;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/3/5 14:29
 * @since JDK 1.8
 */
@Component
public class ScheduledService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledService.class);


    @Autowired
    private MythSendMessageService mythSendMessageService;

    @Autowired
    private CoordinatorService coordinatorService;

    @Autowired
    private MythTransactionEventPublisher publisher;

    public void scheduledAutoRecover(MythConfig mythConfig) {
        new ScheduledThreadPoolExecutor(1,
                MythTransactionThreadFactory.create("MythAutoRecoverService",
                        true)).scheduleWithFixedDelay(() -> {
            LogUtil.debug(LOGGER, "auto recover execute delayTime:{}",
                    mythConfig::getScheduledDelay);
            try {
                final List<MythTransaction> mythTransactionList =
                        coordinatorService.listAllByDelay(acquireData(mythConfig));
                if (CollectionUtils.isNotEmpty(mythTransactionList)) {
                    mythTransactionList
                            .forEach(mythTransaction -> {
                                final Boolean success = mythSendMessageService.sendMessage(mythTransaction);
                                //发送成功 ，更改状态
                                if (success) {
                                    mythTransaction.setStatus(MythStatusEnum.COMMIT.getCode());
                                    publisher.publishEvent(mythTransaction, EventTypeEnum.UPDATE_STATUS.getCode());

                                }
                            });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, mythConfig.getScheduledDelay(), TimeUnit.SECONDS);

    }


    private Date acquireData(MythConfig mythConfig) {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - (mythConfig.getRecoverDelayTime() * 1000));
    }

}
