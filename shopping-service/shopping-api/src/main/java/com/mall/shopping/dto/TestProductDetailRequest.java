package com.mall.shopping.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.shopping.constants.ShoppingRetCode;
import lombok.Data;

@Data
public class TestProductDetailRequest extends AbstractRequest {

    Long productId;

    @Override
    public void requestCheck() {
        // 判断如果productId < 0， 抛出异常
        if (productId < 0) {
            throw new ValidateException(
                    ShoppingRetCode.PARAMETER_VALIDATION_FAILED.getCode(),
                    ShoppingRetCode.PARAMETER_VALIDATION_FAILED.getMessage());
        }
    }
}
