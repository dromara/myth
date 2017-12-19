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

package com.github.myth.demo.springcloud.account.service.impl;

import com.github.myth.annotation.Myth;
import com.github.myth.common.exception.MythRuntimeException;
import com.github.myth.demo.springcloud.account.api.dto.AccountDTO;
import com.github.myth.demo.springcloud.account.api.entity.AccountDO;
import com.github.myth.demo.springcloud.account.mapper.AccountMapper;


import com.github.myth.demo.springcloud.account.api.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author xiaoyu
 */
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);


    private final AccountMapper accountMapper;

    @Autowired(required = false)
    public AccountServiceImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 扣款支付
     *
     * @param accountDTO 参数dto
     * @return true
     */
    @Override
    @Myth(destination = "account")
    public boolean payment(AccountDTO accountDTO) {
        LOGGER.info("============springcloud执行付款接口===============");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        if (accountDO.getBalance().compareTo(accountDTO.getAmount()) <= 0) {
            throw new MythRuntimeException("spring cloud account-service 资金不足！");
        }
        accountDO.setBalance(accountDO.getBalance().subtract(accountDTO.getAmount()));
        accountDO.setUpdateTime(new Date());
        final int update = accountMapper.update(accountDO);
        if (update != 1) {
            throw new MythRuntimeException("spring cloud account-service 资金不足！");
        }
        return Boolean.TRUE;
    }

    /**
     * 获取用户账户信息
     *
     * @param userId 用户id
     * @return AccountDO
     */
    @Override
    public AccountDO findByUserId(String userId) {
        final AccountDO byUserId = accountMapper.findByUserId(userId);
        return byUserId;
    }
}
