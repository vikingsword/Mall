package com.mall.order.mq.promo;

import com.alibaba.fastjson.JSON;
import com.mall.order.OrderPromoService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author lc
 * @Description:
 * @date 2021/8/31
 */
@Component
public class CreatePromoOrderConsumer {
    private  DefaultMQPushConsumer consumer;
    @Reference
    OrderPromoService orderPromoService;

    @PostConstruct
    public void init(){
        consumer = new DefaultMQPushConsumer("promo_order_consumer");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            consumer.subscribe("create_promo_order","*");
            consumer.setMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    MessageExt message = list.get(0);
                    byte[] body = message.getBody();
                    String requestStr = new String(body, 0, body.length, Charset.forName("utf-8"));
                    CreateSeckillOrderRequest request = JSON.parseObject(requestStr, CreateSeckillOrderRequest.class);
                    CreateSeckillOrderResponse promoOrder = orderPromoService.createPromoOrder(request);//创建订单
                    if(promoOrder.getCode().equals(OrderRetCode.SUCCESS.getCode())){
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }

                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            });
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }
}
