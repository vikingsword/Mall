package com.mall.order;


import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;

/**
 * @Description
 **/
public interface OrderPromoService {

    /**
     * 秒杀订单
     * @param request
     * @return
     */
    CreateSeckillOrderResponse createPromoOrder(CreateSeckillOrderRequest request);
}