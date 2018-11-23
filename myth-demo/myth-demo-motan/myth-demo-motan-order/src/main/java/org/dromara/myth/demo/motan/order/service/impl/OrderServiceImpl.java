package org.dromara.myth.demo.motan.order.service.impl;

import org.dromara.myth.common.utils.IdWorkerUtils;
import org.dromara.myth.demo.motan.order.entity.Order;
import org.dromara.myth.demo.motan.order.enums.OrderStatusEnum;
import org.dromara.myth.demo.motan.order.mapper.OrderMapper;
import org.dromara.myth.demo.motan.order.service.OrderService;
import org.dromara.myth.demo.motan.order.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


/**
 * The type Order service.
 *
 * @author xiaoyu
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    private final PaymentService paymentService;

    /**
     * Instantiates a new Order service.
     *
     * @param orderMapper    the order mapper
     * @param paymentService the payment service
     */
    @Autowired(required = false)
    public OrderServiceImpl(OrderMapper orderMapper, PaymentService paymentService) {
        this.orderMapper = orderMapper;
        this.paymentService = paymentService;
    }


    @Override
    public String orderPay(Integer count, BigDecimal amount) {
        final Order order = buildOrder(count, amount);
        final int rows = orderMapper.save(order);



        if (rows > 0) {
            paymentService.makePayment(order);
        }


        return "success";
    }


    @Override
    public void updateOrderStatus(Order order) {
        orderMapper.update(order);
    }

    private Order buildOrder(Integer count, BigDecimal amount) {
        Order order = new Order();
        order.setCreateTime(new Date());
        order.setNumber(IdWorkerUtils.getInstance().createUUID());
        //demo中的表里只有商品id为1的数据
        order.setProductId("1");
        order.setStatus(OrderStatusEnum.NOT_PAY.getCode());
        order.setTotalAmount(amount.doubleValue());
        order.setCount(count);
        //demo中 表里面存的用户id为10000
        order.setUserId("10000");
        return order;
    }
}
