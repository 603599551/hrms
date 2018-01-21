package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginCodeAjaxCtrl extends Controller {
    public void index(){
        String phone=getPara("phone");
        Map reMap=new HashMap();
        try{
            //查询数据库中是否存在此电话号码
            Record r= Db.findFirst("select * from user where phone=?",phone);
            if(r==null){
                reMap.put("code",-104);
                reMap.put("msg","数据库没有此手机号！");
                renderJson(reMap);
                return;
            }

//            String result=SendCode.send(phone);
//            JSONObject json=JSON.parseObject(result);
//            if(json.getIntValue("code")==200){
//                reMap.put("code",1);
//                String loginCode=json.getString("obj");
//                setSessionAttr("phone",phone);
//                setSessionAttr("loginCode",loginCode);
//                setSessionAttr("loginCodeTime",new Date());
//            }else{
//                reMap.put("code",0);
//                reMap.put("msg","发生验证码错误！");
//            }
            renderJson(reMap);
        }catch (Exception e){
            e.printStackTrace();;
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
}
