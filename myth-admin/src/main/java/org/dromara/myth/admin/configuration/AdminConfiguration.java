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
import org.dromara.myth.common.serializer.ObjectSerializer;
import org.dromara.myth.common.utils.extension.ExtensionLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * AdminConfiguration.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
public class AdminConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
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
            return ExtensionLoader.getExtensionLoader(ObjectSerializer.class)
                    .getActivateExtension(env.getProperty("myth.serializer.support"));
        }
    }

}
