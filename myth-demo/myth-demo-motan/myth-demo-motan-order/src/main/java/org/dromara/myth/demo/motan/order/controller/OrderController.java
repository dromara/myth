package org.dromara.myth.demo.motan.order.controller;

import org.dromara.myth.demo.motan.order.service.OrderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * The type Order controller.
 *
 * @author xiaoyu
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * Instantiates a new Order controller.
     *
     * @param orderService the order service
     */
    @Autowired(required = false)
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    /**
     * Order pay string.
     *
     * @param count  the count
     * @param amount the amount
     * @return the string
     */
    @PostMapping(value = "/orderPay")
    @ApiOperation(value = "订单支付接口（注意这里模拟的是创建订单并进行支付扣减库存等操作）")
    public String orderPay(@RequestParam(value = "count") Integer count,
                           @RequestParam(value = "amount") BigDecimal amount) {

        return orderService.orderPay(count, amount);

    }

}
