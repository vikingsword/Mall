package com.mall.user.dal.persistence;

import com.mall.user.dal.entitys.User;
import com.mall.user.dal.entitys.UserVerify;

import java.util.List;

public interface UserMapper {
    List<User> selectAllUser();

    Boolean insertUser(User user);

    Integer selectByUsername(String userName);

    Integer selectByEmail(String email);

    Boolean insertVerify(UserVerify userVerify);
}
