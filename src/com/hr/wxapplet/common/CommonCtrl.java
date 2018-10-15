package com.hr.wxapplet.common;

import com.common.controllers.BaseCtrl;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import utils.bean.JsonHashMap;
import org.apache.http.HttpEntity;

public class CommonCtrl extends BaseCtrl{

    private static final long serialVersionUID = 1L;

    private static final String APPID = "wx9xxxxxxxxxxx9b4";
    private static final String SECRET = "685742***************84xs859";

    /**
     * url:https://ip:port/context/wx/common/wxLogin
     * 1006.AIII.微信登录
     */
    public void wxLogin(){
        JsonHashMap jhm=new JsonHashMap();
        String code=getPara("code");

        //微信的接口
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+APPID+
                "&secret="+SECRET+"&js_code="+ code +"&grant_type=authorization_code";

        try{
//            RestTemplate restTemplate = new RestTemplate();
//            //进行网络请求,访问url接口
//            ResponseEntity<String>  responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
//            //根据返回值进行后续操作
//            if(responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK)
//            {
//                String sessionData = responseEntity.getBody();
//                Gson gson = new Gson();
//                //解析从微信服务器获得的openid和session_key;
//                WeChatSession weChatSession = gson.fromJson(sessionData,WeChatSession.class);
//                //获取用户的唯一标识
//                String openid = weChatSession.getOpenid();
//                //获取会话秘钥
//                String session_key = weChatSession.getSession_key();
//                //下面就可以写自己的业务代码了
//                //最后要返回一个自定义的登录态,用来做后续数据传输的验证
//            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
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

        //非空验证
        if(StringUtils.isEmpty(phone)){
            jhm.putCode(0).putMessage("手机号不能为空！");
            renderJson(jhm);
            return;
        }

        try{
            String sql = "SELECT phone FROM h_staff WHERE phone=?";
            Record info = Db.findFirst(sql,phone);
            if (info==null){
                jhm.putCode(0).putMessage("验证失败！");
            }else{
                jhm.putCode(1).putMessage("验证成功！");
            }
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