package com.mall.user.dto;

import com.mall.commons.result.AbstractRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CheckAuthRequest2 extends AbstractRequest {
    private String userInfoParseReslt;

    @Override
    public void requestCheck() {
        return;
    }
}
