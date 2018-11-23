package org.dromara.myth.demo.motan.order.service;


import org.dromara.myth.demo.motan.order.entity.Order;

/**
 * The interface Payment service.
 *
 * @author xiaoyu
 */
public interface PaymentService {

    /**
     * 订单支付
     *
     * @param order 订单实体
     */
    void makePayment(Order order);




}
