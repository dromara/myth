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

package com.github.myth.dubbo.proxy;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;
import com.github.myth.annotation.Myth;
import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.utils.DefaultValueUtils;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.engine.MythTransactionEngine;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * MythInvokerInvocationHandler.
 * @author xiaoyu
 */
public class MythInvokerInvocationHandler extends InvokerInvocationHandler {

    private Object target;

    public MythInvokerInvocationHandler(final Invoker<?> handler) {
        super(handler);
    }

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
