package com.hr.store.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class MoveOutService extends BaseService {

    @Before(Tx.class)
    public JsonHashMap out(Record r, UserSessionUtil usu, String[] str) {

        JsonHashMap jhm = new JsonHashMap();
        String time = DateTool.GetDateTime();
        String infoUUID = UUIDTool.getUUID();
        Record noticeRecord = new Record();
        Record infoRecord = new Record();
        List<Record> staffList = new ArrayList<Record>();
        //登录人所在门店ID
        Record fromDept = Db.findFirst("SELECT dept_id  FROM h_staff WHERE id = ?", usu.getUserId());
        //接收门店的经理ID
        Record toDeptManagerId = Db.findFirst("SELECT id FROM h_staff WHERE dept_id = ? AND job = 'store_manager'", r.getStr("toStore"));

        if (toDeptManagerId == null) {
            jhm.putCode(0).putMessage("该门店不存在");
            return jhm;
        }

        //对h_notice表进行操作
        noticeRecord.set("id", UUIDTool.getUUID());
        noticeRecord.set("content", "调入通知");
        noticeRecord.set("sender_id", usu.getUserId());
        noticeRecord.set("receiver_id", toDeptManagerId.getStr("id"));
        noticeRecord.set("create_time", time);
        noticeRecord.set("status", "0");
        noticeRecord.set("type", "movein_notice");
        noticeRecord.set("fid", infoUUID);

        //对h_move_info表进行操作
        infoRecord.set("id", infoUUID);
        infoRecord.set("from_dept", fromDept.getStr("dept_id"));
        infoRecord.set("to_dept", r.getStr("toStore"));
        infoRecord.set("date", r.getStr("date"));
        infoRecord.set("desc", r.getStr("desc"));
        infoRecord.set("creater_id", usu.getUserId());
        infoRecord.set("create_time", time);
        infoRecord.set("type", r.getStr("type"));
        infoRecord.set("status", "0");
        infoRecord.set("result", "");

        //对h_move_staff表进行操作
        for (int i = 0; i < str.length; ++i) {
            Record staffRecord = Db.findFirst("SELECT s.id AS staff_id, s.`name` AS `name`, s.gender AS gender, s.birthday AS birthday, s.phone AS phone, s.address AS address, s.emp_num AS emp_num, s.hiredate AS hiredate, s.job AS job, s.kind AS kind, s.`status` AS `status`, s.id_num AS id_num, s.work_type AS work_type, s.`level` AS `level`, s.hour_wage AS hour_wage, s.month_wage AS month_wage, s.bank AS bank, s.bank_card_num AS bank_card_num, s.creater_id AS creater_id, s.create_time AS create_time FROM h_staff s WHERE s.id = ?", str[i]);
            staffRecord.set("id", UUIDTool.getUUID());
            staffRecord.set("move_info_id", infoUUID);
            staffRecord.set("dept_id", fromDept.getStr("dept_id"));
            staffRecord.set("modifier_id", usu.getUserId());
            staffRecord.set("modify_time", time);
            staffList.add(staffRecord);
        }

        try {
            Db.save("h_notice", noticeRecord);
            Db.save("h_move_info", infoRecord);
            Db.batchSave("h_move_staff", staffList, staffList.size());
            jhm.putMessage("调出成功");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        return jhm;
    }

}
