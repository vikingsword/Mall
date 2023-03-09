package com.mall.promo.service;
import com.mall.order.OrderPromoService;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.PromoService;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.converter.PromoInfoConverter;
import com.mall.promo.converter.PromoProductConverter;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dal.entitys.PromoSession;
import com.mall.promo.dal.persistence.PromoItemMapper;
import com.mall.promo.dal.persistence.PromoSessionMapper;
import com.mall.promo.dto.*;
import com.mall.promo.mq.CreatePromoOrderProducer;
import com.mall.shopping.IProductService;
import com.mall.shopping.dto.ProductDetailDto;
import com.mall.shopping.dto.ProductDetailRequest;
import com.mall.shopping.dto.ProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 **/
@Service
@Slf4j
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoSessionMapper sessionMapper;

    @Autowired
    PromoItemMapper promoItemMapper;

    @Reference(check = false, timeout = 3000)
    IProductService productService;

    @Reference(check = false, timeout = 3000)
    OrderPromoService orderPromoService;

    @Autowired
    PromoInfoConverter promoInfoConverter;


    @Autowired
    PromoProductConverter promoProductConverter;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    CreatePromoOrderProducer createPromoOrderProducer;


    /**
     * 获得秒杀商品列表
     * @param request
     * @return
     */

    @Override
    public PromoInfoResponse getPromoList(PromoInfoRequest request) {
        PromoInfoResponse response = new PromoInfoResponse();

        try {
            request.requestCheck();

            log.info("start getPromoList sessionId = " + request.getSessionId() + ", yyyymmdd " + request.getYyyymmdd());
            //查询场次信息
            Example promoSessionExample = new Example(PromoSession.class);
            promoSessionExample.createCriteria()
                    .andEqualTo("sessionId",request.getSessionId())
                    .andEqualTo("yyyymmdd",request.getYyyymmdd());
            List<PromoSession> sessionList = sessionMapper.selectByExample(promoSessionExample);

            if (CollectionUtils.isEmpty(sessionList)) {
                // 如果没找到秒杀场次
                response.setCode(PromoRetCode.PROMO_NOT_EXIST.getCode());
                response.setMsg(PromoRetCode.PROMO_NOT_EXIST.getMessage());
                return response;
            }

            // 获取某天中唯一的秒杀场次
            PromoSession promoSession = sessionList.get(0);

            Example promoItemExample = new Example(PromoItem.class);
            promoItemExample.createCriteria().andEqualTo("psId", promoSession.getId());
            List<PromoItem> promoItems = promoItemMapper.selectByExample(promoItemExample);

            List<PromoItemInfoDto> productList = new ArrayList<>();

            // 对该秒杀场次的每一个商品，查找其商品详情
            promoItems.forEach(promoItem -> {
                Long itemId = promoItem.getItemId();

                ProductDetailRequest productDetailRequest = new ProductDetailRequest();
                productDetailRequest.setId(itemId);
                ProductDetailResponse productDetail = productService.getProductDetail(productDetailRequest);
                ProductDetailDto productDetailDto = productDetail.getProductDetailDto();
                PromoItemInfoDto promoItemInfoDto = promoInfoConverter.convert2InfoDto(productDetailDto);
                promoItemInfoDto.setInventory(promoItem.getItemStock());
                promoItemInfoDto.setSeckillPrice(promoItem.getSeckillPrice());
                productList.add(promoItemInfoDto);

            });

            //组装参数
            response.setPsId(promoSession.getId());
            response.setSessionId(request.getSessionId());
            response.setCode(PromoRetCode.SUCCESS.getCode());
            response.setMsg(PromoRetCode.SUCCESS.getMessage());
            response.setProductList(productList);
        } catch (Exception e) {
            response.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
        }
        return response;
    }

    /**
     * 秒杀下单
     * @param request
     * @return
     */
    @Override
    @Transactional
    public CreatePromoOrderResponse createPromoOrder(CreatePromoOrderRequest request) {
        CreatePromoOrderResponse createPromoOrderResponse = new CreatePromoOrderResponse();
        createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());


        List<PromoItem> promoItems = null;
        try {
            request.requestCheck();
            int effectiveRow = 0;

            effectiveRow = promoItemMapper.decreaseStock(request.getProductId(), request.getPsId());

            if (effectiveRow < 1) {
                createPromoOrderResponse.setCode(PromoRetCode.PROMO_ITEM_STOCK_NOT_ENOUGH.getCode());
                createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ITEM_STOCK_NOT_ENOUGH.getMessage());
                return createPromoOrderResponse;
            }

            //获取商品的秒杀价格
            Example example = new Example(PromoItem.class);
            example.createCriteria()
                    .andEqualTo("psId",request.getPsId())
                    .andEqualTo("itemId",request.getProductId());
            promoItems = promoItemMapper.selectByExample(example);
        } catch (Exception e) {
            log.error("PromoServiceImpl.createPromoOrder occurs error" + e);
            createPromoOrderResponse.setCode(PromoRetCode.DB_EXCEPTION.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.DB_EXCEPTION.getMessage());
            return createPromoOrderResponse;
        }
        if (CollectionUtils.isEmpty(promoItems)) {
            createPromoOrderResponse.setCode(PromoRetCode.PROMO_ITEM_NOT_EXIST.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ITEM_NOT_EXIST.getMessage());
            return createPromoOrderResponse;
        }

        PromoItem promoItem = promoItems.get(0);

        // 生成订单
        CreateSeckillOrderRequest createSeckillOrderRequest = new  CreateSeckillOrderRequest();
        createSeckillOrderRequest.setUsername(request.getUserName());
        createSeckillOrderRequest.setUserId(request.getUserId());
        createSeckillOrderRequest.setProductId(request.getProductId());
        createSeckillOrderRequest.setPrice(promoItem.getSeckillPrice());
        createSeckillOrderRequest.setAddressId(request.getAddressId());
        createSeckillOrderRequest.setStreetName(request.getStreetName());
        createSeckillOrderRequest.setTel(request.getTel());
        CreateSeckillOrderResponse createSeckillOrderResponse
                = orderPromoService.createPromoOrder(createSeckillOrderRequest);

        if (!createSeckillOrderResponse.getCode().equals(PromoRetCode.SUCCESS.getCode())) {
            createPromoOrderResponse.setCode(createSeckillOrderResponse.getCode());
            createPromoOrderResponse.setMsg(createSeckillOrderResponse.getMsg());
            return createPromoOrderResponse;
        }

        createPromoOrderResponse.setInventory(promoItem.getItemStock());
        createPromoOrderResponse.setProductId(promoItem.getItemId());
        createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());

        return createPromoOrderResponse;
    }

    /**
     * 分布式事务 秒杀
     * @param request
     * @return
     */
    @Override
    @Transactional
    public CreatePromoOrderResponse createPromoOrderInTransaction(CreatePromoOrderRequest request) {

        CreatePromoOrderResponse createPromoOrderResponse = new CreatePromoOrderResponse();
        createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());

        //获取商品的秒杀价格
        Example example = new Example(PromoItem.class);
        Long psId = request.getPsId();
        Long productId = request.getProductId();

        example.createCriteria()
                .andEqualTo("psId", psId)
                .andEqualTo("itemId", productId);
        List<PromoItem> promoItems = promoItemMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(promoItems)) {
            createPromoOrderResponse.setCode(PromoRetCode.PROMO_ITEM_NOT_EXIST.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ITEM_NOT_EXIST.getMessage());
            return createPromoOrderResponse;
        }
        PromoItem promoItem = promoItems.get(0);

        // 生成订单
        CreateSeckillOrderRequest createSeckillOrderRequest = new  CreateSeckillOrderRequest();
        createSeckillOrderRequest.setUsername(request.getUserName());
        createSeckillOrderRequest.setUserId(request.getUserId());
        createSeckillOrderRequest.setProductId(request.getProductId());
        createSeckillOrderRequest.setPrice(promoItem.getSeckillPrice());
        createSeckillOrderRequest.setAddressId(request.getAddressId());
        createSeckillOrderRequest.setStreetName(request.getStreetName());
        createSeckillOrderRequest.setTel(request.getTel());


        // 发送事物消息，开启分布式事务
        boolean ret = createPromoOrderProducer.sendPromOrderMessage(createSeckillOrderRequest, productId, psId);
        if (ret) {
            // 秒杀下单成功的响应
            createPromoOrderResponse.setInventory(promoItem.getItemStock());
            createPromoOrderResponse.setProductId(promoItem.getItemId());
            createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());
        } else {
            // 秒杀下单失败的响应
            createPromoOrderResponse.setCode(PromoRetCode.PROMO_ORDER_FAILED.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ORDER_FAILED.getMessage());

        }


        return createPromoOrderResponse;
    }

    /**
     * 获取秒杀商品详情
     * @param request
     * @return
     */
    @Override
    public PromoProductDetailResponse getPromoProductDetail(PromoProductDetailRequest request) {

        PromoProductDetailResponse promoProductDetailResponse = new PromoProductDetailResponse();

        Example example = new Example(PromoItem.class);
        example.createCriteria()
                .andEqualTo("psId", request.getPsId())
                .andEqualTo("itemId", request.getProductId());


        List<PromoItem> promoItems = null;
        try {
            request.requestCheck();
            promoItems = promoItemMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(promoItems)) {
                promoProductDetailResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
                promoProductDetailResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
                return promoProductDetailResponse;
            }

            // 秒杀商品条目
            PromoItem promoItem = promoItems.get(0);

            ProductDetailRequest productDetailRequest = new ProductDetailRequest();
            productDetailRequest.setId(request.getProductId());
            ProductDetailResponse productDetailResponse = productService.getProductDetail(productDetailRequest);
            if (!PromoRetCode.SUCCESS.getCode().equals(productDetailResponse.getCode())) {
                promoProductDetailResponse.setCode(productDetailResponse.getCode());
                promoProductDetailResponse.setMsg(productDetailResponse.getMsg());
                return promoProductDetailResponse;
            }

            PromoProductDetailDTO promoProductDetailDTO
                    = promoProductConverter.convert2DetailDTO(promoItem, productDetailResponse.getProductDetailDto());

            promoProductDetailResponse.setPromoProductDetailDTO(promoProductDetailDTO);
            promoProductDetailResponse.setCode(PromoRetCode.SUCCESS.getCode());
            promoProductDetailResponse.setMsg(PromoRetCode.SUCCESS.getMessage());
            return promoProductDetailResponse;
        } catch (Exception e) {
            log.error("PromoServiceImpl.getPromoProductDetail occurs error");
            promoProductDetailResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
            promoProductDetailResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
            return promoProductDetailResponse;
        }


    }
}