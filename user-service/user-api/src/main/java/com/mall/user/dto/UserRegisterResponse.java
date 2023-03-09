package com.mall.user.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

/**
 *  cskaoyan
 * create-date: 2019/7/23-12:48
 */

@Data
public class UserRegisterResponse extends AbstractResponse {
    private String success;
    private String result;
    private Long timestamp;
}
