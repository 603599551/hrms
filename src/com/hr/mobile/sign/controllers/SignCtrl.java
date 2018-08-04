package com.hr.mobile.sign.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SignCtrl extends BaseCtrl {

    /*
      根据人员id、日期查看签到签退时间
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
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }
        String time = getPara("time");
        if (StringUtils.isEmpty(time)) {
            jhm.putCode(0).putMessage("请获取当前时间");
            renderJson(jhm);
            return;
        }
        //判断员工是否存在
        Record recordStaff = Db.findById("h_staff", id);
        if (recordStaff == null) {
            jhm.putCode(0).putMessage("员工不存在");
        }

        //寻找该员工该天的排班情况
        String selectClock = "SELECT clock.status as status , clock.start_time as start , clock.end_time as end, clock.sign_in_time as sign_in ,clock.sign_back_time as sign_out FROM h_staff_clock clock WHERE clock.staff_id = ? AND clock.date = ? ORDER BY clock.start_time";
        //寻找该员工的工作时长
        String selectNum = "SELECT time.real_number num FROM h_work_time time WHERE time.staff_id = ? and time.date = ? ";
        //上下班时间的格式转化
        SimpleDateFormat sdfWorkTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //当前时间
            Date DateTime = sdfWorkTime.parse(time);
            //上班结束时间
            Date DateEndTime;
            //排班情况
            List<Record> recordList = Db.find(selectClock, id, date);
            //存在排班记录
            if (recordList.size() > 0 && recordList != null) {

                //提示签退的情况
                for (int i = 0; i < recordList.size(); ++i) {
                    //存在只签到未签退的情况
                    if (StringUtils.equals(recordList.get(i).getStr("status"), "1")) {
                        //寻找工作时长
                        Record recordTime = Db.findFirst(selectNum, id, date);
                        if (recordTime != null && recordTime.getInt("num") != 0) {
                            jhm.put("all_time", recordTime.getInt("num") * 15.00 / 60);
                        } else {
                            jhm.put("all_time", 0.00);
                        }
                        jhm.put("status", "1");
                        if (recordList.get(i).getStr("sign_in") != null) {
                            jhm.put("sign_in", recordList.get(i).getStr("sign_in").substring(11, 16));
                        } else {
                            jhm.put("sign_in", "--:--");
                        }
                        renderJson(jhm);
                        return;
                    }

                    //虽然已经签退  但在工作时间内
                    if (sdfWorkTime.parse(recordList.get(i).getStr("start")).before(DateTime) && sdfWorkTime.parse(recordList.get(i).getStr("end")).after(DateTime) && StringUtils.equals(recordList.get(i).getStr("status"), "2")) {
                        Record recordTime = Db.findFirst(selectNum, id, date);
                        if (recordTime != null && recordTime.getInt("num") != 0) {
                            jhm.put("all_time", recordTime.getInt("num") * 15.00 / 60);
                        } else {
                            jhm.put("all_time", 0.00);
                        }
                        jhm.put("status", "1");
                        if (recordList.get(i).getStr("sign_in") != null) {
                            jhm.put("sign_out", recordList.get(i).getStr("sign_out").substring(11, 16));
                        } else {
                            jhm.put("sign_out", "--:--");
                        }
                        renderJson(jhm);
                        return;
                    }

                    //在两个工作时间之间  但是上一班在工作时间内签退了  依然提示签退
                    if (i < recordList.size() - 1) {
                        //是否在两个班之间的休息时间内并且签退过
                        if (sdfWorkTime.parse(recordList.get(i).getStr("end")).before(DateTime) && sdfWorkTime.parse(recordList.get(i + 1).getStr("start")).after(DateTime) && StringUtils.equals(recordList.get(i).getStr("status"), "2")) {
                            Record recordTime = Db.findFirst(selectNum, id, date);
                            if (recordTime != null && recordTime.getInt("num") != 0) {
                                jhm.put("all_time", recordTime.getInt("num") * 15.00 / 60);
                            } else {
                                jhm.put("all_time", 0.00);
                            }
                            jhm.put("status", "1");
                            if (recordList.get(i).getStr("sign_in") != null) {
                                jhm.put("sign_out", recordList.get(i).getStr("sign_out").substring(11, 16));
                            } else {
                                jhm.put("sign_out", "--:--");
                            }
                            renderJson(jhm);
                            return;
                        }
                    }
                }

                //显示签到的情况
                //结束时间晚于当前时间，并且该时间段没有签到签退记录
                for (int i = 0; i < recordList.size(); ++i) {
                    DateEndTime = sdfWorkTime.parse(recordList.get(i).getStr("end"));
                    if (DateEndTime.after(DateTime) && StringUtils.equals(recordList.get(i).getStr("status"), "0")) {
                        if (i == 0) {
                            jhm.put("all_time", "0.00");
                            jhm.put("status", "0");
                            jhm.put("sign_in", "--:--");
                            renderJson(jhm);
                            return;
                        } else {
                            boolean isOut = false;
                            Record recordTime = Db.findFirst(selectNum, id, date);
                            if (recordTime != null) {
                                jhm.put("all_time", recordTime.getInt("num") * 15.00 / 60);
                            } else {
                                jhm.put("all_time", 0.00);
                            }
                            for (int j = i - 1; j >= 0; --j) {
                                if (StringUtils.equals(recordList.get(j).getStr("status"), "2")) {
                                    jhm.put("sign_out", recordList.get(i - 1).getStr("sign_out").substring(11, 16));
                                    isOut = true;
                                }
                            }
                            jhm.put("all_time", recordTime.getInt("num") * 15.00 / 60);
                            if (!isOut) {
                                jhm.put("sign_in", "--:--");
                            }
                            jhm.put("status", "0");
                            renderJson(jhm);
                            return;
                        }
                    }
                }
            } else {
                jhm.putCode(0).putMessage("未排班!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }


    /*
        签到
     */
    public void in() {
        JsonHashMap jhm = new JsonHashMap();
        String dateTime = getPara("time");
        if (StringUtils.isEmpty(dateTime)) {
            jhm.putCode(0).putMessage("未获取当前时间");
            renderJson(jhm);
            return;
        }
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }

        //寻找该员工该天的排班情况
        String selectClock = "SELECT clock.id , clock.status , clock.start_time , clock.end_time ,clock.sign_in_time ,clock.sign_back_time FROM h_staff_clock clock WHERE clock.staff_id = ? AND clock.date = ? ORDER BY clock.start_time";
        //查询工作明细
        String selectDetail = "SELECT start_time , end_time , work_time_id , status , id FROM h_work_time_detail WHERE staff_id = ? AND date = ? ORDER BY start_time";
        SimpleDateFormat sdfWorkTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date signIn = sdfWorkTime.parse(dateTime);
            List<Record> clockList = Db.find(selectClock, id, date);
            List<Record> detailList = Db.find(selectDetail, id, date);

            for (int i = 0; i < clockList.size(); ++i) {
                //当前时间段未签到 并且下班时间晚于当前时间  进行签到
                if (StringUtils.equals(clockList.get(i).getStr("status"), "0") && sdfWorkTime.parse(clockList.get(i).getStr("end_time")).after(signIn)) {
                    //当前时间晚于上班时间  记录迟到
                    if (signIn.getTime() > sdfWorkTime.parse(clockList.get(i).getStr("start_time")).getTime()) {
                        clockList.get(i).set("is_late", "2");
                    } else {
                        clockList.get(i).set("is_late", "1");
                    }

                    //更改明细表中的状态
                    for (int j = 0; j < detailList.size(); ++j) {
                        //明细表中开始键时间早于签到时间
                        if (sdfWorkTime.parse(detailList.get(j).getStr("start_time")).getTime() <= signIn.getTime()) {
                            //当前时间段有其他操作跳过
                            if (!StringUtils.equals(detailList.get(j).getStr("status"), "0")) {
                            } else {
                                //没有操作记录为未上班
                                detailList.get(j).set("status", "4");
                            }
                        }

                    }

                    clockList.get(i).remove("status");
                    clockList.get(i).set("status", "1");
                    clockList.get(i).set("sign_in_time", dateTime);
                    Db.batchUpdate("h_work_time_detail", detailList, detailList.size());
                    boolean flag = Db.update("h_staff_clock", clockList.get(i));
                    if (flag) {
                        jhm.putMessage("添加成功");
                    } else {
                        jhm.putCode(0).putMessage("添加失败");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常!");
        }
        renderJson(jhm);
    }

    /*
           签退
     */

    public void out() {
        JsonHashMap jhm = new JsonHashMap();
        String dateTime = getPara("time");
        if (StringUtils.isEmpty(dateTime)) {
            jhm.putCode(0).putMessage("未获取当前时间");
            renderJson(jhm);
            return;
        }
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }

        //查询该员工该天的排班情况
        String selectClock = "SELECT clock.id , clock.status , clock.start_time , clock.end_time , clock.sign_in_time , clock.sign_back_time , is_late FROM h_staff_clock clock WHERE clock.staff_id = ? AND clock.date = ? ORDER BY clock.start_time";
        //查询工作明细
        String selectDetail = "SELECT id, start_time , end_time,work_time_id , status FROM h_work_time_detail WHERE staff_id = ? AND date = ? ORDER BY start_time";
        //查询工作工时
        String selectRealNum = "SELECT id , real_number FROM h_work_time WHERE id = ? and date = ?";
        //更改日期格式
        SimpleDateFormat sdfWorkTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //实际工时数
        int workNum = 0;


        try {

            Date signOut = sdfWorkTime.parse(dateTime);
            List<Record> clockList = Db.find(selectClock, id, date);
            List<Record> detailList = Db.find(selectDetail, id, date);
            Record realNum = Db.findFirst(selectRealNum, detailList.get(0).getStr("work_time_id"), date);

            //遍历排班情况寻找所处时间段
            for (int i = 0; i < clockList.size(); ++i) {
                //在当前工作时间内签到过
                if (!StringUtils.equals(clockList.get(i).getStr("status"), "0") && (sdfWorkTime.parse(clockList.get(i).getStr("start_time")).getTime() <= signOut.getTime()) && sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() >= signOut.getTime()) {
                    //更改明细表中的状态
                    for (int j = 0; j < detailList.size(); ++j) {
                        //只操作状态为0的数据
                        if (!StringUtils.equals(detailList.get(j).getStr("status"), "0")) {
                            //当前工作时间段的结束时间早于签退时间  标记为正常上班
                        } else if (signOut.getTime() <= sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() && sdfWorkTime.parse(detailList.get(j).getStr("end_time")).getTime() <= (signOut.getTime())) {
                            detailList.get(j).remove("status");
                            detailList.get(j).set("status", "1");
                        }
                        //计算工作时长
                        if (StringUtils.equals(detailList.get(j).getStr("status"), "3") || StringUtils.equals(detailList.get(j).getStr("status"), "1")) {
                            workNum++;
                        }
                    }
                    //判断早退情况
                    if (sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() > signOut.getTime()) {
                        clockList.get(i).set("is_leave_early", "2");
                    } else {
                        clockList.get(i).set("is_leave_early", "1");
                    }

                    realNum.set("real_number", workNum);
                    clockList.get(i).remove("status");
                    clockList.get(i).set("status", "2");
                    clockList.get(i).set("sign_back_time", dateTime);
                    Db.update("h_work_time", realNum);
                    Db.batchUpdate("h_work_time_detail", detailList, detailList.size());
                    boolean flag = Db.update("h_staff_clock", clockList.get(i));
                    if (flag) {
                        jhm.putMessage("添加成功");
                    } else {
                        jhm.putCode(0).putMessage("添加失败");
                    }
                    break;
                    //不是第一班 在当前工作时间段内 但是没有签到过
                } else if (i > 0 && StringUtils.equals(clockList.get(i).getStr("status"), "0") && (sdfWorkTime.parse(clockList.get(i).getStr("start_time")).getTime() <= (signOut.getTime())) && sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() >= signOut.getTime()) {
                    //寻找上一个签到过的班
                    for (int k = i - 1; k >= 0; --k) {
                        if (StringUtils.equals(clockList.get(k).getStr("status"), "1")) {
                            //更改明细表中的状态
                            for (int j = 0; j < detailList.size(); ++j) {
                                //只操作状态为0的数据
                                if (!StringUtils.equals(detailList.get(j).getStr("status"), "0")) {
                                    //当前工作时间段的结束时间早于签退时间  标记为正常上班
                                } else if (sdfWorkTime.parse(detailList.get(j).getStr("end_time")).getTime() <= sdfWorkTime.parse(clockList.get(k).getStr("end_time")).getTime()) {
                                    detailList.get(j).remove("status");
                                    detailList.get(j).set("status", "1");
                                }
                                //计算工作时长
                                if (StringUtils.equals(detailList.get(j).getStr("status"), "3") || StringUtils.equals(detailList.get(j).getStr("status"), "1")) {
                                    workNum++;
                                }
                            }
                        }
                    }
                    //判断早退情况
                    if (sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() > signOut.getTime()) {
                        clockList.get(i).set("is_leave_early", "2");
                    } else {
                        clockList.get(i).set("is_leave_early", "1");
                    }
                    realNum.set("real_number", workNum);
                    clockList.get(i).remove("status");
                    clockList.get(i).set("status", "2");
                    clockList.get(i).set("sign_back_time", dateTime);
                    Db.update("h_work_time", realNum);
                    Db.batchUpdate("h_work_time_detail", detailList, detailList.size());
                    boolean flag = Db.update("h_staff_clock", clockList.get(i));
                    if (flag) {
                        jhm.putMessage("添加成功");
                    } else {
                        jhm.putCode(0).putMessage("添加失败");
                    }
                    break;
                }
                //下班后签退
                if (i < clockList.size() - 1) {
                    if (signOut.getTime() >= sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() && signOut.getTime() <= sdfWorkTime.parse(clockList.get(i + 1).getStr("start_time")).getTime()) {
                        //更改明细表中的状态
                        for (int j = 0; j < detailList.size(); ++j) {
                            //只操作状态为0的数据
                            if (!StringUtils.equals(detailList.get(j).getStr("status"), "0")) {
                                //当前工作时间段的结束时间早于签退时间  标记为正常上班
                            } else if (signOut.getTime() >= sdfWorkTime.parse(detailList.get(j).getStr("end_time")).getTime()) {
                                detailList.get(j).remove("status");
                                detailList.get(j).set("status", "1");
                            }
                            //计算工作时长
                            if (StringUtils.equals(detailList.get(j).getStr("status"), "3") || StringUtils.equals(detailList.get(j).getStr("status"), "1")) {
                                workNum++;
                            }
                        }

                        //判断早退情况
                        if (sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() > signOut.getTime()) {
                            clockList.get(i).set("is_leave_early", "2");
                        } else {
                            clockList.get(i).set("is_leave_early", "1");
                        }
                        realNum.set("real_number", workNum);
                        clockList.get(i).remove("status");
                        clockList.get(i).set("status", "2");
                        clockList.get(i).set("sign_back_time", dateTime);
                        Db.update("h_work_time", realNum);
                        Db.batchUpdate("h_work_time_detail", detailList, detailList.size());
                        boolean flag = Db.update("h_staff_clock", clockList.get(i));
                        if (flag) {
                            jhm.putMessage("添加成功");
                        } else {
                            jhm.putCode(0).putMessage("添加失败");
                        }
                        break;
                    }
                } else {
                    //更改明细表中的状态
                    for (int j = 0; j < detailList.size(); ++j) {
                        //只操作状态为0的数据
                        if (!StringUtils.equals(detailList.get(j).getStr("status"), "0")) {
                            //当前工作时间段的结束时间早于签退时间  标记为正常上班
                        } else if (signOut.getTime() >= sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime()) {
                            detailList.get(j).remove("status");
                            detailList.get(j).set("status", "1");
                        }
                        //计算工作时长
                        if (StringUtils.equals(detailList.get(j).getStr("status"), "3") || StringUtils.equals(detailList.get(j).getStr("status"), "1")) {
                            workNum++;
                        }
                    }
                    //判断早退情况
                    if (sdfWorkTime.parse(clockList.get(i).getStr("end_time")).getTime() > signOut.getTime()) {
                        clockList.get(i).set("is_leave_early", "2");
                    } else {
                        clockList.get(i).set("is_leave_early", "1");
                    }
                    realNum.set("real_number", workNum);
                    clockList.get(i).remove("status");
                    clockList.get(i).set("status", "2");
                    clockList.get(i).set("sign_back_time", dateTime);
                    Db.update("h_work_time", realNum);
                    Db.batchUpdate("h_work_time_detail", detailList, detailList.size());
                    boolean flag = Db.update("h_staff_clock", clockList.get(i));
                    if (flag) {
                        jhm.putMessage("添加成功");
                    } else {
                        jhm.putCode(0).putMessage("添加失败");
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
