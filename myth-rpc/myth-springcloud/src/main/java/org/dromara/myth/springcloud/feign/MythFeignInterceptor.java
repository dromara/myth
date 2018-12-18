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

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.utils.GsonUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.springframework.context.annotation.Configuration;

/**
 * MythRestTemplateInterceptor.
 *
 * @author xiaoyu
 */
@Configuration
public class MythFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        final MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
        requestTemplate.header(CommonConstant.MYTH_TRANSACTION_CONTEXT,
                GsonUtils.getInstance().toJson(mythTransactionContext));
    }

}
