package com.cskaoyan.gateway.controller.test;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.ITestProductDetailService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.TestProductDetailRequest;
import com.mall.shopping.dto.TestProductDetailResponse;
import com.mall.user.annotation.Anonymous;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestProductController {

    @Reference(timeout = 3000, retries = 0, check = false)
    ITestProductDetailService productDetailService;


    @GetMapping("/product")
    @Anonymous
    public ResponseData getProductDetail(Long productId) {
        TestProductDetailRequest testProductDetailRequest = new TestProductDetailRequest();
        testProductDetailRequest.setProductId(productId);

        TestProductDetailResponse productDetail =
                productDetailService.getProductDetail(testProductDetailRequest);

        if (ShoppingRetCode.SUCCESS.getCode().equals(productDetail.getCode())) {
            // 调用成功
            return new ResponseUtil().setData(productDetail.getProductDetailDto());
        }

        return new ResponseUtil().setErrorMsg(productDetail.getMsg());
    }
}
