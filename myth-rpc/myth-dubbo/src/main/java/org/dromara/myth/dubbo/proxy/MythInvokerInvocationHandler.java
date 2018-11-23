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

package org.dromara.myth.dubbo.proxy;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.bean.entity.MythInvocation;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.utils.DefaultValueUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.engine.MythTransactionEngine;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * MythInvokerInvocationHandler.
 *
 * @author xiaoyu
 */
public class MythInvokerInvocationHandler extends InvokerInvocationHandler {

    private Object target;

    /**
     * Instantiates a new Myth invoker invocation handler.
     *
     * @param handler the handler
     */
    public MythInvokerInvocationHandler(final Invoker<?> handler) {
        super(handler);
    }

    /**
     * Instantiates a new Myth invoker invocation handler.
     *
     * @param target  the target
     * @param invoker the invoker
     */
    public <T> MythInvokerInvocationHandler(final T target, final Invoker<T> invoker) {
        super(invoker);
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Myth myth = method.getAnnotation(Myth.class);
        final Class<?>[] arguments = method.getParameterTypes();
        final Class clazz = method.getDeclaringClass();
        if (Objects.nonNull(myth)) {
            final MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
            try {
                final MythParticipant participant =
                        buildParticipant(mythTransactionContext, myth,
                                method, clazz, args, arguments);
                if (Objects.nonNull(participant)) {
                    final MythTransactionEngine mythTransactionEngine =
                            SpringBeanUtils.getInstance().getBean(MythTransactionEngine.class);
                    mythTransactionEngine.registerParticipant(participant);
                }
                return super.invoke(target, method, args);
            } catch (Throwable throwable) {
                //todo 需要记录下错误日志
                throwable.printStackTrace();
                return DefaultValueUtils.getDefaultValue(method.getReturnType());
            }
        } else {
            return super.invoke(target, method, args);
        }
    }

    private MythParticipant buildParticipant(final MythTransactionContext mythTransactionContext,
                                             final Myth myth, final Method method,
                                             final Class clazz, final Object[] arguments,
                                             final Class... args) throws MythRuntimeException {
        if (Objects.nonNull(mythTransactionContext)) {
            MythInvocation mythInvocation = new MythInvocation(clazz, method.getName(), args, arguments);
            //有tags的消息队列的特殊处理
            final String destination;
            if (myth.tags().length() > 0) {
                destination = myth.destination() + "," + myth.tags();
            } else {
                destination = myth.destination();
            }
            final Integer pattern = myth.pattern().getCode();
            //封装调用点
            return new MythParticipant(mythTransactionContext.getTransId(), destination, pattern, mythInvocation);
        }
        return null;
    }

}
