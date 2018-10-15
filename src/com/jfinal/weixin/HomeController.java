package com.jfinal.weixin;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.demo.WeixinConfig;
import com.jfinal.weixin.sdk.api.SnsAccessToken;
import com.jfinal.weixin.sdk.api.SnsAccessTokenApi;

import java.util.Map;

public class HomeController extends Controller{
    public void index(){
        String code=getPara("code");
        String appId= PropKit.get("appId");
        String appSecret= PropKit.get("appSecret");
        SnsAccessToken sat=SnsAccessTokenApi.getSnsAccessToken(appId,appSecret,code);
        String accessToken=sat.getAccessToken();
        String openId=sat.getOpenid();
        setSessionAttr("wx_code",code);
        setSessionAttr("wx_access_token",accessToken);
        setSessionAttr("wx_open_id",openId);
        redirect(WeixinConfig.DOMAIN+PropKit.get("wx_home"));
    }
}
