package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkTimeDetailCtrl extends BaseCtrl {

//    public void getWorkTimeDetail(){
//        JsonHashMap jhm = new JsonHashMap();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
//        //工号
////        String emp_num = getPara("emp_num");
//        String emp_num = getPara("keyword");
//        String startDate = getPara("start_date");
//        String endDate = getPara("end_date");
//
//        List<String> params = new ArrayList<>();
//        Record staffR = new Record();
//        List <Record> staffList = new ArrayList<>();
//        List<Record> recordList = new ArrayList<>();
//        List<Record> maxList = new ArrayList<>();
//        List <Record> resultList = new ArrayList<>();
//        StringBuilder staffSql = new StringBuilder("SELECT s.store_color AS store_color, s.name name, hs.name staff_name, SUM(wt.real_number * 0.25) total_work_time, hs.hour_wage, SUM(0.25 * wt.real_number * hs.hour_wage ) total FROM h_work_time wt, h_store s, h_staff hs WHERE wt.store_id = s.id AND wt.staff_id = hs.id");
//        StringBuilder workDetailSearch = new StringBuilder("SELECT c.date AS date, c.is_leave , c.start_time AS sb_time, c.end_time AS xb_time, c.sign_in_time AS sb_dk, c.sign_back_time AS xb_dk, hs.hour_wage AS hour_wage, ( SELECT CASE c.is_leave WHEN '1' THEN ( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '8000' ) ELSE ( CASE ISNULL(c.sign_in_time) WHEN TRUE THEN ( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '9000' ) ELSE ( CASE c.is_late WHEN '1' THEN ( CASE c.is_leave_early WHEN '1' THEN ( SELECT NAME FROM h_dictionary WHERE VALUE = '1' AND parent_id = '7000' ) WHEN '2' THEN ( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '7000' ) END ) ELSE ( CASE c.is_leave_early WHEN '1' THEN ( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '6000' ) ELSE ( CONCAT(( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '6000' ), \"/\", ( SELECT NAME FROM h_dictionary WHERE VALUE = '2' AND parent_id = '7000' ))) END ) END ) END ) END ) status_text FROM h_staff_clock c, h_staff hs WHERE hs.id = c.staff_id AND ( c.STATUS = 2 or (c.status = 0 and c.is_leave = 1)) ");
//        StringBuilder sql = new StringBuilder("SELECT wtd.date AS date, wtd.start_time AS sb_time, wtd.start_time AS sb_dk, wtd.end_time AS xb_time, wtd.end_time AS xb_dk, ( CASE wtd. STATUS WHEN '2' THEN ( SELECT NAME FROM h_dictionary WHERE id = '5020' AND parent_id = '5000' ) WHEN '3' THEN ( SELECT NAME FROM h_dictionary WHERE id = '5030' AND parent_id = '5000' ) END ) AS status_text FROM h_work_time_detail wtd, h_staff hs WHERE hs.id = wtd.staff_id AND ( wtd. STATUS = '2' OR wtd. STATUS = '3' ) ");
//        if(StringUtils.isEmpty(emp_num)){
//            jhm.putCode(1);
//            jhm.put("staff", staffR);
//            jhm.put("data", staffList);
//            renderJson(jhm);
//            return;
//        } else {
//            staffSql.append(" AND hs.emp_num = ? ");
//            workDetailSearch.append(" AND c.staff_id = ( SELECT id FROM h_staff WHERE emp_num = ? ) ");
//            sql.append(" AND wtd.staff_id = ( SELECT id FROM h_staff WHERE emp_num = ? ) ");
//            params.add(emp_num);
//        }
//        if(StringUtils.isEmpty(startDate)){
//            jhm.putCode(1);
//            jhm.put("staff", staffR);
//            jhm.put("data", staffList);
//            renderJson(jhm);
//            return;
//        } else {
//            staffSql.append(" AND wt.date >= ? ");
//            workDetailSearch.append(" AND c.date >= ? ");
//            sql.append(" AND wtd.date >= ? ");
//            params.add(startDate);
//        }
//        if(StringUtils.isEmpty(endDate)){
//            jhm.putCode(1);
//            jhm.put("staff", staffR);
//            jhm.put("data", staffList);
//            renderJson(jhm);
//            return;
//        } else {
//            staffSql.append(" AND wt.date <= ? ");
//            workDetailSearch.append(" AND c.date <= ? ");
//            sql.append(" AND wtd.date <= ? ");
//            params.add(endDate);
//        }
//
//        try {
//            String staffSearch = "select count(*) as c from h_staff where emp_num = ? ";
//            Record record = Db.findFirst(staffSearch, emp_num);
//            if(record.getInt("c") > 0){
//                staffR = Db.findFirst(staffSql.toString(), params.toArray());
//
//                //查出正常排班时间内的工资情况
//                workDetailSearch.append(" order by c.date and c.start_time");
//                staffList = Db.find(workDetailSearch.toString(), params.toArray());
//                Double workTime = 0D;
//                Long workTimeNumber = 0L;
//                Long standardTime = new Long(15 * 60 * 1000);
//                Double fifthMinutes = 0.25;
//
//                for(int i = 0; i < staffList.size(); i++){
//                    if(StringUtils.equals(staffList.get(i).getStr("is_leave"), "1") && StringUtils.isEmpty(staffList.get(i).getStr("sb_dk"))){
//                        staffList.get(i).remove("is_leave");
//                        staffList.get(i).set("work_time", "0");
//                        staffList.get(i).set("total", "0");
//                        staffList.get(i).set("status_color","warning");
//                        continue;
//                    }
//                    Long sbTime = simpleDateFormat.parse(staffList.get(i).getStr("sb_time")).getTime();
//                    Long sbDk = simpleDateFormat.parse(staffList.get(i).getStr("sb_dk")).getTime();
//                    Long xbTime =simpleDateFormat.parse(staffList.get(i).getStr("xb_time")).getTime();
//                    Long xbDk = simpleDateFormat.parse(staffList.get(i).getStr("xb_dk")).getTime();
//                    Long sb;
//                    Long xb;
//
//                    //算出实际工作时间
//
//                    if(sbTime >= sbDk){
//                        if(xbDk >= xbTime){
//                            workTimeNumber = (xbTime - sbTime) / standardTime ;
//                        } else {
//                            xb = (xbTime - xbDk) / standardTime +1L;
//                            workTimeNumber = (xbTime - sbTime) / standardTime -xb;
//                        }
//                    } else {
//                        sb = (sbDk - sbTime) / standardTime + 1L;
//                        if(xbDk >= xbTime){
//                            workTimeNumber = (xbTime - sbDk) / standardTime - sb;
//                        } else {
//                            if(xbTime - sbTime > standardTime){
//                                xb = (xbTime - xbDk)/standardTime + 1L;
//                            } else {
//                                xb = 0L;
//                            }
//                            workTimeNumber = (xbTime - sbTime) / standardTime - sb - xb;
//                        }
//                    }
//                    workTime = workTimeNumber * fifthMinutes;
//                    staffList.get(i).set("work_time", workTime);
//                    staffList.get(i).set("total", (workTime * Double.valueOf(staffR.getStr("hour_wage"))));
//                    if(!StringUtils.equals("正常", staffList.get(i).getStr("status_text"))){
//                        staffList.get(i).set("status_color", "warning");
//                    } else {
//                        staffList.get(i).set("status_color", "");
//                    }
//                }
//
//                //查出加班和减班的工资状态
//                sql.append(" order by wtd.date, wtd.start_time");
//                recordList = Db.find(sql.toString(), params.toArray());
//                String startTime = "";
//                int count = 0;
//                for(int i = 0; i < recordList.size(); i++){
////                    Long sbTime = simpleDateFormat.parse(recordList.get(i).getStr("sb_time")).getTime();
////                    Long xbTime =simpleDateFormat.parse(recordList.get(i).getStr("xb_time")).getTime();
////                    workTimeNumber = (xbTime - sbTime) / standardTime;
////                    workTime = workTimeNumber * fifthMinutes;
////                    if(i != recordList.size()-1){
////                        if(StringUtils.equals(recordList.get(i).getStr("xb_time"), recordList.get(i+1).getStr("sb_time"))){
////                            startTime = recordList.get(i).getStr("sb_time");
////                        } else {
////                            recordList.get(i).set("sb_time", startTime);
////                            maxList.add(recordList.get(i));
////                        }
////
////                    }
//                    if(i != recordList.size()-1){
//                        if(count == 0){
//                            if(StringUtils.equals(recordList.get(i).getStr("xb_time"), recordList.get(i+1).getStr("sb_time"))){
//                                startTime = recordList.get(i).getStr("sb_time");
//                                count++;
//                            } else {
//                                recordList.get(i).set("work_time", fifthMinutes);
//                                recordList.get(i).set("total", String.valueOf(fifthMinutes * Double.valueOf(staffR.getStr("hour_wage"))));
//                                recordList.get(i).set("status_color", "");
//                                maxList.add(recordList.get(i));
//                            }
//                        } else {
//                            if(!StringUtils.equals(recordList.get(i).getStr("xb_time"), recordList.get(i+1).getStr("sb_time"))){
//                                recordList.get(i).set("sb_time", startTime);
//                                recordList.get(i).set("sb_dk", startTime + ":00");
//                                recordList.get(i).set("xb_dk", recordList.get(i).getStr("xb_dk") + ":00");
//                                recordList.get(i).set("work_time", String.valueOf((1+count)* fifthMinutes));
//                                recordList.get(i).set("total", String.valueOf((((1+count)* fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage")))));
//                                recordList.get(i).set("status_color", "");
//                                maxList.add(recordList.get(i));
//                                count = 0;
//                            } else {
//                                count++;
//                            }
//                        }
//                    } else {
//                        if(count == 0){
//                            recordList.get(i).set("work_time", fifthMinutes);
//                            recordList.get(i).set("total", String.valueOf(fifthMinutes * Double.valueOf(staffR.getStr("hour_wage"))));
//                            recordList.get(i).set("status_color", "");
//                            maxList.add(recordList.get(i));
//                        } else {
//                            recordList.get(i).set("sb_time", startTime);
//                            recordList.get(i).set("sb_dk", startTime + ":00");
//                            recordList.get(i).set("xb_dk", recordList.get(i).getStr("xb_dk") + ":00");
//                            recordList.get(i).set("work_time", String.valueOf((1+count)* fifthMinutes));
//                            recordList.get(i).set("total", String.valueOf((((1+count)* fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage")))));
//                            recordList.get(i).set("status_color", "");
//                            maxList.add(recordList.get(i));
//                        }
//                    }
////                    recordList.get(i).set("work_time", workTime);
////                    recordList.get(i).set("total", (workTime * Double.valueOf(staffR.getStr("hour_wage"))));
////                    recordList.get(i).set("status", "");
//                }
//
//                resultList.addAll(staffList);
//                resultList.addAll(maxList);
//
//                //重写sort方法对resultList进行排序
//                Collections.sort(resultList, new Comparator<Record>() {
//                    @Override
//                    public int compare(Record record, Record t1) {
//                        return record.getStr("date").compareTo(t1.getStr("date"));
//                    }
//                });
//                jhm.putCode(1);
//                jhm.put("staff", staffR);
//                jhm.put("data", resultList);
//            } else {
//                jhm.putCode(0).putMessage("该员工不存在！");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage("服务器发生异常！");
//        }
//        renderJson(jhm);
//    }

    public void getWorkTimeDetail(){
        JsonHashMap jhm = new JsonHashMap();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        //门店id
        String dept = getPara("dept");
        //开始日期
        String startDate = getPara("start_date");
        //结束日期
        String endDate = getPara("end_date");
        //员工工号
        String empNum = getPara("emp_num");

        StringBuilder staffSearch = new StringBuilder("SELECT s.store_color AS store_color, s.name name, hs.name staff_name, SUM(wt.real_number * 0.25) total_work_time, hs.hour_wage, SUM(0.25 * wt.real_number * hs.hour_wage ) total FROM h_work_time wt, h_store s, h_staff hs WHERE wt.store_id = s.id AND wt.staff_id = hs.id");
        StringBuilder workSearch = new StringBuilder("SELECT c.date, c.start_time as sb_time, c.end_time as xb_time, c.sign_in_time as sb_dk, c.sign_back_time as xb_dk, c.is_leave, c.status from h_staff hs, h_staff_clock c where hs.id = c.staff_id");
        StringBuilder workDetailSearch = new StringBuilder("SELECT wtd.date as date, wtd.start_time, wtd.end_time, wtd.`status` from h_work_time_detail wtd, h_staff hs where hs.id = wtd.staff_id");
        //数字对应字典值表id---1:id = 7010, 2:id = 7020, 3:id = 7030, 4:id = 7050, 5:id = 8010, 6:id = 8020, 7:id = 8030, 8:id = 8040
        String dictionarySearch = "select d.status_color as status_color, name1.name as '1', name2.name as '2', name3.name as '3', name4.name as '4', name5.name as '5', name6.name as '6', name7.name as '7', name8.name as '8' from (SELECT name from h_dictionary where id = '7010')name1 ,(SELECT name from h_dictionary where id = '7020')name2, (SELECT name from h_dictionary where id = '7030')name3, (SELECT name from h_dictionary where id = '7040')name4, (SELECT name from h_dictionary where id = '8010')name5, (SELECT name from h_dictionary where id = '8020')name6, (SELECT name from h_dictionary where id = '8030')name7, (SELECT name from h_dictionary where id = '8040')name8, (select status_color from h_dictionary where id = '1120')d ";

        List<String> params = new ArrayList<>();
        Record staffR = new Record();
        Record dictionaryR = new Record();
        List <Record> workList = new ArrayList<>();
        List<Record> workDetailList = new ArrayList<>();
        List <Record> resultList = new ArrayList<>();
        List<Record> staffList = new ArrayList<>();

        if(StringUtils.isEmpty(dept)){
            jhm.putCode(1);
            jhm.put("staff", staffR);
            jhm.put("data", resultList);
            jhm.put("staffList",staffList);
            renderJson(jhm);
            return;
        }

        if(StringUtils.isEmpty(empNum)){
            if(!StringUtils.isEmpty(dept)){
                staffList = Db.find("select name, emp_num from h_staff where dept_id = ?",dept);
            }
            jhm.putCode(1);
            jhm.put("staff", staffR);
            jhm.put("data", resultList);
            jhm.put("staffList",staffList);
            renderJson(jhm);
            return;
        } else {
            staffSearch.append(" AND hs.emp_num = ? ");
            workSearch.append(" AND c.staff_id = ( SELECT id FROM h_staff WHERE emp_num = ? ) ");
            workDetailSearch.append(" AND wtd.staff_id = ( SELECT id FROM h_staff WHERE emp_num = ? ) ");
            params.add(empNum);
        }
        if(StringUtils.isEmpty(startDate)){
            jhm.putCode(1);
            jhm.put("staff", staffR);
            jhm.put("data", resultList);
            jhm.put("staffList",staffList);
            renderJson(jhm);
            return;
        } else {
            staffSearch.append(" AND wt.date >= ? ");
            workSearch.append(" AND c.date >= ? ");
            workDetailSearch.append(" AND wtd.date >= ? ");
            params.add(startDate);
        }
        if(StringUtils.isEmpty(endDate)){
            jhm.putCode(1);
            jhm.put("staff", staffR);
            jhm.put("data", resultList);
            jhm.put("staffList",staffList);
            renderJson(jhm);
            return;
        } else {
            staffSearch.append(" AND wt.date <= ? ");
            workSearch.append(" AND c.date <= ? ");
            workDetailSearch.append(" AND wtd.date <= ? ");
            params.add(endDate);
        }

        try {
            String sql ="select count(*) as c from h_staff where emp_num = ?";
            Record record = Db.findFirst(sql, empNum);
            if(record.getInt("c") > 0 ){
                //15分钟毫秒数
                Long standardTime = 15 * 60 * 1000L;
                Double fifthMinutes = 0.25;

                dictionaryR = Db.findFirst(dictionarySearch.toString());
                staffR = Db.findFirst(staffSearch.toString(), params.toArray());
                workSearch.append(" order by c.date, start_time ");
                workList = Db.find(workSearch.toString(), params.toArray());
                workDetailSearch.append(" order by wtd.date, start_time");
                workDetailList = Db.find(workDetailSearch.toString(), params.toArray());

                //遍历workList
                for(int i = 0; i < workList.size(); i++){
                    if(StringUtils.equals("0", workList.get(i).getStr("is_leave"))){
                        if(StringUtils.equals("0", workList.get(i).getStr("status"))){
                            workList.get(i).set("sb_dk", "");
                            workList.get(i).set("xb_dk", "");
                            workList.get(i).set("work_time", "0");
                            workList.get(i).set("total", "0");
                            workList.get(i).set("status_text", dictionaryR.getStr("8"));
                            workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                            workList.get(i).remove("is_leave");
                        } else if(StringUtils.equals("1", workList.get(i).getStr("status"))){
                            workList.get(i).set("xb_dk", "");
                            workList.get(i).set("work_time", "0");
                            workList.get(i).set("total", "0");
                            workList.get(i).set("status_text", dictionaryR.getStr("6"));
                            workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                            workList.get(i).remove("is_leave");
                        } else {
                            Long sbTime = simpleDateFormat.parse(workList.get(i).getStr("sb_time")).getTime();
                            Long sbDk = simpleDateFormat.parse(workList.get(i).getStr("sb_dk")).getTime();
                            Long xbTime =simpleDateFormat.parse(workList.get(i).getStr("xb_time")).getTime();
                            Long xbDk = simpleDateFormat.parse(workList.get(i).getStr("xb_dk")).getTime();
                            Long workTimeNumber = 0L;
                            Long sb;
                            Long xb;

                            if(sbTime >= sbDk){
                                if(xbDk >= xbTime){
                                    workTimeNumber = (xbTime - sbTime) / standardTime;
                                    workList.get(i).set("work_time", String.valueOf(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", String.valueOf((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("1"));
                                    workList.get(i).set("status_color", "");
                                    workList.get(i).remove("is_leave");
                                } else {
                                    xb = (xbTime - xbDk) / standardTime +1L;
                                    workTimeNumber = (xbTime - sbTime) / standardTime -xb;
                                    workList.get(i).set("work_time", String.valueOf(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", String.valueOf((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("6"));
                                    workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                                    workList.get(i).remove("is_leave");
                                }
                            } else {
                                sb = (sbDk - sbTime) / standardTime + 1L;
                                if(xbDk >= xbTime){
                                    workTimeNumber = (xbTime - sbTime) / standardTime - sb;
                                    workList.get(i).set("work_time", String.valueOf(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", String.valueOf((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("5"));
                                    workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                                    workList.get(i).remove("is_leave");
                                } else {
                                    if(xbTime - sbTime > standardTime){
                                        xb = (xbTime - xbDk)/standardTime + 1L;
                                    } else {
                                        xb = 0L;
                                    }
                                    workTimeNumber = (xbTime - sbTime) / standardTime - sb - xb;
                                    workList.get(i).set("work_time", String.valueOf(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", String.valueOf((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("7"));
                                    workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                                    workList.get(i).remove("is_leave");
                                }
                            }
                        }
                    } else {
                        //请假的情况
                        int count = 0;
                        for(int j = 0; j < workDetailList.size(); j++){
                            if(!StringUtils.equals(workDetailList.get(j).getStr("date"), workList.get(i).getStr("date"))){
                                continue;
                            }
                            Date start = simpleDateFormat.parse(workList.get(i).getStr("sb_time"));
                            Date end = simpleDateFormat.parse(workList.get(i).getStr("xb_time"));
                            Date startTime = simpleDateFormat.parse(workDetailList.get(j).getStr("start_time"));
                            Date endTime = simpleDateFormat.parse(workDetailList.get(j).getStr("end_time"));
//                            if(startTime.getTime() > start.getTime()){
//                                continue;
//                            } else if(endTime.getTime() < end.getTime()){
//                                continue;
//                            } else {
//                                if(StringUtils.equals("1", workDetailList.get(i).getStr("status"))){
//                                    count++;
//                                }
//                            }
                            if(startTime.getTime() >= start.getTime() && endTime.getTime() <= end.getTime()){
                                if(StringUtils.equals("1", workDetailList.get(j).getStr("status"))){
                                    count++;
                                }
                            } else {
                                continue;
                            }
                        }
                        workList.get(i).set("work_time", String.valueOf(count * fifthMinutes));
                        workList.get(i).set("total", String.valueOf((count * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                        workList.get(i).set("status_text", dictionaryR.getStr("4"));
                        workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                        workList.get(i).remove("is_leave");
                    }
                }


                //遍历workDetailList
                int count = 0;
                String begin = "";
                for(int i = 0; i < workDetailList.size(); i++){
//                    if(i != workDetailList.size() - 1){
                    if(!StringUtils.equals("2", workDetailList.get(i).getStr("status")) && !StringUtils.equals("3", workDetailList.get(i).getStr("status"))){
                        continue;
                    } else {
                        if(count == 0){
                            if(i != workDetailList.size() - 1 && StringUtils.equals(workDetailList.get(i).getStr("status"), workDetailList.get(i + 1).getStr("status")) && StringUtils.equals(workDetailList.get(i).getStr("end_time"), workDetailList.get(i + 1).getStr("start_time"))){
                                begin = workDetailList.get(i).getStr("start_time");
                                count++;
                            } else {
                                workDetailList.get(i).set("sb_time", workDetailList.get(i).getStr("start_time"));
                                workDetailList.get(i).set("xb_time", workDetailList.get(i).getStr("end_time"));
                                workDetailList.get(i).set("work_time", String.valueOf(fifthMinutes));
                                workDetailList.get(i).set("total", String.valueOf(fifthMinutes * Double.valueOf(staffR.getStr("hour_wage"))));
                                workDetailList.get(i).set("sb_dk", "");
                                workDetailList.get(i).set("xb_dk", "");
                                workDetailList.get(i).set("status_color", "");
                                if(StringUtils.equals(workDetailList.get(i).getStr("status"),"2")){
                                    workDetailList.get(i).set("status_text", dictionaryR.getStr("2"));
                                }
                                if(StringUtils.equals(workDetailList.get(i).getStr("status"),"3")){
                                    workDetailList.get(i).set("status_text", dictionaryR.getStr("3"));
                                }
                                workDetailList.get(i).remove("start_time");
                                workDetailList.get(i).remove("end_time");
                                resultList.add(workDetailList.get(i));
                            }
                        } else {
                            if(i != workDetailList.size() - 1 && StringUtils.equals(workDetailList.get(i).getStr("status"), workDetailList.get(i + 1).getStr("status")) && StringUtils.equals(workDetailList.get(i).getStr("end_time"), workDetailList.get(i + 1).getStr("start_time"))){
                                count++;
                            } else {
                                workDetailList.get(i).set("sb_time", begin);
                                workDetailList.get(i).set("xb_time", workDetailList.get(i).getStr("end_time"));
                                workDetailList.get(i).set("sb_dk", "");
                                workDetailList.get(i).set("xb_dk", "");
                                workDetailList.get(i).set("status_color", "");
                                if(StringUtils.equals(workDetailList.get(i).getStr("status"),"2")){
                                    workDetailList.get(i).set("status_text", dictionaryR.getStr("2"));
//                                    workDetailList.get(i).set("total", String.valueOf(-((count + 1) * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workDetailList.get(i).set("work_time", "0");
                                    workDetailList.get(i).set("total", "0");
                                }
                                if(StringUtils.equals(workDetailList.get(i).getStr("status"),"3")){
                                    workDetailList.get(i).set("work_time", String.valueOf((count + 1) * fifthMinutes));
                                    workDetailList.get(i).set("status_text", dictionaryR.getStr("3"));
                                    workDetailList.get(i).set("total",  String.valueOf(((count + 1) * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                }
                                workDetailList.get(i).remove("start_time");
                                workDetailList.get(i).remove("end_time");
                                resultList.add(workDetailList.get(i));
                                count = 0;
                            }
                        }
                    }
                }

                //将两个List合并重写排序方法排序
                resultList.addAll(workList);

                Collections.sort(resultList, new Comparator<Record>() {
                    @Override
                    public int compare(Record record, Record t1) {
                        return record.getStr("date").compareTo(t1.getStr("date"));
                    }
                });

                if(!StringUtils.isEmpty(dept)){
                    staffList = Db.find("select name , emp_num from h_staff where dept_id = ?",dept);
                }
                jhm.put("staffList",staffList);

                jhm.putCode(1);
                jhm.put("staff", staffR);
                jhm.put("data", resultList);

            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
