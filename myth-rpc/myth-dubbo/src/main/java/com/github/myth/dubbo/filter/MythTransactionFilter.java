/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.myth.dubbo.filter;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.github.myth.annotation.MessageTypeEnum;
import com.github.myth.annotation.Myth;
import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.utils.GsonUtils;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.service.impl.MythTransactionManager;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author xiaoyu
 */
@Activate(group = {Constants.SERVER_KEY, Constants.CONSUMER})
public class MythTransactionFilter implements Filter {

    private MythTransactionManager mythTransactionManager;

    public void setMythTransactionManager(MythTransactionManager mythTransactionManager) {
        this.mythTransactionManager = mythTransactionManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String methodName = invocation.getMethodName();
        Class clazz = invoker.getInterface();
        Class[] args = invocation.getParameterTypes();
        final Object[] arguments = invocation.getArguments();

        Method method = null;
        Myth myth = null;
        try {
            method = clazz.getDeclaredMethod(methodName, args);
            myth = method.getAnnotation(Myth.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (Objects.nonNull(myth)) {
            try {
                final MythTransactionContext mythTransactionContext =
                        TransactionContextLocal.getInstance().get();
                if (Objects.nonNull(mythTransactionContext)) {
                    RpcContext.getContext()
                            .setAttachment(CommonConstant.MYTH_TRANSACTION_CONTEXT,
                                    GsonUtils.getInstance().toJson(mythTransactionContext));
                }
                final MythParticipant participant =
                        buildParticipant(mythTransactionContext, myth,
                                method, clazz, arguments, args);
                if (Objects.nonNull(participant)) {
                    mythTransactionManager.registerParticipant(participant);
                }
                return invoker.invoke(invocation);

            } catch (RpcException e) {
                e.printStackTrace();
                return new RpcResult();
            }
        } else {
            return invoker.invoke(invocation);
        }

    }

    private MythParticipant buildParticipant(MythTransactionContext mythTransactionContext,
                                             Myth myth, Method method,
                                             Class clazz, Object[] arguments, Class... args)
            throws MythRuntimeException {

        if (Objects.nonNull(mythTransactionContext)) {

            MythInvocation mythInvocation = new MythInvocation(clazz,
                    method.getName(),
                    args, arguments);

            final String destination = myth.destination();

            final Integer pattern = myth.pattern().getCode();


            //封装调用点
            return new MythParticipant(
                    mythTransactionContext.getTransId(),
                    destination,
                    pattern,
                    mythInvocation);

        }

        return null;


    }
}
