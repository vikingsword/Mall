package com.mall.user.dto;

import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.constants.SysRetCodeConstants;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 *  cskaoyan
 * create-date: 2019/7/31-19:23
 */
@Data
public class UpdateAddressRequest extends AbstractRequest {

    private Long addressId;

    private Long userId;

    private String userName;

    private String tel;

    private String streetName;

    private Integer isDefault;

    @Override
    public void requestCheck() {
        if(addressId==null|| StringUtils.isBlank(streetName)||StringUtils.isBlank(tel)||StringUtils.isBlank(userName)){
            throw new ValidateException(
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getCode(),
                    SysRetCodeConstants.REQUEST_CHECK_FAILURE.getMessage());
        }
    }
}
