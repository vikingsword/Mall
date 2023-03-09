package com.mall.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckAuthDto2 implements Serializable {
    private Integer uid;

    private String username;
}