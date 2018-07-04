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

package com.github.myth.admin.service;

import com.github.myth.admin.vo.LogVO;
import com.github.myth.admin.page.CommonPager;
import com.github.myth.admin.query.ConditionQuery;

import java.util.List;

/**
 * LogService.
 * @author xiaoyu(Myth)
 */
public interface LogService {


    /**
     * acquired {@linkplain LogVO} by page.
     *
     * @param query {@linkplain ConditionQuery}
     * @return CommonPager LogVO
     */
    CommonPager<LogVO> listByPage(ConditionQuery query);

    /**
     * batch remove transaction log by ids.
     *
     * @param ids      ids
     * @param appName  appName
     * @return true
     */
    Boolean batchRemove(List<String> ids, String appName);

    /**
     * modify retry count.
     *
     * @param id      transId
     * @param retry   retry
     * @param appName appName
     * @return true
     */
    Boolean updateRetry(String id, Integer retry, String appName);
}
