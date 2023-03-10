package com.mall.shopping.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.shopping.dal.entitys.PanelContent;

import java.util.List;

import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dto.PanelContentItemDto2;
import org.apache.ibatis.annotations.Param;

public interface PanelContentMapper extends TkMapper<PanelContent> {

    List<PanelContentItem> selectPanelContentAndProductWithPanelId(@Param("panelId") Integer panelId);

    List<PanelContentItemDto2> selectPanelContentItemDto2sWithPanelId(@Param("panelId") int panelId);
}