/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jfinal.weixin.sdk.api;

import java.io.Serializable;
import java.util.Map;

import com.jfinal.weixin.sdk.utils.JsonUtils;
import com.jfinal.weixin.sdk.utils.RetryUtils.ResultCheck;

/**
 * 封装 access_token
 */
@SuppressWarnings("unchecked")
public class AccessToken implements ResultCheck, Serializable {
    private static final long serialVersionUID = -822464425433824314L;

    private String access_token;    // 正确获取到 access_token 时有值
    private Integer expires_in;        // 正确获取到 access_token 时有值
    private Integer errcode;        // 出错时有值
    private String errmsg;            // 出错时有值

    private Long expiredTime;        // 正确获取到 access_token 时有值，存放过期时间
    private String json;

    public AccessToken(String jsonStr) {
        this.json = jsonStr;

        try {
            Map<String, Object> temp = JsonUtils.parse(jsonStr, Map.class);
            access_token = (String) temp.get("access_token");
            expires_in = (Integer) temp.get("expires_in");
            errcode = (Integer) temp.get("errcode");
            errmsg = (String) temp.get("errmsg");

            if (expires_in != null) {
            	// expires_in - 9  用于控制在 access token 过期之前 9 秒就 "主动" 再次获取 access token
            	// 避免大并发场景下多线程同时获取 access token，造成公众平台 api 调用额度的浪费
                expiredTime = System.currentTimeMillis() + ((expires_in - 9) * 1000);
            }
            
            // 用户缓存时还原
            if (temp.containsKey("expiredTime")) {
                 expiredTime = (Long) temp.get("expiredTime");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getJson() {
        return json;
    }

    public String getCacheJson() {
        Map<String, Object> temp = JsonUtils.parse(json, Map.class);
        temp.put("expiredTime", expiredTime);
        temp.remove("expires_in");
        return JsonUtils.toJson(temp);
    }

    public boolean isAvailable() {
        if (expiredTime == null)
            return false;
        if (errcode != null)
            return false;
        if (expiredTime < System.currentTimeMillis())
            return false;
        return access_token != null;
    }

    public String getAccessToken() {
        return access_token;
    }

    public Integer getExpiresIn() {
        return expires_in;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public Integer getErrorCode() {
        return errcode;
    }

    public String getErrorMsg() {
        if (errcode != null) {
            String result = ReturnCode.get(errcode);
            if (result != null)
                return result;
        }
        return errmsg;
    }

    @Override
    public boolean matching() {
        return isAvailable();
    }
}
