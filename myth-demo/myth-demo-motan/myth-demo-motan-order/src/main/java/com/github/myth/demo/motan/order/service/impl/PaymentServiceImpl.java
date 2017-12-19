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

package com.github.myth.demo.motan.order.service.impl;


import com.github.myth.annotation.Myth;
import com.github.myth.demo.motan.account.api.dto.AccountDTO;
import com.github.myth.demo.motan.account.api.entity.AccountDO;
import com.github.myth.demo.motan.account.api.service.AccountService;
import com.github.myth.demo.motan.inventory.api.dto.InventoryDTO;
import com.github.myth.demo.motan.inventory.api.entity.Inventory;
import com.github.myth.demo.motan.inventory.api.service.InventoryService;
import com.github.myth.demo.motan.order.entity.Order;
import com.github.myth.demo.motan.order.enums.OrderStatusEnum;
import com.github.myth.demo.motan.order.mapper.OrderMapper;
import com.github.myth.demo.motan.order.service.PaymentService;
import com.weibo.api.motan.config.springsupport.annotation.MotanReferer;
import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@MotanService
public class PaymentServiceImpl implements PaymentService {


    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);


    private final OrderMapper orderMapper;

    @MotanReferer(basicReferer = "basicRefererConfig")
    private  AccountService accountService;

    @MotanReferer(basicReferer = "basicRefererConfig")
    private  InventoryService inventoryService;

    private static final String SUCCESS = "success";

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper
                             ) {
        this.orderMapper = orderMapper;
    }


    @Override
    @Myth(destination = "")
    public void makePayment(Order order) {

        //做库存和资金账户的检验工作 这里只是demo 。。。

        LOGGER.debug("===check data over===");

        order.setStatus(OrderStatusEnum.PAY_SUCCESS.getCode());
        orderMapper.update(order);
        //扣除用户余额
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAmount(order.getTotalAmount());
        accountDTO.setUserId(order.getUserId());
        accountService.payment(accountDTO);
        //进入扣减库存操作
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(order.getCount());
        inventoryDTO.setProductId(order.getProductId());
        inventoryService.decrease(inventoryDTO);
        LOGGER.debug("=============Myth分布式事务执行完成！=======");
    }


}
