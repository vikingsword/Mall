package com.mall.shopping.converter;

import com.mall.shopping.dal.entitys.*;
import com.mall.shopping.dto.PanelContentDto;
import com.mall.shopping.dto.PanelContentItemDto;
import com.mall.shopping.dto.PanelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PanelConverter {

    @Mappings({})
    PanelDto panelToPanelDto(Panel panel);

    List<PanelDto> panelListToPanelDtoList(List<Panel> panelList);


    @Mappings({})
    PanelContentItemDto panlContentItem2PanelContentItemDto(PanelContentItem panelContentItem);

    List<PanelContentItemDto> panelContentItemList2PanelContentItemDtoList(List<PanelContentItem> panelContentItemList);


    @Mappings({
            @Mapping(source = "status",target = "type"),
            @Mapping(source = "icon",target = "fullUrl"),
            @Mapping(source = "name",target = "picUrl")
    })
    PanelContentDto ItemCat2PanelContentDto(ItemCat itemCat);

    List<PanelContentDto> ItemCatList2PanelContentDtoList(List<ItemCat> itemCatList);

}
