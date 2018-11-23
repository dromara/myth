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

package org.dromara.myth.core.schedule;

import org.apache.commons.collections.CollectionUtils;
import org.dromara.myth.common.bean.entity.MythTransaction;
import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.enums.EventTypeEnum;
import org.dromara.myth.common.enums.MythStatusEnum;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.core.concurrent.threadpool.MythTransactionThreadFactory;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import org.dromara.myth.core.service.MythSendMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledService.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class ScheduledService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledService.class);

    private final MythSendMessageService mythSendMessageService;

    private final MythCoordinatorService mythCoordinatorService;

    private final MythTransactionEventPublisher publisher;

    /**
     * Instantiates a new Scheduled service.
     *
     * @param mythSendMessageService the myth send message service
     * @param mythCoordinatorService the myth coordinator service
     * @param publisher              the publisher
     */
    @Autowired
    public ScheduledService(MythSendMessageService mythSendMessageService, MythCoordinatorService mythCoordinatorService, MythTransactionEventPublisher publisher) {
        this.mythSendMessageService = mythSendMessageService;
        this.mythCoordinatorService = mythCoordinatorService;
        this.publisher = publisher;
    }

    /**
     * Scheduled auto recover.
     *
     * @param mythConfig the myth config
     */
    public void scheduledAutoRecover(final MythConfig mythConfig) {
        new ScheduledThreadPoolExecutor(1, MythTransactionThreadFactory.create("MythAutoRecoverService", true))
                .scheduleWithFixedDelay(() -> {
                    LogUtil.debug(LOGGER, "auto recover execute delayTime:{}", mythConfig::getScheduledDelay);
                    try {
                        final List<MythTransaction> mythTransactionList = mythCoordinatorService.listAllByDelay(acquireData(mythConfig));
                        if (CollectionUtils.isNotEmpty(mythTransactionList)) {
                            mythTransactionList.forEach(mythTransaction -> {
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

    private Date acquireData(final MythConfig mythConfig) {
        return new Date(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli() - (mythConfig.getRecoverDelayTime() * 1000));
    }

}
