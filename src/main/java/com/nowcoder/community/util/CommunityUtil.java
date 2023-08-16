package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {

        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密，对用户提交的密码加密
    // hello -> abc123def456
    //用户密码+随机字符串--->加密后，这样就不容易破解
    // hello + 3e4a8 -> abc123def456abc
    //返回加密结果，参数就是密码
    public static String md5(String key) {
        //StringUtils，就是加入的依赖提供的类
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
    //获得json格式的字符串
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    //方法重载
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    //方法重载
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }


}
