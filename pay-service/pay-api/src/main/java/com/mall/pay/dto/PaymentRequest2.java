package com.mall.pay.dto;

import com.mall.commons.result.AbstractRequest;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lc
 * @Description:
 * @date 2021/8/28
 */
@Data
public class PaymentRequest2 extends AbstractRequest {
    //private static final long serialVersionUID = -3585301759653485462L;
    String info;
    BigDecimal money;
    String nickName;
    String orderId;
    String payType;
    Integer userId;

    @Override
    public void requestCheck() {

    }
}
