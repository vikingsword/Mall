package com.mall.user.services;

import com.mall.user.RegisterService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.User;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import com.mall.user.dto.UserVerifyRequest1;
import com.mall.user.utils.ExceptionProcessorUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.mall.commons.tool.utils.UtilDate.getDate;


@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    MemberMapper memberMapper;
    @Autowired
    UserVerifyMapper userVerifyMapper;

    @Override
    public UserRegisterResponse register(UserRegisterRequest userRegisterRequest) {
        UserRegisterResponse response = new UserRegisterResponse();
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        Integer integer = userMapper.selectByUsername(userRegisterRequest.getUserName());
        if (integer >= 1) {
            response.setCode(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getCode());
            response.setMsg(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getMessage());

        }

        //过长？
        if (userRegisterRequest.getUserName().length() > 20) {
            response.setCode(SysRetCodeConstants.USER_INFOR_INVALID.getCode());
            response.setMsg(SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
        }

        //邮箱？
        Integer integer1 = userMapper.selectByEmail(userRegisterRequest.getEmail());
        if (integer1 >= 1) {
            response.setCode(SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getCode());
            response.setMsg(SysRetCodeConstants.EMAIL_ALREADY_EXISTS.getMessage());
        }

        if (!userRegisterRequest.getEmail().substring(userRegisterRequest.getEmail().length() - 4).equals(".com")) {
            response.setCode(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getCode());
            response.setMsg(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getMessage());
        }

        //密码？
        if (userRegisterRequest.getUserPwd().length() < 4) {
            response.setCode(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getCode());
            response.setMsg(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getMessage());
        }

        if (userRegisterRequest.getUserPwd().length() > 20) {
            response.setCode(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getCode());
            response.setMsg(SysRetCodeConstants.REQUEST_FORMAT_ILLEGAL.getMessage());
        }

        //加密
        String s = DigestUtils.md5DigestAsHex(userRegisterRequest.getUserPwd().getBytes());
        userRegisterRequest.setUserPwd(s);


        User user = new User();
        user.setEmail(userRegisterRequest.getEmail());
        user.setPassword(userRegisterRequest.getUserPwd());
        user.setUsername(userRegisterRequest.getUserName());

        user.setId(null);

        user.setCreated(new Date());
        user.setUpdated(new Date());

        user.setState(1);
        userMapper.insertUser(user);
//        memberMapper.insertSelective(user);
        //发送邮件
        new Thread(() -> verifyMail(String.valueOf(user.getId()), user.getUsername(),user.getEmail())).start();
        //verifyMail(String.valueOf(user.getId()), user.getUsername(),user.getEmail());

        return response;
    }



    //待验证记录插入
    @Override
    public Boolean verify(UserRegisterRequest userRegisterRequest, String uuid) {

        UserVerify userVerify = new UserVerify();
        userVerify.setRegisterDate(new Date());
        userVerify.setId(null);
        userVerify.setIsExpire("N");
        userVerify.setIsVerify("N");
        userVerify.setUsername(userRegisterRequest.getUserName());
        userVerify.setUuid(uuid);

        userMapper.insertVerify(userVerify);
        return true;
    }


    @Autowired
    JavaMailSender mailSender;

    @Override
    public void verifyMail(String uid, String username,String email)  {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String from = "vikingsword@163.com";
        String to = email;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("多来买商城账号激活！！");
            message.setText("http://localhost:8080/user/verify?uid=" + uid + "&username=" + username);
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }

        System.out.println("---------发送邮件成功----------");
    }

    @Override
    public UserRegisterResponse verifyMember(UserVerifyRequest1 userVerifyRequest1) {
        UserRegisterResponse userRegisterResponse = new UserRegisterResponse();
        Member memberEmail=null;
        try {
            Example example = new Example(Member.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("id", userVerifyRequest1.getUid());
            criteria.andEqualTo("username", userVerifyRequest1.getUserName());
            Member member = new Member();
            member.setIsVerified("Y");
            memberMapper.updateByExampleSelective(member, example);
            List<Member> members = memberMapper.selectByExample(example);
            memberEmail=members.get(0);
            Example example1 = new Example(UserVerify.class);
            example1.createCriteria().andEqualTo("username", userVerifyRequest1.getUserName());

            UserVerify userVerify = new UserVerify();
            userVerify.setIsVerify("Y");
            userVerifyMapper.updateByExampleSelective(userVerify, example1);
            userRegisterResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            userRegisterResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionProcessorUtils.wrapperHandlerException(userRegisterResponse, e);
        }
        String email=memberEmail.getEmail();
        //发送一封已经激活的邮件
        new Thread(() -> verifyAfterMail(email)).start();

        return userRegisterResponse;
    }
    @Override
    public void verifyAfterMail(String email) {
        String from = "vikingsword@163.com";
        String to = email;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("账号激活成功！！");
            message.setText("账号激活成功！！");
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }

        System.out.println("---------发送邮件成功----------");
    }
}