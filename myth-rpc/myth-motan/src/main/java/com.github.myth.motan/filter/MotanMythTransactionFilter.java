package com.github.myth.motan.filter;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.Scope;
import com.weibo.api.motan.core.extension.Spi;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.util.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * @author xiaoyu
 */
@Spi(scope = Scope.SINGLETON)
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
    public Response filter(Caller<?> caller, Request request) {

        final String interfaceName = request.getInterfaceName();
        final String methodName = request.getMethodName();
        try {
            Class clazz = Class.forName(interfaceName);
            final Method[] methods = clazz.getMethods();
            final Optional<Class[]> params =
                    Stream.of(methods)
                            .filter(method -> method.getName().equals(methodName))
                            .findFirst()
                            .map(Method::getParameterTypes);
            if (params.isPresent()) {
                final Class[] args = params.get();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return caller.call(request);
    }
}
