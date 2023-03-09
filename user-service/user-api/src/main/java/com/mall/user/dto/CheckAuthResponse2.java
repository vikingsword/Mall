package com.mall.user.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

@Data
public class CheckAuthResponse2 extends AbstractResponse {
    private CheckAuthDto2 checkAuthDto2;
}