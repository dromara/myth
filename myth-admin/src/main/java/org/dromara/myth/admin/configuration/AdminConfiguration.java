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

package org.dromara.myth.admin.configuration;

import org.dromara.myth.admin.interceptor.AuthInterceptor;
import org.dromara.myth.common.enums.SerializeEnum;
import org.dromara.myth.common.serializer.KryoSerializer;
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.ServiceBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * AdminConfiguration.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
public class AdminConfiguration {

    /**
     * Cors configurer web mvc configurer.
     *
     * @return the web mvc configurer
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
            }
        };
    }

    /**
     * The type Serializer configuration.
     */
    @Configuration
    static class SerializerConfiguration {

        private final Environment env;

        /**
         * Instantiates a new Serializer configuration.
         *
         * @param env the env
         */
        @Autowired
        SerializerConfiguration(final Environment env) {
            this.env = env;
        }

        /**
         * Object serializer object serializer.
         *
         * @return the object serializer
         */
        @Bean
        public ObjectSerializer objectSerializer() {
            final SerializeEnum serializeEnum =
                    SerializeEnum.acquire(env.getProperty("myth.serializer.support"));
            final ServiceLoader<ObjectSerializer> objectSerializers =
                    ServiceBootstrap.loadAll(ObjectSerializer.class);
            return StreamSupport.stream(objectSerializers.spliterator(), false)
                    .filter(objectSerializer ->
                            Objects.equals(objectSerializer.getScheme(),
                                    serializeEnum.getSerialize())).findFirst()
                    .orElse(new KryoSerializer());
        }
    }

}
