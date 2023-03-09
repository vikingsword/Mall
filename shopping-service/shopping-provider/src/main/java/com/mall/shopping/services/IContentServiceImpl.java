package com.mall.shopping.services;

import com.mall.shopping.IContentService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.converter.PanelConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dto.NavListResponse;
import com.mall.shopping.dto.PanelContentDto;
import org.apache.dubbo.config.annotation.Service;
import org.checkerframework.checker.units.qual.Area;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class IContentServiceImpl implements IContentService {

    @Autowired
    ItemCatMapper itemCatMapper;

    @Autowired
    PanelContentMapper panelContentMapper;

    @Autowired
    PanelConverter panelConverter;

    @Autowired
    ContentConverter contentConverter;

    @Override
    public NavListResponse queryNavList() {
        NavListResponse navListResponse = new NavListResponse();
        try {
            //Example example = new Example(ItemCat.class);
            //example.createCriteria().andEqualTo("isParent",true);
            //List<ItemCat> itemCats = itemCatMapper.selectByExample(example);

            //List<PanelContentDto> panelContentDtos = panelConverter.ItemCatList2PanelContentDtoList(itemCats);
            //for (PanelContentDto panelContentDto : panelContentDtos) {
            //    panelContentDto.setPanelId(0);
            //}
            Example example = new Example(PanelContent.class);
            example.createCriteria().andEqualTo("panelId", 0);
            List<PanelContent> panelContents = panelContentMapper.selectByExample(example);
            List<PanelContentDto> panelContentDtos = contentConverter.panelContents2Dto(panelContents);

            navListResponse.setPannelContentDtos(panelContentDtos);
            navListResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            navListResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            navListResponse.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            navListResponse.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }

        return navListResponse;
    }
}
