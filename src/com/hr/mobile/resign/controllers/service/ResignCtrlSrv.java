package com.hr.mobile.resign.controllers.service;

import com.common.service.BaseService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.Map;

public class ResignCtrlSrv extends BaseService {

    public JsonHashMap Review(Map paraMap) {
        JsonHashMap jhm = new JsonHashMap();
        //员工端发送离职申请，在h_notice表中添加一条记录
        String status = (String) paraMap.get("status");
        String reply = (String) paraMap.get("reply");
        String item = (String) paraMap.get("item");
        String noticeId = (String) paraMap.get("noticeId");
        UserSessionUtil usu = (UserSessionUtil) paraMap.get("usu");
        Record r = Db.findFirst("select n.sender_id staffId from h_notice n where n.id=?", noticeId);
        //申请离职员工id
        String staffId = r.getStr("staffId");
        //向h_notice表中添加一条记录（发给申请员工的结果通知）
        Record noticeRecord = new Record();
        String id = UUIDTool.getUUID();
        noticeRecord.set("id", id);
        noticeRecord.set("title", "离职申请回复");//title里写什么内容？？？
        noticeRecord.set("content", reply);//记录拒绝原因
        noticeRecord.set("sender_id", usu.getUserId());//经理id就是当前操作人（登录人）id
        noticeRecord.set("receiver_id", staffId);
        String date = DateTool.GetDateTime();
        noticeRecord.set("create_time", date);//通知的创建时间
        noticeRecord.set("status", "0");//0未读
        noticeRecord.set("type", "3");//1为申请调入2为调入通知3为离职申请
        Db.save("h_notice", noticeRecord);//向notice表中添加一条记录，发送给员工通知
        //更新h_resign日志表的员工离职申请记录
        String sqlResign = "select * from h_resign r where r.applicant_id=? and r.status='0' ";
        Record record = Db.findFirst(sqlResign, staffId);
        if (record == null) {
            jhm.putCode(0).putMessage("该条记录不存在！");
            return jhm;
        }
        //获得离职原因，下面同意离职时向h_staff_log表插入记录时desc备注
        String staffLogDesc = record.getStr("content");
        String resignId = record.getStr("id");//获得到id存到h_resign_return中
        String reviewTime = DateTool.GetDateTime();
        record.set("leave_date", reviewTime);
        record.set("reply", reply);
        if (StringUtils.equals(status, "0")) {
            record.set("status", "2");
        } else if (StringUtils.equals(status, "1")) {
            record.set("status", "1");
        }


        record.set("reviewer_id", id);//审核人id
        record.set("review_time", reviewTime);//审核时间
        Db.update("h_resign", record);
        //向h_resign_return表中添加物品归还记录
        JSONArray timeArray = JSONArray.fromObject(item);
        for (int i = 0; i < timeArray.size(); i++) {
            String returnId = UUIDTool.getUUID();
//                String name = timeArray.getJSONObject(i).getString("name");
            String value = timeArray.getJSONObject(i).getString("value");
            String returnStatus = timeArray.getJSONObject(i).getString("status");

//                Record recordName=Db.findFirst("select d.name name from dictionary d where d.value=? and d.parent_id='4000'",value);
//                String name=recordName.getStr("name");
            Record returnRecord = new Record();
            returnRecord.set("id", returnId);
            returnRecord.set("resign_id", resignId);
            //h_resign_return表中存的是value吗？？？
            returnRecord.set("name", value);
            returnRecord.set("status", returnStatus);
            returnRecord.set("creater_id", usu.getUserId());
            returnRecord.set("create_time", reviewTime);//创建时间就是审核时间
            returnRecord.set("modifier_id", usu.getUserId());
            returnRecord.set("modify_time", reviewTime);
            Db.save("h_resign_return", returnRecord);
        }
        //删除h_notice表中的员工本条申请离职记录
        Db.deleteById("h_notice", noticeId);
        //如果店长同意离职 还要操作：将离职员工从h_staff表中删除在h_staff_log表中添加一条记录
        if (StringUtils.equals(status, "1")) {
            String sqlStaff = "select * from h_staff s where s.id=?";
            //获取当前离职员工信息
            Record staffRecord = Db.findFirst(sqlStaff, staffId);
            if (staffRecord == null) {
                jhm.putCode(0).putMessage("该员工不存在！");
                return jhm;
            }

            String staffLogId = UUIDTool.getUUID();
            staffRecord.set("staff_id", staffRecord.getStr("id"));
            staffRecord.set("id", staffLogId);
            staffRecord.set("operater_id", usu.getUserId());//当前操作人id 即店长
            staffRecord.set("operate_time", reviewTime);//店长点击同意的时间即审核时间
            staffRecord.set("operate_type", "quit");//店员主动提出离职
            staffRecord.set("desc", staffLogDesc);//离职原因
            staffRecord.remove("pinyin");
            staffRecord.remove("username");
            staffRecord.remove("password");
            Db.save("h_staff_log", staffRecord);
            //删除h_staff离职员工记录
            Db.deleteById("h_staff", staffId);


//            //查找员工姓名推送用
//            Record pushRecord = Db.findFirst("select s.name as name from h_staff s where s.id = ?",staffId);
//            //推送
//            JiguangPush push = new JiguangPush("6863f15c5be031f95b5de21c", "130e4cbb7f9e821a26158183");
//            String tag = staffId + "-"+pushRecord.getStr("name");
//            push.setAlert("有一条离职申请的回复");
//            push.setTag(tag);
        }
        jhm.putMessage("审核提交成功！");
        return jhm;
    }
}