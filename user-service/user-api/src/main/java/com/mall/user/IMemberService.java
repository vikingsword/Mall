package com.mall.user;
import com.mall.user.dto.*;

/**
 *  cskaoyan
 * create-date: 2019/7/30-下午11:47
 * 会员服务
 */
public interface IMemberService {

    /**
     * 根据用户id查询用户会员信息
     * @param request
     * @return
     */
    QueryMemberResponse queryMemberById(QueryMemberRequest request);

    /**
     * 修改用户头像
     * @param request
     * @return
     */
    HeadImageResponse updateHeadImage(HeadImageRequest request);

    /**
     * 更新信息
     * @param request
     * @return
     */
    UpdateMemberResponse updateMember(UpdateMemberRequest request);

    /**
     * 验证用户是否激活邮箱
     * @param request
     * @return
     */
    UserVerifyResponse verifyMember(UserVerifyRequest request);
}
