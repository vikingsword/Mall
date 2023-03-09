package com.mall.user.converter;

import com.mall.user.dal.entitys.Member;
import com.mall.user.dto.UserLoginDto;
import com.mall.user.dto.UserLoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 *  cskaoyan
 * create-date: 2019/7/22-18:05
 */
@Mapper(componentModel = "spring")
public interface UserConverterMapper {

//    UserConverterMapper INSTANCE= Mappers.getMapper(UserConverterMapper.class);

    @Mappings({})
    UserLoginDto converter(Member member);

}
