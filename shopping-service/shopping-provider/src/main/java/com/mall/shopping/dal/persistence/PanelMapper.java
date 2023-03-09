package com.mall.shopping.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.shopping.dal.entitys.Panel;

import java.util.List;

import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dto.PanelContentItemDto;
import com.mall.shopping.dto.PanelDto;
import com.mall.shopping.dto.PanelDto2;
import org.apache.ibatis.annotations.Param;

public interface PanelMapper extends TkMapper<Panel> {

    List<Panel> selectPanelContentById(@Param("panelId")Integer panelId);

    List<Panel> selectAllPanel();

    PanelDto2 selectPanelDtoById(int recommendPanelId);

}