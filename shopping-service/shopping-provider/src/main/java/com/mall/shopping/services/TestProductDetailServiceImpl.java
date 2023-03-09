package com.mall.shopping.services;

import com.mall.commons.tool.exception.ValidateException;
import com.mall.shopping.ITestProductDetailService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.TestProductConverter;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dto.TestProductDetailDto;
import com.mall.shopping.dto.TestProductDetailRequest;
import com.mall.shopping.dto.TestProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class TestProductDetailServiceImpl implements ITestProductDetailService {

    @Autowired
    ItemMapper itemMapper;

    @Autowired
    TestProductConverter productConverter;

    @Override
    public TestProductDetailResponse getProductDetail(TestProductDetailRequest request) {
        TestProductDetailResponse response = new TestProductDetailResponse();
        try {

            System.out.println("调用到了getProductDetail");
            // 校验请求参数的有效性
            request.requestCheck();

            // 查询数据库
            Item item = itemMapper.selectByPrimaryKey(request.getProductId());
            // 转化dto对象
            TestProductDetailDto testProductDetailDto = productConverter.itemToItemDto(item);
            // 成功执行
            response.setProductDetailDto(testProductDetailDto);
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        }catch (ValidateException e) {
            response.setCode(e.getErrorCode());
            response.setMsg(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            response.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }
        return response;
    }
}
