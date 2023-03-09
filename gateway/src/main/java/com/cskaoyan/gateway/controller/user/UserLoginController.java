package com.cskaoyan.gateway.controller.user;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.cskaoyan.gateway.form.user.LoginForm;
import com.cskaoyan.gateway.form.user.RegisterUserBo;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.commons.tool.utils.UserInfoUtils;
import com.mall.user.IKaptchaService;
import com.mall.user.IUesrLoginService;
import com.mall.user.RegisterService;
import com.mall.user.annotation.Anonymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.mall.user.intercepter.TokenIntercepter.ACCESS_TOKEN;
import static com.mall.user.intercepter.TokenIntercepter.USER_INFO_KEY;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserLoginController {
    @Reference(check = false, retries = 0, timeout = 3000)
    IUesrLoginService iUesrLoginService;

    @Reference(check = false, retries = 0, timeout = 3000)
    IKaptchaService iKaptchaService;
    @Reference(timeout = 3000, retries = 0, check = false)
    RegisterService registerService;


    @Anonymous       //无需登录验证
    @PostMapping("/login")
    public ResponseData login(@RequestBody LoginForm loginForm, HttpServletResponse servletResponse, HttpServletRequest servletRequest) {
        ResponseData responseData = new ResponseData();

        //首先进行验证码验证
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        String uuid = CookieUtil.getCookieValue(servletRequest, "kaptcha_uuid");
        kaptchaCodeRequest.setUuid(uuid);
        kaptchaCodeRequest.setCode(loginForm.getCaptcha());
        KaptchaCodeResponse kaptchaCodeResponse = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        if (!(SysRetCodeConstants.SUCCESS.getCode().equals(kaptchaCodeResponse.getCode()))) {
            return new ResponseUtil().setErrorMsg(Integer.parseInt(kaptchaCodeResponse.getCode()), kaptchaCodeResponse.getMsg());
        }


//        if (loginForm.getUserName() == null && loginForm.getUserPwd() == null &&
//                loginForm.getCaptcha() == null) {
//            responseData.setCode(500);
//            responseData.setMessage("token已失效");
//            responseData.setSuccess(false);
//            return responseData;
//        }
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        //进行验证登录
        userLoginRequest.setPassword(loginForm.getUserPwd());
        userLoginRequest.setUserName(loginForm.getUserName());
        userLoginRequest.setCaptcha(loginForm.getCaptcha());//验证码校验(已经验证了，冗余了）
        UserLoginResponse userLoginResponse = iUesrLoginService.userlogin(userLoginRequest);
        if (SysRetCodeConstants.SUCCESS.getCode().equals(userLoginResponse.getCode())) {
            //返回token
            String token = userLoginResponse.getToken();
//            Cookie cookie1 = CookieUtil.genCookie(ACCESS_TOKEN, token, "/", 6000000);
            Cookie cookie = new Cookie(ACCESS_TOKEN, token);
            cookie.setPath("/");
            servletResponse.addCookie(cookie);
            return new ResponseUtil().setData(userLoginResponse.getUserLoginDto());
        }
        return new ResponseUtil().setErrorMsg(Integer.parseInt(userLoginResponse.getCode()), userLoginResponse.getMsg());
    }

       // @Anonymous  //get登录验证
    @GetMapping("/login")
    public ResponseData login(HttpServletRequest servletRequest) {
        String attribute = (String) servletRequest.getAttribute(USER_INFO_KEY);
        //从token中获取用户信息，（揭秘操作）
        CheckAuthRequest2 checkAuthRequest2 = new CheckAuthRequest2();
        checkAuthRequest2.setUserInfoParseReslt(attribute);
        CheckAuthResponse2 checkAuthResponse2 = (CheckAuthResponse2) iUesrLoginService.getUserInfo(checkAuthRequest2);
//        Map uesrInfo = UserInfoUtils.getUesrInfo(servletRequest);
        //checkAuthResponse2.setCode(SysRetCodeConstants.SUCCESS.getCode());

        //验证成功
        if (checkAuthResponse2.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(checkAuthResponse2.getCheckAuthDto2());
        }
        //验证失败

        return new ResponseUtil().setErrorMsg(Integer.parseInt(checkAuthResponse2.getCode()), checkAuthResponse2.getMsg());

    }

       // @Anonymous
   //@PostMapping("/register")
    public ResponseData register(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String username = map.get("userName");
        String userPwd = map.get("userPwd");
        String captcha = map.get("captcha");
        String email = map.get("email");

        //验证验证码
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();

        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
        kaptchaCodeRequest.setUuid(uuid);
        kaptchaCodeRequest.setCode(captcha);
        KaptchaCodeResponse kaptchaCodeResponse = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        if (!(SysRetCodeConstants.SUCCESS.getCode().equals(kaptchaCodeResponse.getCode()))) {
            return new ResponseUtil().setErrorMsg(Integer.parseInt(kaptchaCodeResponse.getCode()), kaptchaCodeResponse.getMsg());
        }

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
        userRegisterRequest.setEmail(email);
        userRegisterRequest.setUserName(username);
        userRegisterRequest.setUserPwd(userPwd);
        UserRegisterResponse userRegisterResponse = iUesrLoginService.registerUser(userRegisterRequest);
        if (userRegisterResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil().setData(null);
        }
        return new ResponseUtil().setErrorMsg(userRegisterResponse.getMsg());

    }

    //    @Anonymous  //退出需要登录验证
    @GetMapping("/loginOut")
    public ResponseData loginOut(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        servletResponse.addCookie(cookie);
        log.info("退出:{}", "chenggong");

        return new ResponseUtil().setData(null);

    }

    @Anonymous
    @PostMapping("/register")
    public ResponseData register(@RequestBody RegisterUserBo registerUserBo, HttpServletRequest request) {

        //验证
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        kaptchaCodeRequest.setCode(registerUserBo.getCaptcha());
        kaptchaCodeRequest.setUuid(CookieUtil.getCookieValue(request, "kaptcha_uuid"));
        KaptchaCodeResponse kaptchaCodeResponse = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        if (!kaptchaCodeResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(Integer.parseInt(kaptchaCodeResponse.getCode()), "验证码错误");
        }
        System.out.println("a");
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();

        userRegisterRequest.setEmail(registerUserBo.getEmail());
        userRegisterRequest.setUserName(registerUserBo.getUserName());
        userRegisterRequest.setUserPwd(registerUserBo.getUserPwd());

        UserRegisterResponse response = registerService.register(userRegisterRequest);
        if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode()))
        {//成功

            registerService.verify(userRegisterRequest, kaptchaCodeRequest.getUuid());

            return new ResponseUtil().setData(response.getResult());
        }
        /*else if (register == 2) {
            return new ResponseUtil<>().setErrorMsg("用户名重复");
        } else if (register == 3) {
            return new ResponseUtil<>().setErrorMsg("用户名过长");
        } else if (register == 4) {
            return new ResponseUtil<>().setErrorMsg("邮箱已注册");
        } else if (register == 5) {
            return new ResponseUtil<>().setErrorMsg("邮箱格式错误");
        } else if (register == 6) {
            return new ResponseUtil<>().setErrorMsg("密码至少为4位");
        } else if (register == 7) {
            return new ResponseUtil<>().setErrorMsg("密码过长");
        }*/
        return new ResponseUtil().setErrorMsg(response.getMsg());
    }


    @Anonymous
    @GetMapping("/verify")
    public ResponseData verify(String uid, String username) {
        UserVerifyRequest1 userVerifyRequest1 = new UserVerifyRequest1();
        userVerifyRequest1.setUid(uid);
        userVerifyRequest1.setUserName(username);
        UserRegisterResponse userRegisterResponse = registerService.verifyMember(userVerifyRequest1);
        if (userRegisterResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //成功后需要再次发送邮件
            return new ResponseUtil().setData(null);
        }
        return new ResponseUtil().setErrorMsg(userRegisterResponse.getMsg());
    }


}

