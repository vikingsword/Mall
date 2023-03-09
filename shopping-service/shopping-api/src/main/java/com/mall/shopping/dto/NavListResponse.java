package com.mall.shopping.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.util.List;

/**
 *  cskaoyan
 * create-date: 2019/7/23-16:27
 */
@Data
public class NavListResponse extends AbstractResponse {

    private List<PanelContentDto> pannelContentDtos;
}
