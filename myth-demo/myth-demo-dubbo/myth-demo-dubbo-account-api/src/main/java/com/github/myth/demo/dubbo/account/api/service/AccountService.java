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

package com.github.myth.demo.dubbo.account.api.service;


import com.github.myth.annotation.Myth;
import com.github.myth.demo.dubbo.account.api.dto.AccountDTO;
import com.github.myth.demo.dubbo.account.api.entity.AccountDO;

/**
 * @author xiaoyu
 */
public interface AccountService {


    /**
     * 扣款支付
     *
     * @param accountDTO 参数dto
     * @return true
     */
    @Myth(destination = "account")
    boolean payment(AccountDTO accountDTO);


    /**
     * 获取用户资金信息
     * @param userId 用户id
     * @return AccountDO
     */
    AccountDO findByUserId(String userId);
}
