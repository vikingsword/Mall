package com.mall.promo.mq;

import com.alibaba.fastjson.JSON;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.dal.persistence.PromoItemMapper;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class CreatePromoOrderProducer {

    private TransactionMQProducer transactionMQProducer;
    @Autowired
    PromoItemMapper promoItemMapper;

    @Autowired
    CacheManager cacheManager;
    @Autowired
    RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        transactionMQProducer
                = new TransactionMQProducer("promo_order_producer");

        transactionMQProducer.setNamesrvAddr("127.0.0.1:9876");

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                Map<String, Long> paramMap = (Map<String, Long>) arg;
                Long productId = paramMap.get("productId");
                Long psId = paramMap.get("psId");
                // 扣减秒杀商品库存
                //分布式锁
                String lockKey = "promo_session_item" + productId + "-" + psId;
                RLock lock = redissonClient.getLock(lockKey);
                lock.lock();
                Integer effectiveRow = null;
                try {
                    effectiveRow = promoItemMapper.decreaseStock(productId, psId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } finally {
                    lock.unlock();
                }
                String stockNotEnoughKey = "prom_item_stock" + productId + "-" + psId;

                String localTransactionKey = "promo_order_local_transaction:" + msg.getTransactionId();
                if (effectiveRow < 1) {
                   // 库存扣减失败
                    // 在redis中保存，本地事物执行的结果
                    cacheManager.setCache(localTransactionKey, "failed", 1);
                    cacheManager.setCache(stockNotEnoughKey,"none",1);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                //扣减库存成功
                cacheManager.setCache(localTransactionKey, "success", 1);
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                String transactionKey = "promo_order_local_transaction:" + msg.getTransactionId();
                String result = cacheManager.checkCache(transactionKey);
                if (result == null || result.trim().isEmpty()) {
                    return LocalTransactionState.UNKNOW;
                }

                if("success".equals(result)) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });

        try {
            transactionMQProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    public boolean sendPromOrderMessage(CreateSeckillOrderRequest request, Long productId, Long psId) {

        String requestStr = JSON.toJSONString(request);
        Message message = new Message();
        message.setTopic("create_promo_order");
        message.setBody(requestStr.getBytes(Charset.forName("utf-8")));


        // 准备本地事物执行所需参数
       Map<String, Long> paramMap = new HashMap<>();
       paramMap.put("productId", productId);
       paramMap.put("psId", psId);

        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message, paramMap);
        } catch (MQClientException e) {
            e.printStackTrace();
        }

        if (sendResult != null && LocalTransactionState.COMMIT_MESSAGE.equals(sendResult.getLocalTransactionState())) {
           // 说明 事物消息发送成功且本地事物执行成功
            return true;
        }


        return false;
    }


}
