package com.mall.commons.tool.tkmapper;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.Date;

/**
 * @author cskaoyan
 * @email:
 * @Date: 2019-08-12 17:40
 */
public interface TkMapper<T>  extends Mapper<T>, MySqlMapper<T> {

}
