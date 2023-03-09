package com.mall.shopping.converter;

import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dto.TestProductDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TestProductConverter {

    @Mappings({
            @Mapping(source = "title", target = "productName"),
            @Mapping(source = "image", target = "img")
    })
    TestProductDetailDto itemToItemDto(Item item);
}
