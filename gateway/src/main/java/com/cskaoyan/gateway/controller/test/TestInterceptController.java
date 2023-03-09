package com.cskaoyan.gateway.controller.test;

import com.mall.user.annotation.Anonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestInterceptController {

    @GetMapping("/intercept")
    @Anonymous
    public String testIntercept() {
        return "hello, token interceptor";
    }
}
