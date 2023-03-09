package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cskaoyan.gateway.form.shopping.CartsForm;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.redisconfig.RedissonAutoConfiguration;
import com.mall.shopping.*;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.*;
import com.mall.user.annotation.Anonymous;
import com.mall.user.intercepter.TokenIntercepter;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author vikingar
 * @desc 该controller主要处理首页显示内容
 */

//@Anonymous
@RestController
@RequestMapping("shopping")
public class HomepageController {

    @Reference(timeout = 3000, retries = 0, check = false)
    IHomeService homeService;

    @Reference
    IContentService contentService;

    @Reference
    IProductCateService productCateService;

    @Reference
    IProductService productService;

    @Reference(timeout = 3000, retries = 0, check = false)
    ICartService cartService;

    @Anonymous
    @ApiOperation("主页显示商品")
    @GetMapping("homepage")
    public ResponseData getHomePage() {
        HomePageResponse homepage = homeService.homepage();
        // 调用成功
        if (ShoppingRetCode.SUCCESS.getCode().equals(homepage.getCode())) {
            return new ResponseUtil().setData(homepage.getPanelContentItemDtos());
        }
        return new ResponseUtil().setErrorMsg(homepage.getMsg());
    }

    @Anonymous
    @ApiOperation("主页上方的导航栏显示")
    @GetMapping("navigation")
    public ResponseData getShoppingNavigation() {
        NavListResponse navListResponse = contentService.queryNavList();
        // 查询成功
        if (ShoppingRetCode.SUCCESS.getCode().equals(navListResponse.getCode())) {
            return new ResponseUtil().setData(navListResponse.getPannelContentDtos());
        }
        return new ResponseUtil().setErrorMsg(navListResponse.getMsg());
    }

    @Anonymous
    @ApiOperation("悬浮至导航栏显示子类目")
    @GetMapping("categories")
    public ResponseData getShoppingCategories() {
        AllProductCateResponse allProductCate = productCateService.getAllProductCate(null);
        if (ShoppingRetCode.SUCCESS.getCode().equals(allProductCate.getCode())) {
            return new ResponseUtil().setData(allProductCate.getProductCateDtoList());
        }
        return new ResponseUtil().setErrorMsg(allProductCate.getMsg());
    }

    @Anonymous
    @ApiOperation("查看商品详细信息")
    @GetMapping("product/{id}")
    public ResponseData getProductDetailById(@PathVariable("id") Long id) {
        ProductDetailRequest request = new ProductDetailRequest();
        request.setId(id);
        ProductDetailResponse response = productService.getProductDetail(request);
        ProductDetailDto productDetailDto = response.getProductDetailDto();
        return new ResponseUtil().setData(productDetailDto);
    }

    /**
     * 此搜索功能前端没有具体实现，
     * 写了只是为了保证不返回 404
     */
    @PostMapping("search")
    public ResponseData searchGoods() {
        return new ResponseUtil().setData(null);
    }


    /**
     * 分页查询商品列表
     * by AL
     *
     * @param page
     * @param size
     * @param sort
     * @param priceGt
     * @param priceLte
     * @return
     */
    @Anonymous
    @GetMapping("goods")
    public ResponseData shoppingGoods(Integer page, Integer size, String sort, Integer priceGt, Integer priceLte) {

        AllProductRequest request = new AllProductRequest();

        request.setPage(page);
        request.setSize(size);
        request.setSort(sort);
        request.setPriceGt(priceGt);
        request.setPriceLte(priceLte);

        AllProductResponse allProduct = productService.getAllProduct(request);

        GoodsListResponse goodsListResponse = new GoodsListResponse();
        goodsListResponse.setData(allProduct.getProductDtoList());
        goodsListResponse.setTotal(allProduct.getTotal());

        if (ShoppingRetCode.SUCCESS.getCode().equals(allProduct.getCode())) {
            ResponseData responseData = new ResponseUtil().setData(goodsListResponse);
            return responseData;
        }

        return new ResponseUtil().setErrorMsg(allProduct.getMsg());

    }

