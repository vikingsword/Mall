package com.mall.user.intercepter;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IUesrLoginService;
import com.mall.user.annotation.Anonymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 用来实现token拦截认证
 * <p>
 * 其实就是用来判断当前这个操作是否需要登录
 */
public class TokenIntercepter extends HandlerInterceptorAdapter {


    @Reference(timeout = 3000,check = false)
    IUesrLoginService iUserLoginService;


    public static String ACCESS_TOKEN = "access_token";

    public static String USER_INFO_KEY = "userInfo";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. handler 对象，指的是处理该请求的Controller类中对应action方法
        // 2. 如果该请求是一个动态资源的请求，该action对应的对象的类型，就是HandlerMethod
        if (!(handler instanceof HandlerMethod)) {
            // 不是动态资源就放行
            return true;
        }


        // 如果代码运行到这里，意味者本次请求，一定是一个由某个Controller中的action处理的动态请求
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (isAnoymous(handlerMethod)) {
            // 有 @Anonymous注解就放行
            return true;
        }

        // 代码运行到这里，就意味着，该请求是需要先登录在处理的请求
        // getCookieValue(request r,string s) --> 获取request的cookie中key为s对应的value值
        String token = CookieUtil.getCookieValue(request, ACCESS_TOKEN);
        if (StringUtils.isEmpty(token)) {
            ResponseData responseData = new ResponseUtil().setErrorMsg("token已失效");
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(JSON.toJSON(responseData).toString());
            return false;
        }

        // 能运行到这里说明token一定不为空,所以可以设置token
        //从token中获取用户信息
        CheckAuthRequest checkAuthRequest = new CheckAuthRequest();
        checkAuthRequest.setToken(token);
        // 将token放到checkAuthResponse
        CheckAuthResponse checkAuthResponse=iUserLoginService.validToken(checkAuthRequest);
        if(checkAuthResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())){
            // 将token放到request中
            request.setAttribute(USER_INFO_KEY,checkAuthResponse.getUserinfo()); //保存token解析后的信息后续要用
            return super.preHandle(request, response, handler);
        }
        // 如果上面的checkAuthResponse.getCode()不成功，则返回失败的结果信息
        ResponseData responseData=new ResponseUtil().setErrorMsg(checkAuthResponse.getMsg());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(JSON.toJSON(responseData).toString());
        return false;
    }

    private boolean isAnoymous(HandlerMethod handlerMethod) {

        /**
         * 先看类上有没有@Anonymous注解，
         * 再看方法上有没有这个注解，
         * 如果有一个有，那么就返回true
         */
        // 包含action方法的那个Controller对象
        Object bean = handlerMethod.getBean();

        // 获取Controller类对应的Class对象
        Class clazz = bean.getClass();
        // 如果说Controller类上 有Anoymous
        if (clazz.getAnnotation(Anonymous.class) != null) {
            return true;
        }

        // 获取action方法对应的Method对象
        Method method = handlerMethod.getMethod();
        // 如果在方法上获取到了Anoymous注解，返回true，否则返回false
        return method.getAnnotation(Anonymous.class) != null;
    }
}
