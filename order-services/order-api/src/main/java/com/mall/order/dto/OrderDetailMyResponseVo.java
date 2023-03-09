package com.mall.order.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lc
 * @Description:
 * @date 2021/8/25
 */
@Data
public class OrderDetailMyResponseVo {
    private Integer orderStatus;
    private BigDecimal orderTotal;

    private String streetName;
    private String tel;
    private Long userId;
    private String userName;

    private List<OrderItemDto> goodsList;

}
