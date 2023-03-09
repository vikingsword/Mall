package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.commons.tool.utils.NumberUtils;
import com.mall.order.biz.callback.SendEmailCallback;
import com.mall.order.biz.callback.TransCallback;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dto.CartProductDto;
import com.mall.order.utils.GlobalIdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * create-date: 2019/8/1-下午5:01
 * 初始化订单 生成订单
 */

@Slf4j
@Component
public class InitOrderHandler extends AbstractTransHandler {

    @Autowired
    GlobalIdGeneratorUtil globalIdGeneratorUtil;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
        //向订单表中插入数据
        Order order = new Order();
        String s = UUID.randomUUID().toString();
        String orderId = globalIdGeneratorUtil.getNextSeq(s,1);
        order.setOrderId(orderId);
        order.setPayment(createOrderContext.getOrderTotal());
        order.setStatus(OrderConstants.ORDER_STATUS_INIT);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setUserId(createOrderContext.getUserId());
        order.setBuyerNick(createOrderContext.getUserName());

        int insert = orderMapper.insert(order);
        if(insert == 0){//如果没有插入数据库
            throw new BizException(OrderRetCode.DB_SAVE_EXCEPTION.getMessage());
        }
        //向订单商品关联表中插入数据
        List<CartProductDto> productDtoList = createOrderContext.getCartProductDtoList();
        List<Long> buyProductIds = new ArrayList<>();//订单中的商品IDList
        for (CartProductDto cartProductDto : productDtoList) {
            OrderItem orderItem = new OrderItem();
            String s1 = UUID.randomUUID().toString();
            String nextSeq = globalIdGeneratorUtil.getNextSeq(s1, 1);
            orderItem.setId(nextSeq);
            orderItem.setItemId(cartProductDto.getProductId());
            orderItem.setOrderId(orderId);
            orderItem.setNum(cartProductDto.getProductNum().intValue());
            orderItem.setPrice(cartProductDto.getSalePrice().doubleValue());
            orderItem.setPicPath(cartProductDto.getProductImg());
            BigDecimal totalFee = cartProductDto.getSalePrice().multiply(new BigDecimal(cartProductDto.getProductNum()));
            orderItem.setTotalFee(totalFee.doubleValue());
            orderItem.setTitle(cartProductDto.getProductName());
            orderItem.setStatus(1);
            buyProductIds.add(cartProductDto.getProductId());
            int insert1 = orderItemMapper.insert(orderItem);
            if(insert1 < 1){
                throw new BizException(OrderRetCode.SHIPPING_DB_SAVED_FAILED.getMessage());
            }
        }
        createOrderContext.setOrderId(orderId);
        createOrderContext.setBuyProductIds(buyProductIds);



        return true;
    }
}
