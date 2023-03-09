package com.mall.user.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

@Data
public class UserLoginOutResponse extends AbstractResponse {
    private Boolean success;
    private String result;
    private Long timestamp;

}

