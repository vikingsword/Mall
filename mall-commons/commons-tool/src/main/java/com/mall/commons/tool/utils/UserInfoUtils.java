package com.mall.commons.tool.utils;

import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @descriptions:
 * @author:
 * @date: 2021-08-25 15:35
 * @version: 1.0
 */
public class UserInfoUtils {
    public static Map getUesrInfo(HttpServletRequest request) {
        String attribute = (String) request.getAttribute("userInfo");
        HashMap<String, String> map = new HashMap<>();
        Map parse = (Map) JSON.parse(attribute);
        String id = (String) parse.get("id");
        String username = (String) parse.get("username");
        map.put("id", id);
        map.put("username", username);
        return map;


    }
}