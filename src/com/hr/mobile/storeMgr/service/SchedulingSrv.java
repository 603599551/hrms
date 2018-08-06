package com.hr.mobile.storeMgr.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.Map;

public class SchedulingSrv extends BaseService {
    /*
      增加事务
       */
    @Before(Tx.class)
    public JsonHashMap set(Map paraMap) {

        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = (UserSessionUtil) paraMap.get("usu");
        String staffId = (String) paraMap.get("staff_id");
        String date = (String) paraMap.get("date");
        String time = (String) paraMap.get("time");
        String status = (String) paraMap.get("status");
        //json字符串转成json数组
        JSONArray timeArray = JSONArray.fromObject(time);
        JSONObject timeObject = timeArray.getJSONObject(0);
        String startTime = (String) timeObject.get("start");
        String endTime = (String) timeObject.get("end");

        String sql = "select * from h_work_time_detail d where d.staff_id=? and d.date =? and d.start_time=? and d.end_time=?";
        Record record = Db.findFirst(sql, staffId, date, startTime, endTime);
        //  查询h_work_time表获得work_time_id字段的值
        String sqlId = "select * from h_work_time t where t.staff_id=? and t.date=? ";
        Record recordTime = Db.findFirst(sqlId, staffId, date);
        //status为2减班
        if (StringUtils.equals(status, "2")) {
            if (record == null || StringUtils.equals(record.getStr("status"), "2")) {
                jhm.putCode(0).putMessage("您当前没有排班，无法执行减班操作！");
                return jhm;
            } else {
                if (recordTime == null || StringUtils.equals(recordTime.getStr("real_number"), "0")) {
                    jhm.putCode(0).putMessage("您当前没有排班，无法执行减班操作！");
                    return jhm;
                } else {
                    if(StringUtils.equals(record.getStr("status"),"3")){
                        //先h_work_time_detail中删除
                      boolean b=  Db.deleteById("h_work_time_detail",record.getStr("id"));
                      b=false;
                    }else{
                        record.set("status", "2");
                        Db.update("h_work_time_detail", record);
                    }
                    String realNumber = (int)(recordTime.getInt("real_number") - 1) + "";
                    recordTime.set("real_number", realNumber);
                    Db.update("h_work_time", recordTime);
//                    可以减班，update h_work_time_detail表status为2 update h_work_time表real_num减一
                }
                jhm.putMessage("提交成功！");
            }
        }
        else if (StringUtils.equals(status, "3")) {
                String workTimeId = "";
                String storeId = "";
                if(record !=null && !StringUtils.equals(record.getStr("status"),"2")){
//                    有班不能加班
                    jhm.putCode(0).putMessage("您当前已经排班，无法执行加班操作!");
                    return jhm;

                }else {
                    if(recordTime==null){
//                        save()h_work_time_detail表增加一条记录 number=0 real_number=1
                        //如果员工当天没有排班，加班时向h_work_time表添加一条记录
                        String id = UUIDTool.getUUID();
                        String sqlStore = "select dept_id from h_staff where h_staff.id=?";
                        Record r = Db.findFirst(sqlStore, staffId);
                        if (r == null) {
                            jhm.putCode(0).putMessage("员工不存在！");
                        } else {
                            //workTimeId、storeId 是在h_work_time_detail表添加记录时所需的
                            workTimeId = id;
                            storeId = r.getStr("dept_id");
                            Record recordTimeAdd = new Record();
                            recordTimeAdd.set("id", id);
                            recordTimeAdd.set("staff_id", staffId);
                            recordTimeAdd.set("store_id", storeId);
                            recordTimeAdd.set("date", date);
                            recordTimeAdd.set("number", "0");
                            recordTimeAdd.set("real_number", "1");
                            Db.save("h_work_time", recordTimeAdd);
                        }
                    }else{
//                        update h_work_time_表real_number+1
                        String realNumber = (recordTime.getInt("real_number") + 1) + "";
                        recordTime.set("real_number", realNumber);
                        Db.update("h_work_time",recordTime);
                        workTimeId = recordTime.getStr("id");
                        storeId = recordTime.getStr("store_id");
                    }
                    if(record==null){
//                        save()h_work_time_detail表增加一条记录
                        String id = UUIDTool.getUUID();
                        String dateTime = DateTool.GetDateTime();
                        Record recordAdd = new Record();
                        recordAdd.set("id", id);
                        recordAdd.set("work_time_id", workTimeId);
                        recordAdd.set("staff_id", staffId);
                        recordAdd.set("store_id", storeId);
                        recordAdd.set("date", date);
                        recordAdd.set("start_time", startTime);
                        recordAdd.set("end_time", endTime);
                        recordAdd.set("status", status);
                        recordAdd.set("creater_id", usu.getUserId());
                        recordAdd.set("create_time", dateTime);
                        recordAdd.set("modifier_id", usu.getUserId());
                        recordAdd.set("modify_time", dateTime);
                        recordAdd.set("signin_status", "1");
                        recordAdd.set("signout_status", "1");
                        Db.save("h_work_time_detail", recordAdd);
                    }
                    else {
                       //把h_work_time_detail表update status为1
                        //先减班，再加班，把h_work_time_detail表status字段改成正常上班
                        record.set("status","1");
                        Db.update("h_work_time_detail",record);
                    }
                    jhm.putMessage("提交成功！");
                }

            }

        return jhm;
    }
}
