package com.mall.order.biz.context;/**
 * Created by cskaoyan on 2019/8/2.
 */

import com.mall.order.dto.CartProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 *  cskaoyan
 * create-date: 2019/8/2-下午11:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderContext extends AbsTransHandlerContext{

    private Long userId;

    private Long addressId;

    private String tel;

    private String userName;

    private String streetName;

    private BigDecimal orderTotal;

    List<CartProductDto> cartProductDtoList;

    private List<Long> buyProductIds;  // 生成订单商品的ID list

    private String buyerNickName;  // 购买用户的昵称

    private String uniqueKey; //业务唯一id

}
