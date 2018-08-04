package com.hr.mobile.leave.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.leave.services.LeaveSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.*;

public class LeaveCtrl extends BaseCtrl {


    /**
     * 5.1.根据人员id、日期查看请假信息
     名称	根据人员id、日期查看请假信息
     描述	根据人员id，日期查看请假信息
     验证	根据人员id验证员工是否存在
     日期格式是否正确
     如果是店长，可查看本店所有店员请假信息
     如果是店员，只能查看自己的请假信息
     权限
     URL	http://localhost:8081/mgr/mobile/leave/showDetailByStaffIdAndDate
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	员工id
     date	string		否	日期，格式：yyyy-MM-dd

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "staff_id": "10239723894",
     "date": "2018-07-20",
     "list":  [{
     "start": "07:00",
     "end": "07:15"
     }, {
     "start": "07:15",
     "end": "07:30"
     }]

     }
     staff_id：员工id
     date：当天日期
     list：请假时间段
     失败	{
     "code": 0,
     "message": "员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void showDetailByStaffIdAndDate(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        String date = getPara("date");
        UserSessionUtil usu = new UserSessionUtil(getRequest());

        //进行非空判断
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("日期不能为空！");
            renderJson(jhm);
            return;
        }

        try {
            String sql = "select count(*) as c, s.job as job from h_staff s where id = ? ";
            Record record = Db.findFirst(sql, id);
            if(record.getInt("c") != 0){
                if(StringUtils.equals("store_manager",record.getStr("job"))){ //店长
                    String noticeSearch = "SELECT leave_start_time AS lt, leave_end_time AS et FROM h_staff_leave s WHERE  s.date = ? AND s.store_id = ? ";
                    List<Record> recordList = Db.find(noticeSearch ,date, usu.getUserBean().getDeptId());
                    jhm.put("staff_id", id);
                    jhm.put("date", date);
                    jhm.put("list", recordList);
                    jhm.putCode(1);
                } else {  //员工
                    String noticeSearch = "select leave_start_time as lt, leave_end_time as et from h_staff_leave where staff_id = ? and date = ?";
                    List <Record> recordList = Db.find(noticeSearch, id, date);
                    jhm.put("staff_id", id);
                    jhm.put("date", date);
                    jhm.put("list", recordList);
                    jhm.putCode(1);
                }
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }




    /**
     * 5.2.店员请假
     名称	店员请假
     描述	店员请假。
     将请假信息保存到h_staff_leave表，并给店长发通知，将请假通知保存h_notice表

     验证	根据人员id验证员工是否存在
     日期格式是否正确
     权限	无
     URL	http://localhost:8081/mgr/mobile/leave/apply
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	员工id
     date	string		否	日期，格式：yyyy-MM-dd
     time	array		否	时间
     [{
     "start": "07:00",
     "end": "07:15"
     }, {
     "start": "07:15",
     "end": "07:30"
     }]

     reason	string	150	否	请假原因

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "提交成功，等待店长审核！"
     }

     失败	{
     "code": 0,
     "message": "员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void apply(){
        JsonHashMap jhm=new JsonHashMap();
        //获取用户信息的工具类
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String userId = getPara("id");
        /*
        getParameter()
         */
        String date=getPara("date");
        String time=getPara("time");
        String reason = getPara("reason");

        //进行非空判断
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("请输入请假的日期！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isBlank("reason")){
            jhm.putCode(0).putMessage("请输入请假原因！");
            renderJson(jhm);
            return;
        }
        Map paraMap=new HashMap();
        paraMap.put("usu",usu);
        paraMap.put("id", userId);
        paraMap.put("date",date);
        paraMap.put("time",time);
        paraMap.put("reason",reason);

