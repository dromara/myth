package org.dromara.myth.demo.motan.order.service;


import org.dromara.myth.demo.motan.order.entity.Order;

import java.math.BigDecimal;

/**
 * The interface Order service.
 *
 * @author xiaoyu
 */
public interface OrderService {

    /**
     * 创建订单并且进行扣除账户余额支付，并进行库存扣减操作
     *
     * @param count  购买数量
     * @param amount 支付金额
     * @return string string
     */
    String orderPay(Integer count, BigDecimal amount);


    /**
     * 更新订单状态
     *
     * @param order 订单实体类
     */
    void updateOrderStatus(Order order);
}
