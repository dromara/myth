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

package org.dromara.myth.core.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.dromara.myth.annotation.Myth;

/**
 * AbstractMythTransactionAspect.
 *
 * @author xiaoyu
 */
@Aspect
public abstract class AbstractMythTransactionAspect {

    private MythTransactionInterceptor mythTransactionInterceptor;

    /**
     * set MythTransactionInterceptor.
     *
     * @param mythTransactionInterceptor {@linkplain MythTransactionInterceptor}
     */
    protected void setMythTransactionInterceptor(final MythTransactionInterceptor mythTransactionInterceptor) {
        this.mythTransactionInterceptor = mythTransactionInterceptor;
    }


    /**
     * this is point cut with {@linkplain Myth }.
     */
    @Pointcut("@annotation(org.dromara.myth.annotation.Myth)")
    public void mythTransactionInterceptor() {

    }

    /**
     * this is around in {@linkplain Myth }.
     *
     * @param proceedingJoinPoint proceedingJoinPoint
     * @return Object object
     * @throws Throwable Throwable
     */
    @Around("mythTransactionInterceptor()")
    public Object interceptMythAnnotationMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return mythTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

    /**
     * spring bean Order.
     *
     * @return int order
     */
    public abstract int getOrder();
}
