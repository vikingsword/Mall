package com.mall.order;

import com.mall.order.dto.*;

/**
 * create-date: 2019/7/30-上午9:13
 * 订单相关业务
 */
public interface OrderCoreService {

    /**
     * 创建订单
     * @param request
     * @return
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);

    /**
     * 获得订单列表
     * @param orderListRequest
     * @return
     */
    OrderListResponse getOrderList(OrderListRequest orderListRequest);

    /**
     * 删除订单
     * @param request
     * @return
     */
    DeleteOrderResponse deleteOrder(DeleteOrderRequest request);

    /**
     * 取消订单
     * @param cancelOrderRequest
     * @return
     */
    CancelOrderResponse cancelOrder(CancelOrderRequest cancelOrderRequest);

    /**
     * 获得订单详细信息
     * @param request
     * @return
     */
    OrderDetailMyResponse getOrderDetail(OrderDetailRequest request);

    /**
     *  支付成功时，更新订单状态，更新锁定库存
     * @param
     * @return
     */
    PayOrderSuccessResponse payOrderSuccess(String  orderId);
}
