/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dromara.myth.demo.springcloud.order.configuration;

import org.dromara.myth.springcloud.feign.MythFeignHandler;
import org.dromara.myth.springcloud.feign.MythRestTemplateInterceptor;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * The type My configuration.
 *
 * @author xiaoyu
 */
@Configuration
public class MyConfiguration {

    /**
     * Feign builder feign . builder.
     *
     * @return the feign . builder
     */
    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .requestInterceptor(new MythRestTemplateInterceptor())
                .invocationHandlerFactory(invocationHandlerFactory());
    }

    /**
     * Invocation handler factory invocation handler factory.
     *
     * @return the invocation handler factory
     */
    @Bean
    public InvocationHandlerFactory invocationHandlerFactory() {
        return (target, dispatch) -> {
            MythFeignHandler handler = new MythFeignHandler();
            handler.setTarget(target);
            handler.setHandlers(dispatch);
            return handler;
        };
    }

    /**
     * Feign options request . options.
     *
     * @return the request . options
     */
    @Bean
    Request.Options feignOptions() {
        return new Request.Options(5000, 5000);
    }

    /**
     * Feign retryer retryer.
     *
     * @return the retryer
     */
    @Bean
    Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}