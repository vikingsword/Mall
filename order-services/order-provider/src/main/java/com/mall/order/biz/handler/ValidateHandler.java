package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.commons.tool.exception.ProcessException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.user.IMemberService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.QueryMemberRequest;
import com.mall.user.dto.QueryMemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * create-date: 2019/8/1-下午4:47
 *
 */
@Slf4j
@Component
public class ValidateHandler extends AbstractTransHandler {

    @Reference(check = false)
    private IMemberService memberService;


    /**
     * 验证用户合法性
     * @return
     */

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        CreateOrderContext createOrderContext = (CreateOrderContext) context;
        Long userId = createOrderContext.getUserId();
        QueryMemberRequest request = new QueryMemberRequest();
        request.setUserId(userId);
        QueryMemberResponse queryMemberResponse = memberService.queryMemberById(request);//根据用户Id获得用户会员信息
        if(queryMemberResponse == null){
            throw new ProcessException(SysRetCodeConstants.DATA_NOT_EXIST.getMessage());
        }





        return true;
    }
}
