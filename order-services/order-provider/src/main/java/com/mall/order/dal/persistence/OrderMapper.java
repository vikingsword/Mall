package com.mall.order.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dto.OrderDetailInfo;
import com.mall.order.dto.OrderDetailResponse;

import java.util.List;

public interface OrderMapper extends TkMapper<Order> {
    Long countAll();


    List<OrderDetailInfo> getOrderList(Long userId);

    OrderDetailResponse getOrderDetail(String orderId);
}