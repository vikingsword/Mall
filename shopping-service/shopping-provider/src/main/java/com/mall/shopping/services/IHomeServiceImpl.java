package com.mall.shopping.services;

import com.mall.shopping.IHomeService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.PanelConverter;
import com.mall.shopping.dal.entitys.Panel;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.HomePageResponse;
import com.mall.shopping.dto.PanelContentItemDto;
import com.mall.shopping.dto.PanelDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Slf4j
public class IHomeServiceImpl implements IHomeService {

    @Autowired
    PanelMapper panelMapper;

    @Autowired
    PanelConverter panelConverter;

    @Autowired
    PanelContentMapper panelContentMapper;




    @Override
    public HomePageResponse homepage() {
        /**
         * 查询panel表中的数据，转化为dto对象，
         * 再通过查询到的记录的pid去查找panel_content表,找到对应的product_id
         * 再通过查询到的product_id 去找tb_item 表查找响应信息进行封装
         */
        HomePageResponse response = new HomePageResponse();

        List<PanelDto> panelDtos = null;
        try {
            List<Panel> panels = panelMapper.selectAll();
            //List<Panel> panels = panelMapper.selectAllPanel();
            panelDtos = panelConverter.panelListToPanelDtoList(panels);
            for (PanelDto panelDto : panelDtos) {
                // TODO 先试一下根据给定的方法是否可以成功进行查询
                // 根据pid查找panel_content表

                // 构造查询的条件,进行panel_Content表的查询
                //Example example = new Example(PanelContent.class);
                //example.createCriteria().andEqualTo("panelId", panelId);
                //List<PanelContent> panelContents = panelContentMapper.selectByExample(example);

                // 将panelContentItems的list 转化为 panelPanelItemDto的list

                Integer panelId = panelDto.getId();
                List<PanelContentItem> panelContentItems1 = panelContentMapper.selectPanelContentAndProductWithPanelId(panelId);
                List<PanelContentItemDto> panelContentItemDtos = panelConverter.panelContentItemList2PanelContentItemDtoList(panelContentItems1);
                panelDto.setPanelContentItems(panelContentItemDtos);

                // 执行成功
                response.setPanelContentItemDtos(panelDtos);
                response.setCode(ShoppingRetCode.SUCCESS.getCode());
                response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            }
        } catch (Exception e) {
            // 查询失败返回数据库查询错误
            e.printStackTrace();
            response.setCode(ShoppingRetCode.DB_EXCEPTION.getCode());
            response.setMsg(ShoppingRetCode.DB_EXCEPTION.getMessage());
        }


        return response;
    }
}
