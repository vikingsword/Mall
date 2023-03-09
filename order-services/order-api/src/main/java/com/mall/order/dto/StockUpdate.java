package com.mall.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

/**
 * @Author Yang
 * @Date 2021/8/26 11:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdate {
    private Long itemId;

    private Integer num;
}
