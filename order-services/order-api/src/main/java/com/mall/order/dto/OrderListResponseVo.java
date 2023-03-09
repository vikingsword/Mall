package com.mall.order.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.util.List;

/**

 * create-date: 2019/7/30-上午10:02
 */
@Data
public class OrderListResponseVo {

    private List<OrderDetailInfo> data;

    /**
     * 总记录数
     */
    private Long total;

}
