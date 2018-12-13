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

package org.dromara.myth.spring.boot.starter.parent.configuration;

import org.dromara.myth.common.config.MythConfig;
import org.dromara.myth.core.bootstrap.MythTransactionBootstrap;
import org.dromara.myth.core.service.MythInitService;
import org.dromara.myth.spring.boot.starter.parent.config.MythConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Myth spring boot starter.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties
@ComponentScan(basePackages = {"org.dromara.myth"})
public class MythAutoConfiguration {

    private final MythConfigProperties mythConfigProperties;

    /**
     * Instantiates a new Myth auto configuration.
     *
     * @param mythConfigProperties the myth config properties
     */
    @Autowired(required = false)
    public MythAutoConfiguration(final MythConfigProperties mythConfigProperties) {
        this.mythConfigProperties = mythConfigProperties;
    }

    /**
     * init MythTransactionBootstrap.
     *
     * @param mythInitService {@linkplain MythInitService}
     * @return MythTransactionBootstrap myth transaction bootstrap
     */
    @Bean
    public MythTransactionBootstrap tccTransactionBootstrap(final MythInitService mythInitService) {
        final MythTransactionBootstrap bootstrap = new MythTransactionBootstrap(mythInitService);
        bootstrap.builder(builder());
        return bootstrap;
    }

    /**
     * init bean of  MythConfig.
     *
     * @return {@linkplain MythConfig}
     */
    @Bean
    public MythConfig mythConfig() {
        return builder().build();
    }

    private MythConfig.Builder builder() {
        return MythTransactionBootstrap.create()
                .setSerializer(mythConfigProperties.getSerializer())
                .setRepositorySuffix(mythConfigProperties.getRepositorySuffix())
                .setRepositorySupport(mythConfigProperties.getRepositorySupport())
                .setNeedRecover(mythConfigProperties.getNeedRecover())
                .setBufferSize(mythConfigProperties.getBufferSize())
                .setConsumerThreads(mythConfigProperties.getConsumerThreads())
                .setScheduledThreadMax(mythConfigProperties.getScheduledThreadMax())
                .setScheduledDelay(mythConfigProperties.getScheduledDelay())
                .setRetryMax(mythConfigProperties.getRetryMax())
                .setRecoverDelayTime(mythConfigProperties.getRecoverDelayTime())
                .setMythDbConfig(mythConfigProperties.getMythDbConfig())
                .setMythFileConfig(mythConfigProperties.getMythFileConfig())
                .setMythMongoConfig(mythConfigProperties.getMythMongoConfig())
                .setMythRedisConfig(mythConfigProperties.getMythRedisConfig())
                .setMythZookeeperConfig(mythConfigProperties.getMythZookeeperConfig());
    }
}
