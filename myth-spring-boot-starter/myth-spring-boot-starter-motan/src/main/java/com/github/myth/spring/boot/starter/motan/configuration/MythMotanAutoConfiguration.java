package com.github.myth.spring.boot.starter.motan.configuration;

import com.github.myth.common.config.MythConfig;
import com.github.myth.core.bootstrap.MythTransactionBootstrap;
import com.github.myth.core.service.MythInitService;
import com.github.myth.spring.boot.starter.motan.config.MythConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2018/4/16 13:55
 * @since JDK 1.8
 */
public class MythMotanAutoConfiguration {

    private final MythConfigProperties mythConfigProperties;

    @Autowired
    public MythMotanAutoConfiguration(MythConfigProperties mythConfigProperties) {
        this.mythConfigProperties = mythConfigProperties;
    }

    @Bean
    public MythConfig mythConfig() {
        return builder().build();
    }

    @Bean
    public MythTransactionBootstrap mythTransactionBootstrap(MythInitService mythInitService) {
        final MythTransactionBootstrap bootstrap =
                new MythTransactionBootstrap(mythInitService);
        bootstrap.builder(builder());
        return bootstrap;
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
