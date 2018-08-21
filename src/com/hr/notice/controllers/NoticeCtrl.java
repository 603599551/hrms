package com.hr.notice.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class NoticeCtrl extends BaseCtrl {

    /**
     * 4.1.	通知列表
     * 名称	通知列表接口
     * 描述	从session中取出当前登录人的部门id，根据该部门id查询通知。
     * 验证	无
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/notice/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * <p>
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "totalRow": 1,
     * "pageNumber": 1,
     * "firstPage": true,
     * "lastPage": true,
     * "totalPage": 1,
     * "pageSize": 10,
     * "list": [{
     * "content": "内容摘要",//显示通知，内容不超过10字
     * "sender_name": "发送人名称",
     * "datetime": "日期",//格式：yyyy-MM-dd HH:mm
     * "id": "通知id",
     * "status": "0",//0表示未读，1表示已读
     * "status_text":"未读",
     * "show_in ": true//是否显示调入按钮。false：不显示，true显示
     * }]
     * }
     * <p>
     * }
     * 失败	{
     * "code": 0,
     * "message": ""//显示失败提示
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void list() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        List<Object> params = new ArrayList<>();

        //页码和每页数据量
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");

        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);

        String select = "SELECT n.content as content , (SELECT s.`name` FROM h_staff s WHERE s.id = n.sender_id ) as sender_name , left(n.create_time,16) as datetime , n.id as id , n.type as type , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = n.type AND d.parent_id = '1000') as type_text , n.`status` as `status` , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = n.`status` AND d.parent_id = '900') as status_text , (CASE n.`status` WHEN '0' THEN 'warning' ELSE 'success' END ) as status_color , (CASE n.type WHEN 'movein_notice' THEN 'true' ELSE 'false' END) as show_in ";
        StringBuilder sql = new StringBuilder(" FROM h_notice n where 1 = 1 and (n.type = 'apply_movein' OR n.type = 'movein_notice') ");

        String date = getPara("date");
        if (!StringUtils.isEmpty(date)) {
            date = date + "%";
            sql.append(" and n.create_time like ? ");
            params.add(date);
        }
        String status = getPara("status");
        if (!StringUtils.isEmpty(status)) {
            sql.append(" and n.status = ? ");
            params.add(status);
        }
        String type = getPara("type");
        if (!StringUtils.isEmpty(type)) {
            sql.append(" and n.type = ? ");
            params.add(type);
        }

        try {
            //把部门id转为经理id  按经理id来判断显示信息
            String dept = getPara("dept");
            if (!StringUtils.isEmpty(dept)) {
                Record managerId = Db.findFirst("select s.id from h_staff s where s.job = 'store_manager' and s.dept_id = ? ", dept);
                if (managerId == null) {
                    jhm.putCode(0).putMessage("该门店不存在!");
                    renderJson(jhm);
                    return;
                }
                sql.append(" and n.receiver_id = ? ");
                params.add(managerId.getStr("id"));
            }

            sql.append(" and n.receiver_id = ? order by n.create_time desc");
            String temp = usu.getUserId();
            params.add(temp);

            Page<Record> noticePage = Db.paginate(pageNum, pageSize, select, sql.toString(), params.toArray());
            for (int i = 0; i < noticePage.getList().size(); ++i) {
                noticePage.getList().get(i).set("datetime", noticePage.getList().get(i).getStr("datetime").substring(0, 10));
            }
            jhm.put("data", noticePage);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常!");
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"content\":\"申请调入内容\",\"sender_name\":\"马云\",\"datetime\":\"2018-06-28\",\"id\":\"通知id\",\"type\":1,\"type_text\":\"申请调入\",\"status\":\"0\",\"status_text\":\"未读\",\"status_color\":\"warning\",\"show_in \":true},{\"content\":\"调入通知内容\",\"sender_name\":\"马华腾\",\"datetime\":\"2018-06-28\",\"id\":\"通知id\",\"type\":2,\"type_text\":\"调入通知\",\"status\":\"0\",\"status_text\":\"未读\",\"status_color\":\"warning\",\"show_in \":true},{\"content\":\"离职申请内容\",\"sender_name\":\"刘强东\",\"datetime\":\"2018-06-29\",\"id\":\"通知id\",\"type\":3,\"type_text\":\"离职申请\",\"status\":\"1\",\"status_text\":\"已读\",\"status_color\":\"success\",\"show_in \":true}]}}");
    }

    /**
     * 4.2.	查看通知
     * 名称	查看通知
     * 描述	根据通知id，显示通知详细信息。并设置该条记录已读
     * 验证	根据id验证是否存在该通知
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/notice/showById
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	通知id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "content": "内容摘要",//显示通知，内容不超过10字
     * "sender_name": "发送人名称",
     * "datetime": "日期",//格式：yyyy-MM-dd HH:mm
     * "id": "通知id",
     * "status": "0",//0表示未读，1表示已读
     * "status_text":"未读",
     * "show_in ": true//是否显示调入按钮。false：不显示，true显示
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": ""//显示失败提示
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void showById() {
        JsonHashMap jhm = new JsonHashMap();

        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择查看的通知！");
            renderJson(jhm);
            return;
        }

        try {
            Record record = Db.findFirst("SELECT n.content as content , (SELECT s.`name` FROM h_staff s WHERE s.id = n.sender_id ) as sender_name , left(n.create_time,16) as datetime , n.id as id , n.`status` as `status` , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = n.`status` AND d.parent_id = '900') as status_text , (CASE n.type WHEN 'movein_notice' THEN 'true' ELSE 'false' END) as show_in FROM h_notice n WHERE n.id = ?", id);
            if (record == null) {
                jhm.putCode(0).putMessage("通知不存在！");
                renderJson(jhm);
                return;
            } else {
                jhm.put("data", record);
            }
            if (StringUtils.equals(record.getStr("status"), "0")) {
                record.set("status", "1");
            }
            Db.update("h_notice", record);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"content\":\"内容摘要\",\"sender_name\":\"发送人名称\",\"datetime\":\"日期\",\"id\":\"通知id\",\"status\":\"0\",\"status_text\":\"未读\",\"show_in \":true}}");
    }

    /**
     * 4.3.	查看通知中的申请详细信息
     * 名称	查看申请
     * 描述	业务说明：其他店申请从本店调派店员
     * 根据申请id查看申请详细信息
     * 并将该条通知设置为已读
     * 验证	根据id验证是否存在此申请
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/notice/showApplyById
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	申请id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "work_date": "上岗日期",
     * "type":"1",//1：申请调入，2：调入通知，3：离职申请
     * "type_text": "",//调动类别，参见数据字典
     * "from_store_name": "店名",//来源门店名称
     * "id": "该记录id",
     * "remark":"我家狗狗不喜欢",
     * "status_text": "未读",//申请调入状态，参见数据字典
     * }
     * <p>
     * }
     * 失败	{
     * "code": 0,
     * "message": "此记录不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "保存失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void showApplyById() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择记录！");
            renderJson(jhm);
            return;
        }

        try {
            Record record = Db.findFirst("SELECT (SELECT s.`name` FROM h_store s WHERE s.id = info.from_dept) as from_store_name , info.date as work_date , n.type as type , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = info.`status` AND d.parent_id = '700') as type_text , n.id as id , info.`desc` as remark , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = n.`status` AND d.parent_id = '900') as status_text FROM h_notice n , h_move_info info WHERE info.id = n.fid AND n.type = 'movein_notice' AND n.id = ?", id);
            if (record == null) {
                jhm.putCode(0).putMessage("此记录不存在");
            } else {
                jhm.put("data", record);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"work_date\":\"2018-06-28\",\"type\":\"1\",\"type_text\":\"申请调入\",\"from_store\":\"4a8d594591ea4c1eb708fcc8a5c67c47\",\"id\":\"该记录id\",\"remark\":\"这人可以用\",\"status_text\":\"未读\"}}");
    }

    /**
     * 4.5.	查看通知中的调入的详细信息
     * 名称	查看调入通知的详细信息
     * 描述	根据调出记录的id查看详细信息（注意是调出记录的id）
     * 验证	此记录是否存在
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/notice/showMoveInById
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	调出记录的id（注意是调出记录的id）
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "out_store_name": "长大店",//来源门店名称
     * "staffList":[
     * {
     * "name": "鹿晗",
     * "gender": "男",
     * "phone": "1370000",
     * "job": "员工",//职位
     * "kind": "服务员、传菜员",
     * "money": "16",
     * "work_type": "全职",//工作类型
     * }
     * ],
     * "date": "2018-06-23",//调出日期
     * "type": "2",//数据字典的字面值
     * "type_text": "调入",//数据字典的字面值
     * "remark":"我家狗狗不喜欢",
     * "id": ""//调出记录id
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "此记录不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "调入失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void showMoveInById() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择查看通知");
            renderJson(jhm);
            return;
        }

        try {
            Record infoRecord = Db.findFirst("SELECT info.date as date , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = info.`status` AND d.parent_id = '700') as type , info.id as id , (SELECT s.`name` FROM h_store s WHERE s.id = info.from_dept) as out_store_name FROM h_move_info info WHERE info.id = ? ", id);
            if (infoRecord == null) {
                jhm.putCode(0).putMessage("此记录不存在");
                renderJson(jhm);
                return;
            }
            List<Record> staffList = Db.find("SELECT REPLACE((SELECT GROUP_CONCAT(d.`name`) FROM h_dictionary d WHERE FIND_IN_SET(d.`value`,staff.kind)),',','/' )as kind, staff.phone as phone,(SELECT d.`name` FROM h_dictionary d WHERE d.`value` = staff.work_type AND d.parent_id = '300')as work_type,(CONCAT(staff.month_wage, '/' , staff.hour_wage )) as money,(SELECT d.`name` FROM h_dictionary d WHERE d.`value` = staff.job AND d.parent_id = '200')as job,staff.`name` as `name` , (CASE staff.gender WHEN '0' THEN '女' ELSE '男' END) as gender FROM h_move_staff staff WHERE staff.move_info_id = ?", id);
            infoRecord.set("staffList", staffList);
            jhm.put("data", infoRecord);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"out_store_name\":\"面对面（长大店）\",\"staffList\":[{\"name\":\"鹿晗\",\"gender\":\"男\",\"phone\":\"13888888888\",\"job\":\"员工\",\"kind\":\"收银员/传菜员\",\"money\":\"20\",\"work_type\":\"全职\"}],\"date\":\"2018-06-28\",\"type\":\"2\",\"type_text\": \"调入\",\"remark\":\"这个人可以用\",\"id\":\"id\"}}");
    }

    /**
     * 4.7.	辞职申请
     * 名称	店长查看员工辞职数据
     * 描述	店长查看员工辞职具体信息
     * 验证	此记录是否存在
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/notice/showStaffFireById
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	通知id
     * <p>
     * <p>
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "date": "辞职日期",
     * "name": "辞职员工姓名",
     * "user": "操作人",
     * "from_store_name": "店名",//来源门店名称
     * "type":"1",//1：申请调入，2：调入通知，3：离职申请
     * "type_text": "",//调动类别，参见数据字典
     * "id": "该记录id",
     * "remark":"我家狗狗不喜欢"
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "此记录不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "操作失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void showStaffFireById() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择查看的通知!");
            renderJson(jhm);
            return;
        }

        try {
            Record noticeRecord = Db.findFirst("SELECT n.fid as fid, (SELECT (SELECT store.`name` FROM h_store store WHERE store.id = s.dept_id) FROM h_staff s WHERE s.id = n.sender_id) as from_store , n.id as id , n.type as type , (SELECT d.`name` FROM h_dictionary d WHERE d.`value` = n.type AND d.parent_id = '1000') as type_text FROM h_notice n WHERE n.id = ?", id);
            if (noticeRecord == null) {
                jhm.putCode(0).putMessage("此记录不存在！");
                renderJson(jhm);
                return;
            }
            Record resignRecord = Db.findFirst("SELECT (SELECT s.`name` FROM h_staff s WHERE s.id = r.reviewer_id) as 'user',r.reason as 'desc',LEFT(r.apply_time,10) as date , (SELECT s.`name` FROM h_staff s WHERE s.id = r.applicant_id) as name FROM h_resign r WHERE r.id = ?", noticeRecord.getStr("fid"));
            noticeRecord.remove("fid");
            jhm.put("data", resignRecord);
            jhm.put("data", noticeRecord);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"date\":\"2018-06-29\",\"name\":\"小强\",\"user\":\"刘强东\",\"from_store\":\"4a8d594591ea4c1eb708fcc8a5c67c47\",\"type\":\"3\",\"type_text\":\"\",\"id\":\"该记录id\",\"remark\":\"世界那么大，我想去看看！\"}}");
    }
}
