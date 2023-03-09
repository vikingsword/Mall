package com.cskaoyan.gateway.controller.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.*;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.user.annotation.Anonymous;
import com.mall.user.intercepter.TokenIntercepter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * @author lc
 * @Description:
 * @date 2021/8/24
 */
@Slf4j
@RestController
@RequestMapping("/shopping")
public class OrderController {
    @Reference(timeout = 3000,retries = 2,check = false)
    OrderCoreService orderCoreService;

    /**
     * 创建订单
     * @param request
     * @param servletRequest
     * @return
     */
    @PostMapping("/order")
    public ResponseData createOrder(@RequestBody CreateOrderRequest request,HttpServletRequest servletRequest){
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());//userId
        //Long uid = Long.valueOf(62);

        request.setUserId(uid);
        request.setUniqueKey(UUID.randomUUID().toString());//唯一
        CreateOrderResponse response = orderCoreService.createOrder(request);
        if(response.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            return  new ResponseUtil().setData(response.getOrderId());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());


    }


    /**
     * 获取当前用户所有订单
     * @return
     */
    @GetMapping("/order")
    public ResponseData orderList(Integer size, Integer page, HttpServletRequest request){
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());//userId
       //Long uid = Long.valueOf(62);
        OrderListRequest orderListRequest = new OrderListRequest();
        orderListRequest.setPage(page);
        orderListRequest.setSize(size);
        orderListRequest.setUserId(uid);
        OrderListResponse orderListResponse = orderCoreService.getOrderList(orderListRequest);
        OrderListResponseVo orderListResponseVo = new OrderListResponseVo();
        orderListResponseVo.setData(orderListResponse.getDetailInfoList());
        orderListResponseVo.setTotal(orderListResponse.getTotal());
        if(orderListResponse.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            return new ResponseUtil().setData(orderListResponseVo);
        }
        return new ResponseUtil().setErrorMsg(orderListResponse.getMsg());
    }

    /**
     * 获得订单详细信息
     * @param id
     * @return
     */
    @GetMapping("/order/{id}")
    public ResponseData orderDetail(@PathVariable("id") String id){
        OrderDetailRequest request = new OrderDetailRequest();
        request.setOrderId(id);
        OrderDetailMyResponse response = orderCoreService.getOrderDetail(request);
        OrderDetailMyResponseVo responseVo = new OrderDetailMyResponseVo();
        responseVo.setGoodsList(response.getOrderItemDto());
        responseVo.setOrderStatus(response.getOrderStatus());
        responseVo.setOrderTotal(response.getOrderTotal());
        responseVo.setStreetName(response.getStreetName());
        responseVo.setTel(response.getTel());
        responseVo.setUserId(response.getUserId());
        responseVo.setUserName(response.getUserName());

        if(response.getCode().equals(OrderRetCode.SUCCESS.getCode())){
            return  new ResponseUtil().setData(responseVo);
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());





    }

    //根据订单id删除订单(物理删除)
    @DeleteMapping("/order/{id}")
    public ResponseData OrderDel(@PathVariable("id") String orderId, HttpServletRequest httpServletRequest) {
        //判断用户是否登录
        String userInfo = (String) httpServletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        if(userInfo == null){
            return new ResponseUtil().setErrorMsg("请重新登录!");
        }
        //已登录
        DeleteOrderRequest request = new DeleteOrderRequest();
        request.setOrderId(orderId);
        DeleteOrderResponse response = orderCoreService.deleteOrder(request);

        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getMsg());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    //根据订单id取消订单
    @PostMapping("/cancelOrder")
    public ResponseData OrderCancel(@RequestBody Map<String, String> map, HttpServletRequest httpServletRequest) {
        //判断用户是否登录
        String userInfo = (String) httpServletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        if(userInfo == null){
            return new ResponseUtil().setErrorMsg("请重新登录!");
        }
        //已登录
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        String orderId = map.get("orderId");
        cancelOrderRequest.setOrderId(orderId);

        CancelOrderResponse response = orderCoreService.cancelOrder(cancelOrderRequest);
        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getMsg());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

}
