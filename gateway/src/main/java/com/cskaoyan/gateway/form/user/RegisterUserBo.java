package com.cskaoyan.gateway.form.user;

import lombok.Data;

@Data
public class RegisterUserBo {
    String captcha;
    String email;
    String userName;
    String userPwd;
}