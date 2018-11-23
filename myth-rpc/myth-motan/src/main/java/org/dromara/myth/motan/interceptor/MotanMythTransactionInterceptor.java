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

package org.dromara.myth.motan.interceptor;

import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.RpcContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.utils.GsonUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.interceptor.MythTransactionInterceptor;
import org.dromara.myth.core.service.MythTransactionAspectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * MotanMythTransactionInterceptor.
 *
 * @author xiaoyu
 */
@Component
public class MotanMythTransactionInterceptor implements MythTransactionInterceptor {

    private final MythTransactionAspectService mythTransactionAspectService;

    /**
     * Instantiates a new Motan myth transaction interceptor.
     *
     * @param mythTransactionAspectService the myth transaction aspect service
     */
    @Autowired
    public MotanMythTransactionInterceptor(final MythTransactionAspectService mythTransactionAspectService) {
        this.mythTransactionAspectService = mythTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        MythTransactionContext mythTransactionContext = null;
        final Request request = RpcContext.getContext().getRequest();
        if (Objects.nonNull(request)) {
            final Map<String, String> attachments = request.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                String context = attachments.get(CommonConstant.MYTH_TRANSACTION_CONTEXT);
                mythTransactionContext = GsonUtils.getInstance().fromJson(context, MythTransactionContext.class);
            }
        } else {
            mythTransactionContext = TransactionContextLocal.getInstance().get();
        }
        return mythTransactionAspectService.invoke(mythTransactionContext, pjp);
    }

}
