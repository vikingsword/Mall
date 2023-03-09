//package com.mall.user.services;
//
//import com.alibaba.fastjson.JSON;
//import com.google.gson.Gson;
//import com.mall.commons.result.ResponseData;
//import com.mall.commons.result.ResponseUtil;
//import com.mall.user.IKaptchaService;
//import com.mall.user.UserManageService;
//import com.mall.user.constants.SysRetCodeConstants;
//import com.mall.user.converter.UserConverterMapper;
//import com.mall.user.dal.entitys.ImageResult;
//import com.mall.user.dal.entitys.Member;
//import com.mall.user.dal.entitys.MemberJson;
//import com.mall.user.dal.persistence.MemberMapper;
//import com.mall.user.dal.persistence.UserVerifyMapper;
//import com.mall.user.dto.*;
//import com.mall.user.utils.AESUtil;
//import com.mall.user.utils.ExceptionProcessorUtils;
//import com.mall.user.utils.JwtTokenUtils;
//import com.mall.user.utils.VerifyCodeUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.dubbo.config.annotation.Reference;
//import org.apache.dubbo.config.annotation.Service;
//import org.redisson.Redisson;
//import org.redisson.api.RBucket;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.DigestUtils;
//import tk.mybatis.mapper.entity.Example;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//
//@Slf4j
//@Component
//@Service
//public class UserManageServiceImpl implements UserManageService {
//
//    @Autowired
//    MemberMapper memberMapper;
//
//    @Reference
//    IKaptchaService kaptchaService;
//
//    @Autowired
//    RedissonClient redissonClient;
//
//
//    @Autowired
//    UserConverterMapper converterMapper;
//
//    @Autowired
//    UserVerifyMapper userVerifyMapper;
//
//    /**
//     * 验证token
//     * @param token
//     * @return
//     */
//    @Override
//    public ResponseData verifyLogin(String token) {
//        String freeJwt = JwtTokenUtils.builder().token(token).build().freeJwt();
//        //验证token是否有效
//        MemberJson memberJson = new Gson().fromJson(freeJwt, MemberJson.class);
//        Member member = memberMapper.selectByPrimaryKey(memberJson.getUid());
//        if (member == null){
//            return new ResponseUtil<>().setErrorMsg(500,"token错误");
//        }
//        return new ResponseUtil<>().setData(new Gson().toJson(freeJwt),"success");
//    }
//
//
//    /**
//     * 注册新账号
//     * @param userName
//     * @param userPwd
//     * @param captcha
//     * @param email
//     * @return
//     */
//    @Override
//    public ResponseData register(String userName, String userPwd, String captcha, String email,String uuid) {
//        ResponseData results = verifyCaptcha(captcha,uuid);
//        if (!results.isSuccess()){
//            return results;
//        }
//
//        Example example = new Example(Member.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("username",userName);
//        Member member = memberMapper.selectOneByExample(example);
//        //数据库有这个用户名，不可以新建
//        if (member != null) {
//            return new ResponseUtil<>().setErrorMsg("用户名已被使用");
//        }
//
//
//        Example example1 = new Example(Member.class);
//        Example.Criteria criteria1 = example1.createCriteria();
//        criteria1.andEqualTo("email",email);
//        member = memberMapper.selectOneByExample(example);
//        //数据库有这个邮箱，不可以新建
//        if (member != null){
//            return new ResponseUtil<>().setErrorMsg("邮箱已被使用");
//        }
//        //加密存储密码
//        AESUtil aesUtil = new AESUtil(userPwd);
//        String password = aesUtil.encrypt();
//
//        member = new Member();
//        member.setUsername(userName);
//        member.setEmail(email);
//        member.setPassword(password);
//        member.setCreated(new Date());
//        member.setUpdated(new Date());
//        memberMapper.insertUseGeneratedKeys(member);
//        return new ResponseUtil<>().setData(null,"success");
//    }
//
//
//    /**
//     * 退出账号
//     * @return
//     */
//    @Override
//    public ResponseData loginOut() {
//        return new ResponseUtil<>().setData(null,"success");
//    }
//
//    /**
//     * 登录，返回token
//     */
//    @Override
//    public UserLoginResponse userlogin(UserLoginRequest userLoginRequest) {
//        UserLoginResponse userLoginResponse = new UserLoginResponse();
//        Example example = new Example(Member.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("username", userLoginRequest.getUserName());
//        String s = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());
//        criteria.andEqualTo("password", s);
//        try {
//            userLoginRequest.requestCheck();//验证，出错直接抛出异常，varlidateEx
//            //先检验验证码
////            if(userLoginRequest.getCaptcha()){}
//            List<Member> members = memberMapper.selectByExample(example);
//
//            if (members.size() == 0) {
//                // 用户名或密码不正确
//                userLoginResponse.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
//                userLoginResponse.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
//            } else if (members.get(0).getIsVerified().equals("N")) {
//                userLoginResponse.setCode(SysRetCodeConstants.USER_ISVERFIED_ERROR.getCode());
//                userLoginResponse.setMsg(SysRetCodeConstants.USER_ISVERFIED_ERROR.getMessage());
//            } else {
//                Member member = members.get(0);
//                userLoginResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
//                userLoginResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
//            }
//
//        } catch (Exception e) {
//            ExceptionProcessorUtils.wrapperHandlerException(userLoginResponse, e);
//        }
//
//
//        return userLoginResponse;
//
//    }
//
//
//    /**
//     *根据昵称密码查找用户
//     */
//    public Member verifyMember(String userName,String userPwd){
//        //加密密码后对比
//        AESUtil aesUtil = new AESUtil(userPwd);
//        String password = aesUtil.encrypt();
//
//        Example example = new Example(Member.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("username",userName);
//        criteria.andEqualTo("password",password);
//        Member member = memberMapper.selectOneByExample(example);
//        return member;
//    }
//
//    /**
//     *验证验证码
//     */
//    public ResponseData verifyCaptcha(String captcha,String uuid) {
//
//        KaptchaCodeRequest codeRequest = new KaptchaCodeRequest();
//        codeRequest.setCode(captcha);
//        codeRequest.setUuid(uuid);
//        KaptchaCodeResponse response = kaptchaService.validateKaptchaCode(codeRequest);
//
//        if(response.getMsg().equals("成功")){
//            return new ResponseUtil<>().setData("success");
//        }else {
//            return new ResponseUtil<>().setErrorMsg("验证码错误");
//        }
//    }
//
////
////    /**
////     * 发送验证码
////     * @param timestamp
////     * @return
////     */
////    @Override
////    public ResponseData captcha(String timestamp) {
////        KaptchaCodeResponse response=new KaptchaCodeResponse();
////        try {
////            ImageResult capText = VerifyCodeUtils.VerifyCode(140, 43, 4);
////            //随机生成的uuid
////            String uuid= UUID.randomUUID().toString();
////
////            RBucket rBucket=redissonClient.getBucket(uuid);
////            rBucket.set(capText.getCode());//存入验证码
////            log.info("产生的验证码:{},uuid:{}",capText.getCode(),uuid);
////            rBucket.expire(120, TimeUnit.SECONDS);
////
////            return new ResponseUtil<>().setData(capText.getImg(),"success");
////        } catch (IOException e) {
////           log.error("error:", e.getMessage());
////        }
////        return new ResponseUtil<>().setErrorMsg("图片跑丢了");
////    }
//}
