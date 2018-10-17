package com.hr.wxapplet.manager.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;


import java.util.Map;

public class ManageSrv extends BaseService {
    @Before(Tx.class)
    public void agreeCheck(Map paraMap){
        String userId = (String) paraMap.get("userId");
        String status = (String) paraMap.get("status");
        String noticeId = (String) paraMap.get("noticeId");
        String time = (String) paraMap.get("time");
        String address = (String) paraMap.get("address");

        String noticeSql="SELECT * FROM h_notice WHERE id=?";
        //入职日期、岗位value
        String hiredateSql="SELECT s.hiredate,d.value FROM h_staff s,h_dictionary d WHERE s.id=? AND d.name=?";
        String createTime= DateTool.GetDateTime();

        Record mNotice=Db.findFirst(noticeSql,noticeId);
        String mSenderId=mNotice.getStr("sender_id");
        String mReceiverId=mNotice.getStr("receiver_id");
        String mTitle=mNotice.getStr("title");

        Record hNotice=Db.findFirst(hiredateSql,mSenderId,mTitle);
        String hiredate=hNotice.getStr("hiredate");
        String kindId=hNotice.getStr("value");

        String examId=UUIDTool.getUUID();
        //向exam表增加考试记录
        Record exam=new Record();
        exam.set("id",examId );
        exam.set("staff_id", mSenderId);
        exam.set("create_time", createTime);
        exam.set("hiredate", hiredate);
        exam.set("kind_id", kindId);
        exam.set("review_time", time);
        exam.set("examiner_id", mReceiverId);
        exam.set("type_id", "");
        try{
            Db.save("h_exam",exam);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("Fail to add examRecord!");
        }

        //更新经理端能看到的申请考核信息状态为“同意”
        try{
            Db.update("UPDATE h_notice SET content=?,modify_time=?,status='1',fid=? WHERE id=?",address, createTime,examId,noticeId);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("Fail to update notice!");
        }


        //向员工端发送一条同意通知
        Record sNotice=new Record();
        sNotice.set("id", UUIDTool.getUUID());
        sNotice.set("title", mTitle);
        sNotice.set("content",address);
        sNotice.set("sender_id",mReceiverId);
        sNotice.set("receiver_id",mSenderId);
        sNotice.set("create_time",createTime);
        sNotice.set("modify_time",createTime);
        sNotice.set("status","1");
        sNotice.set("type","check");
        sNotice.set("fid",examId);

        try{
            Db.save("h_notice",sNotice);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("Fail to add notice!");
        }
    }

    @Before(Tx.class)
    public void refuseCheck(Map paraMap){
        String userId = (String) paraMap.get("userId");
        String status = (String) paraMap.get("status");
        String noticeId = (String) paraMap.get("noticeId");
        String reason = (String) paraMap.get("reason");

        String noticeSql="SELECT * FROM h_notice WHERE id=?";
        String createTime= DateTool.GetDateTime();

            //更新经理端能看到的申请考核信息状态为“拒绝”
        try{
            Db.update("UPDATE h_notice SET content=?,modify_time=?,status='2' WHERE id=?",reason, createTime,noticeId);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("Fail to update notice!");
        }

        Record mNotice=Db.findFirst(noticeSql,noticeId);
        //向员工端发送一条拒绝通知
        Record sNotice=new Record();
        sNotice.set("id", UUIDTool.getUUID());
        sNotice.set("title", mNotice.getStr("title"));
        sNotice.set("content",reason);
        sNotice.set("sender_id",mNotice.getStr("receiver_id"));
        sNotice.set("receiver_id",mNotice.getStr("sender_id"));
        sNotice.set("create_time",createTime);
        sNotice.set("modify_time",createTime);
        sNotice.set("status","2");
        sNotice.set("type","check");

        try{
            Db.save("h_notice",sNotice);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("Fail to add notice!");
        }
    }

    @Before(Tx.class)
    public void applyCheckResult(Map paraMap){
        //经理id
        String userId=(String)paraMap.get("userId");
        //考核记录id
        String checkId=(String)paraMap.get("checkId");
        //考核结果
        String status=(String)paraMap.get("status");

        //提交考核结果
        Record result = new Record();
        result.set("examiner_id",userId);
        result.set("id",checkId);
        result.set("result",status);

        try{
            Db.update("h_exam",result);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("提交失败！");
        }

        String sql="SELECT d.name AS title,e.id AS fid,e.staff_id AS receiver_id FROM h_exam e,h_dictionary d WHERE e.id=? AND e.kind_id=d.value";

        String title;
        String fid;
        String receiverId;
        try{
            Record info=Db.findFirst(sql,checkId);
            title=info.getStr("title");
            fid=info.getStr("fid");
            receiverId=info.getStr("receiver_id");
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("获取考核信息失败！");
        }

        String time=DateTool.GetDateTime();


        Record notice=new Record();
        notice.set("id",UUIDTool.getUUID());
        notice.set("title",title);
        notice.set("sender_id",userId);
        notice.set("receiver_id",receiverId);
        notice.set("create_time",time);
        notice.set("modify_time",time);
        notice.set("type","check");
        notice.set("fid",fid);
        //考核通过
        if (StringUtils.equals(status,"0")){
            notice.set("status","3");
        }else {
            //未通过
            notice.set("status","4");
        }
        try{
            Db.save("h_notice",notice);
        }catch (ActiveRecordException e){
            throw new ActiveRecordException("通知失败");
        }

    }
}