    /**
     * 获取推荐商品
     * by AL
     *
     * @return
     */
    @Anonymous
    @GetMapping("recommend")
    public ResponseData recommend() {

        RecommendResponse2 response = productService.getRecommendGoods();
        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData(response.getPanelContentItemDtos());
        }

        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    /**
     * 购物车相关
     * 获得购物车列表
     * by AL
     *
     * @return
     */
    @GetMapping("carts")
    public ResponseData getCartListById(HttpServletRequest request) {

        CartListByIdRequest cartListByIdRequest = new CartListByIdRequest();
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long userId = Long.parseLong(object.get("uid").toString());
        cartListByIdRequest.setUserId(userId);

        CartListByIdResponse response = cartService.getCartListById(cartListByIdRequest);

        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData(response.getCartProductDtos());
        }

        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    /**
     * 添加至购物车
     *
     * @param string
     * @return
     */
    @PostMapping("carts")
    public ResponseData addCart(@RequestBody String string, HttpServletRequest request) {

        JSONObject object = JSON.parseObject(string);
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object2 = JSON.parseObject(userInfo);
        Long userId = Long.parseLong(object2.get("uid").toString());

        // Long userId = 74L;
//        Long.parseLong(object.get("userId").toString());
        Long productId = Long.parseLong(object.get("productId").toString());
        Integer productNum = Integer.parseInt(object.get("productNum").toString());

        AddCartRequest addCartRequest = new AddCartRequest();
        addCartRequest.setItemId(productId);
        addCartRequest.setNum(productNum);
        addCartRequest.setUserId(userId);

        AddCartResponse response = cartService.addToCart(addCartRequest);

        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData("成功");
        }

        return new ResponseUtil().setErrorMsg(response.getMsg());

    }

    /**
     * 更新购物车
     */

    @PutMapping("carts")
    public ResponseData updateCarts(@RequestBody CartsForm cartsForm,HttpServletRequest httpServletRequest) {
        UpdateCartNumRequest request = new UpdateCartNumRequest();

        String userInfo = (String) httpServletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object2 = JSON.parseObject(userInfo);
        Long userId = Long.parseLong(object2.get("uid").toString());
        // 临时写死的
        //Long userId = 74L;
        request.setUserId(userId);
        request.setChecked(cartsForm.getChecked());
        request.setItemId(cartsForm.getProductId());
        request.setNum(cartsForm.getProductNum());
        UpdateCartNumResponse response = cartService.updateCartNum(request);

        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData("成功");
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    /**
     * 删除单个购物车
     */
    @DeleteMapping("carts/{uid}/{pid}")
    public ResponseData deleteCarts(@PathVariable("uid") Long uid, @PathVariable("pid") Long pid) {
        DeleteCartItemRequest request = new DeleteCartItemRequest();
        request.setItemId(pid);
        // 临时写死的
        //uid = 74L;
        request.setUserId(uid);
        DeleteCartItemResponse response = cartService.deleteCartItem(request);
        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData("成功");
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }


    /**
     * 删除勾选的购物车
     */
    @DeleteMapping("items/{uid}")
    public ResponseData deleteCheckedCarts(@PathVariable("uid") Long uid) {
        DeleteCheckedItemRequest request = new DeleteCheckedItemRequest();
        // 临时写死的
        //uid = 74L;
        request.setUserId(uid);
        request.setUserId(uid);
        DeleteCheckedItemResposne response = cartService.deleteCheckedItem(request);
        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil().setData("成功");
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    @PutMapping("items")
    public ResponseData itemsChecked(@RequestBody CartsForm cartsForm){
        UpdateCartNumRequest request = new UpdateCartNumRequest();
        request.setChecked(cartsForm.getChecked());
        request.setUserId(cartsForm.getUserId());
        UpdateCartNumResponse response = cartService.itemsChecked(request);

        if (ShoppingRetCode.SUCCESS.getCode().equals(response.getCode())) {

            return new ResponseUtil().setData("成功");
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

}
