package com.hr.mobile.addresslist;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddressListCtrl extends BaseCtrl {
    /**
     * @author zhanjinqi
     * @date 2018-08-01
     * 名称  	显示通讯录信息
     * 描述	    根据岗位&职位/姓名首字母排序显示信息
     * 验证
     * 权限	    Hr可见
     * URL	    http://localhost:8081/mgr/mobile/addresslist
     * 请求方式    	get
     * 请求参数类型
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * deptid	string		不允许	根据店铺id显示本店信息
     * kind	string		否	    岗位名
     * 2.服务员 waiter
     * 3.传菜员 passed
     * 4.带位员 band
     * 5.清理员 cleanup
     * 6.输单员 inputorder
     * 7.收银员 cashier
     * job	    string		否	    职位名
     * 1.全部 all
     * 2.餐厅经理 store_manager
     * 3.副经理assistant_manager
     * 4.见习经理trainee_manager
     * 5.训练员trainer
     * 6.员工staff
     * <p>
     * 返回数据：
     * <p>
     * 返回格式	JSON
     * 成功
     * 若为员工，则返回
     * [{
     * "code": 1,
     * "name": "姓名",
     * "left(pinyin,1)": "姓名首字母",
     * "kind": "岗位",
     * "phone": "电话"
     * },
     * {
     * "code": 1,
     * "name": "姓名",
     * "left(pinyin,1)": "姓名首字母",
     * "kind": "岗位",
     * "phone": "电话"
     * }..]
     * <p>
     * 若非员工，则返回
     * [{
     * "code": 1,
     * "name": "姓名",
     * "left(pinyin,1)": "姓名首字母",
     * "job": "职位",
     * "phone": "电话"
     * },
     * {
     * "code": 1,
     * "name": "姓名",
     * "left(pinyin,1)": "姓名首字母",
     * "job": "职位",
     * "phone": "电话"
     * }..]
     * <p>
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */

    //员工端
    public void sortByInitial() {
        JsonHashMap jhm = new JsonHashMap();
        String deptId = getPara("deptid");
        String job = getPara("job");  //职位
        String kind = getPara("kind"); //岗位
        String init;    //首字母
        try {
            String sql;
            List<Record> list;
            //若全部职位参与排序
            if (job.equals("all")) {
                sql = "select name,pinyin,job,phone from h_staff where dept_id=? order by left(pinyin,1) ASC";
                list = Db.find(sql, deptId);
                if (list != null && list.size() > 0) {

                    for (Record r : list) {
                        init=r.getStr("pinyin").substring(0, 1);
                        r.set("pinyin",init);
                        //若是员工，则显示岗位
                        if (r.getStr("job") .equals("staff")) {
                            r.set("job", r.getStr("kind"));
                        }
                    }
                }
            }
            //特定职位/岗位排序
            else {
                //员工职位 返回岗位
                if (job.equals("staff")) {
                    sql = "select name,pinyin,kind,phone from h_staff where dept_id=? and kind=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, kind);
                }//非员工 则返回职位
                else {
                    sql = "select name,pinyin,job,phone from h_staff where dept_id=? and job=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, job);
                }
                if (list != null && list.size() > 0) {
                    for (Record r : list) {
                        init=r.getStr("pinyin").substring(0, 1);
                        r.set("pinyin",init);
                    }
                }
            }

            //存放相同首字母的list
            List<Record> l=new ArrayList<>();
            //存放分组了的list
            List<List> sList = new ArrayList<>();
            //记录首字母
            String initial="";
            //计数
            int count=0;

            //遍历list将数据按首字母分组
            if (list != null && list.size() > 0) {
                for (Record r : list) {
                    count++;
                    if(initial.equals(r.getStr("pinyin"))){
                        l.add(r);
                    }else{
                        //首次
                        if(initial.equals("")){
                            l.add(r);
                        }
                        else{
                            sList.add(l);
                            l=new ArrayList<>();
                            l.add(r);
                            if(r==list.get(list.size()-1)){
                                sList.add(l);
                            }
                        }
                    }
                    initial=r.getStr("pinyin");
                }
            }
            jhm.put("data", sList);
            jhm.put("count",count);
        }catch(Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
            renderJson(jhm);
        }


    private static Calendar calendar = Calendar.getInstance();//实例化日历

    // 获取当月第一天
    public static String getFirstDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 获取当月第一天
    public static String getMidDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 15);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 获取当天时间
    public static String getNowTime(String dateformat) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        return hehe;
    }

    //经理端
    public void sortByInitial2() {
        JsonHashMap jhm = new JsonHashMap();
        String deptId = getPara("deptid");
        String job = getPara("job");  //职位
        String kind = getPara("kind"); //岗位
        String init;    //首字母

        int timesOfLeave;   //请假次数
        int timesOfLate;   //迟到次数
        int timesOfEarly;   //早退次数

        int day =calendar.get(Calendar.DAY_OF_MONTH);   //本月的第几天
        String dateStart="";    //开始日期
        String dateEnd="";  //结束日期
        if(day<=15){
            dateStart=AddressListCtrl.getFirstDayOfMonth();
        }else{
            dateStart=AddressListCtrl.getMidDayOfMonth();
        }
        //dateStart="2018-07-01";
        dateEnd=AddressListCtrl.getNowTime("yyyy-MM-dd");

        try {
            String sql; //查找符合条件的人
            String sql2;    //查找每个人的请假次数
            String sql3;    //查找每个人的迟到次数
            String sql4;    //查找每个人的早退次数
            String staffId;//人员id
            List<Record> list;
            sql2="select count(*) from h_staff_leave_info where store_id=? and staff_id=? and date>=? and date<=? and status='1'";
            sql3="select count(*) from h_staff_clock where store_id=? and staff_id=? and date>=? and date<=? and is_late=2";
            sql4="select count(*) from h_staff_clock where store_id=? and staff_id=? and date>=? and date<=? and is_leave_early=2";
            //若全部职位参与排序
            if (job.equals("all")) {
                sql = "select id,name,pinyin,job,phone from h_staff where dept_id=? order by left(pinyin,1) ASC";
                list = Db.find(sql, deptId);
                if (list != null && list.size() > 0) {
                    for (Record r : list) {
                        staffId=r.getStr("id");
                        timesOfLeave=Db.findFirst(sql2,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        timesOfLate=Db.findFirst(sql3,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        timesOfEarly=Db.findFirst(sql4,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        r.set("timeofleave",timesOfLeave);
                        r.set("timeoflate",timesOfLate);
                        r.set("timeofearly",timesOfEarly);
                        init=r.getStr("pinyin").substring(0, 1);
                        r.set("pinyin",init);
                        //若是员工，则显示岗位
                        if (r.getStr("job") .equals("staff") ) {
                            r.set("job", r.getStr("kind"));
                        }
                    }
                }
            }
            //特定职位/岗位排序
            else {
                //员工职位 返回岗位
                if (job.equals("staff")) {
                    sql = "select id,name,pinyin,kind,phone from h_staff where dept_id=? and kind=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, kind);
                }//非员工 则返回职位
                else {
                    sql = "select id,name,pinyin,job,phone from h_staff where dept_id=? and job=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, job);
                }
                if (list != null && list.size() > 0) {
                    for (Record r : list) {
                        staffId=r.getStr("id");
                        timesOfLeave=Db.findFirst(sql2,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        timesOfLate=Db.findFirst(sql3,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        timesOfEarly=Db.findFirst(sql4,deptId,staffId,dateStart,dateEnd).getInt("count(*)");
                        r.set("timeofleave",timesOfLeave);
                        r.set("timeoflate",timesOfLate);
                        r.set("timeofearly",timesOfEarly);
                        init=r.getStr("pinyin").substring(0, 1);
                        r.set("pinyin",init);
                    }
                }
            }

            //存放相同首字母的list
            List<Record> l=new ArrayList<>();
            //存放分组了的list
            List<List> sList = new ArrayList<>();
            //记录首字母
            String initial="";
            //计数
            int count=0;


            //遍历list将数据按首字母分组
            if (list != null && list.size() > 0) {
                for (Record r : list) {
                    count++;
                    if(initial.equals(r.getStr("pinyin"))){
                        l.add(r);
                    }else{
                        //首次
                        if(initial.equals("")){
                            l.add(r);
                        }
                        else{
                            sList.add(l);
                            l=new ArrayList<>();
                            l.add(r);
                            if(r==list.get(list.size()-1)){
                                sList.add(l);
                            }
                        }
                    }
                    initial=r.getStr("pinyin");
                }
            }
            jhm.put("data", sList);
            jhm.put("count",count);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
