package com.jsoft.crm.timer;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import utils.DateTool;
import utils.NumberUtils;
import utils.UUIDTool;

import java.util.Calendar;
import java.util.List;
/*
每月1日凌晨2:30分执行

统计市场部员工招生情况
（由于是每月1日执行，所以统计的是上个月的情况，注意时间计算）
1.按照分配时间，统计上个月之前（不包含上个月）的未报名学员
2.按照分配时间，统计上个月的未报名学员
3.按照分配时间，统计上个月的报名学员
 */
public class MyTranslateTimer implements Job {
    static final String beforeSQL="select count(s.id) as count from student s,student_owner so where s.id=so.student_id and s.status in ('9','16') and so.staff_id=? and so.create_time<=?";
    static final String inputSQL ="select count(s.id) as count from student s,student_owner so where s.id=so.student_id and so.staff_id=? and so.create_time like ?";
    static final String signSQL ="select count(s.id) as count from student s,student_owner so where s.id=so.student_id and so.staff_id=? and s.status='10' and s.enroll_time like ? ";
    public void exe() throws Exception{
        //计算上上个月的最后一天的23:59:59
        Calendar lastMonthCld=Calendar.getInstance();
        lastMonthCld.add(Calendar.MONTH,-1);//计算上个月
        String lastMonth= DateTool.getDate(lastMonthCld.getTime(),"yyyy-MM");
        String lastMonthWhere=lastMonth+"%";
        lastMonthCld.set(Calendar.DAY_OF_MONTH,1);
        lastMonthCld.add(Calendar.DAY_OF_MONTH,-1);//计算上上个月的最后一天
        lastMonthCld.set(Calendar.HOUR_OF_DAY,23);
        lastMonthCld.set(Calendar.MINUTE,59);
        lastMonthCld.set(Calendar.SECOND,59);

        /*
         * 计算上上个月的最后一天
         */
        String lastMonthEnd=DateTool.getDate(lastMonthCld.getTime(),"yyyy-MM-dd HH:mm:ss");

        String currentTime=DateTool.GetDateTime();
        try {
            //查询市场部门员工
            List<Record> list = Db.find("select id,name from staff where dept=?", "0401709b7b5e47d8825bf91233e5aadc");
            for (Record staff : list) {
                String staffId = staff.getStr("id");
                String staffName = staff.getStr("name");
                //查询该员工上个月之前未报名的学生
                Record beforeCountR = Db.findFirst(beforeSQL, staffId, lastMonthEnd);
                Object beforeCountObj = beforeCountR.get("count");
                int beforeCount = NumberUtils.parseInt(beforeCountObj, 0);
                //查询该员工上个月新入库的学生
                Record rukuCountR = Db.findFirst(inputSQL, staffId, lastMonthWhere);
                Object rukuCountObj = rukuCountR.get("count");
                int rukuCount = NumberUtils.parseInt(rukuCountObj, 0);
                //查询该员工上个月报名的学生
                Record baomingCountR = Db.findFirst(signSQL, staffId, lastMonthWhere);
                Object baomingCountObj = baomingCountR.get("count");
                int signCount = NumberUtils.parseInt(baomingCountObj, 0);

                Record saveR = new Record();
                saveR.set("id", UUIDTool.getUUID());
                saveR.set("before_input_num", beforeCount);
                saveR.set("month_input_num", rukuCountObj);
                saveR.set("month_sign_num", signCount);
                saveR.set("staff_id", staffId);
                saveR.set("staff_name", staffName);
                saveR.set("create_time", currentTime);
                saveR.set("month", lastMonth);
                Db.save("translate_staff", saveR);
            }
        }catch(Exception e){
            throw e;
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            exe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
