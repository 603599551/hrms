package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;

public class MoveInCtrl extends BaseCtrl {

    /**
    7.1.	调入员工列表
名称	查询调入员工列表
描述	查询调入员工的列表信息
验证	无
权限	店长可见
URL	http://localhost:8081/mgr/moveIn/list
请求方式	get
请求参数类型	key=value

请求参数列表：
参数名	类型	最大长度	允许空	描述
out_store_id	string		是	调出门店
In_store_id	string		是	调入门店
keyword	string		是	姓名、拼音
start_date	string		是	开始日期
end_date	string		是	结束日期
type	string		是	调出类型。接口获取

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
			"type": "调出",//数据字典的字面值
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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"staff_id\":\"员工id\",\"out_store_name\":\"调出门店名称\",\"in_store_name\":\"调入门店名称\",\"name\":\"姓名\",\"date\":\"2018-06-23\",\"type\":\"调出\",\"id\":\"id\"}]}}");
    }
    /**
7.2.	查看调入信息
名称	查询调入详细信息
描述	根据调入记录的id查询详细信息
验证	无
权限	店长可见
URL	http://localhost:8081/mgr/moveIn/showById
请求方式	get
请求参数类型	key=value

请求参数列表：
参数名	类型	最大长度	允许空	描述
id	string		是	调入记录的id

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
	"message": "失败原因！"
}
报错	{
	"code": -1,
	"message": "服务器发生异常！"
}
     {"code":1,"data":{"out_store_name":"长大店","staffList":[{"name":"鹿晗","gender":"男","phone":"1370000","job":"员工","kind":["服务员","传菜员"],"money":"16","work_type":"全职"}],"date":"2018-06-23","type":"调入","id":""}}

     */
    public void showById(){
        renderJson("");
    }

}
