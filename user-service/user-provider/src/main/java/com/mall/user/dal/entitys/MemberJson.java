package com.mall.user.dal.entitys;

import lombok.Data;

/**
 * 用于token封装数据
 */
@Data
public class MemberJson {
    private Long uid;
    private String username;
}
