package com.hr.mobile.addresslist;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AddressListCtrl class
 * @author zhanjinqi
 * @date 2018-08-06
 */
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
        //职位
        String job = getPara("job");
        //岗位 得到一个
        String kind = getPara("kind");
        String init;    //首字母
        try {
            String sql="";
            String sql7="";
            String sql6="";
            String jobNames="";
            List<Record> list;
            List<Record> list7;
            //若全部职位参与排序
            if ("all".equals(job)) {
                sql = "select id,name,pinyin,job,phone,kind from h_staff where dept_id=? order by left(pinyin,1) ASC";
                list = Db.find(sql, deptId);
                if (list != null && list.size() > 0) {
                    for (Record r : list) {
                        init=r.getStr("pinyin").substring(0, 1);
                        r.set("pinyin",init);
                        //若是员工，则显示岗位
                        if ("staff".equals(r.getStr("job"))) {
                            r.set("job", r.getStr("kind"));
                            sql7="select h_dictionary.name as name from h_dictionary,h_staff where find_in_set(h_dictionary.value,h_staff.kind) and h_staff.id=?";
                            list7=Db.find(sql7,r.getStr("id"));
                            if (list7 != null && list7.size() > 0){
                                for (Record r7:list7){
                                    if (r7==list7.get(0)){
                                        jobNames=r7.getStr("name");
                                    }else{
                                        jobNames+=","+r7.getStr("name");
                                    }
                                }
                            }
                            r.set("job",jobNames);
                        }else{
                            sql6="select h_dictionary.name as name from h_dictionary,h_staff where h_staff.job=h_dictionary.value and h_staff.id=?";
                            Record chineseName=Db.findFirst(sql6,r.getStr("id"));
                            if (chineseName==null){
                                jhm.putCode(0).putMessage("找不到中文名！");
                            }else{
                                String jobName=chineseName.getStr("name");
                                r.set("job",jobName);
                            }
                        }
                    }
                }
            }
            //特定职位/岗位排序
            else {
                //员工职位 返回岗位
                if ("staff".equals(job)) {
                    sql = "select id,name,pinyin,kind,phone from h_staff where dept_id=? and kind like CONCAT('%',?,'%') order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, kind);
                    if (list != null && list.size() > 0){
                        for (Record r : list){
                            sql7="select h_dictionary.name as name from h_dictionary,h_staff where find_in_set(h_dictionary.value,h_staff.kind) and h_staff.id=?";
                            list7=Db.find(sql7,r.getStr("id"));
                            if (list7 != null && list7.size() > 0){
                                for (Record r7:list7){
                                    if (r7==list7.get(0)){
                                        jobNames=r7.getStr("name");
                                    }else{
                                        jobNames+=","+r7.getStr("name");
                                    }
                                }
                            }
                            r.set("kind",jobNames);
                        }
                    }
                }//非员工 则返回职位
                else {
                    sql = "select id,name,pinyin,job,phone from h_staff where dept_id=? and job=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, job);
                    if (list != null && list.size() > 0){
                        for (Record r : list){
                            sql6="select h_dictionary.name as name from h_dictionary,h_staff where h_staff.job=h_dictionary.value and h_staff.id=?";
                            String jobName=Db.findFirst(sql6,r.getStr("id")).getStr("name");
                            r.set("job",jobName);
                        }
                    }
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
                for (int i=0;i<list.size();i++){
                    if (i==0){
                        initial=list.get(0).getStr("pinyin");
                        l.add(list.get(0));
                    }else {
                        if (initial.equals(list.get(i).getStr("pinyin"))){
                            l.add(list.get(i));
                        }else {
                            sList.add(l);
                            l=new ArrayList<>();
                            initial=list.get(i).getStr("pinyin");
                            l.add(list.get(i));
                        }

                    }
                    if (i==list.size()-1){
                        sList.add(l);
                    }
                    count++;
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

    //实例化日历
    private static Calendar calendar = Calendar.getInstance();

    // 获取当月第一天
    public static String getFirstDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        // 设为当前月的1号
        lastDate.set(Calendar.DATE, 1);
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 获取当月第一天
    public static String getMidDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        // 设为当前月的1号
        lastDate.set(Calendar.DATE, 15);
        str = sdf.format(lastDate.getTime());
        return str;
    }

    // 获取当天时间
    public static String getNowTime(String dateformat) {
        Date now = new Date();
        // 可以方便地修改日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
        String hehe = dateFormat.format(now);
        return hehe;
    }

    //经理端
    public void sortByInitial2() {
        JsonHashMap jhm = new JsonHashMap();
        String deptId = getPara("deptid");
        //职位
        String job = getPara("job");
        //岗位
        String kind = getPara("kind");
        //首字母
        String init;

        //请假次数
        int timesOfLeave;
        //迟到次数
        int timesOfLate;
        //早退次数
        int timesOfEarly;

        //本月的第几天
        int day =calendar.get(Calendar.DAY_OF_MONTH);
        //开始日期
        String dateStart="";
        //结束日期
        String dateEnd="";
        if(day<=15){
            dateStart=AddressListCtrl.getFirstDayOfMonth();
        }else{
            dateStart=AddressListCtrl.getMidDayOfMonth();
        }
        dateEnd=AddressListCtrl.getNowTime("yyyy-MM-dd");

        try {
            //查找符合条件的人
            String sql;
            //查找每个人的请假次数
            String sql2;
            //查找每个人的迟到次数
            String sql3;
            //查找每个人的早退次数
            String sql4;
            //人员id
            String staffId;
            String sql7="";
            String sql6="";
            String jobNames="";
            List<Record> list7;
            List<Record> list;
            sql2="select count(*) from h_staff_leave_info where store_id=? and staff_id=? and date>=? and date<=? and status='1'";
            sql3="select count(*) from h_staff_clock where store_id=? and staff_id=? and date>=? and date<=? and is_late=2";
            sql4="select count(*) from h_staff_clock where store_id=? and staff_id=? and date>=? and date<=? and is_leave_early=2";

            //若全部职位参与排序
            if ("all".equals(job)) {
                sql = "select id,name,pinyin,job,phone,kind from h_staff where dept_id=? order by left(pinyin,1) ASC";
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
                        if ("staff".equals(r.getStr("job"))) {
                            r.set("job", r.getStr("kind"));
                            sql7="select h_dictionary.name as name from h_dictionary,h_staff where find_in_set(h_dictionary.value,h_staff.kind) and h_staff.id=?";
                            list7=Db.find(sql7,r.getStr("id"));
                            if (list7 != null && list7.size() > 0){
                                for (Record r7:list7){
                                    if (r7==list7.get(0)){
                                        jobNames=r7.getStr("name");
                                    }else{
                                        jobNames+=","+r7.getStr("name");
                                    }
                                }
                            }
                            r.set("job",jobNames);
                        }else{
                            sql6="select h_dictionary.name as name from h_dictionary,h_staff where h_staff.job=h_dictionary.value and h_staff.id=?";
                            Record chineseName=Db.findFirst(sql6,r.getStr("id"));
                            if (chineseName==null){
                                jhm.putCode(0).putMessage("找不到中文名！");
                            }else{
                                String jobName=chineseName.getStr("name");
                                r.set("job",jobName);
                            }
                        }
                    }
                }
            }
            //特定职位/岗位排序
            else {
                //员工职位 返回岗位
                if ("staff".equals(job)) {
                    sql = "select id,name,pinyin,kind,phone from h_staff where dept_id=? and kind like CONCAT('%',?,'%') order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, kind);
                    if (list != null && list.size() > 0){
                        for (Record r : list){
                            sql7="select h_dictionary.name as name from h_dictionary,h_staff where find_in_set(h_dictionary.value,h_staff.kind) and h_staff.id=?";
                            list7=Db.find(sql7,r.getStr("id"));
                            if (list7 != null && list7.size() > 0){
                                for (Record r7:list7){
                                    if (r7==list7.get(0)){
                                        jobNames=r7.getStr("name");
                                    }else{
                                        jobNames+=","+r7.getStr("name");
                                    }
                                }
                            }
                            r.set("kind",jobNames);
                        }
                    }
                }//非员工 则返回职位
                else {
                    sql = "select id,name,pinyin,job,phone from h_staff where dept_id=? and job=? order by left(pinyin,1) ASC";
                    list = Db.find(sql, deptId, job);
                    if (list != null && list.size() > 0){
                        for (Record r : list){
                            sql6="select h_dictionary.name as name from h_dictionary,h_staff where h_staff.job=h_dictionary.value and h_staff.id=?";
                            String jobName=Db.findFirst(sql6,r.getStr("id")).getStr("name");
                            r.set("job",jobName);
                        }
                    }
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
                for (int i=0;i<list.size();i++){
                    if (i==0){
                        initial=list.get(0).getStr("pinyin");
                        l.add(list.get(0));
                    }else {
                        if (initial.equals(list.get(i).getStr("pinyin"))){
                            l.add(list.get(i));
                        }else {
                            sList.add(l);
                            l=new ArrayList<>();
                            initial=list.get(i).getStr("pinyin");
                            l.add(list.get(i));
                        }

                    }
                    if (i==list.size()-1){
                        sList.add(l);
                    }
                    count++;
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

    public void addressNum(){
        JsonHashMap jhm = new JsonHashMap();
        String deptId = getPara("deptid");
        String job1="store_manager";
        String job2="assistant_manager";
        String job3="trainee_manager";
        String job4="trainer";
        String kind1="waiter";
        String kind2="passed";
        String kind3="band";
        String kind4="cleanup";
        String kind5="inputorder";
        String kind6="cashier";

        try{
            String sql="select count(*)as c from h_staff where kind like CONCAT('%',?,'%') and dept_id=?";
            String sql2="select count(*)as c from h_staff where job=? and dept_id=?";
            //数据类型有可能是int long 等等
            Object countObj1=Db.findFirst(sql2,job1,deptId).get("c");
            //将object转化为int          ..
            int count1= NumberUtils.parseInt(countObj1,0);

            Object countObj2=Db.findFirst(sql2,job2,deptId).get("c");
            int count2= NumberUtils.parseInt(countObj2,0);

            Object countObj3=Db.findFirst(sql2,job3,deptId).get("c");
            int count3= NumberUtils.parseInt(countObj3,0);

            Object countObj4=Db.findFirst(sql2,job4,deptId).get("c");
            int count4= NumberUtils.parseInt(countObj4,0);

            Object countObj5=Db.findFirst(sql,kind1,deptId).get("c");
            int count5= NumberUtils.parseInt(countObj5,0);

            Object countObj6=Db.findFirst(sql,kind2,deptId).get("c");
            int count6= NumberUtils.parseInt(countObj6,0);

            Object countObj7=Db.findFirst(sql,kind3,deptId).get("c");
            int count7= NumberUtils.parseInt(countObj7,0);

            Object countObj8=Db.findFirst(sql,kind4,deptId).get("c");
            int count8= NumberUtils.parseInt(countObj8,0);

            Object countObj9=Db.findFirst(sql,kind5,deptId).get("c");
            int count9= NumberUtils.parseInt(countObj9,0);

            Object countObj10=Db.findFirst(sql,kind6,deptId).get("c");
            int count10= NumberUtils.parseInt(countObj10,0);

            Record r=new Record();
            r.set("store_managerNum",count1);
            r.set("assistant_managerNum",count2);
            r.set("trainee_managerNum",count3);
            r.set("trainerNum",count4);
            r.set("waiterNum",count5);
            r.set("passedNum",count6);
            r.set("bandNum",count7);
            r.set("cleanupNum",count8);
            r.set("inputorderNum",count9);
            r.set("cashierNum",count10);

            jhm.putCode(1);
            jhm.put("numbers",r);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
