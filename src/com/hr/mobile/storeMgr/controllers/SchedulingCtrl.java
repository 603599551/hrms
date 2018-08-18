package com.hr.mobile.storeMgr.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.storeMgr.service.SchedulingSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulingCtrl extends BaseCtrl {

    public void showNum(){
        JsonHashMap jhm = new JsonHashMap();
        //日期
        String date=getPara("date");
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("日期不能为空！");
            renderJson(jhm);
            return;
        }
        String time="";
        List listNum=new ArrayList();
        for(int i=7;i<24;i+=2){
            HashMap map=new HashMap();
            String timeList="0"+i+":00-0"+(i+2)+":00";
            time="[{\"start\": \"0"+i+":00\",\"end\": \"0"+(i+2)+":00\"}]";
            if(i==9){
                time="[{\"start\": \"0"+i+":00\",\"end\": \""+(i+2)+":00\"}]";
                timeList="0"+i+":00-"+(i+2)+":00";
            }
            if(i>=10){
                time="[{\"start\": \""+i+":00\",\"end\": \""+(i+2)+":00\"}]";
                timeList=i+":00-"+(i+2)+":00";
            }
            if(i==23){
                time="[{\"start\": \""+i+":00\",\"end\": \""+(i+1)+":00\"}]";
                timeList=i+":00-"+(i+1)+":00";
            }
            JSONArray timeArray = JSONArray.fromObject(time);
            JSONObject timeObject = timeArray.getJSONObject(0);
            String startTime = (String) timeObject.get("start");
            String endTime = (String) timeObject.get("end");
            String sql ="SELECT  count(*) num FROM h_staff s WHERE s.id IN ( SELECT d.staff_id FROM h_work_time_detail d WHERE d.date =? AND d.start_time >=? AND d.end_time <= ?)";
            Record record = Db.findFirst(sql, date, startTime, endTime);
            if(record==null){
                jhm.putCode(0).putMessage("查询失败！");
                renderJson(jhm);
                return;
            }
            //应到人数
            String due=record.getStr("num");
            String dateTime= DateTool.GetDateTime();
            String sqlWork="select count(*) num from h_staff_clock c where c.sign_in_time<? and c.sign_back_time is null and c.date=?";
            Record workRecord=Db.findFirst(sqlWork,dateTime,date);
            if(workRecord==null){
                jhm.putCode(0).putMessage("查询失败！");
                renderJson(jhm);
                return;
            }
            //正在工作人数
            String working=workRecord.getStr("num");
            map.put("time",timeList);
            map.put("working",working);
            map.put("due",due);
            listNum.add(map);
        }
        jhm.put("list",listNum);
        renderJson(listNum);
    }
    /**
     * 店长查看排班列表
     * 查看员工排班
     * 根据日期和时间段查询所有已排班的员工
     */

    public void showList() {
        JsonHashMap jhm = new JsonHashMap();
        String date = getPara("date");
        //time为json字符串
        String time = getPara("time");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择查询日期！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(time)) {
            jhm.putCode(0).putMessage("请选择查询时间段！");
            renderJson(jhm);
            return;
        }
        //json字符串转成json数组
        JSONArray timeArray = JSONArray.fromObject(time);
        JSONObject timeObject = timeArray.getJSONObject(0);
        String startTime = (String) timeObject.get("start");
        String endTime = (String) timeObject.get("end");
        try {
            String sql = "SELECT s.id staff_id, upper(LEFT(s.pinyin, 1)) intial, s. NAME name, ( SELECT d. NAME FROM h_dictionary d WHERE d. VALUE = s.job ) job, s.phone phone, ( SELECT CASE c.is_leave WHEN '1' THEN '2' ELSE ( CASE c. STATUS WHEN '2' THEN ( CASE c.is_leave_early WHEN '2' THEN '4' ELSE ( CASE c.is_late WHEN '2' THEN '0' ELSE '1' END ) END ) WHEN '1' THEN ( CASE c.is_late WHEN '2' THEN '0' ELSE '1' END ) WHEN '0' THEN '3' END ) END FROM h_staff_clock c WHERE c.date = ? AND c.staff_id = s.id AND c.start_time < ? AND c.end_time > ? limit 1)  'condition', ( SELECT CASE c. STATUS WHEN '0' THEN '0' ELSE '1' END FROM h_staff_clock c WHERE c.date =? AND c.staff_id = s.id AND c.start_time < ? AND c.end_time > ? limit 1) arrive FROM h_staff s WHERE s.id IN ( SELECT d.staff_id FROM h_work_time_detail d WHERE d.date = ? AND d.start_time >= ? AND d.end_time <= ? )";
            List<Record> list = Db.find(sql, date,endTime,startTime,date,endTime,startTime,date,startTime, endTime);
            if (list != null && list.size() > 0) {
                jhm.put("list", list);
            } else {
                jhm.putCode(1).putMessage("查询结果为空!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 查看员工排班情况
     * 根据日期、时间段、员工id查询排班信息
     */

    public void showDetail() {
        JsonHashMap jhm = new JsonHashMap();
        String staffId = getPara("staff_id");
        String date = getPara("date");
        //time为json字符串
        String time = getPara("time");
        if (StringUtils.isEmpty(staffId)) {
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择查询日期！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(time)) {
            jhm.putCode(0).putMessage("请选择查询时间段！");
            renderJson(jhm);
            return;
        }
        //json字符串转成json数组
        JSONArray timeArray = JSONArray.fromObject(time);
        JSONObject timeObject = timeArray.getJSONObject(0);
        String startTime = (String) timeObject.get("start");
        String endTime = (String) timeObject.get("end");
        try {
            String sql = "select d.start_time start ,d.end_time end,d.status status from h_work_time_detail d where  d.staff_id=? and d.date=? and d.start_time>=? and d.end_time<=? order by d.start_time";
            List<Record> list = Db.find(sql, staffId, date, startTime, endTime);
            if (list != null && list.size() > 0) {
                jhm.put("list", list);
            } else {
                jhm.putCode(1).putMessage("该员工在此时间内没有排班!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 店长为员工加班减班
     */
    //加班是往h_work_time_detail中插入一条记录，减班是更新数据库中相应记录status为减班2有特殊情况
    //加班减班还要更改h_work_tine_detail表的real_number字段
    public void set() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //员工id
        String staffId = getPara("staff_id");
        //日期
        String date = getPara("date");
        //time为json字符串
        //时间段
        String time = getPara("time");
        //2减班3加班
        String status = getPara("status");
        if (StringUtils.isEmpty(staffId)) {
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(time)) {
            jhm.putCode(0).putMessage("请选择时间段！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(status)) {
            jhm.putCode(0).putMessage("请选择操作类型！");
            renderJson(jhm);
            return;
        }
        Map paraMap = new HashMap();
        paraMap.put("usu", usu);
        paraMap.put("staff_id", staffId);
        paraMap.put("date", date);
        paraMap.put("time", time);
        paraMap.put("status", status);
        try {
            SchedulingSrv srv = enhance(SchedulingSrv.class);
            jhm = srv.set(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);

    }

    /**
     * 	经理端排班详情回显
     */
    public void showDetailbyId (){
        JsonHashMap jhm = new JsonHashMap();
        String staffId = getPara("staff_id");
        if(StringUtils.isEmpty(staffId)){
            jhm.putCode(0).putMessage("请选择员工");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("请选择时间");
            renderJson(jhm);
            return;
        }
        //起止时间格式
        SimpleDateFormat sdfWorkTime = new SimpleDateFormat("HH:mm");
        //签到时间格式
        SimpleDateFormat sdfSignTime = new SimpleDateFormat("HH:mm:ss");
        String sql = "SELECT c.start_time as start,c.end_time as end,c.sign_in_time as signin,c.sign_back_time as signback, c.is_late as late , c.status as status , (SELECT s.pinyin FROM h_staff s WHERE s.id = c.staff_id) as firstname , (SELECT s.name FROM h_staff s WHERE s.id = c.staff_id) as name , (SELECT s.phone FROM h_staff s WHERE s.id = c.staff_id) as phone , ( SELECT group_concat(h. NAME) kind FROM h_staff s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind) WHERE s.id = c.staff_id GROUP BY s.id ORDER BY s.id ASC ) AS job FROM h_staff_clock c WHERE c.staff_id = ? AND c.date = ?";
        try {
            //查出全部记录
            List<Record> staffList = Db.find(sql, staffId, date);
            if (!(staffList != null && staffList.size() > 0)) {
                jhm.putCode(0).putMessage("找不到指定信息");
                renderJson(jhm);
                return;
            }
            //存content信息
            List list = new ArrayList();

            //处理content
            for(int i = 0 ; i < staffList.size() ; ++i ){
                Record record = new Record();
                record.set("time",staffList.get(i).getStr("start") + "-" + staffList.get(i).getStr("end"));
                //是否迟到以及迟到时间
                if(StringUtils.equals("2",staffList.get(i).getStr("late"))){
                    record.set("late",String.valueOf((sdfSignTime.parse(staffList.get(i).getStr("signin")).getTime() - sdfWorkTime.parse(staffList.get(i).getStr("start")).getTime()) / (60 * 1000)));
                }else {
                    record.set("late","0");
                }
                //判断签到签退
                if(StringUtils.equals("0",staffList.get(i).getStr("status"))){
                    //未签到寻找上一个签到签退时间
                    if(i > 0){
                        for(int j = i - 1 ; j >= 0 ; --j){
                            if (StringUtils.equals("2",staffList.get(j).getStr("status"))){
                                record.set("signout",staffList.get(j).getStr("signback").substring(0,5));
                                break;
                            }else {
                                record.set("signin","0");
                                record.set("signout","0");
                            }
                        }
                        for(int j = i - 1 ; j >= 0 ; --j){
                            if (StringUtils.equals("1",staffList.get(j).getStr("status"))){
                                record.set("signin",staffList.get(j).getStr("signin").substring(0,5));
                                break;
                            }else {
                                record.set("signin","0");
                                record.set("signout","0");
                            }
                        }
                    } else {
                        record.set("signin","0");
                        record.set("signout","0");
                    }
                } else if(StringUtils.equals("1",staffList.get(i).getStr("status"))){
                    record.set("signin",staffList.get(i).getStr("signin").substring(0,5));
                    if(i > 0){
                        //寻找上一个签退时间
                        for(int j = i - 1 ; j >= 0 ; --j){
                            if (StringUtils.equals("2",staffList.get(j).getStr("status"))){
                                record.set("signout",staffList.get(j).getStr("signback").substring(0,5));
                                break;
                            }
                        }
                        record.set("signout","0");
                    } else {
                        record.set("signout","0");
                    }
                } else {
                    record.set("signin",staffList.get(i).getStr("signin").substring(0,5));
                    record.set("signout",staffList.get(i).getStr("signback").substring(0,5));
                }
                list.add(record);
            }

            jhm.put("firstname",staffList.get(0).getStr("firstname").toUpperCase().substring(0,1));
            jhm.put("name",staffList.get(0).getStr("name"));
            jhm.put("job",staffList.get(0).getStr("job"));
            jhm.put("phone",staffList.get(0).getStr("phone"));
            jhm.put("content",list);

        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }


}
