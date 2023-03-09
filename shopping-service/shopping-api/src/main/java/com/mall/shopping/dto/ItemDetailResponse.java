package com.mall.shopping.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDetailResponse extends AbstractResponse implements Serializable {


    private Long productId;
    private BigDecimal salePrice;
    private String productName;
    private String subTitle;
    private Long limitNum;
    private String productImageBig;
    private String detail;

    private List<String> productImageSmall;
}
