package com.mall.shopping.dal.persistence;

import com.mall.shopping.dal.entitys.Item;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface ItemMapper extends Mapper<Item> {

    List<Item> selectItemFront(@Param("cid") Long cid,
                                 @Param("orderCol") String orderCol,@Param("orderDir") String orderDir,
                                 @Param("priceGt") Integer priceGt,@Param("priceLte") Integer priceLte);
}