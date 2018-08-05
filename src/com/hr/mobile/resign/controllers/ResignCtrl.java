package com.hr.mobile.resign.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.resign.controllers.service.ResignCtrlSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResignCtrl extends BaseCtrl {

    /**
     * 经理端查看离职申请
     * firsename ???
     */
    public void apply() {
        JsonHashMap jhm = new JsonHashMap();
        //员工申请离职时信息发送给门店id
        String staffId = getPara("staff_id");
        //id非空验证
        if (StringUtils.isEmpty(staffId)) {
            jhm.putCode(0).putMessage("当前员工不存在！");
            renderJson(jhm);
            return;
        }
        try {
            //参数为店长id
            String sql = "select notice.id id , left(staff.pinyin, 1) firstname ,staff.name name,staff.job job, notice.create_time time,notice.content reason,'0' ishandle,'0' isagree,(select count(*) from h_notice  where h_notice.status='0') untreated from h_staff staff,(select *  from h_notice n where n.receiver_id=(select s.dept_id from h_staff s where s.id=?)and n.type='3' ) notice where notice.sender_id=staff.id";
            List<Record> applyList = Db.find(sql, staffId);
            if (applyList != null && applyList.size() > 0) {
                jhm.put("list", applyList);
            } else {
                jhm.putMessage("申请列表为空！");
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
            String sqlStaff = "SELECT left (s.pinyin, 1) firstname, s. NAME name, s.job job, s.phone phone, n.create_time time, n.content reason, '不同意' replay FROM h_staff s, h_notice n WHERE s.id = n.sender_id AND n.id = ?";
            String sqlReturn = "select d.name name,d.value value,'0' status from h_dictionary d where d.parent_id='4000'";
            Record resignRecord = Db.findFirst(sqlStaff, id);
            List<Record> returnList = Db.find(sqlReturn);
            if (resignRecord == null) {
                jhm.putCode(0).putMessage("记录不存在！");
            } else {
                resignRecord.set("item", returnList);
                jhm.put("data", resignRecord);
            }
            //经查看通知后还要把通知状态改为已读
            Record noticeRecord = new Record();
            noticeRecord.set("id", id);
            noticeRecord.set("status", "1");//1已读
            Db.update("h_notice", noticeRecord);

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 经理端审核离职
     */
    public void review() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String status = getPara("status");//0拒绝1同意
        String reply = getPara("reply");//拒绝时必须填写
        String item = getPara("item");//物品归还情况
        String noticeId =getPara("id");//离职记录id

//        status="1";
//        reply="";
//
//        item="[{\"value\":\"work_clothes\",\"status\":\"1\"},{\"value\":\"chest_card\"，\"status\":\"1\"}]";
//        noticeId="875693af12f54e8489b00c65df1ae63d";

        Map paraMap=new HashMap();
        paraMap.put("usu",usu);
        paraMap.put("status",status);
        paraMap.put("reply",reply);
        paraMap.put("item",item);
        paraMap.put("noticeId",noticeId);
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
            //如果为空是前台没传过来，不是用户没填，怎么提示？？？
            jhm.putCode(0).putMessage("物品归还状态不能为空！");
            renderJson(jhm);
            return;
        }
        //涉及多张表的增删改，要用到事务管理
        try {
            ResignCtrlSrv srv = enhance(ResignCtrlSrv.class);
            jhm=srv.Review(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
