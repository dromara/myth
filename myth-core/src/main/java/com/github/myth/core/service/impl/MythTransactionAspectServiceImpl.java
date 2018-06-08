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

package com.github.myth.core.service.impl;

import com.github.myth.common.bean.context.MythTransactionContext;
import com.github.myth.core.helper.SpringBeanUtils;
import com.github.myth.core.service.MythTransactionAspectService;
import com.github.myth.core.service.MythTransactionFactoryService;
import com.github.myth.core.service.MythTransactionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MythTransactionAspectServiceImpl.
 *
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionAspectServiceImpl implements MythTransactionAspectService {

    private final MythTransactionFactoryService mythTransactionFactoryService;

    @Autowired
    public MythTransactionAspectServiceImpl(final MythTransactionFactoryService mythTransactionFactoryService) {
        this.mythTransactionFactoryService = mythTransactionFactoryService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(final MythTransactionContext mythTransactionContext, final ProceedingJoinPoint point) throws Throwable {
        final Class clazz = mythTransactionFactoryService.factoryOf(mythTransactionContext);
        final MythTransactionHandler mythTransactionHandler = (MythTransactionHandler) SpringBeanUtils.getInstance().getBean(clazz);
        return mythTransactionHandler.handler(point, mythTransactionContext);
    }
}
