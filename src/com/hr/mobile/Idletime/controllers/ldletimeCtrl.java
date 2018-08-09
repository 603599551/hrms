package com.hr.mobile.Idletime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.ContentTransformationUtil;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ldletimeCtrl extends BaseCtrl {
    /*
        添加闲时
         */
    public void add() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String userId = usu.getUserId();

        Record staffRecord = Db.findFirst("select * from h_staff where id = ?", userId);
//        String deptId = staffRecord.getStr("username");

        //为空判断
        if (staffRecord == null) {
            jhm.putCode(0).putMessage("找不到该员工！");
            renderJson(jhm);
            return;
        }
        String kind = staffRecord.getStr("kind");
        String staffId = getPara("id");
        String date = getPara("date");
        String time = getPara("time");
        String datetime = DateTool.GetDateTime();

        //判断有没有这条信息
        Record recordSelect = Db.findFirst("SELECT time.id as id, time.modify_time as modify_time, time.modifier_id as modifier_id , time.app_content as app_content FROM h_staff_idle_time time WHERE time.staff_id = ?  AND date = ? ", staffId, date);
        if (recordSelect == null) {
            Record record = new Record();
            record.set("id", UUIDTool.getUUID());
            record.set("store_id", usu.getUserBean().getDeptId());
            record.set("staff_id", staffId);
            record.set("date", date);
            record.set("app_content", time);
            record.set("create_time", datetime);
            record.set("creater_id", userId);
            record.set("modify_time", datetime);
            record.set("modifier_id", userId);
            record.set("kind", kind);
            record.set("content", AppToPcIdleTime(time));

            try {
                boolean flag = Db.save("h_staff_idle_time", record);
                if (flag) {
                    jhm.putCode(1).putMessage("保存成功！");
                } else {
                    jhm.putCode(0).putMessage("保存失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        } else {
            recordSelect.set("modify_time", datetime);
            recordSelect.set("modifier_id", userId);
            recordSelect.set("app_content", time);
            recordSelect.set("content", AppToPcIdleTime(time));

            try {
                boolean flag = Db.update("h_staff_idle_time", recordSelect);
                if (flag) {
                    jhm.putCode(1).putMessage("保存成功！");
                } else {
                    jhm.putCode(0).putMessage("保存失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        }

        renderJson(jhm);
    }


    public void showDetailByStaffIdAndDate() {
        JsonHashMap jhm = new JsonHashMap();
        JSONArray jsonArray = new JSONArray();

        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("未选择员工");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("未选择日期");
            renderJson(jhm);
            return;
        }

        String sql = "SELECT time.staff_id as staff_id , time.date as date , time.app_content as app_content FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ? ";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            //验证员工是否存在
            Record staffRecord = Db.findFirst("select * from h_staff where id = ?", id);
            if (staffRecord == null) {
                jhm.putCode(0).putMessage("找不到该员工！");
                renderJson(jhm);
                return;
            }

            //之前的七天
            Date nowDate = sdf.parse(date);
            calendar.setTime(nowDate);
            calendar.add(Calendar.DAY_OF_MONTH, -7);

            String temp = sdf.format(calendar.getTime());

            //寻找七天之前的闲时
            Record record = Db.findFirst(sql, id, sdf.format(calendar.getTime()));
            //寻找当天闲时  用于判断状态值
            Record statusRecord = Db.findFirst("SELECT time.staff_id as staff_id , time.date as date , time.app_content as app_content FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ? ", id, date);
//            if (record != null) {
            if (statusRecord != null) {
                if(statusRecord.getStr("app_content").length() <= 2){
                    jhm.putCode(0).putMessage("未录入闲时！");
                }else {
                    jhm.put("status", "1");
                    jhm.put("staff_id", statusRecord.getStr("staff_id"));
                    jhm.put("date", statusRecord.getStr("date"));
                    jsonArray = JSONArray.fromObject(statusRecord.getStr("app_content"));
                    jhm.put("list", jsonArray);
                }
            } else {
                if (record != null && record.getStr("app_content").length() > 2) {
                    jhm.put("status", "0");
                    jhm.put("staff_id", record.getStr("staff_id"));
                    jhm.put("date", record.getStr("date"));
                    jsonArray = JSONArray.fromObject(record.getStr("app_content"));
                    jhm.put("list", jsonArray);
                } else {
                    jhm.putCode(0).putMessage("未录入闲时！");
                }
            }

//            } else {
//                jhm.putCode(0).putMessage("未排班！");
//            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    public void delete() {
        JsonHashMap jhm = new JsonHashMap();
        JSONArray listArray = new JSONArray();
        JSONArray delArray = new JSONArray();


        String deltime = getPara("deltime");
        if (StringUtils.isEmpty(deltime)) {
            jhm.putCode(0).putMessage("请选择删除时间");
            renderJson(jhm);
            return;
        }

        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("未选择员工");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("未选择日期");
            renderJson(jhm);
            return;
        }

        String sql = "SELECT time.id as id ,  time.staff_id as staff_id , time.date as date , time.app_content as app_content FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ? ";
        //七天前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            Record staffRecord = Db.findFirst("select * from h_staff where id = ?", id);
            if (staffRecord == null) {
                jhm.putCode(0).putMessage("找不到该员工！");
                renderJson(jhm);
                return;
            }
            //之前的七天
            Date nowDate = sdf.parse(date);
            calendar.setTime(nowDate);
            calendar.add(Calendar.DAY_OF_MONTH, -7);

            String temp = sdf.format(calendar.getTime());

            Record record = Db.findFirst(sql, id, date);
            //获取七天前的排班
            Record beforeRecord = Db.findFirst("SELECT time.store_id , time.staff_id , time.content , time.app_content , time.creater_id,time.create_time,time.kind  FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ?", id, sdf.format(calendar.getTime()));

            String content;
            if (record != null) {
                //删除时间
                if (record.getStr("app_content") != null) {
                    delArray = JSONArray.fromObject(deltime);
                    listArray = JSONArray.fromObject(record.getStr("app_content"));
                    for (int i = delArray.size() - 1; i >= 0; --i) {
                        JSONObject defJson = delArray.getJSONObject(i);
                        String delStart = defJson.getString("start");
                        for (int j = listArray.size() - 1; j >= 0; --j) {
                            JSONObject dbJson = listArray.getJSONObject(j);
                            String dbStart = dbJson.getString("start");
                            if (StringUtils.equals(delStart, dbStart)) {
                                listArray.remove(j);
                            }
                        }
                    }
                    content = AppToPcIdleTime(listArray.toString());
                } else {
                    content = AppToPcIdleTime("");
                }
            } else {
                beforeRecord.set("id", UUIDTool.getUUID());
                beforeRecord.set("date", date);
                beforeRecord.set("modify_time", DateTool.GetDateTime());
                beforeRecord.set("modifier_id", id);
                Db.save("h_staff_idle_time", beforeRecord);

                record = new Record();
                record.set("id", beforeRecord.getStr("id"));
                record.set("staff_id", beforeRecord.getStr("staff_id"));
                record.set("date", beforeRecord.getStr("date"));
                record.set("store_id", beforeRecord.getStr("store_id"));
                record.set("content", beforeRecord.getStr("content"));
                record.set("creater_id", beforeRecord.getStr("creater_id"));
                record.set("create_time", beforeRecord.getStr("create_time"));
                record.set("kind", beforeRecord.getStr("kind"));
                record.set("app_content", beforeRecord.getStr("app_content"));
                record.set("modify_time", DateTool.GetDateTime());
                record.set("modifier_id", id);
                if (record.getStr("app_content") != null) {
                    delArray = JSONArray.fromObject(deltime);
                    listArray = JSONArray.fromObject(record.getStr("app_content"));
                    for (int i = delArray.size() - 1; i >= 0; --i) {
                        JSONObject defJson = delArray.getJSONObject(i);
                        String delStart = defJson.getString("start");
                        for (int j = listArray.size() - 1; j >= 0; --j) {
                            JSONObject dbJson = listArray.getJSONObject(j);
                            String dbStart = dbJson.getString("start");
                            if (StringUtils.equals(delStart, dbStart)) {
                                listArray.remove(j);
                            }
                        }
                    }
                    content = AppToPcIdleTime(listArray.toString());
                } else {
                    content = AppToPcIdleTime("");
                }
            }

            record.remove("app_content");
            record.set("app_content", listArray.toString());
            record.set("content", content);
            boolean flag = Db.update("h_staff_idle_time", record);
            if (flag) {
                jhm.putCode(1).putMessage("修改成功");
            } else {
                jhm.putCode(0).putMessage("修改失败");
            }


        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    /*
        没有秒的时间格式转换
     */
    public static String AppToPcIdleTime(String appStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonSave = new JSONObject();
        String appContent = "";

        try {
            jsonArray = JSONArray.fromObject(appStr);
            Date initTime = simpleDateFormat.parse("07:30");        //最早上班时间
            Long initMilliSecond = initTime.getTime();                     //最早上班时间的毫秒数
            int standardTime = 15 * 60 * 1000;                             //每15分钟的毫秒数
            String startTime = "";                         //连续时间段中开始的时间
            Long startMilliSecond;
            Date transDate = new Date();

            int[] key = new int[66];
            for (int i : key) {
                key[i] = 0;
            }

            for (int i = 0; i < key.length; i++) {
                jsonSave.put(String.valueOf(i), "0");
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                startTime = jsonObject.getString("start");
                transDate = simpleDateFormat.parse(startTime);
                startMilliSecond = transDate.getTime();
                jsonSave.put(String.valueOf((startMilliSecond - initMilliSecond) / standardTime), "1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        appContent = jsonSave.toString();
        return appContent;
    }


}
