//package com.cskaoyan.gateway.controller.user;
//
//import com.mall.commons.result.ResponseData;
//import com.mall.commons.result.ResponseUtil;
//import com.mall.commons.tool.utils.CookieUtil;
//import com.mall.order.dto.DeleteOrderRequest;
//import com.mall.order.dto.DeleteOrderResponse;
//import com.mall.shopping.constants.ShoppingRetCode;
//import com.mall.user.IKaptchaService;
//import com.mall.user.IMemberService;
//import com.mall.user.UserManageService;
//import com.mall.user.annotation.Anonymous;
//import com.mall.user.constants.SysRetCodeConstants;
//import com.mall.user.dto.*;
//import org.apache.dubbo.config.annotation.Reference;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import static com.mall.user.intercepter.TokenIntercepter.ACCESS_TOKEN;
//
//@RestController
//@RequestMapping("user")
//public class UserController {
//
//    @Reference
//    UserManageService userManageService;
//
//    @Reference
//    IMemberService iMemberService;
//
//    //登录接口
//    @PostMapping("login")
//    @Anonymous
//    public ResponseData login(String username,String userPwd, HttpServletResponse servletResponse, HttpServletRequest servletRequest) {
//        ResponseData responseData = new ResponseData();
//
//
//        UserLoginRequest userLoginRequest = new UserLoginRequest();
//        //进行验证登录
//        userLoginRequest.setPassword(username);
//        userLoginRequest.setUserName(userPwd);
//        UserLoginResponse userLoginResponse = userManageService.userlogin(userLoginRequest);
//        if (SysRetCodeConstants.SUCCESS.getCode().equals(userLoginResponse.getCode())) {
//            //返回token
//            String token = userLoginResponse.getToken();
////            Cookie cookie1 = CookieUtil.genCookie(ACCESS_TOKEN, token, "/", 6000000);
//            Cookie cookie = new Cookie(ACCESS_TOKEN, token);
//            cookie.setPath("/");
//            servletResponse.addCookie(cookie);
//            return new ResponseUtil().setData(null);
//        }
//        return new ResponseUtil().setErrorMsg(Integer.parseInt(userLoginResponse.getCode()), userLoginResponse.getMsg());
//    }
//
//    //验证token接口
//    @GetMapping("login")
//    public ResponseData verifyToken(HttpServletRequest request) {
//        String token = CookieUtil.getCookieValue(request, "access_token");
//        ResponseData data = userManageService.verifyLogin(token);
//        return data;
//    }
//
//
//    //用户注册
//    @RequestMapping("register")
//    @Anonymous
//    public ResponseData register(@RequestBody UserRegisterRequest request,HttpServletRequest httpRequest) {
//        String uuid = CookieUtil.getCookieValue(httpRequest, "kaptcha_uuid");
//        ResponseData data = userManageService.register(
//                request.getUserName(),request.getUserPwd()
//                ,request.getCaptcha(),request.getEmail(),uuid);
//        return data;
//    }
//
//    //退出登录，不携带token时会被拦截
//    @RequestMapping("loginOut")
//    public ResponseData loginOut() {
//        ResponseData data = userManageService.loginOut();
//        return data;
//    }
//
////    //发送验证码图片
////    @GetMapping("kaptcha")
////    public ResponseData captcha(String timestamp) {
////        ResponseData data = userManageService.captcha(timestamp);
////        return data;
////    }
//
//
//
//    //验证用户的注册
//    @RequestMapping("/verify")
//    public ResponseData userVerify(String uuid, String username) {
//        UserVerifyRequest request = new UserVerifyRequest();
//        request.setUuid(uuid);
//        request.setUserName(username);
//        UserVerifyResponse response = iMemberService.verifyMember(request);
//
//        if (response.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
//            return new ResponseUtil().setData(response.getMsg());
//        }
//        return new ResponseUtil().setErrorMsg(response.getMsg());
//    }
//}
