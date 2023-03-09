package com.mall.user.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

@Data
public class UserLoginResponse extends AbstractResponse {
    UserLoginDto userLoginDto;
    String token;
}
