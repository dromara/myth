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

package org.dromara.myth.springcloud.feign;

import feign.InvocationHandlerFactory;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MythRestTemplateConfiguration.
 *
 * @author xiaoyu
 */
@Configuration
public class MythRestTemplateConfiguration {

    /**
     * Request interceptor request interceptor.
     *
     * @return the request interceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new MythRestTemplateInterceptor();
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

}
