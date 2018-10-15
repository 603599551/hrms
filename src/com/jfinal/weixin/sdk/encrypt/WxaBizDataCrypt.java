/**
 * Copyright (c) 2011-2014, L.cm 卢春梦 (qq596392912@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jfinal.weixin.sdk.encrypt;

import java.security.AlgorithmParameters;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.jfinal.kit.Base64Kit;
import com.jfinal.weixin.sdk.utils.Charsets;

/**
 * 微信小程序-加密数据解密算法
 * @author L.cm
 */
public class WxaBizDataCrypt {
    private final String sessionKey;

    public WxaBizDataCrypt(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    /**
     * AES解密
     * @param encryptedData 密文
     * @param ivStr iv
     * @return {String}
     */
    public String decrypt(String encryptedData, String ivStr) {
        byte[] bizData = Base64Kit.decode(encryptedData);
        byte[] keyByte = Base64Kit.decode(sessionKey);
        byte[] ivByte  = Base64Kit.decode(ivStr);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");
            // 初始化
            AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
            params.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, params);
            byte[] original = cipher.doFinal(bizData);
            // 去除补位字符
            byte[] result = PKCS7Encoder.decode(original);
            return new String(result, Charsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("aes解密失败");
        }
    }

}
