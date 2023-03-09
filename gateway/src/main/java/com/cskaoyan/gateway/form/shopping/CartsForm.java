package com.cskaoyan.gateway.form.shopping;

import lombok.Data;

/**
 * @Author: JianHui
 * @Date: 2021/8/26
 * @Time: 16:57
 * @Description:
 */

@Data
public class CartsForm {
    private String checked;
    private Long productId;
    private Integer productNum;
    private Long userId;
}
