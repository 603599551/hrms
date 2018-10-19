package com.hr.wxapplet.common;

import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import utils.bean.JsonHashMap;
import org.apache.http.HttpEntity;

import java.io.IOException;

public class CommonCtrl extends BaseCtrl{

    private static final String APPID = "wx236db34923f577e3";
    private static final String SECRET = "7c6869e372b6e8e90da4f010da40db3a";
    private static final String authorization_code = "authorization_code";
    public String login_url = "https://api.weixin.qq.com/sns/jscode2session?";
    public String getAccessToken_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + SECRET;
    public String sendCustomerMessage_url = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
    public static String templateId = "3JlxvZ61_mey-sNJzseGZA1GNwyY2sG8yLj2mPKza_w";

    /**
     * url:https://ip:port/context/wx/common/wxLogin
     * 1006.AIII.微信登录
     */
    public void wxLogin() throws IOException {
        JsonHashMap jhm = new JsonHashMap();
        String code = getPara("code");
        String url = this.login_url + "appid=" + APPID + "&secret=" + SECRET + "&js_code=" + code + "&grant_type=" + authorization_code;
        String doc = Jsoup.connect(url).execute().body();
        jhm.put("data", JSONObject.parseObject(doc));
        renderJson(jhm);
    }


    /**
     * url:https://ip:port/context/wx/common/login
     * 1005.C.登录
     */
    public void login(){
        JsonHashMap jhm=new JsonHashMap();
        /**
         * 接收前端参数
         */
        String phone=getPara("phone");
        String openId=getPara("openId");
        //员工端0 经理端1
        String type=getPara("type");


        //非空验证
        if(StringUtils.isEmpty(phone)){
            jhm.putCode(0).putMessage("手机号不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(openId)){
            jhm.putCode(0).putMessage("openId不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(type)){
            jhm.putCode(0).putMessage("type不能为空！");
            renderJson(jhm);
            return;
        }

        try{
            Record userId=Db.findFirst("SELECT id,job FROM h_staff WHERE phone=?",phone);
            if (StringUtils.equals(type,"0")){
                if (!StringUtils.equals(userId.getStr("job"),"staff")){
                    jhm.putCode(0).putMessage("你不是员工,无法登录！");
                    renderJson(jhm);
                    return;
                }
            }else {
                if (!StringUtils.equals(userId.getStr("job"),"store_manager")){
                    jhm.putCode(0).putMessage("你不是经理,无法登录！");
                    renderJson(jhm);
                    return;
                }
            }
            int flag=Db.update("UPDATE h_staff SET open_id=? WHERE phone=?",openId,phone);
            if (flag==0){
                jhm.putCode(0).putMessage("更新失败！");
                renderJson(jhm);
                return;
            }else {
                jhm.putCode(1).putMessage("更新成功！");
            }


            jhm.put("userId",userId.getStr("id"));

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
        //renderJson("{\"code\":1,\"message\":\"验证成功！\"}");

    }

    /**
     * url:https://ip:port/context/wx/common/editPassword
     * 1007.C.修改密码
     */
    public void editPassword(){
//        JsonHashMap jhm=new JsonHashMap();
//        String userId=getPara("userId");
//        String password=getPara("password");
//
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"message\":\"修改成功！\"}");
    }


}