package com.hr.notice.controllers;

import com.common.controllers.BaseCtrl;

public class NoticeCtrl extends BaseCtrl {

    /**
     4.1.	通知列表
     名称	通知列表接口
     描述	从session中取出当前登录人的部门id，根据该部门id查询通知。
     验证	无
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/notice/list
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述


     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": [{
     "content": "内容摘要",//显示通知，内容不超过10字
     "sender_name": "发送人名称",
     "datetime": "日期",//格式：yyyy-MM-dd HH:mm
     "id": "通知id",
     "status": "0",//0表示未读，1表示已读
     "status_text":"未读",
     "show_in ": true//是否显示调入按钮。false：不显示，true显示
     }]
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
    public void list(){
        renderJson("{\"code\":1,\"list\":[{\"content\":\"内容摘要\",\"sender_name\":\"发送人名称\",\"datetime\":\"日期\",\"id\":\"通知id\",\"status\":\"0\",\"status_text\":\"未读\",\"show_in \":true}]}");
    }
    /**
     4.2.	查看通知
     名称	查看通知
     描述	根据通知id，显示通知详细信息。并设置该条记录已读
     验证	根据id验证是否存在该通知
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/notice/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	通知id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "content": "内容摘要",//显示通知，内容不超过10字
     "sender_name": "发送人名称",
     "datetime": "日期",//格式：yyyy-MM-dd HH:mm
     "id": "通知id",
     "status": "0",//0表示未读，1表示已读
     "status_text":"未读",
     "show_in ": true//是否显示调入按钮。false：不显示，true显示
     }
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
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"content\":\"内容摘要\",\"sender_name\":\"发送人名称\",\"datetime\":\"日期\",\"id\":\"通知id\",\"status\":\"0\",\"status_text\":\"未读\",\"show_in \":true}}");
    }
    /**
     4.3.	查看通知中的申请详细信息
     名称	查看申请
     描述	业务说明：其他店申请从本店调派店员
     根据申请id查看申请详细信息
     并将该条通知设置为已读
     验证	根据id验证是否存在此申请
     权限	店长可见
     URL	http://localhost:8081/mgr/notice/showApplyById
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
    public void showApplyById(){
        renderJson("{\"code\":1,\"data\":{\"work_date\":\"上岗日期\",\"type_text\":\"\",\"from_store_name\":\"店名\",\"id\":\"该记录id\",\"status_text\":\"未读\"}}");
    }

    /**
     4.5.	查看通知中的调入的详细信息
     名称	查看调入通知的详细信息
     描述	根据调出记录的id查看详细信息（注意是调出记录的id）
     验证	此记录是否存在
     权限	店长可见
     URL	http://localhost:8081/mgr/notice/showMoveInById
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	调出记录的id（注意是调出记录的id）

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "out_store_name": "长大店",//来源门店名称
     "staffList":[
     {
     "name": "鹿晗",
     "gender": "男",
     "phone": "1370000",
     "job": "员工",//职位
     "kind": "服务员、传菜员",
     "money": "16",
     "work_type": "全职",//工作类型
     }
     ],
     "date": "2018-06-23",//调出日期
     "type": "调入",//数据字典的字面值
     "id": ""//调出记录id
     }
     }
     失败	{
     "code": 0,
     "message": "此记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": "调入失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void showMoveInById(){
        renderJson("{\"code\":1,\"data\":{\"out_store_name\":\"长大店\",\"staffList\":[{\"name\":\"鹿晗\",\"gender\":\"男\",\"phone\":\"1370000\",\"job\":\"员工\",\"kind\":\"服务员、传菜员\",\"money\":\"16\",\"work_type\":\"全职\"}],\"date\":\"2018-06-23\",\"type\":\"调入\",\"id\":\"id\"}}");
    }
}
