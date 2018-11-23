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

package org.dromara.myth.core.bootstrap;

import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.MythInitService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Myth Bootstrap.
 *
 * @author xiaoyu
 */
public class MythTransactionBootstrap extends MythConfig implements ApplicationContextAware {

    private final MythInitService mythInitService;

    @Autowired
    public MythTransactionBootstrap(final MythInitService mythInitService) {
        this.mythInitService = mythInitService;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
        start(this);
    }

    private void start(final MythConfig mythConfig) {
        mythInitService.initialization(mythConfig);
    }
}
