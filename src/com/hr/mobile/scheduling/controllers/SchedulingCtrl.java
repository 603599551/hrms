package com.hr.mobile.scheduling.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        //判断是否存在该员工
        String selectStaff = "SELECT count(*) c FROM h_staff s WHERE s.id = ? ";
        Record record = Db.findFirst(selectStaff, id);
        if (record.getInt("c") <= 0) {
            jhm.putCode(0).putMessage("员工不存在");
            renderJson(jhm);
            return;
        }

        //查询排班信息
        String select = "SELECT  p.content , p.app_area_content FROM h_staff_paiban p WHERE p.staff_id = ? and p.date = ?";
        String[] params = {id, date};
        Record timeRecord = Db.findFirst(select, params);
        if(timeRecord==null){
            jhm.putCode(2).putMessage("未排班!");
            jhm.put("staff_id", id);
            jhm.put("date", date);
            renderJson(jhm);
            return;
        }
//        String content = timeRecord.getStr("content");
        String appContent = timeRecord.getStr("app_area_content");
        if (appContent == null && appContent.trim().length() == 0) {
            jhm.putCode(2).putMessage("未排班！");
            jhm.put("staff_id", id);
            jhm.put("date", date);
            renderJson(jhm);
            return;
        }

        //查询请假信息
        String selectLeave = "SELECT DISTINCT l.leave_start_time as start FROM h_staff_leave l,h_staff_leave_info i WHERE i.id = l.leave_info_id and l.staff_id = ? and i.status='1' AND l.date = ? order by l.leave_start_time";
        List<Record> leaveTime = Db.find(selectLeave, id, date);

        //查询岗位数据字典，并放入到map中，便于后面翻译岗位
        List<Record> kindList=Db.find("select name,value from h_dictionary where parent_id=?",3000);
        Map<String,String> kindMap=new HashMap();
        for(Record r:kindList){
            String name=r.get("name");
            String value=r.get("value");
            kindMap.put(value,name);
        }
        try {

            if ( leaveTime != null && leaveTime.size() > 0 ) {
                //是否存在排班信息
                JSONArray jsonArray = JSONArray.fromObject(appContent);
                for (int i = 0; i < jsonArray.size(); ++i) {
                    //找请假表
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    String startTime=jsonObj.getString("start");
                    String kind=jsonObj.getString("kind");
                    String kindText=kindMap.get(kind);
                    jsonObj.put("kind_text",kindText);
                    for (int j = 0; j < leaveTime.size(); ++j) {
                        String startLeaveTime=leaveTime.get(j).getStr("start");
                        if(startTime.equals(startLeaveTime )) {
                            jsonObj.put("flag", "1");
                            break;
                        } else {
                            jsonObj.put("flag", "0");
                        }
                    }
                }
                jhm.put("list", jsonArray);
            } else {
                //不存在请假信息
                JSONArray jsonArray = JSONArray.fromObject(appContent);
                for (int i = 0; i < jsonArray.size(); ++i) {
                    //找请假表
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    jsonObj.put("flag", "0");

                    String kind=jsonObj.getString("kind");
                    String kindText=kindMap.get(kind);
                    jsonObj.put("kind_text",kindText);
                }
                jhm.put("list", jsonArray);
            }
            //存在请假信息
            jhm.put("staff_id", id);
            jhm.put("date", date);




        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

}
