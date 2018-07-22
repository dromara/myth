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

package com.github.myth.common.bean.context;

import lombok.Data;

import java.io.Serializable;

/**
 * MythTransactionContext.
 * @author xiaoyu
 */
@Data
public class MythTransactionContext implements Serializable {

    private static final long serialVersionUID = -5289080166922118073L;

    private String transId;

    /**
     * 事务参与的角色. {@linkplain com.github.myth.common.enums.MythRoleEnum}
     */
    private int role;


}
