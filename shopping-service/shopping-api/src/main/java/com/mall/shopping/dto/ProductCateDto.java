package com.mall.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created  on 2019/8/8
 * 21:49.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCateDto implements Serializable {
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;
    private String iconUrl;
}
