package com.hr.mobile.setting.controllers.service;

import com.common.service.BaseService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import java.util.Map;

public class SettingCtrlSrv extends BaseService {

    public JsonHashMap modifyPhone(Map paraMap){

        JsonHashMap jhm = new JsonHashMap();
        //员工端发送离职申请，在h_notice表中添加一条记录
        String staffId = (String) paraMap.get("staffid");
        String stepId = (String) paraMap.get("stepid");
        String reason =(String)paraMap.get("reason");
        String id= UUIDTool.getUUID();
        String title="离职申请";
        String createTime= DateTool.GetDateTime();
        String status="0";//0为未读1为已读
        String type="3";//1为申请调入2为调入通知3为离职申请

        Record recordNotice =new Record();
        recordNotice.set("id",id);
        recordNotice.set("title",title);
        recordNotice.set("content",reason);
        recordNotice.set("sender_id",staffId);
        //接收人id填写的是员工所在部门
        recordNotice.set("receiver_id",stepId);
        recordNotice.set("create_time",createTime);
        recordNotice.set("status",status);
        recordNotice.set("type",type);
        boolean flag1 = Db.save("h_notice", recordNotice);
        //向h_resign日志表插入一条记录
        String resignId= UUIDTool.getUUID();
        Record resignRecord=new Record();
        resignRecord.set("id",resignId);
        resignRecord.set("applicant_id",staffId);//申请人id 传过来的staffid也是当前登录人的id
        resignRecord.set("apply_time",createTime);//申请日期
        resignRecord.set("reason",reason);//申请离职原因
        resignRecord.set("status","0");//0:申请离职，1：同意离职，2：拒绝离职
        boolean flag2= Db.save("h_resign",resignRecord);

        if(flag1&&flag2) {
            //离职申请提交成功
            jhm.putCode(1).putMessage("提交成功！");
        }else{
            //离职申请提交失败
            jhm.putCode(0).putMessage("提交失败！");
        }
        return jhm;
    }
}
