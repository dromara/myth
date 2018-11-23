
package org.dromara.myth.demo.springcloud.account.controller;


import org.dromara.myth.demo.springcloud.account.api.dto.AccountDTO;
import org.dromara.myth.demo.springcloud.account.api.entity.AccountDO;
import org.dromara.myth.demo.springcloud.account.api.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Account controller.
 *
 * @author xiaoyu
 */
@RestController
@RequestMapping("/account")
public class AccountController {


    private final AccountService accountService;

    /**
     * Instantiates a new Account controller.
     *
     * @param accountService the account service
     */
    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Save boolean.
     *
     * @param accountDO the account do
     * @return the boolean
     */
    @RequestMapping("/payment")
    public Boolean save(@RequestBody AccountDTO accountDO) {
        return accountService.payment(accountDO);
    }

    /**
     * Find by user id account do.
     *
     * @param userId the user id
     * @return the account do
     */
    @RequestMapping("/findByUserId")
    public AccountDO findByUserId(@RequestParam("userId") String userId) {
        return accountService.findByUserId(userId);
    }


}
