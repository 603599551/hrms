package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.store.service.ApplyService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class ApplyCtrl extends BaseCtrl {

    /**
     * 申请调入状态 状态值->中文
     */
    public String translate(String str) {
        String sql = "SELECT name FROM h_dictionary WHERE parent_id='5000' AND value=?";
        Record r = Db.findFirst(sql, str);
        if (r != null) {
            return r.getStr("name");
        } else {
            return null;
        }
    }

    /**
     * 9.1.	申请调入员工列表
     * 名称	显示申请调入员工列表
     * 描述	根据当前登录人所在的门店id，查询本店申请调入员工列表。
     * 不考虑Hr访问。
     * 验证	无
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/apply/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "list": [{
     * "work_date": "上岗日期",
     * "type_text": "",//调动类别
     * "from_store_name": "店名",//来源门店名称
     * "id": "该记录id",
     * "status_text": "未读",//参见数据字典
     * "store_color": "#b7a6d4",//颜色值
     * "status":"0" //状态值，参见数据字典
     * }]
     * }
     * 失败	{
     * "code": 0,
     * "message": "提示失败信息！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    @Override
    public void list() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());

        try {
            //当前登录人id
            String userId = usu.getUserId();
            if (StringUtils.isEmpty(userId)) {
                jhm.putCode(0).putMessage("userId为空！");
                renderJson(jhm);
                return;
            }

            //通过userId查询其门店id
            String sql1 = "SELECT dept_id AS deptId FROM h_staff WHERE id=?";
            Record r1 = Db.findFirst(sql1, userId);
            if (r1 == null) {
                jhm.putCode(0).putMessage("当前登录人的门店id为空！");
                renderJson(jhm);
                return;
            }
            String deptId = r1.getStr("deptId");

            String sql2 = "SELECT work_date,type AS type_text,from_dept AS from_store_name,id,status AS status_text  FROM h_apply_move WHERE to_dept=?";
            List<Record> list2 = Db.find(sql2, deptId);

            //通过门店id查询store_color
            String sql3 = "SELECT store_color FROM h_store WHERE id=?";
            Record r3 = Db.findFirst(sql3, deptId);
            if (r3 == null) {
                jhm.putCode(0).putMessage("store_color为空！");
                renderJson(jhm);
                return;
            }
            String storeColor = r3.getStr("store_color");

            if (list2 != null && list2.size() > 0) {
                for (Record r2 : list2) {
                    //数字->中文
                    String status = r2.getStr("status_text");
                    r2.set("status", status);
                    r2.set("status_text", translate(status));
                    r2.set("store_color", storeColor);
                }
            }
            jhm.putCode(1);
            jhm.put("data", list2);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

    /**
     * 9.2.	申请调入
     * 名称	申请调入店员
     * 描述	申请调入店员。将数据保存到申请表，并向“来源门店”发通知
     * 验证	无
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/apply/moveIn
     * 请求方式	Post
     * 请求参数类型	JSON
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * work_date	string		否	上岗日期
     * type	string		否	调动类别id
     * from_store	string		否	来源门店id
     * desc	string		否	说明
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "提交成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "请输入上岗日期！"
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
    public void moveIn() {
        JsonHashMap jhm = new JsonHashMap();

        //上岗日期word_date
        String workDate = getPara("work_date");
        if (StringUtils.isEmpty(workDate)) {
            jhm.putCode(0).putMessage("上岗时间不能为空！");
            renderJson(jhm);
            return;
        }
        //调动类别id type
        String type = getPara("type");
        if (StringUtils.isEmpty(type) || StringUtils.equals(type, "-1")) {
            jhm.putCode(0).putMessage("调动类型不能为空！");
            renderJson(jhm);
            return;
        }
        //来源门店id from_dept
        String fromStore = getPara("from_store");
        if (StringUtils.isEmpty(fromStore) || StringUtils.equals(fromStore, "-1")) {
            jhm.putCode(0).putMessage("来源部门不能为空！");
            renderJson(jhm);
            return;
        }
        //说明 reason
        String desc = getPara("desc");
        if (StringUtils.isEmpty(desc)) {
            jhm.putCode(0).putMessage("说明不能为空！");
            renderJson(jhm);
            return;
        }

        String applymoveId = UUIDTool.getUUID();
        String noticeId = UUIDTool.getUUID();
        String createTime = DateTool.GetDateTime();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String createrId = usu.getUserId();

        try {
            //查找当前门店的id
            Record recordInfo = Db.findFirst("SELECT (SELECT store.id FROM h_store store WHERE store.id = staff.dept_id) to_dept FROM h_staff staff WHERE staff.id = ?", createrId);
            Record recordNotice = new Record();

            //获取目标门店店长Id
            Record recordFromDeptId = Db.findFirst("SELECT (SELECT staff.id FROM h_staff staff WHERE staff.job = 'store_manager' AND staff.dept_id = store.id) id FROM  h_store store WHERE store.id = ?", fromStore);
            if (recordFromDeptId == null) {
                jhm.putCode(0).putMessage("来源门店店长id为空！");
                renderJson(jhm);
                return;
            }
            String reviewerId = recordFromDeptId.getStr("id");

            //info相关信息存入apply_move表
            recordInfo.set("id", applymoveId);
            recordInfo.set("from_dept", fromStore);
            recordInfo.set("reason", desc);
            recordInfo.set("status", "0");
            recordInfo.set("work_date", workDate);
            recordInfo.set("type", type);
            recordInfo.set("creater_id", createrId);
            recordInfo.set("create_time", createTime);
            recordInfo.set("reason", desc);
            recordInfo.set("reviewer_id", reviewerId);
            recordInfo.set("review_time", null);
            recordInfo.set("review_result", null);
            boolean flagInfo = Db.save("h_apply_move", recordInfo);

            //notice相关信息存入noitce表
            recordNotice.set("id", noticeId);
            recordNotice.set("title", "申请调入内容");
            recordNotice.set("content", desc);
            recordNotice.set("sender_id", createrId);
            recordNotice.set("receiver_id", reviewerId);
            recordNotice.set("create_time", createTime);
            recordNotice.set("status", "0");
            recordNotice.set("type", "apply_movein");
            recordNotice.set("fid", applymoveId);
            boolean flagNotice = Db.save("h_notice", recordNotice);

            if (flagInfo && flagNotice) {
                jhm.putCode(1).putMessage("保存成功！");
            } else {
                jhm.putCode(0).putMessage("保存失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    public void getFromStoreDict(){
        //当前登录人信息
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        //登录人id
        String staffId=usu.getUserId();
        //根据登录人id查询staff表得deptId
        String deptId=Db.findFirst("SELECT dept_id FROM h_staff WHERE id=?",staffId).getStr("dept_id");

        String sql = "select name name, id value from h_store where status = '1'and id!=? order by sort";
        List<Record> list = Db.find(sql,deptId);
        Record record = new Record();
        record.set("name", "请选择");
        record.set("value", "-1");
        if(list != null){
            list.add(0, record);
        }else{
            list = new ArrayList<>();
            list.add(record);
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", list);
        renderJson(jhm);
    }
    /**
     * 9.3.	查看申请
     * 名称	查看申请
     * 描述	根据申请id查看申请详细信息
     * 验证	根据id验证是否存在此申请
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/apply/showById
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
     * "type_text": "",//调动类别，参见数据字典
     * "from_store_name": "店名",//来源门店名称
     * "id": "该记录id",
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
    @Override
    public void showById() {
        JsonHashMap jhm = new JsonHashMap();

        try {
            String id = getPara("id");
            String sql = "SELECT work_date,type AS type_text,from_dept AS from_store_name,id,status AS status_text FROM h_apply_move WHERE id=?";
            Record r = Db.findFirst(sql, id);
            if (r == null) {
                jhm.putCode(0).putMessage("此记录不存在！");
                renderJson(jhm);
                return;
            }
            String status = r.getStr("status_text");
            r.set("status_text", translate(status));

            jhm.putCode(1);
            jhm.put("data", r);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

    /**
     * 9.4.	撤销申请
     * 名称	撤销申请
     * 描述	根据申请id撤销申请
     * 验证	根据id验证是否存在此申请
     * 只有在状态是“未读”时，才能撤销
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/apply/cancelById
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
     * "message": "撤销成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "此记录不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "撤销失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void cancelById() {
        JsonHashMap jhm = new JsonHashMap();

        try {
            String id = getPara("id");
            String sql = "SELECT status FROM h_apply_move WHERE id=?";
            Record r = Db.findFirst(sql, id);
            if (r == null) {
                jhm.putCode(0).putMessage("此记录不存在！");
                renderJson(jhm);
                return;
            }
            String status = r.getStr("status");
            if (status.equals("0")) {
                Db.update("UPDATE h_apply_move SET status='1' WHERE id=?", id);
                jhm.putCode(1).putMessage("撤销成功！");
                renderJson(jhm);
                return;
            } else {
                jhm.putCode(0).putMessage("撤销失败！");
                renderJson(jhm);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 4.4.	处理申请
     * 名称	处理申请
     * 描述	处理其他店申请从本店调派店员
     * 处理申请，同意或者拒绝，如果拒绝必须填写拒绝原因
     * 并将该条通知设置为已读
     * 验证	根据id验证是否存在该通知，该通知是否撤销
     * 权限	店长可见
     * URL	http://localhost:8081/mgr/apply/deal
     * 请求方式	post
     * 请求参数类型	json
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	通知id
     * status	string		否	值如下：
     * 0：拒绝
     * 1：同意
     * desc	string		是	拒绝时必须填写
     * type   string      否  通知类型
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "操作成功！"//提示成功信息
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
    public void deal() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择通知类型!");
            renderJson(jhm);
            return;
        }
        String status = getPara("status");
        if (StringUtils.isEmpty(status)) {
            jhm.putCode(0).putMessage("请选择处理类型!");
            renderJson(jhm);
            return;
        }
        String type = getPara("type");
        if (StringUtils.isEmpty(type)) {
            jhm.putCode(0).putMessage("请选择通知类型!");
            renderJson(jhm);
            return;
        }
        String desc = getPara("desc");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        ApplyService aps = enhance(ApplyService.class);
        jhm = aps.deal(id, status, desc, type, usu);

        renderJson(jhm);
//        renderJson("{\"code\":1,\"message\":\"操作成功！\"}");
    }

}
