package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;

public class MoveOutCtrl extends BaseCtrl {

    /**
     8.1.	调出员工列表
     名称	查询调出员工列表
     描述	查询调出员工的列表信息
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/moveOut/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     out_store_id	string		是	调出门店
     In_store_id	string		是	调入门店
     keyword	string		是	姓名、拼音
     start_date	string		是	开始日期
     end_date	string		是	结束日期
     type	string		是	调出类型。参见数据字典
     status	string		是	调出状态。参见数据字典

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "totalRow": 1,
     "pageNumber": 1,
     "firstPage": true,
     "lastPage": true,
     "totalPage": 1,
     "pageSize": 10,
     "list": [{
     "staff_id": "员工id",
     "out_store_name": "调出门店名称",
     "in_store_name": "调入门店名称",
     "name": "姓名",
     "date": "2018-06-23",//调出日期
     "type": "调出",//【调出类型】数据字典的字面值
     "status": "0",//【调出状态】数据字典的值
     "status_text": "已调出",//【调出状态】数据字典的字面值
     "id": ""//调出记录id
     }]
     }
     }
     失败	{
     "code": 0,
     "message": "失败原因！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void list(){
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"staff_id\":\"员工id\",\"out_store_name\":\"面对面（长大店）\",\"in_store_name\":\"面对面（红旗街店）\",\"out_store_color\":\"#b7a6d4\",\"in_store_color\":\"#fa7a19\",\"name\":\"马云\",\"date\":\"2018-06-23\",\"type\":\"调出\",\"status\":\"0\",\"status_text\":\"已调出\",\"id\":\"id\"}]}}");
    }
    /**
     8.2.	调出店员
     名称	店长调出（借调出）店员
     描述	店长调出店员，要给调入门店发通知
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/moveOut/out
     请求方式	Post
     请求参数类型	JSON

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	array		否	调出员工id
     date	string		否	调出日期
     type	string		否	调动类型。参见数据字典
     to_store	string		否	调入门店id
     desc	string		是	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "调出成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择类别！"
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
    public void out(){
        renderJson("{\n" +
                "     \"code\": 1,\n" +
                "     \"message\": \"调出成功！\"\n" +
                "     }");
    }
    /**
     8.3.	查看调出详细信息
     名称	查看调出信息
     描述	根据调出记录id查询调出详细信息
     验证	根据id验证调出信息是否存在
     权限	店长可见
     URL	http://localhost:8081/mgr/moveOut/showById
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	调出记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "调出记录id",
     "name": "鹿晗、吴亦凡",//多个人们用顿号“、”分隔
     "date": "2018-06-23",//调出日期
     "type_text": "调出",//调出类型字典，显示字面值
     "to_store_name": "长大店",
     "desc": "说明",
     "status": "0",
     "status_text": "已调出",//状态字面值
     }
     }
     失败	{
     "code": 0,
     "message": "该记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//其实失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"id\":\"调出记录id\",\"name\":\"鹿晗、吴亦凡\",\"date\":\"2018-06-23\",\"type_text\":\"调出\",\"to_store_name\":\"长大店\",\"desc\":\"这两个人可以用\",\"status\":\"0\",\"status_text\":\"已调出\"}}");
    }

    /**

     8.4.	撤销调出
     名称	撤销调出
     描述	店长调出本店员工后，还可以撤销此操作
     验证	根据调出id验证此记录是否存在
     如果对方门店已经接收或者拒绝（即status值为2或3），那么将不允许撤销
     权限	店长可见
     URL	http://localhost:8081/mgr/moveOut/cancelOut
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	调出记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "撤销成功！"
     }
     失败	{
     "code": 0,
     "message": "长大店已经接收，不能撤销！"
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

    public void cancelOut(){
        renderJson("{\"code\":1,\"message\":\"撤销成功！\"}");
    }

}
