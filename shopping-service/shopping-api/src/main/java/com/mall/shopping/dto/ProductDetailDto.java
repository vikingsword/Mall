package com.mall.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * create-date: 2019/7/24-18:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto implements Serializable {

    private static final long serialVersionUID = -597050593951733519L;
    private Long productId;
    private BigDecimal salePrice;
    private String productName;
    private String subTitle;
    private Long limitNum;
    private String productImageBig;
    private String detail;

    private List<String> productImageSmall;
}
