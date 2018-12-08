package com.jfinal.weixin;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.weixin.demo.WeixinConfig;
import com.jfinal.weixin.sdk.api.SnsAccessTokenApi;

public class WxAuthorController extends Controller {
    public void index(){
        String appId= PropKit.get("appId");
        String redirect= WeixinConfig.DOMAIN+"/home";
        String url=SnsAccessTokenApi.getAuthorizeURL(appId,redirect,true);
        redirect(url);
    }
}
