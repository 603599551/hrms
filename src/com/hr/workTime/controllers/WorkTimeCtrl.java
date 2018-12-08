package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.NumberFormat;
import utils.bean.JsonHashMap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkTimeCtrl extends BaseCtrl {

    public void getWorkTime(){
        JsonHashMap jhm = new JsonHashMap();
        String storeId = getPara("dept");
        String startDate = getPara("start_date");
        String endDate = getPara("end_date");
        Record record = new Record();

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
            Double allWorkTime = 0d;
            Double allSalary = 0d;
            for(int i = 0; i < workTimeList.size(); i++){
                record = workTimeList.get(i);
                record.set("total", NumberFormat.doubleFormatStr(Double.valueOf(record.getStr("total"))));
                record.set("hour_wage", NumberFormat.doubleFormatStr(Double.valueOf(record.getStr("hour_wage"))));
                record.set("total_work_time", NumberFormat.doubleFormatStr(Double.valueOf(record.getStr("total_work_time"))));
                allWorkTime += Double.valueOf(record.getStr("total_work_time"));
                allSalary += Double.valueOf(record.getStr("total"));
            }
            jhm.put("allWorkTime", NumberFormat.doubleFormatStr(allWorkTime));
            jhm.put("allSalary", NumberFormat.doubleFormatStr(allSalary));
            jhm.put("data", workTimeList);
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }





}