        try {
            /*
            必须通过此方式创建service对象，否则事务不启用
             */
            LeaveSrv srv = enhance(LeaveSrv.class);
//            LeaveSrv srv=new LeaveSrv();
            jhm=srv.apply(paraMap);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生错误！");
        }
        renderJson(jhm);
    }


    /**
     * 5.3.店长查看请假列表
     名称	请假列表
     描述	查看请假列表信息，分页
     未审核信息全部查询，已审核的信息，每次查询10条记录
     验证	只有店长可使用
     权限	店长
     URL	http://localhost:8081/mgr/mobile/leave/list
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     pageNum	int		是	页数
     pageSize	int		是	每页显示记录数

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "reviewList": [{
     "pic": "头像url",
     "name": "李雷",
     "job": "职位",
     "date": "2018-07-30",
     "day": "周五",
     "time": "15:00-16:00",
     "reason": "天气太热，要家里蹲"
     }],
     "reviewedList": [{
     "pic": "头像url",
     "name": "李雷",
     "job": "职位",
     "date": "2018-07-30",
     "day": "周五",
     "time": "15:00-16:00",
     "reason": "天气太热，要家里蹲"
     }]

     }
     reviewList：待审核
     reviewedList：已审核
     失败	{
     "code": 0,
     "message": "员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void list(){
        JsonHashMap jhm = new JsonHashMap();
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");
        UserSessionUtil usu = new UserSessionUtil(getRequest());


        //为空时赋予默认值
        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);

        try {
            String sql = "SELECT s.pic AS pic, s.NAME AS name, ( SELECT d.`name` FROM h_dictionary d WHERE d.`value` = s.job ) job, i.date AS date, i.times AS time, i.reason AS reason, i.id as leave_info_id FROM h_staff s, h_staff_leave_info i WHERE i.staff_id = s.id AND s.dept_id = ? AND i.`status` = '0' ";
            List<Record> recordList = Db.find(sql, usu.getUserBean().getDeptId());

            DateTool dateTool = new DateTool();
            for(int i = 0; i < recordList.size(); i++){
                String date = recordList.get(i).getStr("date");
                dateTool = new DateTool();
                String day = dateTool.getWeekDayName(date);
                //获取星期几
                recordList.get(i).set("day",day);
            }
            jhm.put("reviewList",recordList);

            //已审核
            String sqlReviewed = "SELECT s.pic AS pic, s. NAME AS name, ( SELECT d.`name` FROM h_dictionary d WHERE d.`value` = s.job ) job, i.`status` AS status, i.date AS date, i.times AS time, i.reason AS reason, i.id as leave_info_id  FROM h_staff s, h_staff_leave_info i WHERE i.staff_id = s.id AND store_id = ? AND ( i.`status` = '1' OR i.`status` = '2' )";
            List<Record> records = Db.find(sqlReviewed, usu.getUserBean().getDeptId());
            for(int i = 0; i < records.size(); i++){
                String date = records.get(i).getStr("date");
                dateTool = new DateTool();
                String day = dateTool.getWeekDayName(date);
                records.get(i).set("day",day);
                if(StringUtils.equals(records.get(i).getStr("status"),"1")){ //若数据库中status为1(同意)时返回1，若为2(拒绝)时返回0
                    records.get(i).set("status","1");
                } else {
                    records.get(i).set("status","0");
                }
            }
            jhm.put("reviewedList", records);

            jhm.putCode(1);


        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    /**
     * 5.4.店长审核请假
     名称	审核请假
     描述	店长审核本店店员请假
     更新h_staff_leave表，并给请假人发通知，将请假通知保存h_notice表
     验证	根据id判断记录是否存在
     是否店长
     权限	店长
     URL	http://localhost:8081/mgr/mobile/leave/review
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	请假id
     status	string		否	0:审核通过
     1:审核拒绝
     result	string	150	否	审核结果

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "提交成功！"
     }

     失败	{
     "code": 0,
     "message": "记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void review(){
        JsonHashMap jhm = new JsonHashMap();
        String leaveId = getPara("id");
        String status = getPara("status");
        String result = getPara("result");
        UserSessionUtil usu = new UserSessionUtil(getRequest());

        //进行非空判断
        if(StringUtils.isEmpty(leaveId)){
            jhm.putCode(0).putMessage("请假id不能为空!");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(status)){
            jhm.putCode(0).putMessage("请审核！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(result) && StringUtils.equals("1",status)){
            jhm.putCode(0).putMessage("审核结果不能为空！");
            renderJson(jhm);
            return;
        }

        Map paraMap=new HashMap();
        paraMap.put("usu",usu);
        paraMap.put("leaveId",leaveId);
        paraMap.put("status",status);
        paraMap.put("result",result);

        try {
            LeaveSrv srv = enhance(LeaveSrv.class);
            jhm=srv.review(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    /**
     * 店长查看请假信息
     名称	店长查看请假信息
     描述	店长查看请假申请的详细信息
     验证	根据id判断记录是否存在
     [新增接口，店长查看员工请假信息]
     权限	店长
     URL	http://localhost:8081/mgr/mobile/leave/showById
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	请假id



     返回数据：
     返回格式	JSON
     成功	"code": 1,
     {
     "pic": "头像url",
     "name": "李雷",
     "job": "职位",
     “phone”:”133xxxxxxxx”电话号码
     "date": "2018-07-30",
     "day": "周五",
     "time": "15:00-16:00",
     "reason": "天气太热，要家里蹲"
     “status”:”0/1” 审核结果
     “result”:””审核结果回复
     },

     失败	{
     "code": 0,
     "message": "记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void showById(){
        JsonHashMap jhm = new JsonHashMap();
        DateTool dateTool = new DateTool();
        String leaveId = getPara("id");

        //进行非空验证
        if(StringUtils.isEmpty(leaveId)){
            jhm.putCode(0).putMessage("请假id不能为空！");
            renderJson(jhm);
            return;
        }

        try {
            String sql = "select count(*) as c from h_staff_leave_info where id = ? ";
            Record countR = Db.findFirst(sql, leaveId);
            if(countR.getInt("c") > 0){
                String search = "SELECT s.pic as pic, s.name as name, (SELECT d.name from h_dictionary d where d.value = s.job)as job, s.phone as phone, i.date as date, i.times as time, i.reason as reason, i.status as status, i.result as result from h_staff s, h_staff_leave_info i where i.staff_id = s.id and i.id = ? ";
                Record record = Db.findFirst(search, leaveId);
                record.set("day", dateTool.getWeekDayName(record.getStr("date")));
                //返回status,0为拒绝,1为同意
                if(StringUtils.equals("1",record.getStr("status"))){
                    record.set("status","1");
                } else {
                    record.set("status","0");
                }
                jhm.put("data",record);
                jhm.putCode(1);
            } else {
                jhm.putCode(0).putMessage("记录不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
