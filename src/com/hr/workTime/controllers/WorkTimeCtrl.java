package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class WorkTimeCtrl extends BaseCtrl {

    public void getWorkTime(){
        JsonHashMap jhm = new JsonHashMap();
        String storeId = getPara("dept");
        String startDate = getPara("start_date");
        String endDate = getPara("end_date");

        try {
            StringBuilder sql = new StringBuilder(" SELECT s.store_color AS store_color, s.name name, hs.name staff_name, SUM(wt.real_number * 0.25) total_work_time, hs.hour_wage, SUM((0.25 * wt.real_number * hs.hour_wage )) total FROM h_work_time wt, h_store s, h_staff hs WHERE wt.store_id = s.id AND wt.staff_id = hs.id ");
            List<String> params = new ArrayList<>();
            List<Record> workTimeList = new ArrayList<>();

            //判断是否有填筛选条件
            if(!StringUtils.isEmpty(storeId)){
                sql.append(" AND hs.dept_id = ? ");
                params.add(storeId);
            }
            if(!StringUtils.isEmpty(startDate)){
                sql.append(" AND wt.date >= ? ");
                params.add(startDate);
            } else {
                jhm.putCode(1);
                jhm.put("data", workTimeList);
                renderJson(jhm);
                return;
            }
            if(!StringUtils.isEmpty(endDate)){
                sql.append(" AND wt.date <= ? ");
                params.add(endDate);
            } else {
                jhm.putCode(1);
                jhm.put("data", workTimeList);
                renderJson(jhm);
                return;
            }

            sql.append("GROUP BY wt.staff_id");
            workTimeList = Db.find(sql.toString(), params.toArray());
//            for(int i = 0; i < workTimeList.size(); i++){
//                workTimeList.get(i).set("total_work_time", String.valueOf(Double.valueOf(workTimeList.get(i).getStr("total_work_time")) * 0.25));
//            }
            jhm.put("data", workTimeList);
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }





}
