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

package org.dromara.myth.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.bean.context.MythTransactionContext;
import org.dromara.myth.common.constant.CommonConstant;
import org.dromara.myth.common.utils.GsonUtils;
import org.dromara.myth.core.concurrent.threadlocal.TransactionContextLocal;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * DubboMythTransactionFilter.
 *
 * @author xiaoyu
 */
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
public class DubboMythTransactionFilter implements Filter {

    @Override
    @SuppressWarnings("unchecked")
    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
        String methodName = invocation.getMethodName();
        Class clazz = invoker.getInterface();
        Class[] args = invocation.getParameterTypes();
        Method method;
        Myth myth = null;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
            myth = method.getAnnotation(Myth.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (Objects.nonNull(myth)) {
            final MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
            if (Objects.nonNull(mythTransactionContext)) {
                RpcContext.getContext()
                        .setAttachment(CommonConstant.MYTH_TRANSACTION_CONTEXT, GsonUtils.getInstance().toJson(mythTransactionContext));
            }
        }
        return invoker.invoke(invocation);
    }

}
