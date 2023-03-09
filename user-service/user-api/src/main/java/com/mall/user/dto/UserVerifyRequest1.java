package com.mall.user.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * create-date: 2019/7/23-12:48
 */
@Data
public class UserVerifyRequest1 extends AbstractRequest {

    private String userName;
    /**
     * 用户id
     */
    private String uid;

    @Override
    public void requestCheck() {
        if(StringUtils.isBlank(userName)||StringUtils.isBlank(uid)){
            throw new ValidateException(SysRetCodeConstants.REQUEST_CHECK_FAILURE.getCode(),SysRetCodeConstants.REQUEST_CHECK_FAILURE.getMessage());
        }
    }
}