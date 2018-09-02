package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.NumberFormat;
import utils.bean.JsonHashMap;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkTimeDetailCtrl extends BaseCtrl {

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
        String empNum = getPara("keyword");

        StringBuilder staffSearch = new StringBuilder("SELECT s.store_color AS store_color, s.name name, hs.name staff_name, SUM(wt.real_number * 0.25) total_work_time, hs.hour_wage, SUM(0.25 * wt.real_number * hs.hour_wage ) total FROM h_work_time wt, h_store s, h_staff hs WHERE wt.store_id = s.id AND wt.staff_id = hs.id");
        StringBuilder workSearch = new StringBuilder("SELECT c.date, c.start_time as sb_time, c.end_time as xb_time, c.sign_in_time as sb_dk, c.sign_back_time as xb_dk, c.is_leave, c.status from h_staff hs, h_staff_clock c where hs.id = c.staff_id");
        StringBuilder workDetailSearch = new StringBuilder("SELECT wtd.date as date, wtd.start_time, wtd.end_time, wtd.`status` from h_work_time_detail wtd, h_staff hs where hs.id = wtd.staff_id");
        //数字对应字典值表id---1:id = 7010, 2:id = 7020, 3:id = 7030, 4:id = 7050, 5:id = 8010, 6:id = 8020, 7:id = 8030, 8:id = 8040
        String dictionarySearch = "select d.status_color as status_color, name1.name as '1', name2.name as '2', name3.name as '3', name4.name as '4', name5.name as '5', name6.name as '6', name7.name as '7', name8.name as '8' from (SELECT name from h_dictionary where id = '7010')name1 ,(SELECT name from h_dictionary where id = '7020')name2, (SELECT name from h_dictionary where id = '7030')name3, (SELECT name from h_dictionary where id = '7040')name4, (SELECT name from h_dictionary where id = '8010')name5, (SELECT name from h_dictionary where id = '8020')name6, (SELECT name from h_dictionary where id = '8030')name7, (SELECT name from h_dictionary where id = '8040')name8, (select status_color from h_dictionary where id = '1120')d ";

        List<String> params = new ArrayList<>();
        Record staffR = new Record();
        Record dictionaryR = new Record();
        //装staff_clock表的数据
        List <Record> workList = new ArrayList<>();
        //装h_work_time_detail表的数据
        List<Record> workDetailList = new ArrayList<>();
        //将处理后的staff_clock表数据和h_work_time_detail表的数据进行整合排序
        List <Record> resultList = new ArrayList<>();
        //门店员工姓名工号列表
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
                staffList = Db.find("select name, emp_num as value from h_staff where dept_id = ?",dept);
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
                staffR.set("total", NumberFormat.doubleFormatStr(Double.valueOf(staffR.getStr("total"))));
                staffR.set("total_work_time", NumberFormat.doubleFormatStr(Double.valueOf(staffR.getStr("total_work_time"))));
                staffR.set("hour_wage", NumberFormat.doubleFormatStr(Double.valueOf(staffR.getStr("hour_wage"))));
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
                            workList.get(i).set("work_time", "0.00");
                            workList.get(i).set("total", "0.00");
                            workList.get(i).set("status_text", dictionaryR.getStr("8"));
                            workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                            workList.get(i).remove("is_leave");
                        } else if(StringUtils.equals("1", workList.get(i).getStr("status"))){
                            workList.get(i).set("xb_dk", "");
                            workList.get(i).set("work_time", "0.00");
                            workList.get(i).set("total", "0.00");
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
                                    workList.get(i).set("work_time", NumberFormat.doubleFormatStr(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", NumberFormat.doubleFormatStr((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("1"));
                                    workList.get(i).set("status_color", "");
                                    workList.get(i).remove("is_leave");
                                } else {
                                    xb = (xbTime - xbDk) / standardTime +1L;
                                    workTimeNumber = (xbTime - sbTime) / standardTime -xb;
                                    workList.get(i).set("work_time", NumberFormat.doubleFormatStr(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", NumberFormat.doubleFormatStr((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                                    workList.get(i).set("status_text", dictionaryR.getStr("6"));
                                    workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                                    workList.get(i).remove("is_leave");
                                }
                            } else {
                                sb = (sbDk - sbTime) / standardTime + 1L;
                                if(xbDk >= xbTime){
                                    workTimeNumber = (xbTime - sbTime) / standardTime - sb;
                                    workList.get(i).set("work_time", NumberFormat.doubleFormatStr(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", NumberFormat.doubleFormatStr((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
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
                                    workList.get(i).set("work_time", NumberFormat.doubleFormatStr(workTimeNumber * fifthMinutes));
                                    workList.get(i).set("total", NumberFormat.doubleFormatStr((workTimeNumber * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
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
                        workList.get(i).set("work_time", NumberFormat.doubleFormatStr(count * fifthMinutes));
                        workList.get(i).set("total", NumberFormat.doubleFormatStr((count * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
                        workList.get(i).set("status_text", dictionaryR.getStr("4"));
                        workList.get(i).set("status_color", dictionaryR.getStr("status_color"));
                        workList.get(i).remove("is_leave");
                    }
                }


                //遍历workDetailList,处理加班减班情况
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
                                workDetailList.get(i).set("work_time",NumberFormat.doubleFormatStr(fifthMinutes));
                                workDetailList.get(i).set("total", NumberFormat.doubleFormatStr(fifthMinutes * Double.valueOf(staffR.getStr("hour_wage"))));
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
                                    workDetailList.get(i).set("work_time", "0.00");
                                    workDetailList.get(i).set("total", "0.00");
                                }
                                if(StringUtils.equals(workDetailList.get(i).getStr("status"),"3")){
                                    workDetailList.get(i).set("work_time", NumberFormat.doubleFormatStr((count + 1) * fifthMinutes));
                                    workDetailList.get(i).set("status_text", dictionaryR.getStr("3"));
                                    workDetailList.get(i).set("total",  NumberFormat.doubleFormatStr(((count + 1) * fifthMinutes) * Double.valueOf(staffR.getStr("hour_wage"))));
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
                    staffList = Db.find("select name , emp_num as value from h_staff where dept_id = ?",dept);
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
