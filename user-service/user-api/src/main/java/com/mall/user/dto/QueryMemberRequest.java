package com.mall.user.dto;/**
 * Created  on 2019/7/30.
 */

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.Data;

/**
 * create-date: 2019/7/30-下午11:49
 */
@Data
public class QueryMemberRequest extends AbstractRequest{

    private Long userId;

    @Override
    public void requestCheck() {
        if(userId==null){
            throw new ValidateException(
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getCode(),
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getMessage());
        }
    }
}
