package com.hr.mobile.leave.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class LeaveCtrl extends BaseCtrl {

    public void apply(){
        JsonHashMap jhm=new JsonHashMap();
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
        String[] timeArray=time.split("-");
        String datetime= DateTool.GetDateTime();//yyyy-MM-dd HH:mm:ss

        String sql="select count(*) as c from h_staff_leave where staff_id=? and store_id=? and date=? and leave_start_time=? and leave_end_time=? and status='0'";
        Record countR=Db.findFirst(sql,userId,usu.getUserBean().getDeptId(),date,timeArray[0],timeArray[1]);
        Object cObj=countR.get("c");
        int c= NumberUtils.parseInt(cObj,0);
        if(c>0){
            jhm.putCode(0).putMessage("请不要重复提交数据！");
            renderJson(jhm);
            return;
        }

        Record r=new Record();
        r.set("id", UUIDTool.getUUID());
        r.set("staff_id",userId);
        r.set("store_id",usu.getUserBean().getDeptId());
        r.set("date",date);
        r.set("leave_start_time",timeArray[0]);
        r.set("leave_end_time",timeArray[1]);
        r.set("status",0);
        r.set("creater_id",userId);
        r.set("create_time",datetime);
        r.set("modifier_id",userId);
        r.set("modify_time",datetime);

        Record noticeR=new Record();


        try {
            boolean flag = Db.save("h_staff_leave", r);
            if (flag) {
                jhm.putCode(1).putMessage("提交成功！");
            } else {
                jhm.putCode(0).putMessage("提交失败！");
            }
        }catch (Exception e){
            jhm.putCode(-1).putMessage("服务器发生错误！");
            e.printStackTrace();
        }
        System.out.println("11111111");
        renderJson(jhm);
    }

    public void review(){

    }
}
