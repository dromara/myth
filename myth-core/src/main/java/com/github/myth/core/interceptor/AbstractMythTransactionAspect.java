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

package com.github.myth.core.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * AbstractMythTransactionAspect.
 * @author xiaoyu
 */
@Aspect
public abstract class AbstractMythTransactionAspect {

    private MythTransactionInterceptor mythTransactionInterceptor;

    /**
     * set MythTransactionInterceptor.
     * @param mythTransactionInterceptor {@linkplain MythTransactionInterceptor}
     */
    protected void setMythTransactionInterceptor(final MythTransactionInterceptor mythTransactionInterceptor) {
        this.mythTransactionInterceptor = mythTransactionInterceptor;
    }


    /**
     * this is point cut with {@linkplain com.github.myth.annotation.Myth }.
     */
    @Pointcut("@annotation(com.github.myth.annotation.Myth)")
    public void mythTransactionInterceptor() {

    }

    /**
     * this is around in {@linkplain com.github.myth.annotation.Myth }.
     * @param proceedingJoinPoint proceedingJoinPoint
     * @return Object
     * @throws Throwable  Throwable
     */
    @Around("mythTransactionInterceptor()")
    public Object interceptMythAnnotationMethod(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return mythTransactionInterceptor.interceptor(proceedingJoinPoint);
    }

    /**
     * spring bean Order.
     *
     * @return int
     */
    public abstract int getOrder();
}
