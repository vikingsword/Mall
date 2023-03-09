package com.cskaoyan.gateway.form.user;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: thz
 * @class: LoginForm
 */
@Data
public class LoginForm  extends AbstractRequest {
    private String userName;
    private String userPwd;
    private String captcha;
    @Override
    public void requestCheck() {
        if(StringUtils.isBlank(userName)||StringUtils.isBlank(userPwd)){
            throw new ValidateException(
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getCode(),
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getMessage());
        }
    }
}