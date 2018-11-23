package org.dromara.myth.demo.springcloud.account.service.impl;

import org.dromara.myth.annotation.Myth;
import org.dromara.myth.common.exception.MythRuntimeException;
import org.dromara.myth.demo.springcloud.account.api.dto.AccountDTO;
import org.dromara.myth.demo.springcloud.account.api.entity.AccountDO;
import org.dromara.myth.demo.springcloud.account.mapper.AccountMapper;


import org.dromara.myth.demo.springcloud.account.api.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * The type Account service.
 *
 * @author xiaoyu
 */
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);


    private final AccountMapper accountMapper;

    /**
     * Instantiates a new Account service.
     *
     * @param accountMapper the account mapper
     */
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
    @Transactional(rollbackFor = Exception.class)
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
