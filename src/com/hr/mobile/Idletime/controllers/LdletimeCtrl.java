package com.hr.mobile.Idletime.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.service.MenuService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.ContentTransformationUtil;
import utils.bean.JsonHashMap;

import java.util.List;


public class ldletimeCtrl extends BaseCtrl{
    /*
        添加闲时
         */
    public void add(){
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String userId=usu.getUserId();

        Record staffRecord = Db.findFirst("select * from h_staff where id = ?",userId);
//        String deptId = staffRecord.getStr("username");

        //为空判断
        if(staffRecord == null){
            jhm.putCode(0).putMessage("找不到该员工！");
            renderJson(jhm);
            return;
        }
        String kind=staffRecord.getStr("kind");
        String staffId=getPara("id");
        String date=getPara("date");
        String time=getPara("time");

        String datetime= DateTool.GetDateTime();

        Record record = new Record();
        record.set("id", UUIDTool.getUUID());
        record.set("store_id", usu.getUserBean().getDeptId());
        record.set("staff_id",staffId);
        record.set("date",date);
        record.set("app_content",time);
        record.set("create_time",datetime);
        record.set("creater_id",userId);
        record.set("modify_time",datetime);
        record.set("modifier_id",userId);
        record.set("kind",kind);
        record.set("content", ContentTransformationUtil.AppToPcPaiban(date));

        try{
            boolean flag = Db.save("h_staff_idle_time",record);
            if(flag){
                jhm.putCode(1).putMessage("保存成功！");
            }else {
                jhm.putCode(0).putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }
    public void showDetailByStaffIdAndDate(){
        JsonHashMap jhm=new JsonHashMap();
        List<Record> record= Db.find("select * from h_staff_idle_time limit 2");
        jhm.put("data",record);
        renderJson(jhm);


    }
}
