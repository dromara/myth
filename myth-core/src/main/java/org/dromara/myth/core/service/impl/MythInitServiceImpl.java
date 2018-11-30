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

import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.common.enums.RepositorySupportEnum;
import org.dromara.myth.common.enums.SerializeEnum;
import org.dromara.myth.common.serializer.KryoSerializer;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.common.utils.ServiceBootstrap;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.disruptor.publisher.MythTransactionEventPublisher;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.logo.MythLogo;
import org.dromara.myth.core.schedule.ScheduledService;
import org.dromara.myth.core.service.MythInitService;
import org.dromara.myth.core.spi.MythCoordinatorRepository;
import org.dromara.myth.core.spi.repository.JdbcCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * myth init.
 *
 * @author xiaoyu(Myth)
 */
@Service
public class MythInitServiceImpl implements MythInitService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MythInitServiceImpl.class);

    private final MythCoordinatorService mythCoordinatorService;

    private final MythTransactionEventPublisher publisher;

    private final ScheduledService scheduledService;

    /**
     * Instantiates a new Myth init service.
     *
     * @param mythCoordinatorService the myth coordinator service
     * @param publisher              the publisher
     * @param scheduledService       the scheduled service
     */
    @Autowired
    public MythInitServiceImpl(final MythCoordinatorService mythCoordinatorService,
                               final MythTransactionEventPublisher publisher,
                               final ScheduledService scheduledService) {
        this.mythCoordinatorService = mythCoordinatorService;
        this.publisher = publisher;
        this.scheduledService = scheduledService;
    }

    @Override
    public void initialization(final MythConfig mythConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("myth have error!")));
        try {
            loadSpiSupport(mythConfig);
            publisher.start(mythConfig.getBufferSize());
            mythCoordinatorService.start(mythConfig);
            if (mythConfig.getNeedRecover()) {
                scheduledService.scheduledAutoRecover(mythConfig);
            }
        } catch (Exception ex) {
            LogUtil.error(LOGGER, "Myth init fail:{}", ex::getMessage);
            //非正常关闭
            System.exit(1);
        }
        new MythLogo().logo();
    }

    /**
     * load spi support.
     *
     * @param mythConfig {@linkplain MythConfig}
     */
    private void loadSpiSupport(final MythConfig mythConfig) {
        //spi  serialize
        final SerializeEnum serializeEnum = SerializeEnum.acquire(mythConfig.getSerializer());
        final ServiceLoader<ObjectSerializer> objectSerializers = ServiceBootstrap.loadAll(ObjectSerializer.class);
        final ObjectSerializer serializer =
                StreamSupport.stream(objectSerializers.spliterator(),
                        true)
                        .filter(objectSerializer -> Objects.equals(objectSerializer.getScheme(), serializeEnum.getSerialize()))
                        .findFirst()
                        .orElse(new KryoSerializer());
        mythCoordinatorService.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(ObjectSerializer.class.getName(), serializer);
        //spi  repository support
        final RepositorySupportEnum repositorySupportEnum = RepositorySupportEnum.acquire(mythConfig.getRepositorySupport());
        final ServiceLoader<MythCoordinatorRepository> recoverRepositories = ServiceBootstrap.loadAll(MythCoordinatorRepository.class);
        final MythCoordinatorRepository repository =
                StreamSupport.stream(recoverRepositories.spliterator(), false)
                        .filter(recoverRepository -> Objects.equals(recoverRepository.getScheme(), repositorySupportEnum.getSupport()))
                        .findFirst()
                        .orElse(new JdbcCoordinatorRepository());
        repository.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(MythCoordinatorRepository.class.getName(), repository);
    }

}
