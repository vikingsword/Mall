package com.mall.shopping.services;

import com.mall.shopping.IProductCateService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ProductCateConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.shopping.dto.ProductCateDto;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Service
public class IProductCateServiceImpl implements IProductCateService {

    @Autowired
    ItemCatMapper itemCatMapper;

    @Autowired
    ProductCateConverter productCateConverter;

    @Override
    public AllProductCateResponse getAllProductCate(AllProductCateRequest request) {
        AllProductCateResponse response = new AllProductCateResponse();

        try {
            List<ItemCat> itemCats = itemCatMapper.selectAll();
            List<ProductCateDto> productCateDtos = productCateConverter.items2Dto(itemCats);

            // 查询成功
            response.setProductCateDtoList(productCateDtos);
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            // 查询数据库错误
            e.printStackTrace();
            response.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            response.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }

        return response;
    }
}
