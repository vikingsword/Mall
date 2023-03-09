package com.mall.user;

import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import com.mall.user.dto.UserVerifyRequest1;

public interface RegisterService {
    UserRegisterResponse register(UserRegisterRequest userRegisterRequest);

    Boolean verify(UserRegisterRequest userRegisterRequest, String uuid);

    //测试邮箱
    public void verifyMail(String uid, String username,String email);

    UserRegisterResponse verifyMember(UserVerifyRequest1 userVerifyRequest1);

    public void verifyAfterMail(String email);
}