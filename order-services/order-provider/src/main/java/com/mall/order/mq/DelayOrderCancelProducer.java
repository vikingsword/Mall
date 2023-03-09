package com.mall.order.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.tools.jsonrpc.JsonRpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

/**
 * @author lc
 * @Description: 消息队列 消息生产者
 * @date 2021/8/27
 */
@Component
@Slf4j
public class DelayOrderCancelProducer {
    private DefaultMQProducer defaultMQProducer;

    @PostConstruct
    public void init (){
         defaultMQProducer = new DefaultMQProducer("delay_order_group_producer");//新增消息生产者
         defaultMQProducer.setNamesrvAddr("127.0.0.1:9876");//配置注册中心
        try {
            defaultMQProducer.start();//启动
        } catch (MQClientException e) {
            log.error(e.getErrorMessage());
            e.printStackTrace();
        }
    }
    public boolean sendDelayOrderCancelMsg(String orderId){
        //JSON.toJSONString(orderId)
        byte[] bytes = orderId.getBytes(Charset.forName("utf-8"));
        Message message = new Message("delay_order_cancel", bytes);
        message.setDelayTimeLevel(18);
        SendResult send = null;
        try {
            send = defaultMQProducer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(send != null && SendStatus.SEND_OK.equals(send.getSendStatus())){
            return true;
        }
        return false;

    }


}
