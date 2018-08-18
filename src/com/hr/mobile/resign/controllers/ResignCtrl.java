package com.hr.mobile.resign.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.resign.service.ResignSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;
import java.util.*;

public class ResignCtrl extends BaseCtrl {

    /**
     * 经理端查看离职申请
     *
     * */

    public void apply() {
        JsonHashMap jhm = new JsonHashMap();
        //员工id
        String staffId = getPara("staffid");
        //部门id
        String deptId = getPara("deptid");
        //离职原因
        String reason = getPara("reason");
        if (StringUtils.isEmpty(staffId) || StringUtils.isEmpty(deptId)) {
            jhm.putCode(0).putMessage("员工不存在！");
            renderJson(jhm);
            return;
        }else{
            //从h_staff表中查询验证是否存在、是否离职
            String sql="select * from h_staff where h_staff.id=?";
            Record record=Db.findFirst(sql,staffId);
            if(record==null){
                jhm.putCode(0).putMessage("员工不存在！");
                renderJson(jhm);
                return;
            }else if(StringUtils.equals(record.getStr("status"),"quit")){
                jhm.putCode(0).putMessage("您已经离职，无法执行离职申请！");
                renderJson(jhm);
                return;
            }
        }
        if (StringUtils.isEmpty(reason) || StringUtils.isBlank(reason)) {
            jhm.putCode(0).putMessage("请填写离职原因！");
            renderJson(jhm);
            return;
        } else if (reason.length() > 50) {
            //输入不能超过50个字
            jhm.putCode(0).putMessage("离职原因不能超过50个字！");
            renderJson(jhm);
            return;
        }
        try {
            //检查是否已经提交过离职申请
            //h_notice表type为resign是离职,h_resign表status0申请离职
            String sql = "select count(*) c from h_notice n,h_resign r where n.sender_id=r.applicant_id and n.sender_id=? and n.type='resign' and r.status='0'";
            Record record = Db.findFirst(sql, staffId);
            if (record.getInt("c") != 0) {
                jhm.putCode(0).putMessage("您已经提交过离职申请！");
                renderJson(jhm);
                return;
            }
            Map paraMap = new HashMap();
            paraMap.put("staffid", staffId);
            paraMap.put("deptid", deptId);
            paraMap.put("reason", reason);
            ResignSrv srv = enhance(ResignSrv.class);
            jhm = srv.apply(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);

    }
    public void showList() {
        JsonHashMap jhm = new JsonHashMap();
        //获取经理id
        String staffId = getPara("staff_id");
        //id非空验证
        if (StringUtils.isEmpty(staffId)) {
            jhm.putCode(0).putMessage("经理不存在！");
            renderJson(jhm);
            return;
        }
        try {
            //参数为店长id
            String sql = "SELECT n.id id, upper(LEFT (s.pinyin, 1)) firstname, s. NAME name, (select d.name from h_dictionary d where d.value=s.job and d.parent_id='200') job, n.create_time time, n.content reason, ( CASE r. STATUS WHEN '0' THEN '0' ELSE '1' END ) ishandle, ( CASE r. STATUS WHEN '1' THEN '1' else '0' END ) isagree, ( SELECT count(*) FROM h_notice, h_resign WHERE h_notice.fid = h_resign.id AND h_notice.receiver_id = ? AND h_notice.type = 'resign' AND h_resign. STATUS = '0' ) untreated,(case r.review_time when null then '0' else r.review_time end) sortTime  FROM h_staff s, h_notice n, h_resign r WHERE s.id = n.sender_id AND n.fid = r.id AND n.receiver_id = ? AND n.type = 'resign'order by (case r.status when '0' then n.create_time else r.review_time end) desc";
            List<Record> applyList = Db.find(sql, staffId,staffId);
            String sqlAgree="SELECT n.id id, upper(LEFT (s.pinyin, 1)) firstname, s. NAME name, (select d.name from h_dictionary d where d.value=s.job and d.parent_id='200') job, n.create_time time, n.content reason, ( CASE r. STATUS WHEN '0' THEN '0' ELSE '1' END ) ishandle, ( CASE r. STATUS WHEN '1' THEN '1' else '0' END ) isagree, ( SELECT count(*) FROM h_notice, h_resign WHERE h_notice.fid = h_resign.id AND h_notice.receiver_id = ? AND h_notice.type = 'resign' AND h_resign. STATUS = '0' ) untreated,r.review_time sortTime  FROM h_staff_log s, h_notice n, h_resign r WHERE s.staff_id = n.sender_id AND n.fid = r.id AND n.receiver_id = ? AND n.type = 'resign' and r.status='1' order by r.review_time desc";
            List<Record> agreeList = Db.find(sqlAgree, staffId,staffId);
            if (applyList != null && applyList.size() > 0) {
                if(agreeList != null && agreeList.size() > 0){
                    applyList.addAll(agreeList);
                    //已处理的申请列表要按处理时间排序
                    Collections.sort(applyList,new Comparator<Record>() {
                        @Override
                        public int compare(Record r1, Record r2) {
                            return r2.getStr("sortTime").compareTo(r1.getStr("sortTime"));
                        }
                    });
                }
                jhm.put("list", applyList);
            } else if(agreeList != null && agreeList.size() > 0){
                jhm.put("list",agreeList);
            }else{
                jhm.putCode(0).putMessage("申请列表为空！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 经理端查看离职申请详情
     */
    public void showById() {
        JsonHashMap jhm = new JsonHashMap();
        //离职记录的id
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("记录不存在！");
            renderJson(jhm);
            return;
        }
        try {
            //未处理的离职申请，item查询字典值表
            //处理过的离职申请，item查询h_resign_return表
            String sqlHandle ="select (case r.status when '0' then '0' else '1' end) ishandle,( CASE r. STATUS WHEN '1' THEN '1' else '0' END ) isagree from h_notice n,h_resign r where n.fid=r.id and n.id=?";
            Record recordHandle=Db.findFirst(sqlHandle,id);
            if(recordHandle==null){
                jhm.putCode(0).putMessage("记录不存在！");
                renderJson(jhm);
                return;
            }
            String sqlReturn="";
            String ishandle=recordHandle.getStr("ishandle");
            String isagree=recordHandle.getStr("isagree");
            //ishandle 0未处理 1已处理
            List<Record> returnList=null;
            if(StringUtils.equals(ishandle,"0")){
                sqlReturn = "select d.name name,d.value value,'0' status from h_dictionary d where d.parent_id='4000'";
                returnList = Db.find(sqlReturn);
            }else if(StringUtils.equals(ishandle,"1")){
                sqlReturn="select r.name value,(select d.name from h_dictionary d where d.parent_id='4000' and d.value=r.name) name,r.status status from h_resign_return r where r.resign_id=(select fid from h_notice where id=? )";
                returnList = Db.find(sqlReturn,id);
            }
            String sqlStaff="";
            if(StringUtils.equals(isagree,"1")){
                sqlStaff = "SELECT upper(left (s.pinyin, 1)) firstname, s. NAME name, (select d.name from h_dictionary d where d.value=s.job and d.parent_id='200') job, s.phone phone, n.create_time time, n.content reason, r.reply replay FROM h_staff_log s, h_notice n,h_resign r WHERE s.staff_id = n.sender_id and n.fid=r.id AND n.id = ?";
            }else if(StringUtils.equals(isagree,"0")){
                sqlStaff = "SELECT upper(left (s.pinyin, 1)) firstname, s. NAME name, (select d.name from h_dictionary d where d.value=s.job and d.parent_id='200') job, s.phone phone, n.create_time time, n.content reason, r.reply replay FROM h_staff s, h_notice n ,h_resign r WHERE s.id = n.sender_id AND n.fid=r.id and n.id = ?";
            }
            Record resignRecord = Db.findFirst(sqlStaff, id);
            if (resignRecord == null) {
                jhm.putCode(0).putMessage("记录不存在！");
                renderJson(jhm);
                return;
            } else {
                resignRecord.set("item", returnList);
                jhm.put("data", resignRecord);
            }
            //经查看通知后还要把通知状态改为已读
            Record noticeRecord = new Record();
            noticeRecord.set("id", id);
            //1已读
            noticeRecord.set("status", "1");
            Db.update("h_notice", noticeRecord);

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 经理端审核离职
     */
    public void review() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //0拒绝1同意
        String status = getPara("status");
        //拒绝时必须填写
        String reply = getPara("reply");
        //物品归还情况
        String item = getPara("item");
        //离职记录id
        String noticeId =getPara("id");
        if (StringUtils.isEmpty(noticeId)) {
            jhm.putCode(0).putMessage("该条记录不存在！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(status)) {
            jhm.putCode(0).putMessage("审核提交失败！");
            renderJson(jhm);
            return;
        } else if (StringUtils.equals(status, "0")) {
            if (StringUtils.isEmpty(reply)) {
                jhm.putCode(0).putMessage("请填写拒绝原因！");
                renderJson(jhm);
                return;
            }
        }
        if (StringUtils.isEmpty(item)) {
            //如果为空是前台没传过来
            jhm.putCode(0).putMessage("物品归还状态不能为空！");
            renderJson(jhm);
            return;
        }
        Map paraMap=new HashMap();
        paraMap.put("usu",usu);
        paraMap.put("status",status);
        paraMap.put("reply",reply);
        paraMap.put("item",item);
        paraMap.put("noticeId",noticeId);
        //涉及多张表的增删改，要用到事务管理
        try {
            ResignSrv srv = enhance(ResignSrv.class);
            jhm=srv.review(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
