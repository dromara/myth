package com.github.myth.demo.motan.order.configuration;

import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.BasicRefererConfigBean;
import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * @author xiaoyu
 */
@Configuration
public class MotanClientConfiguration implements EnvironmentAware {

    @Resource
    private Environment environment;


    @Bean
    public AnnotationBean motanAnnotationBean() {
        AnnotationBean motanAnnotationBean = new AnnotationBean();
        motanAnnotationBean.setPackage("com.github.myth.demo.motan.order.service");
        return motanAnnotationBean;
    }



    @Bean(name = "registry")
    public RegistryConfigBean registryConfig() {
        RegistryConfigBean config = new RegistryConfigBean();
        config.setRegProtocol("zookeeper");
        config.setAddress(environment.getProperty("spring.motan.zookeeper"));
        config.setConnectTimeout(300000);
        return config;
    }

    @Bean(name = "motan")
    public ProtocolConfigBean protocolConfig() {
        ProtocolConfigBean config = new ProtocolConfigBean();
        config.setDefault(true);
        config.setName("motan");
        config.setMaxContentLength(5048575);
        return config;
    }

    @Bean(name = "basicRefererConfig")
    public BasicRefererConfigBean basicRefererConfigBean() {
        BasicRefererConfigBean config = new BasicRefererConfigBean();
        config.setProtocol("motan");
        config.setRegistry("registry");
        config.setRequestTimeout(500000);
        config.setRegistry("registry");
        config.setCheck(false);
        config.setAccessLog(true);
        config.setRetries(2);
        config.setThrowException(true);
        return config;
    }

    @Bean
    public BasicServiceConfigBean baseServiceConfig() {
        BasicServiceConfigBean config = new BasicServiceConfigBean();
        config.setExport("motan:8001");
        config.setRegistry("registry");
        config.setAccessLog(false);
        config.setShareChannel(true);
        config.setRequestTimeout(500000);
        config.setUsegz(true);
        config.setCheck(false);
        config.setModule("order_service");
        config.setApplication("order_service");
        return config;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
