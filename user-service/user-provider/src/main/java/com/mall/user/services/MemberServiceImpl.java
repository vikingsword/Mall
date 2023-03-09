package com.mall.user.services;/**
 * Created by cskaoyan on 2019/7/30.
 */

import com.mall.user.IMemberService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.MemberConverter;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.*;
import com.mall.user.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * cskaoyan
 * create-date: 2019/7/30-下午11:51
 */
@Slf4j
@Component
@Service
public class MemberServiceImpl implements IMemberService {

    @Autowired
    MemberMapper memberMapper;

//    @Autowired
//    IUserLoginService userLoginService;

    @Autowired
    MemberConverter memberConverter;

    @Autowired
    UserVerifyMapper userVerifyMapper;

    /**
     * 根据用户id查询用户会员信息
     *
     * @param request
     * @return
     */
    @Override
    public QueryMemberResponse queryMemberById(QueryMemberRequest request) {
        QueryMemberResponse queryMemberResponse = new QueryMemberResponse();
        try {
            request.requestCheck();
            Member member = memberMapper.selectByPrimaryKey(request.getUserId());
            if (member == null) {
                queryMemberResponse.setCode(SysRetCodeConstants.DATA_NOT_EXIST.getCode());
                queryMemberResponse.setMsg(SysRetCodeConstants.DATA_NOT_EXIST.getMessage());
            }
            queryMemberResponse = memberConverter.member2Res(member);
            queryMemberResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            queryMemberResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("MemberServiceImpl.queryMemberById Occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(queryMemberResponse, e);
        }
        return queryMemberResponse;
    }

    @Override
    public HeadImageResponse updateHeadImage(HeadImageRequest request) {
        HeadImageResponse response = new HeadImageResponse();
        //TODO
        return response;
    }

    @Override
    public UpdateMemberResponse updateMember(UpdateMemberRequest request) {
        return null;
    }

    //验证用户的注册
    @Override
    public UserVerifyResponse verifyMember(UserVerifyRequest request) {
        log.error("begin - IMemberService.verifyMember request :" + request);
        UserVerifyResponse response = new UserVerifyResponse();
        try {
            request.requestCheck();

            Example example = new Example(UserVerify.class);
            Example.Criteria criteria = example.createCriteria();
            criteria
                    .andEqualTo("uuid", request.getUuid())
                    .andEqualTo("username", request.getUserName());
            UserVerify userVerify = new UserVerify();
            userVerify.setIsVerify("Y");
            int row = userVerifyMapper.updateByExampleSelective(userVerify, example);

            if (row > 0) {
                //再更新member表中的字段
                Example example1 = new Example(Member.class);
                Example.Criteria criteria1 = example1.createCriteria();
                criteria1.andEqualTo("username", request.getUserName());
                Member member = new Member();
                member.setIsVerified("Y");
                int row1 = memberMapper.updateByExampleSelective(member, example1);
                if (row1 > 0) {
                    response.setCode(SysRetCodeConstants.SUCCESS.getCode());
                    response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
                }

            } else {
                response.setCode(SysRetCodeConstants.DATA_NOT_EXIST.getCode());
                response.setMsg(SysRetCodeConstants.DATA_NOT_EXIST.getMessage());
            }
            log.info("IMemberService.verifyMember :" + row);
        } catch (Exception e) {
            log.error("IMemberService.verifyMember occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }
        return response;
    }

//    @Override
//    public UpdateMemberResponse updateMember(UpdateMemberRequest request) {
//        UpdateMemberResponse response = new UpdateMemberResponse();
//        try{
//            request.requestCheck();
//            CheckAuthRequest checkAuthRequest = new CheckAuthRequest();
//            checkAuthRequest.setToken(request.getToken());
//            CheckAuthResponse authResponse = userLoginService.validToken(checkAuthRequest);
//            if (!authResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
//                response.setCode(authResponse.getCode());
//                response.setMsg(authResponse.getMsg());
//                return response;
//            }
//            Member member = memberConverter.updateReq2Member(request);
//            int row = memberMapper.updateByPrimaryKeySelective(member);
//            response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
//            response.setCode(SysRetCodeConstants.SUCCESS.getCode());
//            log.info("MemberServiceImpl.updateMember effect row :"+row);
//        }catch (Exception e){
//            log.error("MemberServiceImpl.updateMember Occur Exception :"+e);
//            ExceptionProcessorUtils.wrapperHandlerException(response,e);
//        }
//        return response;
//    }
}
