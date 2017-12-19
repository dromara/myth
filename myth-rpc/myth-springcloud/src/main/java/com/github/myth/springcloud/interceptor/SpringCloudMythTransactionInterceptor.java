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
package com.github.myth.springcloud.interceptor;

import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.common.constant.CommonConstant;
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.common.utils.GsonUtils;
import com.github.myth.core.concurrent.threadlocal.TransactionContextLocal;
import com.github.myth.core.interceptor.MythTransactionInterceptor;
import com.github.myth.core.service.MythTransactionAspectService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author xiaoyu
 */
@Component
public class SpringCloudMythTransactionInterceptor implements MythTransactionInterceptor {

    private final MythTransactionAspectService mythTransactionAspectService;

    @Autowired
    public SpringCloudMythTransactionInterceptor(MythTransactionAspectService mythTransactionAspectService) {
        this.mythTransactionAspectService = mythTransactionAspectService;
    }


    @Override
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MythTransactionContext mythTransactionContext = TransactionContextLocal.getInstance().get();
        if (Objects.nonNull(mythTransactionContext) &&
                mythTransactionContext.getRole() == MythRoleEnum.LOCAL.getCode()) {
            mythTransactionContext = TransactionContextLocal.getInstance().get();
        } else {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
            String context = request == null ? null : request.getHeader(CommonConstant.MYTH_TRANSACTION_CONTEXT);
            if (StringUtils.isNoneBlank(context)) {
                mythTransactionContext =
                        GsonUtils.getInstance().fromJson(context, MythTransactionContext.class);
            }
        }
        return mythTransactionAspectService.invoke(mythTransactionContext, pjp);
    }

}
