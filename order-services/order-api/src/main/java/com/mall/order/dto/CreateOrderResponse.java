package com.mall.order.dto;/**
 * Created on 2019/7/30.
 */

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

/**
 * create-date: 2019/7/30-上午9:49
 */
@Data
public class CreateOrderResponse extends AbstractResponse{

    private String orderId;
}
