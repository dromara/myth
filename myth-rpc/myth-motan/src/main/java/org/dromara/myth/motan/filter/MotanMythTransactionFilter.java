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

package org.dromara.myth.motan.filter;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.DefaultResponse;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.util.ReflectUtil;
import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.bean.entity.MythInvocation;
import org.dromara.myth.common.bean.entity.MythParticipant;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.common.utils.GsonUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;
import org.dromara.myth.core.helper.SpringBeanUtils;
import org.dromara.myth.core.service.engine.MythTransactionEngine;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MotanMythTransactionFilter.
 *
 * @author xiaoyu
 */
@SpiMeta(name = "motanMythTransactionFilter")
@Activation(key = {MotanConstants.NODE_TYPE_REFERER})
public class MotanMythTransactionFilter implements Filter {

    @Override
    @SuppressWarnings("unchecked")
    public Response filter(final Caller<?> caller, final Request request) {
        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        final Object[] arguments = request.getArguments();
        Class[] args = null;
        Method method = null;
        Myth myth = null;
        Class clazz = null;
        try {
            //他妈的 这里还要拿方法参数类型
            clazz = ReflectUtil.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            args = Stream.of(methods)
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst()
                    .map(Method::getParameterTypes).get();
            method = clazz.getDeclaredMethod(methodName, args);
            myth = method.getAnnotation(Myth.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(myth)) {
            final MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
            if (Objects.nonNull(mythTransactionContext)) {
                request.setAttachment(CommonConstant.MYTH_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(mythTransactionContext));
            }
            final MythParticipant participant =
                    buildParticipant(mythTransactionContext, myth,
                            method, clazz, arguments, args);
            if (Objects.nonNull(participant)) {
                SpringBeanUtils.getInstance().getBean(MythTransactionEngine.class).registerParticipant(participant);
            }
            try {
                return caller.call(request);
            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultResponse();
            }
        } else {
            return caller.call(request);
        }
    }

    private MythParticipant buildParticipant(final MythTransactionContext mythTransactionContext,
                                             final Myth myth, final Method method,
                                             final Class clazz, final Object[] arguments,
                                             final Class... args) throws MythRuntimeException {
        if (Objects.nonNull(mythTransactionContext)) {
            if (Objects.isNull(method) || (Objects.isNull(clazz))) {
                return null;
            }
            MythInvocation mythInvocation = new MythInvocation(clazz, method.getName(), args, arguments);

            final String destination = myth.destination();

            final Integer pattern = myth.pattern().getCode();
            //封装调用点
            return new MythParticipant(mythTransactionContext.getTransId(), destination, pattern, mythInvocation);
        }
        return null;
    }

}
