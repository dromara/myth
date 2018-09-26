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
import com.github.myth.common.enums.MythRoleEnum;
import com.github.myth.core.service.MythTransactionFactoryService;
import com.github.myth.core.service.engine.MythTransactionEngine;
import com.github.myth.core.service.handler.ActorMythTransactionHandler;
import com.github.myth.core.service.handler.LocalMythTransactionHandler;
import com.github.myth.core.service.handler.StartMythTransactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * MythTransactionFactoryServiceImpl.
 * @author xiaoyu(Myth)
 */
@Component
public class MythTransactionFactoryServiceImpl implements MythTransactionFactoryService {

    private final MythTransactionEngine mythTransactionEngine;

    @Autowired
    public MythTransactionFactoryServiceImpl(final MythTransactionEngine mythTransactionEngine) {
        this.mythTransactionEngine = mythTransactionEngine;
    }

    @Override
    public Class factoryOf(final MythTransactionContext context) throws Throwable {
        //如果事务还没开启或者 myth事务上下文是空， 那么应该进入发起调用
        if (!mythTransactionEngine.isBegin() && Objects.isNull(context)) {
            return StartMythTransactionHandler.class;
        } else {
            if (context.getRole() == MythRoleEnum.LOCAL.getCode()) {
                return LocalMythTransactionHandler.class;
            }
            return ActorMythTransactionHandler.class;
        }
    }
}
