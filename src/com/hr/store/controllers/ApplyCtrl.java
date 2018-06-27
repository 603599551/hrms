package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;

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
        renderJson("{\"code\":1,\"message\":\"提交成功！\"}");
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

}
