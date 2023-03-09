package com.cskaoyan.gateway.controller.cashier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.pay.PayCoreService;
import com.mall.pay.constants.PayReturnCodeEnum;
import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.PaymentRequest2;
import com.mall.pay.dto.alipay.AlipayQueryRetResponse;
import com.mall.pay.dto.alipay.AlipaymentResponse;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lc
 * @Description:
 * @date 2021/8/28
 */
@RestController
@RequestMapping("/cashier")
public class CashierController {
    @Reference
    PayCoreService payCoreService;




    /**
     * 获得支付二维码
     * @return
     */
    @RequestMapping("/pay")
    public ResponseData cashierPay(@RequestBody PaymentRequest request, HttpServletRequest servletRequest){
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());//userId
        request.setUserId(uid.intValue());
        AlipaymentResponse response = new AlipaymentResponse();
         response = payCoreService.aliPay(request);


        String path = "http://localhost:8080/image/"+response.getQrCode();
        return new ResponseUtil().setData(path);
    }

    /**
     * 查询支付状态
     * @param
     * @return
     */
    @RequestMapping("/queryStatus")
    public ResponseData cashierQueryStatus(String orderId){
        AlipayQueryRetResponse retResponse = new AlipayQueryRetResponse();
        retResponse = payCoreService.queryAlipayRet(orderId);
        ResponseData response = new ResponseData();
        response.setMessage(retResponse.getMsg());
        if(retResponse.getCode().equals(PayReturnCodeEnum.SUCCESS .getCode())){
            response.setSuccess(true);
            response.setCode(200);
            response.setMessage("支付成功");
            return  response;
        }

        return new ResponseUtil<>().setErrorMsg(PayReturnCodeEnum.ORDER_HAD_NOT_PAY.getMsg());

    }
}
