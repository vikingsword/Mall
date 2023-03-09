package com.mall.shopping.bootstrap;


import com.mall.shopping.ITestProductDetailService;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;

import com.mall.shopping.dto.TestProductDetailRequest;
import com.mall.shopping.dto.TestProductDetailResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MyTest {

    @Autowired
    ItemMapper itemMapper;

    @Reference
    ITestProductDetailService productService;

    @Test
    public void testMapper() {
        Item item = itemMapper.selectByPrimaryKey(100023501);
        System.out.println(item);

    }

    @Test
    public void testService() {

        TestProductDetailRequest testProductDetailRequest = new TestProductDetailRequest();
        testProductDetailRequest.setProductId(100023501L);

        TestProductDetailResponse productDetail = productService.getProductDetail(testProductDetailRequest);
        System.out.println(productDetail);
    }
    @Test
    public void testTime(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(date);
        System.out.println(format);
    }

}
