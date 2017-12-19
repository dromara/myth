package com.github.myth.demo.motan.inventory.configuration;

import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;


/**
 * @author xiaoyu
 */
@Configuration
public class MotanServerConfiguration implements EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotanServerConfiguration.class);

    @Resource
    private Environment environment;

    @Bean
    public AnnotationBean motanAnnotationBean() {
        AnnotationBean motanAnnotationBean = new AnnotationBean();
        motanAnnotationBean.setPackage("com.github.myth.demo.motan.inventory.service");
        return motanAnnotationBean;
    }

    @Bean(name = "motan")
    public ProtocolConfigBean protocolConfig() {
        ProtocolConfigBean config = new ProtocolConfigBean();
        config.setDefault(true);
        config.setName("motan");
        config.setMaxContentLength(5048566);
        return config;
    }

    @Bean(name = "registry")
    public RegistryConfigBean registryConfig() {
        RegistryConfigBean config = new RegistryConfigBean();
        config.setRegProtocol("zookeeper");
        config.setAddress(environment.getProperty("spring.motan.zookeeper"));
        config.setConnectTimeout(3000000);
        return config;
    }

    @Bean
    public BasicServiceConfigBean baseServiceConfig() {
        BasicServiceConfigBean config = new BasicServiceConfigBean();
        config.setExport("motan:8003");
        config.setRegistry("registry");
        config.setAccessLog(false);
        config.setRequestTimeout(500000);
        config.setCheck(false);
        config.setModule("inventory_service");
        config.setApplication("inventory_service");
        return config;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}
