package com.hr.mobile.setting.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;

import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import org.apache.commons.lang.StringUtils;

public class SettingCtrl extends BaseCtrl {
    /**
     * 员工端申请离职
     */
    public void modifyPhone (){
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String staffId=getPara("staffid");
        String stepId=getPara("stepid");
        String reason=getPara("reason");
        //测试数据
        if (StringUtils.isEmpty(staffId)||StringUtils.isEmpty(stepId)) {
            jhm.putCode(0).putMessage("员工不存在！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(reason)||StringUtils.isBlank(reason)){
            jhm.putCode(0).putMessage("请填写离职原因！");
            renderJson(jhm);
            return;
        }else if(reason.length()>50){
            //输入不能超过50个字
            jhm.putCode(0).putMessage("离职原因不能超过50个字！");
            renderJson(jhm);
            return;
        }
        //检查是否已经提交过离职申请了
        String sql="select count(*) c from h_notice n where n.sender_id=? and n.type='3' ";
        Record record = Db.findFirst(sql, staffId);
        if (record.getInt("c") != 0) {
            jhm.putCode(0).putMessage("您已经提交过离职申请！");
            renderJson(jhm);
            return;
        }
        //员工端发送离职申请，在h_notice表中添加一条记录
        String id= UUIDTool.getUUID();
        String title="离职申请";
        String createTime= DateTool.GetDateTime();
        String status="0";//0为未读1为已读
        String type="3";//1为申请调入2为调入通知3为离职申请
        Record recordNotice =new Record();
        try {
            recordNotice.set("id",id);
            recordNotice.set("title",title);
            recordNotice.set("content",reason);
            recordNotice.set("sender_id",staffId);
            recordNotice.set("receiver_id",stepId);
            recordNotice.set("create_time",createTime);
            recordNotice.set("status",status);
            recordNotice.set("type",type);
            boolean flag = Db.save("h_notice", recordNotice);
            if(flag) {
                //离职申请提交成功
                jhm.putCode(1).putMessage("提交成功！");
            }else{
                //离职申请提交失败
                jhm.putCode(0).putMessage("提交失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);

    }
}
