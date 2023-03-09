package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.commons.tool.utils.UserInfoUtils;
import com.mall.user.IUesrLoginService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.UserConverterMapper;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.*;
import com.mall.user.utils.ExceptionProcessorUtils;
import com.mall.user.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
@Slf4j
public class UserLoginServiceImpl implements IUesrLoginService {
    @Autowired
    MemberMapper memberMapper;
    @Autowired
    UserConverterMapper converterMapper;
    @Autowired
    UserVerifyMapper userVerifyMapper;

    @Override
    public UserLoginResponse userlogin(UserLoginRequest userLoginRequest) {
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        Example example = new Example(Member.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", userLoginRequest.getUserName());
        String s = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());
        criteria.andEqualTo("password", s);
        try {
            userLoginRequest.requestCheck();//验证，出错直接抛出异常，varlidateEx
            //先检验验证码
//            if(userLoginRequest.getCaptcha()){}
            List<Member> members = memberMapper.selectByExample(example);

            if (members.size() == 0) {
                // 用户名或密码不正确
                userLoginResponse.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
                userLoginResponse.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            } else {
                Member member = members.get(0);
                UserLoginDto userLoginDto = converterMapper.converter(member);
                userLoginDto.setToken(getToken(userLoginDto));
                //返回成功信息和代码
                userLoginResponse.setToken(getToken(userLoginDto));
                userLoginResponse.setUserLoginDto(userLoginDto);
                userLoginResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
                userLoginResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
            }

        } catch (Exception e) {
            ExceptionProcessorUtils.wrapperHandlerException(userLoginResponse, e);
        }


        return userLoginResponse;
    }

    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) {
        CheckAuthResponse checkAuthResponse = new CheckAuthResponse();
        try {
            String token = checkAuthRequest.getToken();
            JwtTokenUtils build = JwtTokenUtils.builder().token(token).build();
            String s = build.freeJwt();
            checkAuthResponse.setUserinfo(s);
            checkAuthResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            checkAuthResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            checkAuthResponse.setCode(SysRetCodeConstants.TOKEN_VALID_FAILED.getCode());
            checkAuthResponse.setMsg(SysRetCodeConstants.TOKEN_VALID_FAILED.getMessage());
        }

        return checkAuthResponse;
    }

    @Override
    public CheckAuthResponse2 getUserInfo(CheckAuthRequest2 checkAuthRequest2) {
        String userInfoParseReslt = checkAuthRequest2.getUserInfoParseReslt();
        CheckAuthResponse2 checkAuthResponse2 = null;

        try {
            Map parse = (Map) JSON.parse(userInfoParseReslt);
            checkAuthResponse2 = new CheckAuthResponse2();
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            String uid = String.valueOf(parse.get("uid"));
            String username = (String) parse.get("username");
            CheckAuthDto2 checkAuthDto2 = new CheckAuthDto2();
            checkAuthDto2.setUid(Integer.parseInt(uid));
            checkAuthDto2.setUsername(username);
            checkAuthResponse2.setCheckAuthDto2(checkAuthDto2);
            checkAuthResponse2.setCode(SysRetCodeConstants.SUCCESS.getCode());
            checkAuthResponse2.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            checkAuthResponse2.setCode(SysRetCodeConstants.TOKEN_VALID_FAILED.getCode());
            checkAuthResponse2.setMsg(SysRetCodeConstants.TOKEN_VALID_FAILED.getMessage());

        }


        return checkAuthResponse2;
    }

    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
        UserRegisterResponse userRegisterResponse = new UserRegisterResponse();
        try {
            //检查用户名是否已注册
            userRegisterRequest.requestCheck();
            Example example = new Example(Member.class);
            example.createCriteria().andEqualTo("username", userRegisterRequest.getUserName());
            List<Member> members = memberMapper.selectByExample(example);
            if (!CollectionUtils.isEmpty(members)) {
                userRegisterResponse.setCode(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getCode());
                userRegisterResponse.setMsg(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getMessage());
                return userRegisterResponse;

            }
            //检验邮箱是否被使用
            Example example1 = new Example(Member.class);
            example1.createCriteria().andEqualTo("email", userRegisterRequest.getEmail());
            List<Member> members1 = memberMapper.selectByExample(example1);
            if (!CollectionUtils.isEmpty(members1)) {
                userRegisterResponse.setCode(SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getCode());
                userRegisterResponse.setMsg(SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getMessage());
                return userRegisterResponse;
            }

            //开始插入member
            Member member = new Member();
            member.setUsername(userRegisterRequest.getUserName());
            String s = DigestUtils.md5DigestAsHex(userRegisterRequest.getUserPwd().getBytes());
            member.setPassword(s);
            member.setEmail(userRegisterRequest.getEmail());
            member.setIsVerified("N");
            member.setCreated(new Date());
            member.setUpdated(new Date());
            int i = memberMapper.insertSelective(member);
            System.out.println(i);
            userRegisterResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            userRegisterResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
            //向用户验证表中插入记录
            UserVerify userVerify = new UserVerify();
            userVerify.setUsername(userRegisterRequest.getUserName());
            userVerify.setRegisterDate(new Date());
            userVerify.setUuid(UUID.randomUUID().toString());
            userVerify.setIsVerify("N");
            userVerify.setIsExpire("N");
            int i1 = userVerifyMapper.insertSelective(userVerify);
            System.out.println(i1);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionProcessorUtils.wrapperHandlerException(userRegisterResponse, e);
        }

        return userRegisterResponse;
    }

    private String getToken(UserLoginDto userLoginDto) {
        String token = null;
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", String.valueOf(userLoginDto.getId()));//TODO
        map.put("username", userLoginDto.getUsername());

        JwtTokenUtils.JwtTokenUtilsBuilder builder = JwtTokenUtils.builder();
        JwtTokenUtils build = builder.msg(JSON.toJSONString(map)).build();
        token = build.creatJwtToken();
        return token;
    }
}