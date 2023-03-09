package com.cskaoyan.gateway.controller.shopping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cskaoyan.gateway.form.shopping.AddressForm;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.user.IAddressService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping("/shopping")
public class AddressController {

    @Reference(timeout = 3000,retries = 2, check = false)
    IAddressService addressService;

    /**
     * 获取当前用户的地址列表
     *
     * @return
     */
    @GetMapping("/addresses")
    public ResponseData addressList(HttpServletRequest request) {
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());

        AddressListRequest addressListRequest = new AddressListRequest();
        addressListRequest.setUserId(uid);

        AddressListResponse response = addressService.addressList(addressListRequest);
        if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getAddressDtos());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    @PostMapping("/addresses")
    public ResponseData addressAdd(@RequestBody AddressForm form, HttpServletRequest servletRequest) {
        log.debug(form.is_Default()+"");
        log.debug(form.toString());

        AddAddressRequest request = new AddAddressRequest();
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        request.setUserId(uid);
        request.setUserName(form.getUserName());
        request.setStreetName(form.getStreetName());
        request.setTel(form.getTel());
        request.setIsDefault(form.is_Default() ? 1 : null);
        AddAddressResponse response = addressService.createAddress(request);

        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getMsg());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }

    @DeleteMapping("/addresses/{addressid}")
    public ResponseData addressDel(@PathVariable("addressid") Long addressid) {
        DeleteAddressRequest request = new DeleteAddressRequest();
        request.setAddressId(addressid);
        DeleteAddressResponse response = addressService.deleteAddress(request);

        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getMsg());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());

    }

    @PutMapping("/addresses")
    public ResponseData addressUpdate(@RequestBody AddressForm form, HttpServletRequest servletRequest) {
        UpdateAddressRequest request = new UpdateAddressRequest();
        String userInfo = (String) servletRequest.getAttribute(TokenIntercepter.USER_INFO_KEY);
        JSONObject object = JSON.parseObject(userInfo);
        Long uid = Long.parseLong(object.get("uid").toString());
        request.setAddressId(form.getAddressId());
        request.setIsDefault(form.is_Default() ? 1 : null);
        request.setStreetName(form.getStreetName());
        request.setTel(form.getTel());
        request.setUserId(uid);
        request.setUserName(form.getUserName());

        UpdateAddressResponse response = addressService.updateAddress(request);

        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil().setData(response.getMsg());
        }
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }
}
