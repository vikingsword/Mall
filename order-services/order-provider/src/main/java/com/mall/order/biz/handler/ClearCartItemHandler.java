package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.shopping.ICartService;
import com.mall.shopping.dto.ClearCartItemRequest;
import com.mall.shopping.dto.ClearCartItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Component;

/**
 *
 * create-date: 2019/8/1-下午5:05
 * 将购物车中的缓存失效
 */
@Slf4j
@Component
public class ClearCartItemHandler extends AbstractTransHandler {

    @Reference(timeout = 3000,retries = 2,check = false)
    ICartService iCartService;

    //是否采用异步方式执行
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
        ClearCartItemRequest clearCartItemRequest = new ClearCartItemRequest();
        clearCartItemRequest.setUserId(createOrderContext.getUserId());
        clearCartItemRequest.setProductIds(createOrderContext.getBuyProductIds());
        ClearCartItemResponse clearCartItemResponse = iCartService.clearCartItemByUserID(clearCartItemRequest);
        if(!clearCartItemResponse.getMsg().equals("成功")){
            throw new BizException(clearCartItemResponse.getMsg());
        }
        return true;
    }
}
