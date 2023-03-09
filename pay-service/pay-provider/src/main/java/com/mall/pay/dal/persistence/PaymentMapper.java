package com.mall.pay.dal.persistence;


import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.pay.dal.entitys.Payment;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface PaymentMapper extends TkMapper<Payment> {

    void updateStatus(@Param("orderId") String orderId, @Param("date") Date date);
}