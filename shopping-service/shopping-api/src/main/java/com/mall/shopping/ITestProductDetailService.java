package com.mall.shopping;

import com.mall.shopping.dto.TestProductDetailDto;
import com.mall.shopping.dto.TestProductDetailRequest;
import com.mall.shopping.dto.TestProductDetailResponse;

public interface ITestProductDetailService {
    TestProductDetailResponse getProductDetail(TestProductDetailRequest request);
}
