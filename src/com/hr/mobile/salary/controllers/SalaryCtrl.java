package com.hr.mobile.salary.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.List;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SalaryCtrl extends BaseCtrl{

    /**
     * 16.1.
     名称	查看工资详情
     描述	根据日期和员工id查询员工半月的工资(每天累计 至半个月)
     验证	无
     权限   无
     URL	http://localhost:8081/hrms/mgr/mobile/salary/showSalaryHalfMounth
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	string		否	员工id
     date	string		否	日期，格式：yyyy-MM-dd

     返回数据：
     返回格式	JSON
     成功	{
         "workHour": "87",
         "takePay": "690",
         "duePay": "770",
         "month": "0",
         "list": [{
         "date": "2018-08-04",
         "time": "08:00-09:00",
         "condition": "1",
         "change": "-30"
         }, {
         "date": "2018-08-04",
         "time": "09:00-10:00",
         "condition": "4",
         "change": "+20"
         }]
         }
         month:0:上半月 1:下半月
         workHour:半月实际工作时长（小时）
         takePay:半月实得工资（元）
         duePay:半月应得工资（元）
         condition:1:迟到 2:早退 3:旷工 4:加班 5:减班 6:请假
     失败	{
         "code": 0,
         "message": "失败原因！"
         }
     报错	{
         "code": -1,
         "message": "服务器发生异常！"
         }
     */

    // 获取当天时间
    public static String getNowTime(String dateformat) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        return hehe;
    }

    // 获取当月第一天
    public static String getFirstDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 获取当月第16天
    public static String getMidDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 16);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 计算当月最后一天,返回字符串
    public static String getDefaultDay() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        lastDate.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
        lastDate.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天

        str = sdf.format(lastDate.getTime());
        return str;
    }

    /**
     * 获得今天在本月的第几天(获得当前日)
     *
     * @return
     */
    public static int getDayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public void showSalaryHalfMounth(){

        JsonHashMap jhm=new JsonHashMap();

        //请求参数
        String staffId=getPara("staff_id");
        String date=getPara("date");

        try{
            //获取当月第一天
            String firstDayOfMonth=SalaryCtrl.getFirstDayOfMonth();
            //获取当月第一天
            String midDayOfMonth=SalaryCtrl.getMidDayOfMonth();
            //获取当月最后一天
            String lastDayOfMonth=SalaryCtrl.getDefaultDay();
            //获得今天在本月的第几天(获得当前日)
            int ddCount=SalaryCtrl.getDayOfMonth();

            String dateStart="";
            String dateEnd="";
            //json中的month 0:上半月 1:下半月
            int monthIsSecondHalf=-1;
            if (ddCount<=15){
                dateStart=firstDayOfMonth;
                monthIsSecondHalf=0;
            }else {
                dateStart=midDayOfMonth;
                monthIsSecondHalf=1;
            }
            dateEnd=date;
            /*
                兼职工
                */
            //时薪
            String sql="select hour_wage from h_staff where id=?";
            Record staff=Db.findFirst(sql,staffId);
            float hourWage=-1;
            //每15分钟的薪水
            float every15Wage=-1;
            if(staff==null){
                jhm.putCode(0).putMessage("用户不存在！");
            }else{
                hourWage=staff.getFloat("hour_wage");
                every15Wage=hourWage/4;
            }

            //应工作的时长
            float setWorkHour=0;
            //实际工作的时长
            float workHour=0;
            String sql2="select number,real_number from h_work_time where staff_id=? and date>=? and date<=?";
            List<Record> setWorkHourList=Db.find(sql2,staffId,dateStart,dateEnd);
            if (setWorkHourList!=null&&setWorkHourList.size()>0){
                for (Record r:setWorkHourList){
                    setWorkHour+=((float)r.getInt("number")*15.0/60.0);
                    workHour+=((float)r.getInt("real_number")*15.0/60.0);
                }
            }

            //应得工资=时薪*应工作的时长
            float duePay=hourWage*setWorkHour;
            //实得工资=时薪*实际工作的时长
            float takePay=hourWage*workHour;

            //date time condition change
            String sql3="select date,start_time,end_time,status from h_work_time_detail where date>=?and  date<=? and staff_id=? order by date asc ,start_time asc";
            List<Record> list1=Db.find(sql3,dateStart,dateEnd,staffId);

            jhm.put("month",monthIsSecondHalf);
            jhm.put("workHour",workHour);
            jhm.put("duePay",duePay);
            jhm.put("takePay",takePay);


        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }
}
