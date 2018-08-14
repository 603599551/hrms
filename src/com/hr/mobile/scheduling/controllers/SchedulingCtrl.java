package com.hr.mobile.scheduling.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.List;


public class SchedulingCtrl extends BaseCtrl {


    /**
     * 4.1.	根据人员id、日期查看排班时间
     * <p>
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	员工id
     * date	string		否	日期，格式：yyyy-MM-dd
     * 成功	{
     * "code": 1,
     * "staff_id": "10239723894",
     * "date": "2018-07-20",
     * "list": [{
     * "start": "07:00",
     * "end": "07:15"
     * }, {
     * "start": "07:15",
     * "end": "07:30"
     * }, {
     * "start": "07:30",
     * "end": "07:45"
     * }, {
     * "start": "14:15",
     * "end": "14:30"
     * }, {
     * "start": "14:30",
     * "end": "14:45"
     * }]
     * }
     * staff_id：员工id
     * date：当天日期
     * list：排班时间
     * 失败	{
     * "code": 0,
     * "message": "员工不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": ""//失败信息
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */

    public void showDetailByStaffIdAndDate() {

        JsonHashMap jhm = new JsonHashMap();

        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }

        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期");
            renderJson(jhm);
            return;
        }

        String select = "SELECT  p.app_content FROM h_staff_paiban p WHERE p.staff_id = ? and p.date = ?";
        String selectLeave = "SELECT DISTINCT l.leave_start_time as start FROM h_staff_leave l WHERE  (SELECT i.status FROM h_staff_leave_info i WHERE i.id = l.leave_info_id) = '1' AND l.staff_id = ?  AND l.date = ?";
        String params[] = {id, date};
        String selectStaff = "SELECT count(*) c FROM h_staff s WHERE s.id = ? ";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            //寻找有没有该员工
            Record record = Db.findFirst(selectStaff, id);
            if (record.getInt("c") <= 0) {
                jhm.putCode(0).putMessage("员工不存在");
            } else {
                //转为Json
                Record timeRecord = Db.findFirst(select, params);
                List<Record> leaveTime = Db.find(selectLeave, params);

                if (timeRecord != null) {
                    if (leaveTime.size() > 0 && leaveTime != null) {
                        String app_content = timeRecord.getStr("app_content");
                        //是否存在排班信息
                        if (app_content != null && app_content.trim().length() > 0) {
                            JSONArray jsonArray = JSONArray.fromObject(app_content);
                            for (int i = 0; i < jsonArray.size(); ++i) {
                                //找请假表
                                JSONObject jsonObj = jsonArray.getJSONObject(i);
                                for (int j = 0; j < leaveTime.size(); ++j) {
                                    if(sdf.parse(jsonObj.getString("start")).getTime() == sdf.parse(leaveTime.get(j).getStr("start")).getTime()) {
                                        jsonObj.put("flag", "1");
                                        break;
                                    } else {
                                        jsonObj.put("flag", "0");
                                    }
                                }
                            }
                            jhm.put("list", jsonArray);
                        } else {
                            jhm.putCode(2).putMessage("未排班！");
                        }
                    } else {
                        //不存在请假信息
                        String app_content = timeRecord.getStr("app_content");
                        if (app_content != null && app_content.trim().length() > 0) {
                            //是否存在排班信息
                            JSONArray jsonArray = JSONArray.fromObject(app_content);
                            for (int i = 0; i < jsonArray.size(); ++i) {
                                //找请假表
                                JSONObject jsonObj = jsonArray.getJSONObject(i);
                                jsonObj.put("flag", "0");
                            }
                            jhm.put("list", jsonArray);
                        } else {
                            jhm.putCode(2).putMessage("未排班！");
                        }
                    }
                    //存在请假信息
                    jhm.put("staff_id", id);
                    jhm.put("date", date);
                } else {
                    jhm.putCode(2).putMessage("未排班!");
                    jhm.put("staff_id", id);
                    jhm.put("date", date);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

}
