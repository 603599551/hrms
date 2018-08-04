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
        Record recordSelect = Db.findFirst("SELECT time.id as id, time.modify_time as modify_time, time.modifier_id as modifier_id , time.app_content as app_content FROM h_staff_idle_time time WHERE time.staff_id = ?  AND date = ? ",staffId,date);
        if(recordSelect == null) {
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
            record.set("content", ContentTransformationUtil.AppToPcXianShi(date));

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

            try{
                boolean flag = Db.update("h_staff_idle_time", recordSelect);
                if (flag) {
                    jhm.putCode(1).putMessage("保存成功！");
                } else {
                    jhm.putCode(0).putMessage("保存失败！");
                }
            } catch (Exception e){
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

        String sql = "SELECT time.staff_id as staff_id , time.date as date , time.app_content as list FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ? ";
        //验证日期格式是否正确
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            //验证员工是否存在
            Record staffRecord = Db.findFirst("select * from h_staff where id = ?", id);
            if (staffRecord == null) {
                jhm.putCode(0).putMessage("找不到该员工！");
                renderJson(jhm);
                return;
            }

            Record record = Db.findFirst(sql, id, date);
            if (record != null) {
                jhm.put("staff_id", record.getStr("staff_id"));
                jhm.put("date", record.getStr("date"));
                jsonArray = JSONArray.fromObject(record.getStr("list"));
                jhm.put("list", jsonArray);
            } else {
                jhm.putCode(0).putMessage("未排班！");
            }
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
        if(StringUtils.isEmpty(deltime)){
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

        String sql = "SELECT time.id as id ,  time.staff_id as staff_id , time.date as date , time.app_content as list FROM h_staff_idle_time time WHERE time.staff_id = ? AND time.date = ? ";
        try {
            Record staffRecord = Db.findFirst("select * from h_staff where id = ?", id);
            if (staffRecord == null) {
                jhm.putCode(0).putMessage("找不到该员工！");
                renderJson(jhm);
                return;
            }
            Record record = Db.findFirst(sql, id, date);

            //删除时间
            if (record.getStr("list") != null) {
                delArray = JSONArray.fromObject(deltime);
                listArray = JSONArray.fromObject(record.getStr("list"));
                for(int i =  delArray.size() - 1 ; i >= 0 ; --i){
                    JSONObject defJson=delArray.getJSONObject(i);
                    String delStart=defJson.getString("start");
                    for(int j = listArray.size() - 1; j >= 0 ; --j){
                        JSONObject dbJson=listArray.getJSONObject(j);
                        String dbStart=dbJson.getString("start");
                        if(StringUtils.equals(delStart,dbStart)){
                            listArray.remove(j);
                        }
                    }
                }
            }

            record.remove("list");
            record.set("app_content",listArray.toString());
            boolean flag = Db.update("h_staff_idle_time",record);
            if(flag){
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


}
