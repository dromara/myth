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

package com.github.myth.core.coordinator.command;


import com.github.myth.common.bean.entity.MythTransaction;
import com.github.myth.common.enums.CoordinatorActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaoyu
 */
@Data
@AllArgsConstructor
public class CoordinatorAction implements Serializable {


    private static final long serialVersionUID = -5714963272234819587L;

    private CoordinatorActionEnum action;

    private MythTransaction mythTransaction;



}
