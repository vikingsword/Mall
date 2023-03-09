package com.mall.order.mq;

import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.StockMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * @author lc
 * @Description:
 * @date 2021/8/27
 */
@Component
@Slf4j
public class DelayOrderCancelConsumer {
    private DefaultMQPushConsumer consumer;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    StockMapper stockMapper;
    @Autowired
    OrderItemMapper orderItemMapper;

    @PostConstruct
    public void init(){
        consumer = new DefaultMQPushConsumer("delay_order_group_consumer");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            consumer.subscribe("delay_order_cancel","*");
            consumer.setMessageListener(new MessageListenerConcurrently() {//设置监听者
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    for (MessageExt messageExt : list) {
                        byte[] body = messageExt.getBody();
                        String orderId = null;
                        try {
                            orderId = new String (body, 0, body.length, "utf-8");
                            Order order = orderMapper.selectByPrimaryKey(orderId);
                            Integer status = order.getStatus();//订单状态为未付款状态就下一步
                            if(status == 0){
                                Example example = new Example(Order.class);
                                Example.Criteria criteria = example.createCriteria();
                                criteria.andEqualTo("orderId",orderId);
                                Order order1 = new Order();
                                order1.setStatus(5);
                                order1.setUpdateTime(new Date());
                                orderMapper.updateByExampleSelective(order1,example);//将状态码改变为交易关闭
                                //还原库存
                                //查找订单包含的商品 itemId以及数量 num
                                Example example1 = new Example(OrderItem.class);
                                example1.createCriteria().andEqualTo("orderId",orderId);
                                List<OrderItem> orderItems = orderItemMapper.selectByExample(example1);
                                for (OrderItem orderItem : orderItems) {
                                    Long itemId = orderItem.getItemId();//商品Id
                                    Integer num = orderItem.getNum();//商品库存
                                    //修改商品订单关联表中的状态为库存已释放
                                    orderItemMapper.updateStockStatus(2,orderItem.getOrderId());
                                    //修改库存表中数量以及已锁定的数量
                                    stockMapper.updateStock2(num,itemId);

                                }


                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                        // 消费逻辑
                        //  1. 在下单之后的半小时之后，消费到这条消息
                        //  2. 从message中获取oderId, 查询订单状态，如果订单的状态是已支付，什么都不做
                        //  3. 判断订单的状态，不是初始化状态，不是支付成功的状态，就取消订单
                        //      a. 修改订单状态
                        //      b. 还原库存
                    }


                    return  ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

                }
            });
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }
}
