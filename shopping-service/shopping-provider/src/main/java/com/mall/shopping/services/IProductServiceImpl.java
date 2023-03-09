package com.mall.shopping.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.commons.tool.redisconfig.RedissonAutoConfiguration;
import com.mall.commons.tool.redisconfig.RedissonProperties;
import com.mall.shopping.IProductService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.PanelConverter;
import com.mall.shopping.converter.ProductConverter;
import com.mall.shopping.dal.entitys.*;
import com.mall.shopping.dal.persistence.ItemDescMapper;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.*;
import org.apache.dubbo.config.annotation.Service;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class IProductServiceImpl implements IProductService {

    @Autowired
    ItemMapper itemMapper;

    @Autowired
    ItemDescMapper itemDescMapper;

    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    PanelMapper panelMapper;

    @Autowired
    PanelConverter panelConverter;

    @Autowired
    ProductConverter productConverter;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public ProductDetailResponse getProductDetail(ProductDetailRequest request) {
        ProductDetailResponse response = new ProductDetailResponse();
        ProductDetailDto productDetailDto = null;
        try {
            request.requestCheck();
            Long id = request.getId();
            Item item = itemMapper.selectByPrimaryKey(id);
            ItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
            // 封装对象
            List<String> productImageSmall = Arrays.asList(item.getImages());
            long limitNum = item.getLimitNum().longValue();
            productDetailDto = new ProductDetailDto(
                    id, item.getPrice(), item.getTitle(), item.getSellPoint(), limitNum,
                    item.getImageBig(), itemDesc.getItemDesc(), productImageSmall);
            response.setProductDetailDto(productDetailDto);
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            response.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }
        return response;
    }

    @Override
    public ProductDetailDto getProductDetail2(ProductDetailRequest request) {
        ProductDetailResponse response = new ProductDetailResponse();
        ProductDetailDto productDetailDto = null;
        try {
            request.requestCheck();
            Long id = request.getId();
            Item item = itemMapper.selectByPrimaryKey(id);
            ItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
            // 封装对象
            List<String> productImageSmall = Arrays.asList(item.getImages());
            long limitNum = item.getLimitNum().longValue();
            productDetailDto = new ProductDetailDto(
                    id, item.getPrice(), item.getTitle(), item.getSellPoint(), limitNum,
                    item.getImageBig(), itemDesc.getItemDesc(), productImageSmall);
            response.setProductDetailDto(productDetailDto);
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            response.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }

        return productDetailDto;
    }

    /**
     * 分页查询商品列表
     * by AL
     *
     * @param request
     * @return
     */
    @Override
    public AllProductResponse getAllProduct(AllProductRequest request) {
        AllProductResponse response = new AllProductResponse();

        try {

            //校验
            request.requestCheck();
            //条件设置及查询数据库
            //分页
            PageHelper.startPage(request.getPage(), request.getSize());
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            //价格上下限
            if (request.getPriceGt() != null) {
                criteria.andGreaterThanOrEqualTo("price", request.getPriceGt());
            }
            if (request.getPriceLte() != null) {
                criteria.andLessThanOrEqualTo("price", request.getPriceLte());
            }
            //价格升序or降序

            if ("1".equals(request.getSort())) {
                example.setOrderByClause("price asc");
            }
            if ("-1".equals(request.getSort())){
                example.setOrderByClause("price desc");
            }

            List<Item> items = itemMapper.selectByExample(example);

            PageInfo<Item> pageInfo = new PageInfo<>(items);

            List<ProductDto> productDtos = productConverter.items2Dto(items);

            response.setProductDtoList(productDtos);
            response.setTotal(pageInfo.getTotal());

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());


        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(ShoppingRetCode.SYSTEM_ERROR.getMessage());
        }

        return response;
    }

    /**
     * 获取推荐的商品
     *
     * @return
     */
    @Override
    public RecommendResponse2 getRecommendGoods() {

        RecommendResponse2 response = new RecommendResponse2();

        try {

            //List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(GlobalConstants.RECOMMEND_PANEL_ID);
            List<PanelContentItemDto2> panelContentItems = panelContentMapper.selectPanelContentItemDto2sWithPanelId(GlobalConstants.RECOMMEND_PANEL_ID);

            PanelDto2 panelDto = panelMapper.selectPanelDtoById(GlobalConstants.RECOMMEND_PANEL_ID);

            panelDto.setPanelContentItems(panelContentItems);

            Set<PanelDto2> result = new HashSet<>();
            result.add(panelDto);

            response.setPanelContentItemDtos(result);

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());


        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(ShoppingRetCode.SYSTEM_ERROR.getMessage());
        }


        return response;
    }

    /**
     * 购显示购物车列表
     * @return
     */
    @Override
    public CartListByIdResponse getCartItems() {
//        ApplicationContext context = new ApplicationContext(RedissonAutoConfiguration.class);
//        RedissonClient redissonClient = ((RedissonClient) context.getBean("redissonClient"));
//
//        RBucket<Object> bucket = redissonClient.getBucket("test");
//
//        bucket.set("test-value");
//
//
//        String s = (String) bucket.get();
//        System.out.println(s);

        RMap<Object, Object> map = redissonClient.getMap("testkey");
        map.put("field","value");
        Object testkey = map.get("testkey");
        return null;
    }
}
