package com.hr.store.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class StoreMgrService extends BaseService {
    @Before(Tx.class)//添加事务管理
    public JsonHashMap fire(Record r, UserSessionUtil usu) throws Exception {
        JsonHashMap jhm = new JsonHashMap();
        //在h_staff中删除对应的一条记录
        String[] staffId = r.getStr("staff_id").split(",");
        String date = r.getStr("date");
        String desc = r.getStr("desc");

        List<Record> staffLogList=new ArrayList<>(staffId.length);
        //删除时只传入一个员工id
        Object[][] deleteDataArray=new Object[staffId.length][1];
//        List<Record> staffList=new ArrayList<>(staffId.length);
        int i=0;
        for (String sId : staffId) {
                r.set("staff_id", sId);
                String sql = "select * from h_staff where id=?";
                Record record = Db.findFirst(sql, sId);
                if (record == null) {
                    jhm.putCode(0).putMessage("此员工不存在！");
                    return jhm;//有一条操作失败就回滚
                } else {
                    deleteDataArray[i][0]=sId;
                    i++;
                    //在h_staff_log表中添加一条记录，添加的记录是把status改为离职后的员工信息吗？？？是
                    String status = "quit";
                    record.set("status", status);
                    String id = UUIDTool.getUUID();
                    String operaterId = usu.getUserId();
                    String operaterType = "fire";
                    record.set("id", id);
                    record.set("staff_id", sId);
                    record.set("operater_id", operaterId);
                    record.set("operate_time", date);
                    record.set("operate_type", operaterType);
                    record.set("desc", desc);
                    record.remove("pinyin");
                    record.remove("username");
                    record.remove("password");
                    staffLogList.add(record);
                }
        }
        //批量删除和批量增加
        Db.batch("delete from h_staff where id=?",deleteDataArray,10);
        Db.batchSave("h_staff_log",staffLogList,10);
        jhm.putCode(1).putMessage("辞退成功！");
        return jhm;
    }
}
