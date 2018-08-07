package com.hr.mobile.salary.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.bean.JsonHashMap;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SalaryCtrl extends BaseCtrl {

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



    public void showSalaryHalfMonth(){

        JsonHashMap jhm=new JsonHashMap();

        //请求参数
        String staffId=getPara("staff_id");
        String date=getPara("date");

        try{
            //获取当月第一天
            String firstDayOfMonth=SalaryCtrl.getFirstDayOfMonth();
            //获取当月中间天
            String midDayOfMonth=SalaryCtrl.getMidDayOfMonth();
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
            }else{
                jhm.putCode(0).putMessage("工作记录不存在！");
            }

            //应得工资=时薪*应工作的时长
            float duePay=hourWage*setWorkHour;
            //实得工资=时薪*实际工作的时长
            float takePay=hourWage*workHour;

            //查询h_work_time中该用户在一段时间中 每天的工作情况
            String sql3="select * from h_work_time where date>=? and date <=? and staff_id=? order by date ASC";
            List<Record> list1=Db.find(sql3,dateStart,dateEnd,staffId);
            if (list1==null){
                jhm.putCode(0).putMessage("工作记录不存在！");
            }
            String sql4="";
            //存储该员工 当天的减班加班旷工请假记录（15分钟一条）
            List<Record> list2345;
            //存储该员工 当天的减班加班旷工请假记录（一段连续时间合并成一条）
            List<Record> finalList=new ArrayList<>();
            //finalRecord中的四个元素
            String fRDate="";
            String fRStartTime="";
            String fREndTime="";
            String fRTime="";
            String fRCondition="";
            float fRChange=-1;
            //记录当前合并记录的条数 以此来计算时间 继而算出fRChange
            int count=0;

            //遍历list1 查询h_work_time_detail表 找出每天减班加班旷工请假的情况加入list1中
            if (list1!=null&&list1.size()>0){
                for (Record r1:list1){
                    //找到该员工 当天的情况
                    sql4="select h_work_time_detail.* from h_work_time_detail,h_work_time where h_work_time.date=? and h_work_time.staff_id=? and h_work_time.id=h_work_time_detail.work_time_id\n" +
                            "and h_work_time.date=h_work_time_detail.date and h_work_time.staff_id=h_work_time_detail.staff_id and h_work_time_detail.status!=0 and h_work_time_detail.status!=1 order by start_time ASC";
                    list2345=Db.find(sql4,r1.getStr("date"),staffId);
                    //finalList中的单条记录
                    Record finalRecord=new Record();
                    //遍历list2345 合并连续的相同情况
                    for(int i=0;i< list2345.size()-1;i++){
                        //第一次将要合并的数条记录中的第一条
                        if (count==0){
                            fRDate=list2345.get(i).getStr("date");
                            fRStartTime=list2345.get(i).getStr("start_time");
                            fREndTime=list2345.get(i).getStr("end_time");
                            fRCondition=list2345.get(i).getStr("status");
                            count++;
                        }
                        //当本条记录的结束时间和下一条记录的开始时间相同且两条记录的状态相同时“合并”
                        if (list2345.get(i).getStr("end_time").equals(list2345.get(i+1).getStr("start_time"))&&list2345.get(i).getStr("status").equals(list2345.get(i+1).getStr("status"))){
                            fREndTime=list2345.get(i+1).getStr("end_time");
                            count++;
                        }else {
                            finalRecord.set("date",fRDate);
                            fRTime=fRStartTime+"-"+fREndTime;
                            finalRecord.set("time",fRTime);
                            finalRecord.set("condition",fRCondition);
                            if (fRCondition.equals("3")){
                                fRChange=count*every15Wage;
                            }else{
                                fRChange=-(count*every15Wage);
                            }
                            finalRecord.set("change",fRChange);
                            finalList.add(finalRecord);
                            finalRecord=new Record();
                            count=0;
                            //第二次开始将要合并的数条记录中的第一条
                            fRDate=list2345.get(i+1).getStr("date");
                            fRStartTime=list2345.get(i+1).getStr("start_time");
                            fREndTime=list2345.get(i+1).getStr("end_time");
                            fRCondition=list2345.get(i+1).getStr("status");
                            count++;
                        }
                        if (i==list2345.size()-2){
                            finalRecord.set("date",fRDate);
                            fRTime=fRStartTime+"-"+fREndTime;
                            finalRecord.set("time",fRTime);
                            finalRecord.set("condition",fRCondition);
                            if (fRCondition.equals("3")){
                                fRChange=count*every15Wage;
                            }else{
                                fRChange=-(count*every15Wage);
                            }
                            finalRecord.set("change",fRChange);
                            finalList.add(finalRecord);
                        }
                    }
                    fRDate="";
                    fRStartTime="";
                    fREndTime="";
                    fRTime="";
                    fRCondition="";
                    fRChange=-1;
                    count=0;
                }
            }

            //初始化时间格式
            SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm");//20:40

            //查询h_staff_clock表 迟到记录
            String sql6="select * from h_staff_clock where is_late='2' and staff_id=? and date>=? and date<=?";
            List<Record> list6=Db.find(sql6,staffId,dateStart,dateEnd);
            if (list6!=null&&list6.size()>0){
                for (Record r6:list6){
                    Record finalRecord =new Record();
                    fRDate=r6.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r6.getStr("start_time");
                    fREndTime=r6.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",6);

                    //计算迟到时间
                    String st=r6.getStr("start_time");
                    String et=r6.getStr("sign_in_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count6=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count6*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            //查询h_staff_clock表 早退记录
            String sql7="select * from h_staff_clock where is_leave_early='2' and staff_id=? and date>=? and date<=?";
            List<Record> list7=Db.find(sql7,staffId,dateStart,dateEnd);
            if (list7!=null&&list7.size()>0){
                for (Record r7:list7){
                    Record finalRecord =new Record();
                    fRDate=r7.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r7.getStr("start_time");
                    fREndTime=r7.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",7);

                    //计算早退时间
                    String st=r7.getStr("sign_back_time");
                    String et=r7.getStr("end_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count7=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count7*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            Collections.sort(finalList, new Comparator<Record>(){
                /*
                 * int compare(Record p1, Record p2) 返回一个基本类型的整型，
                 * 返回负数表示：p1的date 大于p2的date，
                 * 返回0 表示：p1和p2相等，
                 * 返回正数表示：p1的date 小于p2的date
                 */
                @Override
                public int compare(Record p1, Record p2) {
                    //按照record的date进行降序排列
                    if(p1.getStr("date").compareTo(p2.getStr("date"))>0){
                        return -1;
                    }
                    if(p1.getStr("date").compareTo(p2.getStr("date"))==0){
                        return 0;
                    }
                    return 1;
                }
            });

            jhm.put("month",monthIsSecondHalf);
            jhm.put("workHour",workHour);
            jhm.put("duePay",duePay);
            jhm.put("takePay",takePay);
            jhm.put("list",finalList);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }

    /**
     * 17.1.    经理查看员工日工资详情
     名称	查看员工日工资详情
     描述	根据日期和员工id查询员工日工资变动
     验证	无
     权限   无
     URL	http://localhost:8081/hrms/mgr/mobile/salary/showSalaryOneDay
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	string		否	员工id
     date	string		否	日期，格式：yyyy-MM-dd

     返回数据：
     返回格式	JSON
     成功	{
     "dayHour": "3",
     "takePay": "100",
     "duePay": "120",
     "list": [{
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
     dayHour:本日实际工作时长（小时）
     takePay:实得工资（元）
     duePay:应得工资（元）
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
    public void showSalaryOneDay(){
        JsonHashMap jhm=new JsonHashMap();
        //请求参数
        String staffId=getPara("staff_id");
        String date=getPara("date");

        try{
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
            String sql2="select number,real_number from h_work_time where staff_id=? and date=?";
            List<Record> setWorkHourList=Db.find(sql2,staffId,date);
            if (setWorkHourList!=null&&setWorkHourList.size()>0){
                for (Record r:setWorkHourList){
                    setWorkHour+=((float)r.getInt("number")*15.0/60.0);
                    workHour+=((float)r.getInt("real_number")*15.0/60.0);
                }
            }else {
                jhm.putCode(0).putMessage("工作记录不存在！");
            }

            //应得工资=时薪*应工作的时长
            float duePay=hourWage*setWorkHour;
            //实得工资=时薪*实际工作的时长
            float takePay=hourWage*workHour;

            //查询h_work_time中该用户一天的工作情况
            String sql3="select * from h_work_time where date=? and staff_id=? order by date ASC";
            List<Record> list1=Db.find(sql3,date,staffId);
            if (list1==null){
                jhm.putCode(0).putMessage("工作记录不存在！");
            }
            String sql4="";
            //存储该员工 当天的迟到早退减班加班记录（15分钟一条）
            List<Record> list2345;
            //存储该员工 当天的迟到早退减班加班记录（一段连续时间合并成一条）
            List<Record> finalList=new ArrayList<>();
            //finalRecord中的四个元素
            String fRDate="";
            String fRStartTime="";
            String fREndTime="";
            String fRTime="";
            String fRCondition="";
            float fRChange=-1;
            //记录当前合并记录的条数 以此来计算时间 继而算出fRChange
            int count=0;

            //遍历list1 查询h_work_time_detail表 找出每天迟到早退减班加班的情况加入list1中
            if (list1!=null&&list1.size()>0){
                //查询h_work_time_detail表该用户一天的迟到早退减班加班记录（15分钟一条）
                sql4="select h_work_time_detail.* from h_work_time_detail,h_work_time where h_work_time.date=? and h_work_time.staff_id=? and h_work_time.id=h_work_time_detail.work_time_id" +
                        " and h_work_time.date=h_work_time_detail.date and h_work_time.staff_id=h_work_time_detail.staff_id and h_work_time_detail.status!=0 and h_work_time_detail.status!=1 order by start_time ASC";
                list2345=Db.find(sql4,date,staffId);
                //finalList中的单条记录
                Record finalRecord=new Record();
                //遍历list2345 合并连续的相同情况
                for(int i=0;i< list2345.size()-1;i++){
                    //第一次将要合并的数条记录中的第一条
                    if (count==0){
                        fRDate=list2345.get(i).getStr("date");
                        fRStartTime=list2345.get(i).getStr("start_time");
                        fREndTime=list2345.get(i).getStr("end_time");
                        fRCondition=list2345.get(i).getStr("status");
                        count++;
                    }
                    //当本条记录的结束时间和下一条记录的开始时间相同且两条记录的状态相同时“合并”
                    if (list2345.get(i).getStr("end_time").equals(list2345.get(i+1).getStr("start_time"))&&list2345.get(i).getStr("status").equals(list2345.get(i+1).getStr("status"))){
                        fREndTime=list2345.get(i+1).getStr("end_time");
                        count++;
                    }else {
                        finalRecord.set("date",fRDate);
                        fRTime=fRStartTime+"-"+fREndTime;
                        finalRecord.set("time",fRTime);
                        finalRecord.set("condition",fRCondition);
                        if (fRCondition.equals("3")){
                            fRChange=count*every15Wage;
                        }else{
                            fRChange=-(count*every15Wage);
                        }
                        finalRecord.set("change",fRChange);
                        finalList.add(finalRecord);
                        finalRecord=new Record();
                        count=0;
                        //第二次开始将要合并的数条记录中的第一条
                        fRDate=list2345.get(i+1).getStr("date");
                        fRStartTime=list2345.get(i+1).getStr("start_time");
                        fREndTime=list2345.get(i+1).getStr("end_time");
                        fRCondition=list2345.get(i+1).getStr("status");
                        count++;
                    }
                    if (i==list2345.size()-2){
                        finalRecord.set("date",fRDate);
                        fRTime=fRStartTime+"-"+fREndTime;
                        finalRecord.set("time",fRTime);
                        finalRecord.set("condition",fRCondition);
                        if (fRCondition.equals("3")){
                            fRChange=count*every15Wage;
                        }else{
                            fRChange=-(count*every15Wage);
                        }
                        finalRecord.set("change",fRChange);
                        finalList.add(finalRecord);
                    }
                }
            }

            //初始化时间格式
            SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm");//20:40

            //查询h_staff_clock表 迟到记录
            String sql6="select * from h_staff_clock where is_late='2' and staff_id=? and date=?";
            List<Record> list6=Db.find(sql6,staffId,date);
            if (list6!=null&&list6.size()>0){
                for (Record r6:list6){
                    Record finalRecord =new Record();
                    fRDate=r6.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r6.getStr("start_time");
                    fREndTime=r6.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",6);

                    //计算迟到时间
                    String st=r6.getStr("start_time");
                    String et=r6.getStr("sign_in_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count6=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count6*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            //查询h_staff_clock表 早退记录
            String sql7="select * from h_staff_clock where is_leave_early='2' and staff_id=? and date=?";
            List<Record> list7=Db.find(sql7,staffId,date);
            if (list7!=null&&list7.size()>0){
                for (Record r7:list7){
                    Record finalRecord =new Record();
                    fRDate=r7.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r7.getStr("start_time");
                    fREndTime=r7.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",7);

                    //计算早退时间
                    String st=r7.getStr("sign_back_time");
                    String et=r7.getStr("end_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count7=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count7*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            Collections.sort(finalList, new Comparator<Record>(){
                /*
                 * int compare(Record p1, Record p2) 返回一个基本类型的整型，
                 * 返回负数表示：p1的date 大于p2的date，
                 * 返回0 表示：p1和p2相等，
                 * 返回正数表示：p1的date 小于p2的date
                 */
                @Override
                public int compare(Record p1, Record p2) {
                    //按照record的date进行降序排列
                    if(p1.getStr("date").compareTo(p2.getStr("date"))>0){
                        return -1;
                    }
                    if(p1.getStr("date").compareTo(p2.getStr("date"))==0){
                        return 0;
                    }
                    return 1;
                }
            });


            jhm.put("dayHour",workHour);
            jhm.put("duePay",duePay);
            jhm.put("takePay",takePay);
            jhm.put("list",finalList);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }

    /**
     * 17.2.  经理查看员工半月工资详情
     名称	查看员工半月工资详情
     描述	根据日期和员工id查询员工日工资变动
     验证	无
     权限   无
     URL	http://localhost:8081/hrms/mgr/mobile/salary/showSalaryHalfMounth2
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	string		否	员工id
     date	string		否	日期，格式：yyyy-MM-dd

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "name": "员工姓名",
     "job": "员工职位",
     "phone": "18104418508",
     "number": "员工工号",
     "month": "0",
     "workHour": "87",
     "takePay": "690",
     "duePay": "870",
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
     workHour:半月工作时长（小时）
     takePay:半月实得工资（元）
     duePay:半月应得工资（元）
     conition:1:迟到 2:早退 3:旷工 4:加班 5:减班 6:请假
     失败	{
     "code": 0,
     "message": "失败原因！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void showSalaryHalfMonth2(){

        JsonHashMap jhm=new JsonHashMap();

        //请求参数
        String staffId=getPara("staff_id");
        String date=getPara("date");

        try{
            //获取当月第一天
            String firstDayOfMonth=SalaryCtrl.getFirstDayOfMonth();
            //获取当月中间天
            String midDayOfMonth=SalaryCtrl.getMidDayOfMonth();
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
            String sql="select * from h_staff where id=?";
            Record staff=Db.findFirst(sql,staffId);
            float hourWage=-1;
            //每15分钟的薪水
            float every15Wage=-1;
            //员工姓名
            String name="";
            //员工姓名首字母
            String initial="";
            //员工职位
            String job="";
            //员工电话
            String phone="";
            //员工工号
            String number="";
            if(staff==null){
                jhm.putCode(0).putMessage("用户不存在！");
            }else{
                hourWage=staff.getFloat("hour_wage");
                every15Wage=hourWage/4;
                name=staff.getStr("name");
                initial=staff.getStr("pinyin").substring(0,1);
                job=staff.getStr("job");
                phone=staff.getStr("phone");
                number=staff.getStr("emp_num");
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
            }else {
                jhm.putCode(0).putMessage("工作记录不存在！");
            }

            //应得工资=时薪*应工作的时长
            float duePay=hourWage*setWorkHour;
            //实得工资=时薪*实际工作的时长
            float takePay=hourWage*workHour;

            //查询h_work_time中该用户在一段时间中 每天的工作情况
            String sql3="select * from h_work_time where date>=? and date <=? and staff_id=? order by date ASC";
            List<Record> list1=Db.find(sql3,dateStart,dateEnd,staffId);
            if (list1==null){
                jhm.putCode(0).putMessage("工作记录不存在！");
            }
            String sql4="";
            //存储该员工 当天的迟到早退减班加班记录（15分钟一条）
            List<Record> list2345;
            //存储该员工 当天的迟到早退减班加班记录（一段连续时间合并成一条）
            List<Record> finalList=new ArrayList<>();
            //finalRecord中的四个元素
            String fRDate="";
            String fRStartTime="";
            String fREndTime="";
            String fRTime="";
            String fRCondition="";
            float fRChange=-1;
            //记录当前合并记录的条数 以此来计算时间 继而算出fRChange
            int count=0;

            //遍历list1 查询h_work_time_detail表 找出每天迟到早退减班加班的情况加入list1中
            if (list1!=null&&list1.size()>0){
                for (Record r1:list1){
                    //找到该员工 当天的情况
                    sql4="select h_work_time_detail.* from h_work_time_detail,h_work_time where h_work_time.date=? and h_work_time.staff_id=? and h_work_time.id=h_work_time_detail.work_time_id" +
                            " and h_work_time.date=h_work_time_detail.date and h_work_time.staff_id=h_work_time_detail.staff_id and h_work_time_detail.status!=0 and h_work_time_detail.status!=1 order by start_time ASC";
                    list2345=Db.find(sql4,r1.getStr("date"),staffId);
                    //finalList中的单条记录
                    Record finalRecord=new Record();
                    //遍历list2345 合并连续的相同情况
                    for(int i=0;i< list2345.size()-1;i++){
                        //第一次将要合并的数条记录中的第一条
                        if (count==0){
                            fRDate=list2345.get(i).getStr("date");
                            fRStartTime=list2345.get(i).getStr("start_time");
                            fREndTime=list2345.get(i).getStr("end_time");
                            fRCondition=list2345.get(i).getStr("status");
                            count++;
                        }
                        //当本条记录的结束时间和下一条记录的开始时间相同且两条记录的状态相同时“合并”
                        if (list2345.get(i).getStr("end_time").equals(list2345.get(i+1).getStr("start_time"))&&list2345.get(i).getStr("status").equals(list2345.get(i+1).getStr("status"))){
                            fREndTime=list2345.get(i+1).getStr("end_time");
                            count++;
                        }else {
                            finalRecord.set("date",fRDate);
                            fRTime=fRStartTime+"-"+fREndTime;
                            finalRecord.set("time",fRTime);
                            finalRecord.set("condition",fRCondition);
                            if (fRCondition.equals("3")){
                                fRChange=count*every15Wage;
                            }else{
                                fRChange=-(count*every15Wage);
                            }
                            finalRecord.set("change",fRChange);
                            finalList.add(finalRecord);
                            finalRecord=new Record();
                            count=0;
                            //第二次开始将要合并的数条记录中的第一条
                            fRDate=list2345.get(i+1).getStr("date");
                            fRStartTime=list2345.get(i+1).getStr("start_time");
                            fREndTime=list2345.get(i+1).getStr("end_time");
                            fRCondition=list2345.get(i+1).getStr("status");
                            count++;
                        }
                        if (i==list2345.size()-2){
                            finalRecord.set("date",fRDate);
                            fRTime=fRStartTime+"-"+fREndTime;
                            finalRecord.set("time",fRTime);
                            finalRecord.set("condition",fRCondition);
                            if (fRCondition.equals("3")){
                                fRChange=count*every15Wage;
                            }else{
                                fRChange=-(count*every15Wage);
                            }
                            finalRecord.set("change",fRChange);
                            finalList.add(finalRecord);
                        }
                    }
                    fRDate="";
                    fRStartTime="";
                    fREndTime="";
                    fRTime="";
                    fRCondition="";
                    fRChange=-1;
                    count=0;
                }
            }

            //初始化时间格式
            SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm");//20:40

            //查询h_staff_clock表 迟到记录
            String sql6="select * from h_staff_clock where is_late='2' and staff_id=? and date>=? and date<=?";
            List<Record> list6=Db.find(sql6,staffId,dateStart,dateEnd);
            if (list6!=null&&list6.size()>0){
                for (Record r6:list6){
                    Record finalRecord =new Record();
                    fRDate=r6.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r6.getStr("start_time");
                    fREndTime=r6.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",6);

                    //计算迟到时间
                    String st=r6.getStr("start_time");
                    String et=r6.getStr("sign_in_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count6=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count6*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            //查询h_staff_clock表 早退记录
            String sql7="select * from h_staff_clock where is_leave_early='2' and staff_id=? and date>=? and date<=?";
            List<Record> list7=Db.find(sql7,staffId,dateStart,dateEnd);
            if (list7!=null&&list7.size()>0){
                for (Record r7:list7){
                    Record finalRecord =new Record();
                    fRDate=r7.getStr("date");
                    finalRecord.set("date",fRDate);
                    fRStartTime=r7.getStr("start_time");
                    fREndTime=r7.getStr("end_time");
                    fRTime=fRStartTime+"-"+fREndTime;
                    finalRecord.set("time",fRTime);
                    finalRecord.set("condition",7);

                    //计算早退时间
                    String st=r7.getStr("sign_back_time");
                    String et=r7.getStr("end_time");
                    Date fd=simpleFormat.parse(st);
                    Date td=simpleFormat.parse(et);
                    String fromDate = simpleFormat.format(fd);
                    String toDate = simpleFormat.format(td);
                    long from = simpleFormat.parse(fromDate).getTime();
                    long to = simpleFormat.parse(toDate).getTime();
                    int minutes = (int) ((to - from)/(1000 * 60));
                    int count7=(int)Math.ceil((double)minutes/(double)15);
                    fRChange=-(count7*every15Wage);
                    finalRecord.set("change",fRChange);

                    finalList.add(finalRecord);
                }
            }

            Collections.sort(finalList, new Comparator<Record>(){
                /*
                 * int compare(Record p1, Record p2) 返回一个基本类型的整型，
                 * 返回负数表示：p1的date 大于p2的date，
                 * 返回0 表示：p1和p2相等，
                 * 返回正数表示：p1的date 小于p2的date
                 */
                @Override
                public int compare(Record p1, Record p2) {
                    //按照record的date进行降序排列
                    if(p1.getStr("date").compareTo(p2.getStr("date"))>0){
                        return -1;
                    }
                    if(p1.getStr("date").compareTo(p2.getStr("date"))==0){
                        return 0;
                    }
                    return 1;
                }
            });

            jhm.putCode(1);
            jhm.put("name",name);
            jhm.put("firstName",initial);
            jhm.put("job",job);
            jhm.put("phone",phone);
            jhm.put("number",number);
            jhm.put("month",monthIsSecondHalf);
            jhm.put("workHour",workHour);
            jhm.put("duePay",duePay);
            jhm.put("takePay",takePay);
            jhm.put("list",finalList);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        renderJson(jhm);
    }
}
