package com.mall.pay.services;

import com.mall.order.OrderCoreService;
import com.mall.order.dto.PayOrderSuccessResponse;
import com.mall.pay.PayCoreService;
import com.mall.pay.constants.PayResultEnum;
import com.mall.pay.constants.PayReturnCodeEnum;
import com.mall.pay.dal.entitys.Payment;
import com.mall.pay.dal.persistence.PaymentMapper;
import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.PaymentRequest2;
import com.mall.pay.dto.alipay.AlipayQueryRetResponse;
import com.mall.pay.dto.alipay.AlipaymentResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class PayCorServiceImpl implements PayCoreService {

    @Autowired
    PayHelper payHelper;
    @Autowired
    PaymentMapper paymentMapper;


    @Reference(timeout = 3000,retries = 2, check = false)
    OrderCoreService orderCoreService;

    @Override
    public AlipaymentResponse aliPay(PaymentRequest request) {

        AlipaymentResponse alipaymentResponse = new AlipaymentResponse();
        // 1. 携帶相关的支付信息，去支付宝后台，请求支付二维码
        String filePath = payHelper.test_trade_precreate(request);
        if (filePath == null) {
            alipaymentResponse.setMsg(PayResultEnum.PAY_FAIL.getDesc());
            alipaymentResponse.setCode(PayResultEnum.PAY_FAIL.getCode());
            return alipaymentResponse;
            // 2. 如果没有获取到支付二维码，直接返回支付失败
            //  return 响应  GET_CODE_FALIED
        }
        // 3. 如果能获取支付二维码，在tb_payment插入一条支付记录(支付中)
        Payment payment = new Payment();
        Date date = new Date();
        payment.setStatus("1");
        payment.setOrderId(request.getOrderId());
        payment.setProductName(request.getInfo());
        payment.setPayerUid(request.getUserId().longValue());
        payment.setPayerName(request.getNickName());
        payment.setOrderAmount(request.getMoney());
        payment.setPayerAmount(request.getMoney());
        payment.setPayWay(request.getPayType());
        payment.setCreateTime(date);
        int insert = paymentMapper.insert(payment);




        // 4. 返回获取到的支付二维码
        alipaymentResponse.setQrCode(filePath);
        return alipaymentResponse;
    }

    /**
     * 查询支付状态
     * @param orderId
     * @return
     */
    @Override
    public AlipayQueryRetResponse queryAlipayRet(String orderId) {
        AlipayQueryRetResponse response = new AlipayQueryRetResponse();
        boolean payResult = payHelper.test_trade_query(orderId);

        if (payResult) {
            Date date = new Date();
            Payment payment = new Payment();
            payment.setStatus(PayResultEnum.PAY_SUCCESS.getCode());
            payment.setUpdateTime(date);
            payment.setCompleteTime(date);
            payment.setPaySuccessTime(date);
            Example example = new Example(Payment.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderId",orderId);
            // 1. 修改tb_payment支付状态(支付成功)
            int i = paymentMapper.updateByExampleSelective(payment, example);



            //   调用订单服务的接口方法去完成的功能
            PayOrderSuccessResponse payOrderSuccessResponse = orderCoreService.payOrderSuccess(orderId);
            response.setCode(PayReturnCodeEnum.SUCCESS.getCode());
            response.setMsg(PayReturnCodeEnum.SUCCESS.getMsg());
            return response;
            // 2. 修改订单状态为已支付
            // 3. 扣减锁定库存(根据订单中的订单商品条目的数量)
            // 4. 返回支付成功的响应
        } else {
            Date date = new Date();
            Payment payment = new Payment();
            payment.setStatus(PayResultEnum.PAY_FAIL.getCode());
            payment.setUpdateTime(date);
           // payment.setCompleteTime(date);
           // payment.setPaySuccessTime(date);
            Example example = new Example(Payment.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderId",orderId);
            // 1. 修改tb_payment支付状态(支付成功)
            int i = paymentMapper.updateByExampleSelective(payment, example);
            response.setCode(PayReturnCodeEnum.ORDER_HAD_NOT_PAY .getCode());
            response.setMsg(PayReturnCodeEnum.ORDER_HAD_NOT_PAY .getMsg());
            // 1. 修改tb_payment中的支付状态(支付失败)
            // 2. 返回支付失败的响应
            return response;
        }
    }
}
