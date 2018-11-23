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

package org.dromara.myth.demo.motan.account.mapper;

import org.dromara.myth.demo.motan.account.api.entity.AccountDO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * The interface Account mapper.
 *
 * @author xiaoyu
 */
public interface AccountMapper {

    /**
     * Update int.
     *
     * @param accountDO the account do
     * @return the int
     */
    @Update("update account set balance =#{balance}," +
            " update_time = #{updateTime}" +
            " where user_id =#{userId}  and  balance > 0  ")
    int update(AccountDO accountDO);

    /**
     * Find by user id account do.
     *
     * @param userId the user id
     * @return the account do
     */
    @Select("select * from account where user_id =#{userId}")
    AccountDO findByUserId(String userId);
}
