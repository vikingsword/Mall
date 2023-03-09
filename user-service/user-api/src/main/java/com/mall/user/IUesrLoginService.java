package com.mall.user;

import com.mall.user.dto.*;

public interface IUesrLoginService {
    UserLoginResponse userlogin(UserLoginRequest userLoginRequest);

    CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest);

    CheckAuthResponse2 getUserInfo(CheckAuthRequest2 checkAuthRequest2);

    UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest);
}