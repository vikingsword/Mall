package com.mall.shopping.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * create-date: 2019/7/23-19:01
 */
@Data
public class CartProductDto implements Serializable {
    private static final long serialVersionUID = -809047960626248847L;

    private Long productId;

    private BigDecimal salePrice;

    private Long productNum;

    private Long limitNum;

    private String checked;

    private String productName;

    private String productImg;
}
