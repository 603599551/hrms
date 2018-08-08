package com.hr.staff.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.HanyuPinyinHelper;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StaffSrv extends BaseService {
    @Before(Tx.class)//添加事务管理
    public HashMap recovery(Record r, UserSessionUtil usu) throws Exception {
        JsonHashMap jhm = new JsonHashMap();
        String[] idArray = r.getStr("ids").split(",");
        //更新h_staff_log表中status为"on"
        String operaterId = usu.getUserId();
        //h_staff_log批量更新，h_staff批量增加
        List<Record> staffList = new ArrayList<>(idArray.length);
        Object[][] updateDataArray = new Object[idArray.length][5];
        int i = 0;
        String time = DateTool.GetDateTime();
        for (String id : idArray) {
            String sql = "select *  from h_staff_log where id =?";
            Record record = Db.findFirst(sql, id);
            if (record == null) {
                jhm.putCode(0).putMessage("员工不存在！");
                return jhm;//有一条操作失败就回滚
            } else {
                updateDataArray[i][0] = time;
                updateDataArray[i][1] = "on";
                updateDataArray[i][2] = operaterId;
                String desc = record.getStr("desc") + " 备注：" + record.getStr("name") + time + "恢复在职";
                updateDataArray[i][3] = desc;
                updateDataArray[i][4] = id;
                i++;
                //在h_staff表中添加一条记录
                String pinyin= HanyuPinyinHelper.getFirstLettersLo(record.getStr("name"));
                String username=HanyuPinyinHelper.getPinyinString(record.getStr("name"));
                record.set("id", record.getStr("staff_id"));
                record.set("pinyin",pinyin);
                record.set("username",username);
                record.set("password","123456");
                record.remove("staff_id");
                record.remove("operater_id");
                record.remove("operate_time");
                record.remove("operate_type");
                record.remove("desc");
                record.remove("fid");
                staffList.add(record);
            }
        }
        Db.batch("update h_staff_log set operate_time=?,status=?,operater_id=?,h_staff_log.desc=? where id=? ", updateDataArray, 10);
        Db.batchSave("h_staff", staffList, 10);
        jhm.putCode(1).putMessage("恢复成功！");
        return jhm;
    }
}
