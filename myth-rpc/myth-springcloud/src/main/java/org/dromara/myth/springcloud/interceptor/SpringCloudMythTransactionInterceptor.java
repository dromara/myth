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

package org.dromara.myth.springcloud.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.enums.MythRoleEnum;
import org.dromara.myth.common.utils.GsonUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.interceptor.MythTransactionInterceptor;
import org.dromara.myth.core.service.MythTransactionAspectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * SpringCloudMythTransactionInterceptor.
 *
 * @author xiaoyu
 */
@Component
public class SpringCloudMythTransactionInterceptor implements MythTransactionInterceptor {

    private final MythTransactionAspectService mythTransactionAspectService;

    @Autowired
    public SpringCloudMythTransactionInterceptor(final MythTransactionAspectService mythTransactionAspectService) {
        this.mythTransactionAspectService = mythTransactionAspectService;
    }

    @Override
    public Object interceptor(final ProceedingJoinPoint pjp) throws Throwable {
        MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
        if (Objects.nonNull(mythTransactionContext)
                && mythTransactionContext.getRole() == MythRoleEnum.LOCAL.getCode()) {
            mythTransactionContext = TransactionContextLocal.getInstance().get();
        } else {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String context = request.getHeader(CommonConstant.MYTH_TRANSACTION_CONTEXT);
            if (StringUtils.isNoneBlank(context)) {
                mythTransactionContext = GsonUtils.getInstance().fromJson(context, MythTransactionContext.class);
            }
        }
        return mythTransactionAspectService.invoke(mythTransactionContext, pjp);
    }

}
