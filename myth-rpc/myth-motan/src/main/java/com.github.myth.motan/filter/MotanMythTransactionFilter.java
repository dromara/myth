package com.github.myth.motan.filter;

import com.github.myth.annotation.Myth;
import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.bean.entity.MythInvocation;
import com.github.myth.common.bean.entity.MythParticipant;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.common.utils.GsonUtils;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.impl.MythTransactionManager;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.Scope;
import com.weibo.api.motan.core.extension.Spi;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.rpc.RpcContext;
import com.weibo.api.motan.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;


/**
 * @author xiaoyu
 */
@SpiMeta(name = "motanMythTransactionFilter")
@Activation(key = {MotanConstants.NODE_TYPE_REFERER})
public class MotanMythTransactionFilter implements Filter {

    /**
     * 实现新浪的filter接口 rpc传参数
     *
     * @param caller  caller
     * @param request 请求
     * @return Response
     */
    @Override
    @SuppressWarnings("unchecked")
    public Response filter(Caller<?> caller, Request request) {

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
            args =
                    Stream.of(methods)
                            .filter(m -> m.getName().equals(methodName))
                            .findFirst()
                            .map(Method::getParameterTypes).get();
            method = clazz.getDeclaredMethod(methodName, args);
            myth = method.getAnnotation(Myth.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Objects.nonNull(myth)) {

            final MythTransactionContext mythTransactionContext =
                    TransactionContextLocal.getInstance().get();
            if (Objects.nonNull(mythTransactionContext)) {
                request.setAttachment(CommonConstant.MYTH_TRANSACTION_CONTEXT,
                        GsonUtils.getInstance().toJson(mythTransactionContext));
            }
            final MythParticipant participant =
                    buildParticipant(mythTransactionContext, myth,
                            method, clazz, arguments, args);
            if (Objects.nonNull(participant)) {
                SpringBeanUtils.getInstance().getBean(MythTransactionManager.class)
                        .registerParticipant(participant);
            }
            try {
                return caller.call(request);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return caller.call(request);
        }
    }

    private MythParticipant buildParticipant(MythTransactionContext mythTransactionContext,
                                             Myth myth, Method method,
                                             Class clazz, Object[] arguments, Class... args)
            throws MythRuntimeException {

        if (Objects.nonNull(mythTransactionContext)) {

            if (Objects.isNull(method) || (Objects.isNull(clazz))) {
                return null;
            }

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
