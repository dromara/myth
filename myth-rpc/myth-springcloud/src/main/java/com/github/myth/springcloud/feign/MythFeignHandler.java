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

package com.github.myth.springcloud.feign;

import com.github.myth.annotation.Myth;
import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.utils.DefaultValueUtils;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.engine.MythTransactionEngine;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * MythFeignHandler.
 * @author xiaoyu
 */
public class MythFeignHandler implements InvocationHandler {

    private Target<?> target;

    private Map<Method, MethodHandler> handlers;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final Myth myth = method.getAnnotation(Myth.class);
            if (Objects.isNull(myth)) {
                return this.handlers.get(method).invoke(args);
            }
            try {
                final MythTransactionEngine mythTransactionEngine =
                        SpringBeanUtils.getInstance().getBean(MythTransactionEngine.class);
                final MythParticipant participant = buildParticipant(myth, method, args);
                if (Objects.nonNull(participant)) {
                    mythTransactionEngine.registerParticipant(participant);
                }
                return this.handlers.get(method).invoke(args);
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

    public void setTarget(final Target<?> target) {
        this.target = target;
    }

    public void setHandlers(final Map<Method, MethodHandler> handlers) {
        this.handlers = handlers;
    }

}
