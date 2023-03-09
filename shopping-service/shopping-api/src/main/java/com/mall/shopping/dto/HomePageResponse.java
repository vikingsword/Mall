package com.mall.shopping.dto;

import com.mall.commons.result.AbstractResponse;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 *  cskaoyan
 * create-date: 2019/7/23-17:48
 */
@Data
public class HomePageResponse extends AbstractResponse {

    private List<PanelDto> panelContentItemDtos;
}
