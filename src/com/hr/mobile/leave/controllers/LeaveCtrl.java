package com.hr.mobile.leave.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.leave.services.LeaveSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.HashMap;
import java.util.Map;

public class LeaveCtrl extends BaseCtrl {

    public void apply(){
        JsonHashMap jhm=new JsonHashMap();
        //获取用户信息的工具类
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String userId=usu.getUserId();
        /*
        getParameter()
         */
        String date=getPara("date");
        String time=getPara("time");
        if(StringUtils.isBlank(date)){
            jhm.putCode(0).putMessage("请输入请假的日期！");
            renderJson(jhm);
            return;
        }
        Map paraMap=new HashMap();
        paraMap.put("usu",usu);
        paraMap.put("date",date);
        paraMap.put("time",time);

        try {
            /*
            必须通过此方式创建service对象，否则事务不启用
             */
            LeaveSrv srv = enhance(LeaveSrv.class);
//            LeaveSrv srv=new LeaveSrv();
            jhm=srv.apply(paraMap);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生错误！");
        }
        System.out.println("11111111");
        renderJson(jhm);
    }

    public void review(){

    }
}
