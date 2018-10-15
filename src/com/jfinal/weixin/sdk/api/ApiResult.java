/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jfinal.weixin.sdk.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.LogKit;
import com.jfinal.weixin.sdk.utils.JsonUtils;

/**
 * 封装 API 响应结果，将 json 字符串转换成 java 数据类型
 *
 * jackson 中 json 类型与 java 类型对应关系如下：
 *  <pre>
 *  http://wiki.fasterxml.com/JacksonInFiveMinutes
 *  JSON TYPE                JAVA TYPE
 *  object                    LinkedHashMap&lt;String,Object&gt;
 *  array                    ArrayList&lt;Object&gt;
 *  string                    String
 *  number (no fraction)    Integer, Long or BigInteger (smallest applicable)
 *  number (fraction)        Double (configurable to use BigDecimal)
 *  true|false                Boolean
 *  null                    null
 *  </pre>
 */
public class ApiResult implements Serializable {
    private static final long serialVersionUID = 722417391137943513L;

    private Map<String, Object> attrs;
    private String json;

    /**
     * 通过 json 构造 ApiResult，注意返回结果不为 json 的 api（如果有的话）
     * @param jsonStr json字符串
     */
    @SuppressWarnings("unchecked")
    public ApiResult(String jsonStr) {
        this.json = jsonStr;

        try {
            Map<String, Object> temp = JsonUtils.parse(jsonStr, Map.class);
            this.attrs = temp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过 json 创建 ApiResult 对象，等价于 new ApiResult(jsonStr)
     * @param jsonStr json字符串
     * @return {ApiResult}
     */
    public static ApiResult create(String jsonStr) {
        return new ApiResult(jsonStr);
    }

    public String getJson() {
        return json;
    }

    public String toString() {
        return getJson();
    }

    /**
     * APi 请求是否成功返回
     * @return {boolean}
     */
    public boolean isSucceed() {
        Integer errorCode = getErrorCode();
        // errorCode 为 0 时也可以表示为成功，详见：http://mp.weixin.qq.com/wiki/index.php?title=%E5%85%A8%E5%B1%80%E8%BF%94%E5%9B%9E%E7%A0%81%E8%AF%B4%E6%98%8E
        return (errorCode == null || errorCode == 0);
    }

    public Integer getErrorCode() {
        return getInt("errcode");
    }

    public String getErrorMsg() {
        Integer errorCode = getErrorCode();
        if (errorCode != null) {
            String result = ReturnCode.get(errorCode);
            if (result != null) {
                return result;
            }
            LogKit.warn("未知返回码：" + errorCode);
        }
        return (String)attrs.get("errmsg");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T)attrs.get(name);
    }

    public String getStr(String name) {
        return (String)attrs.get(name);
    }

    public Integer getInt(String name) {
        Number number = (Number) attrs.get(name);
        return number == null ? null : number.intValue();
    }

    public Long getLong(String name) {
        Number number = (Number) attrs.get(name);
        return number == null ? null : number.longValue();
    }

    public BigInteger getBigInteger(String name) {
        return (BigInteger)attrs.get(name);
    }

    public Double getDouble(String name) {
        return (Double)attrs.get(name);
    }

    public BigDecimal getBigDecimal(String name) {
        return (BigDecimal)attrs.get(name);
    }

    public Boolean getBoolean(String name) {
        return (Boolean)attrs.get(name);
    }

    @SuppressWarnings("rawtypes")
    public List getList(String name) {
        return (List)attrs.get(name);
    }

    @SuppressWarnings("rawtypes")
    public Map getMap(String name) {
        return (Map)attrs.get(name);
    }

    public Map<String, Object> getAttrs(){
        return this.attrs;
    }

    /**
     * 判断 API 请求结果失败是否由于 access_token 无效引起的
     * 无效可能的情况 error_code 值：
     * 40001 = 获取access_token时AppSecret错误，或者access_token无效(刷新后也可以引起老access_token失效)
     * 42001 = access_token超时
     * 42002 = refresh_token超时
     * 40014 = 不合法的access_token
     * @return {boolean}
     */
    public boolean isAccessTokenInvalid() {
        Integer ec = getErrorCode();
        return ec != null && (ec == 40001 || ec == 42001 || ec == 42002 || ec == 40014);
    }
}









