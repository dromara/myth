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

package com.github.myth.admin.query;

import com.github.myth.admin.page.PageParameter;
import lombok.Data;

import java.io.Serializable;

/**
 * ConditionQuery.
 * @author xiaoyu(Myth)
 */
@Data
public class ConditionQuery implements Serializable {

    private static final long serialVersionUID = 3297929795348894462L;

    private String applicationName;

    private String transId;

    private Integer retry;

    private PageParameter pageParameter;

}
