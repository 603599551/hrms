package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class ApplyCtrl extends BaseCtrl {

    /**
     9.1.	申请调入员工列表
     名称	显示申请调入员工列表
     描述	根据当前登录人所在的门店id，查询本店申请调入员工列表。
     不考虑Hr访问。
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/apply/list
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "list": [{
     "work_date": "上岗日期",
     "type_text": "",//调动类别
     "from_store_name": "店名",//来源门店名称
     "id": "该记录id",
     "status_text": "未读",//参见数据字典
     "store_color": "#b7a6d4",//颜色值
     "status":"0" //状态值，参见数据字典
     }]
     }
     失败	{
     "code": 0,
     "message": "提示失败信息！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void list(){
        renderJson("{\"code\":1,\"data\":[{\"work_date\":\"上岗日期\",\"type_text\":\"\",\"from_store_name\":\"店名\",\"id\":\"该记录id\",\"status_text\":\"未读\",\"store_color\":\"#b7a6d4\",\"status\":\"0\"}]}");
    }
    /**
     9.2.	申请调入
     名称	申请调入店员
     描述	申请调入店员。将数据保存到申请表，并向“来源门店”发通知
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/apply/moveIn
     请求方式	Post
     请求参数类型	JSON

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     work_date	string		否	上岗日期
     type	string		否	调动类别id
     from_store	string		否	来源门店id
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "提交成功！"
     }
     失败	{
     "code": 0,
     "message": "请输入上岗日期！"
     }
     或者
     {
     "code": 0,
     "message": "保存失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void moveIn(){
        JsonHashMap jhm = new JsonHashMap();

        String workDate = getPara("work_date");
        if(StringUtils.isEmpty(workDate)){
            jhm.putCode(0).putMessage("上岗时间不能为空！");
            renderJson(jhm);
            return;
        }
        String type = getPara("type");
        if(StringUtils.isEmpty(type) || StringUtils.equals(type , "-1")){
            jhm.putCode(0).putMessage("调动类型不能为空！");
            renderJson(jhm);
            return;
        }
        String fromStore = getPara("from_store");
        if(StringUtils.isEmpty(fromStore) || StringUtils.equals(fromStore , "-1") ){
            jhm.putCode(0).putMessage("来源部门不能为空！");
            renderJson(jhm);
            return;
        }
        String desc = getPara("desc");
        if(StringUtils.isEmpty(desc)){
            jhm.putCode(0).putMessage("说明不能为空！");
            renderJson(jhm);
            return;
        }

        String infoId = UUIDTool.getUUID();
        String noticeId = UUIDTool.getUUID();
        String createTime = DateTool.GetDateTime();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String createrId = usu.getUserId();

        try {
            //查找当前门店的id
            Record recordInfo = Db.findFirst("SELECT (SELECT store.name FROM h_store store WHERE store.id = staff.dept_id) to_dept FROM h_staff staff WHERE staff.id = ?", createrId);
            Record recordNotice = new Record();

            //获取目标门店店长Id
            Record recordFromDeptId = Db.findFirst("SELECT (SELECT staff.id FROM h_staff staff WHERE staff.job = 'store_manager' AND staff.dept_id = store.id) id FROM  h_store store WHERE store.id = ?",fromStore);

            //info相关信息存入info表
            recordInfo.set("id", infoId);
            recordInfo.set("from_dept", fromStore);
            recordInfo.set("reason", desc);
            recordInfo.set("status", "0");
            recordInfo.set("work_date", workDate);
            recordInfo.set("type", type);
            recordInfo.set("creater_id", createrId);
            recordInfo.set("create_time", createTime);
            boolean flagInfo = Db.save("h_apply_move",recordInfo);

            //notice相关信息存入noitce表
            recordNotice.set("id",noticeId);
            recordNotice.set("content","申请调入内容");
            recordNotice.set("sender_id",createrId);
            recordNotice.set("receiver_id",recordFromDeptId.getStr("id"));
            recordNotice.set("create_time",createTime);
            recordNotice.set("status","0");
            recordNotice.set("type","1");
            recordNotice.set("fid",infoId);
            boolean flagNotice = Db.save("h_notice",recordNotice);

            if(flagInfo && flagNotice){
                jhm.putCode(1).putMessage("保存成功！");
            }else {
                jhm.putCode(0).putMessage("保存失败！");
            }
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }


        renderJson(jhm);
    }
    /**
     9.3.	查看申请
     名称	查看申请
     描述	根据申请id查看申请详细信息
     验证	根据id验证是否存在此申请
     权限	店长可见
     URL	http://localhost:8081/mgr/apply/showById
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	申请id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "work_date": "上岗日期",
     "type_text": "",//调动类别，参见数据字典
     "from_store_name": "店名",//来源门店名称
     "id": "该记录id",
     "status_text": "未读",//申请调入状态，参见数据字典
     }

     }
     失败	{
     "code": 0,
     "message": "此记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": "保存失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"work_date\":\"上岗日期\",\"type_text\":\"\",\"from_store_name\":\"店名\",\"id\":\"该记录id\",\"status_text\":\"未读\"}}");
    }
    /**
     9.4.	撤销申请
     名称	撤销申请
     描述	根据申请id撤销申请
     验证	根据id验证是否存在此申请
     只有在状态是“未读”时，才能撤销
     权限	店长可见
     URL	http://localhost:8081/mgr/apply/cancelById
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	申请id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "撤销成功！"
     }
     失败	{
     "code": 0,
     "message": "此记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": "撤销失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void cancelById(){
        renderJson("{\"code\":1,\"message\":\"撤销成功！\"}");
    }
    /**
     4.4.	处理申请
     名称	处理申请
     描述	处理其他店申请从本店调派店员
     处理申请，同意或者拒绝，如果拒绝必须填写拒绝原因
     并将该条通知设置为已读
     验证	根据id验证是否存在该通知，该通知是否撤销
     权限	店长可见
     URL	http://localhost:8081/mgr/apply/deal
     请求方式	post
     请求参数类型	json

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	通知id
     status	string		否	值如下：
     0：拒绝
     1：同意
     desc	string		是	拒绝时必须填写
     type   string      否  通知类型

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "操作成功！"//提示成功信息
     }
     失败	{
     "code": 0,
     "message": ""//显示失败提示
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void deal(){
        renderJson("{\"code\":1,\"message\":\"操作成功！\"}");
    }

}
