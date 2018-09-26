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
import com.alibaba.dubbo.rpc.proxy.jdk.JdkProxyFactory;

import java.lang.reflect.Proxy;

/**
 * MythJdkProxyFactory.
 * @author xiaoyu
 */
public class MythJdkProxyFactory extends JdkProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(final Invoker<T> invoker, final Class<?>[] interfaces) {
        T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                interfaces, new InvokerInvocationHandler(invoker));
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                interfaces, new MythInvokerInvocationHandler(proxy, invoker));
    }

}