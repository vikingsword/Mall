package com.mall.order.biz.handler;

import com.mall.order.mq.DelayOrderCancelProducer;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 利用mq发送延迟取消订单消息
 * @Date: 2019-09-17 23:14
 **/
@Component
@Slf4j
public class SendMessageHandler extends AbstractTransHandler {
	@Autowired
	DelayOrderCancelProducer delayOrderCancelProducer;


	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	public boolean handle(TransHandlerContext context) {
		CreateOrderContext createOrderContext = (CreateOrderContext) context;
		String orderId = createOrderContext.getOrderId();
		boolean b = delayOrderCancelProducer.sendDelayOrderCancelMsg(orderId);


		return true;
	}
}