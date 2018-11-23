package org.dromara.myth.demo.motan.inventory.configuration;

import com.weibo.api.motan.config.springsupport.AnnotationBean;
import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import com.weibo.api.motan.config.springsupport.ProtocolConfigBean;
import com.weibo.api.motan.config.springsupport.RegistryConfigBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;


/**
 * The type Motan server configuration.
 *
 * @author xiaoyu
 */
@Configuration
public class MotanServerConfiguration implements EnvironmentAware {


    @Resource
    private Environment environment;

    /**
     * Motan annotation bean annotation bean.
     *
     * @return the annotation bean
     */
    @Bean
    public AnnotationBean motanAnnotationBean() {
        AnnotationBean motanAnnotationBean = new AnnotationBean();
        motanAnnotationBean.setPackage("com.github.myth.demo.motan.inventory.service");
        return motanAnnotationBean;
    }

    /**
     * Protocol config protocol config bean.
     *
     * @return the protocol config bean
     */
    @Bean(name = "motan")
    public ProtocolConfigBean protocolConfig() {
        ProtocolConfigBean config = new ProtocolConfigBean();
        config.setDefault(true);
        config.setName("motan");
        config.setMaxContentLength(5048566);
        return config;
    }

    /**
     * Registry config registry config bean.
     *
     * @return the registry config bean
     */
    @Bean(name = "registry")
    public RegistryConfigBean registryConfig() {
        RegistryConfigBean config = new RegistryConfigBean();
        config.setRegProtocol("zookeeper");
        config.setAddress(environment.getProperty("spring.motan.zookeeper"));
        config.setConnectTimeout(3000000);
        return config;
    }

    /**
     * Base service config basic service config bean.
     *
     * @return the basic service config bean
     */
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
