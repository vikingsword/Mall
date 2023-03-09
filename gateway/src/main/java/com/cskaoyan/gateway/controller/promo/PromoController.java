package com.cskaoyan.gateway.controller.promo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cskaoyan.gateway.config.CacheManager;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.promo.PromoService;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dto.*;
import com.mall.user.annotation.Anonymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.curator.shaded.com.google.common.util.concurrent.RateLimiter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author lc
 * @Description: 秒杀
 * @date 2021/8/30
 */
@RestController
@RequestMapping("/shopping")
public class PromoController {
    @Reference(timeout = 3000,retries = 2, check = false)
    PromoService promoService;


    @Autowired
    CacheManager cacheManager;

    //@Autowired
    RateLimiter rateLimiter;

    ExecutorService executor;

    @PostConstruct
    public void init(){
        rateLimiter = RateLimiter.create(100);
        executor = Executors.newFixedThreadPool(100);
    }




    /**
     * 获取秒杀商品列表
     * @param request
     * @return
     */
    @Anonymous
    @RequestMapping("/seckilllist")
    public ResponseData promoSeckilllist(PromoInfoRequest request){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(date);
        request.setYyyymmdd(format);

        PromoInfoResponse response = promoService.getPromoList(request);
        if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response);
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());

    }

    /**
     * 获得秒杀商品的详细信息
     * @param request
     * @return
     */
    @RequestMapping("/promoProductDetail")
    public ResponseData promoProductDetail(@RequestBody PromoProductDetailRequest request){
        PromoProductDetailResponse response = promoService.getPromoProductDetail(request);
        if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response);
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    /**
     * 秒杀
     * @param promoOrderRequest
     * @param request
     * @return
     */
    @RequestMapping("/seckill")
    public ResponseData promoSeckill(@RequestBody CreatePromoOrderRequest promoOrderRequest ,HttpServletRequest request){
        //限流
        rateLimiter.acquire();

        Long productId = promoOrderRequest.getProductId();
        Long psId = promoOrderRequest.getPsId();
        String stockKey =  "prom_item_stock" + productId + "-" + psId;
        String result = cacheManager.checkCache(stockKey);
        if(result != null && "none".equals(result)){
            return new ResponseUtil<>().setErrorMsg(PromoRetCode.PROMO_ORDER_FAILED.getMessage());
        }

        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        promoOrderRequest.setUserId(uid);

        //线程池 队列泄洪

        Future<CreatePromoOrderResponse> future = executor.submit(new Callable<CreatePromoOrderResponse>() {
            @Override
            public CreatePromoOrderResponse call() throws Exception {
                //return promoService.createPromoOrder(promoOrderRequest);//使用一般秒杀
                return promoService.createPromoOrderInTransaction(promoOrderRequest);//分布式事务秒杀

            }
        });


        CreatePromoOrderResponse response = null;
        try {
            response = future.get();
            if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
                return new ResponseUtil().setData(response);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());

    }
}
