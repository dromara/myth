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
import com.github.myth.core.service.impl.MythTransactionManager;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * @author xiaoyu
 */
public class MythInvokerInvocationHandler extends InvokerInvocationHandler {

    private Object target;

    public MythInvokerInvocationHandler(Invoker<?> handler) {
        super(handler);
    }

    public <T> MythInvokerInvocationHandler(T target, Invoker<T> invoker) {
        super(invoker);
        this.target = target;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        final Myth myth = method.getAnnotation(Myth.class);
        final Class<?>[] arguments = method.getParameterTypes();
        final Class clazz = method.getDeclaringClass();

        if (Objects.nonNull(myth)) {
            final MythTransactionContext mythTransactionContext =
                    TransactionContextLocal.getInstance().get();
            try {
                final MythParticipant participant =
                        buildParticipant(mythTransactionContext, myth,
                                method, clazz, args, arguments);
                if (Objects.nonNull(participant)) {
                    final MythTransactionManager mythTransactionManager =
                            SpringBeanUtils.getInstance().getBean(MythTransactionManager.class);
                    mythTransactionManager.registerParticipant(participant);
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




    private MythParticipant buildParticipant(MythTransactionContext mythTransactionContext,
                                             Myth myth, Method method,
                                             Class clazz, Object[] arguments,
                                             Class... args)
            throws MythRuntimeException {

        if (Objects.nonNull(mythTransactionContext)) {

            MythInvocation mythInvocation = new MythInvocation(clazz,
                    method.getName(),
                    args, arguments);


            //有tags的消息队列的特殊处理
            final String destination;
            if( Objects.nonNull(myth.tags()) && myth.tags().length() > 0 ){
                destination = myth.destination()+","+myth.tags();
            }else{
                destination = myth.destination();
            }

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
