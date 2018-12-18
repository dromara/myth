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

import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.bean.entity.MythInvocation;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.utils.DefaultValueUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.engine.MythTransactionEngine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * MythFeignHandler.
 *
 * @author xiaoyu
 */
public class MythFeignHandler implements InvocationHandler {

    private InvocationHandler delegate;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final Myth myth = method.getAnnotation(Myth.class);
            if (Objects.isNull(myth)) {
                return this.delegate.invoke(proxy, method, args);
            }
            try {
                final MythTransactionEngine mythTransactionEngine =
                        SpringBeanUtils.getInstance().getBean(MythTransactionEngine.class);
                final MythParticipant participant = buildParticipant(myth, method, args);
                if (Objects.nonNull(participant)) {
                    mythTransactionEngine.registerParticipant(participant);
                }
                return this.delegate.invoke(proxy, method, args);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return DefaultValueUtils.getDefaultValue(method.getReturnType());
            }
        }
    }

    private MythParticipant buildParticipant(final Myth myth, final Method method, final Object[] args) {
        final MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();

        MythParticipant participant;
        if (Objects.nonNull(mythTransactionContext)) {
            final Class declaringClass = myth.target();
            MythInvocation mythInvocation =
                    new MythInvocation(declaringClass, method.getName(), method.getParameterTypes(), args);
            final Integer pattern = myth.pattern().getCode();
            //封装调用点
            participant = new MythParticipant(mythTransactionContext.getTransId(),
                    myth.destination(),
                    pattern,
                    mythInvocation);
            return participant;
        }
        return null;
    }

    public void setDelegate(InvocationHandler delegate) {
        this.delegate = delegate;
    }
}
