package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.bean.JsonHashMap;

import java.util.List;

public class WorkTimeCtrl extends BaseCtrl {

    public void getWorkTime(){
        String storeId = getPara("store_id");

        String sql = "select s.name name, hs.name staff_name, wt.real_number total_work_time, 12 hour_wage, 100 total from h_work_time wt, h_store s, h_staff hs where wt.store_id=s.id and wt.staff_id=hs.id ";
        List<Record> workTimeList = Db.find(sql);

        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", workTimeList);
        renderJson(jhm);

    }

}
