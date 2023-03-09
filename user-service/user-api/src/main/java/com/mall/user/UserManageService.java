//package com.mall.user;
//
//import com.mall.commons.result.ResponseData;
//import com.mall.user.dto.*;
//
///**
// * 用户管理
// */
//public interface UserManageService {
//
//    /**
//     * 验证登录的接口
//     */
//    ResponseData verifyLogin(String token);
//
//    /**
//     * 注册的接口
//     */
//    ResponseData register(String userName,String userPwd,String captcha,String email,String uuid);
//
//    /**
//     * 退出的接口
//     */
//    ResponseData loginOut();
//
//    /**
//     * 登录的接口
//     */
//    UserLoginResponse userlogin(UserLoginRequest userLoginRequest) ;
//
////    /**
////     * 发送验证码的接口
////     */
////    ResponseData captcha(String timestamp);
//
//}
