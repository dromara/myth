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
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.LogUtil;
import org.dromara.myth.common.utils.extension.ExtensionLoader;
import org.dromara.myth.core.coordinator.MythCoordinatorService;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.logo.MythLogo;
import org.dromara.myth.core.service.MythInitService;
import org.dromara.myth.core.spi.MythCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Instantiates a new Myth init service.
     *
     * @param mythCoordinatorService the myth coordinator service
     */
    @Autowired
    public MythInitServiceImpl(final MythCoordinatorService mythCoordinatorService) {
        this.mythCoordinatorService = mythCoordinatorService;
    }

    @Override
    public void initialization(final MythConfig mythConfig) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.error("myth have error!")));
        try {
            loadSpiSupport(mythConfig);
            mythCoordinatorService.start(mythConfig);
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
        //spi serialize
        final ObjectSerializer serializer = ExtensionLoader.getExtensionLoader(ObjectSerializer.class)
                .getActivateExtension(mythConfig.getSerializer());
        //spi repository
        final MythCoordinatorRepository repository = ExtensionLoader.getExtensionLoader(MythCoordinatorRepository.class)
                .getActivateExtension(mythConfig.getRepositorySupport());

        repository.setSerializer(serializer);
        SpringBeanUtils.getInstance().registerBean(MythCoordinatorRepository.class.getName(), repository);
    }

}
