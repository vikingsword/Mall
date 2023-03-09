package com.mall.shopping.services;

import com.mall.shopping.ICartService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.CartItemConverter;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dto.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @PackageName:com.mall.shopping.services.cache
 * @ClassName:CartServiceImpl
 * @Description:
 * @author:AL
 * @date:2021/8/26 8:58
 */

@Service
@Slf4j
public class CartServiceImpl implements ICartService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    ItemMapper itemMapper;


    /**
     * 获得购物车列表
     *
     * @param request
     * @return
     */
    @Override
    public CartListByIdResponse getCartListById(CartListByIdRequest request) {

        CartListByIdResponse response = new CartListByIdResponse();

        try {
            request.requestCheck();
            Long userId = request.getUserId();
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            Set<Map.Entry<Object, Object>> entries = map.entrySet();

            List<CartProductDto> cartProductDtos = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : entries) {

                Object key = entry.getKey();
                CartProductDto cartProductDto = (CartProductDto) entry.getValue();

                cartProductDtos.add(cartProductDto);

            }

            response.setCartProductDtos(cartProductDtos);
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
     * 添加到购物车
     *
     * @param request
     * @return
     */
    @Override
    public AddCartResponse addToCart(AddCartRequest request) {

        AddCartResponse response = new AddCartResponse();
        try {

            request.requestCheck();

            /*
             * 存放到redis中，key为userId，field为itemId，value为CartProductDto
             */
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());

            if (map.containsKey(request.getItemId())) {
                CartProductDto cartProductDto = (CartProductDto) map.get(request.getItemId());
                Long productNum = cartProductDto.getProductNum();
                cartProductDto.setProductNum(productNum + request.getNum());
                map.put(request.getItemId(), cartProductDto);

            } else {
                Item item = itemMapper.selectByPrimaryKey(request.getItemId());
                CartProductDto cartProductDto = CartItemConverter.item2Dto(item);
                cartProductDto.setProductNum(request.getNum().longValue());
                /**
                 * 写死为true
                 * todo
                 */
                cartProductDto.setChecked("true");
                map.put(request.getItemId(), cartProductDto);
            }
//            Object o = map.get(request.getItemId());
//            System.out.println(o);

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
     * 更新购物车
     */
    @Override
    public UpdateCartNumResponse updateCartNum(UpdateCartNumRequest request) {
        UpdateCartNumResponse response = new UpdateCartNumResponse();

        try {
            request.requestCheck();
            /*
             * 在redis中，key为userId，field为itemId，value为CartProductDto
             */
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            if (map.containsKey(request.getItemId())) {
                CartProductDto cartProductDto = (CartProductDto) map.get(request.getItemId());
                cartProductDto.setProductNum((long) request.getNum());
                cartProductDto.setChecked(request.getChecked());
                map.put(request.getItemId(), cartProductDto);
            }
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(ShoppingRetCode.SYSTEM_ERROR.getMessage());
        }
        return response;
    }

    @Override
    public CheckAllItemResponse checkAllCartItem(CheckAllItemRequest request) {
        return null;
    }


    /**
     * 删除单个购物车
     */
    @Override
    public DeleteCartItemResponse deleteCartItem(DeleteCartItemRequest request) {
        DeleteCartItemResponse response = new DeleteCartItemResponse();

        try {
            request.requestCheck();
            /*
             * 在redis中，key为userId，field为itemId，value为CartProductDto
             */
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            // 删除对应itemId的数据
            map.remove(request.getItemId());
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
     * 删除勾选购物车
     */
    @Override
    public DeleteCheckedItemResposne deleteCheckedItem(DeleteCheckedItemRequest request) {
        DeleteCheckedItemResposne response = new DeleteCheckedItemResposne();

        try {
            request.requestCheck();
            /*
             * 在redis中，key为userId，field为itemId，value为CartProductDto
             */
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            if (StringUtil.isNotBlank(map.toString())) {
                // 遍历map
                for (Object field : map.keySet()) {
                    CartProductDto cartProductDto = (CartProductDto) map.get(field);
                    // 如果购物车商品被选中 删除这条数据
                    if ("true".equals(cartProductDto.getChecked())) {
                        map.remove(field);
                    }
                }
            }
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
     * 清空购物车中的商品（下单以后）
     *
     * @param request
     * @return
     */
    @Override
    public ClearCartItemResponse clearCartItemByUserID(ClearCartItemRequest request) {
        ClearCartItemResponse clearCartItemResponse = new ClearCartItemResponse();
        try {
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            List<Long> productIds = request.getProductIds();
            for (Long productId : productIds) {
                if (map.containsKey(productId)) {
                    map.remove(productId);
                }
            }
            /*if(map.containsKey(request.getProductIds())){//如果购物车中包含当前订单商品清除此商品
                map.remove(request.getProductIds());
            }*/
            clearCartItemResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            clearCartItemResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            clearCartItemResponse.setCode(ShoppingRetCode.SYSTEM_ERROR.getCode());
            clearCartItemResponse.setMsg(ShoppingRetCode.SYSTEM_ERROR.getMessage());
        }
        return clearCartItemResponse;
    }

    @Override
    public UpdateCartNumResponse itemsChecked(UpdateCartNumRequest request) {
        UpdateCartNumResponse response = new UpdateCartNumResponse();

        try {
            //request.requestCheck();
            RMap<Object, Object> map = redissonClient.getMap(request.getUserId().toString());
            Set<Map.Entry<Object, Object>> entries = map.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {

                Object key = entry.getKey();
                CartProductDto cartProductDto = (CartProductDto) entry.getValue();

                cartProductDto.setChecked(request.getChecked());
                map.put(key, cartProductDto);

            }
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(ShoppingRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(ShoppingRetCode.SYSTEM_ERROR.getMessage());
        }

        return response;
    }
}
