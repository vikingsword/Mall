package com.mall.user.bootstrap;

import com.mall.user.IMemberService;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import tk.mybatis.mapper.entity.Example;

/**
 * @Author Yang
 * @Date 2021/8/25 22:00
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTest {

    @Autowired
    UserVerifyMapper userVerifyMapper;

    @Reference
    IMemberService iMemberService;

    @Test
    public void testVerifyMapper(){
        Example example = new Example(UserVerify.class);
        Example.Criteria criteria = example.createCriteria();
        criteria
                .andEqualTo("uuid", "0590cb4a2a311f5571d774ddd7c333af")
                .andEqualTo("username", "cskaoyan");
        UserVerify userVerify = new UserVerify();
        userVerify.setIsVerify("Y");
        int row = userVerifyMapper.updateByExampleSelective(userVerify, example);
        System.out.println(row);
    }


    @Test
    public void testVerifyService(){
        UserVerifyRequest request = new UserVerifyRequest();
        request.setUuid("0590cb4a2a311f5571d774ddd7c333af");
        request.setUserName("cskaoyan");
        UserVerifyResponse response = iMemberService.verifyMember(request);
        System.out.println(response);
    }
    @Autowired
    private JavaMailSender mailSender;
    @Test
    public void testSentEmail(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vikingsword@163.com");
        message.setTo("1417493193@qq.com");
        message.setSubject("多来买商城账号激活！！");
        message.setText("111111");
        mailSender.send(message);
    }

}
