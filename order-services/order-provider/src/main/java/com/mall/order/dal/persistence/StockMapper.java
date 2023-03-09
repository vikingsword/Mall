package com.mall.order.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.order.dal.entitys.Stock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Date: 2019-09-16 00:09
 **/
public interface StockMapper extends TkMapper<Stock> {
 void updateStock(Stock stock);

 Stock selectStockForUpdate(Long itemId);

 Stock selectStock(Long itemId);

 List<Stock> findStocksForUpdate(@Param("itemIds") List<Long> itemIds);

 void updateStock2(@Param("num") Integer num, @Param("itemId") Long itemId);
}