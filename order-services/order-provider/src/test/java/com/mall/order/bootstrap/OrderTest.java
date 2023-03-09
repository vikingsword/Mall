package com.mall.order.bootstrap;

import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.user.constants.SysRetCodeConstants;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Yang
 * @Date 2021/8/26 10:21
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderTest {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    StockMapper stockMapper;

    @Reference
    OrderCoreService orderCoreService;



    @Test
    public void testDeleteOrderMapper(){
        int row = orderMapper.deleteByPrimaryKey("20050402540122568");
        System.out.println(row);
    }

    @Test
    public void testDeleteOrderService(){
        DeleteOrderRequest request = new DeleteOrderRequest();
        request.setOrderId("20041914114388454");
        DeleteOrderResponse response = orderCoreService.deleteOrder(request);
        System.out.println(response.toString());
    }

    @Test
    public void testCancelOrderMapper() {
        CancelOrderResponse response = new CancelOrderResponse();
        Date date = new Date();

        //更新订单表
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", "20041914034866739");

        Order order = new Order();
        order.setStatus(OrderConstants.ORDER_STATUS_TRANSACTION_CLOSE);
        order.setUpdateTime(date);
        order.setCloseTime(date);
        int row = orderMapper.updateByExampleSelective(order, example);

        //更新order_item表
        //库存锁定状态 1库存已锁定 2库存已释放 3-库存减扣成功
        List<OrderItem> orderItems = orderItemMapper.queryByOrderId("20041914034866739");
        //保存orderItem表里的itemId和num字段用于修改库存
        ArrayList<StockUpdate> stockUpdates = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            orderItemMapper.updateStockStatus(2, orderItem.getOrderId());

            //ArrayList<StockUpdate>保存数量和itemId用于修改库存
            StockUpdate stockUpdate = new StockUpdate(orderItem.getItemId(), orderItem.getNum());
            stockUpdates.add(stockUpdate);
        }
        //更新stock表
        for (StockUpdate stockUpdate : stockUpdates) {
            //新写的库存更新逻辑
            stockMapper.updateStock2(stockUpdate.getNum(),stockUpdate.getItemId());
        }
        System.out.println(row);
    }

    @Test
    public void testCancelOrderService() {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setOrderId("20041914034866739");

        CancelOrderResponse response = orderCoreService.cancelOrder(cancelOrderRequest);
        System.out.println(response);
    }
}
