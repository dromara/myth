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

package org.dromara.myth.demo.dubbo.order.service.impl;

import org.dromara.myth.annotation.Myth;
import org.dromara.myth.demo.dubbo.account.api.dto.AccountDTO;
import org.dromara.myth.demo.dubbo.account.api.entity.AccountDO;
import org.dromara.myth.demo.dubbo.account.api.service.AccountService;
import org.dromara.myth.demo.dubbo.inventory.api.dto.InventoryDTO;
import org.dromara.myth.demo.dubbo.inventory.api.entity.Inventory;
import org.dromara.myth.demo.dubbo.inventory.api.service.InventoryService;
import org.dromara.myth.demo.dubbo.order.entity.Order;
import org.dromara.myth.demo.dubbo.order.enums.OrderStatusEnum;
import org.dromara.myth.demo.dubbo.order.mapper.OrderMapper;
import org.dromara.myth.demo.dubbo.order.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xiaoyu
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderMapper orderMapper;

    private final AccountService accountService;

    private final InventoryService inventoryService;

    @Autowired(required = false)
    public PaymentServiceImpl(OrderMapper orderMapper,
                              AccountService accountService,
                              InventoryService inventoryService) {
        this.orderMapper = orderMapper;
        this.accountService = accountService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Myth
    public void makePayment(Order order) {
        //做库存和资金账户的检验工作 这里只是demo 。。。
        final AccountDO accountDO = accountService.findByUserId(order.getUserId());
        if (accountDO.getBalance().compareTo(order.getTotalAmount()) <= 0) {
            return;
        }
        final Inventory inventory = inventoryService.findByProductId(order.getProductId());
        if (inventory.getTotalInventory() < order.getCount()) {
            return;
        }
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
