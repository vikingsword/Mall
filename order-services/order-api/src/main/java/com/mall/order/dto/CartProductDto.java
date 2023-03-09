package com.mall.order.dto;/**
 * Created  on 2019/8/1.
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * create-date: 2019/8/1-下午9:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductDto implements Serializable{

    private Long productId;

    private BigDecimal salePrice;

    private Long productNum;

    private Long limitNum;

    private String checked;

    private String productName;

    private String productImg;
}
