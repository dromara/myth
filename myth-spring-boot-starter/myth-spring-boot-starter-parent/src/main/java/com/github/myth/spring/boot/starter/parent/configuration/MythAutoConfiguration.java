package com.github.myth.spring.boot.starter.parent.configuration;

import com.github.myth.common.config.MythConfig;
import com.github.myth.core.bootstrap.MythTransactionBootstrap;
import com.github.myth.core.service.MythInitService;
import com.github.myth.spring.boot.starter.parent.config.MythConfigProperties;
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
@ComponentScan(basePackages = { "com.github.myth" })
public class MythAutoConfiguration {

    private final MythConfigProperties mythConfigProperties;

    @Autowired
    public MythAutoConfiguration(final MythConfigProperties mythConfigProperties) {
        this.mythConfigProperties = mythConfigProperties;
    }

    /**
     * init MythTransactionBootstrap
     *
     * @param mythInitService {@linkplain MythInitService}
     * @return MythTransactionBootstrap
     */
    @Bean
    public MythTransactionBootstrap tccTransactionBootstrap(final MythInitService mythInitService) {
        final MythTransactionBootstrap bootstrap = new MythTransactionBootstrap(mythInitService);
        bootstrap.builder(builder());
        return bootstrap;
    }

    /**
     * init bean of  MythConfig.
     * @return
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
