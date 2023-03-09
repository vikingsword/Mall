package com.mall.shopping.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.util.List;

/**
 * @PackageName:com.mall.shopping.dto
 * @ClassName:GoodsListResponse
 * @Description:
 * @author:AL
 * @date:2021/8/25 14:07
 */

@Data
public class GoodsListResponse extends AbstractResponse {

    private List<ProductDto> data;

    private Long total;
}
