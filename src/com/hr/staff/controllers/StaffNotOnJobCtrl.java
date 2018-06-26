package com.hr.staff.controllers;

import com.common.controllers.BaseCtrl;

public class StaffNotOnJobCtrl extends BaseCtrl {


    /**
     6.7.	不在职员工列表
     名称	不在职员工列表
     描述	显示离职、调出、调出（借调）员工信息列表。根据查询条件进行查询，查询条件及查询方式如下：
     6.	关键字：根据姓名、电话号码、拼音模糊查询
     7.	性别：完全匹配查询
     8.	职位：完全匹配查询
     9.	岗位：完全匹配查询
     10.	工作类型：完全匹配查询
     11.	不在职状态：完全匹配查询
     验证	无
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/staffNotOnJob/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     keyword	string		是	关键字
     gender	string		是	性别id
     dept_id	string		是	门店（部门）的id。
     job	string		是	职位id
     kind	array		是	岗位id
     type	string		是	工作类型id
     status	string		是	不在职状态id

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
     "id": "员工id",
     "store_id": "门店id",
     "store_name": "门店名",
     "name": "姓名",
     "gender": "性别",
     "phone": "电话号码",
     "job": "职位名称",
     "kind": "岗位名称",
     "wage": "时薪/月薪",
     "type": "工作类型名称",
     "status_text": "不在职状态"
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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"员工id\",\"store_id\":\"门店id\",\"store_name\":\"门店名\",\"name\":\"姓名\",\"gender\":\"性别\",\"phone\":\"电话号码\",\"job\":\"职位名称\",\"kind\":\"岗位名称\",\"wage\":\"时薪/月薪\",\"type\":\"工作类型名称\",\"status_text\":\"不在职状态\"}]}}");
    }

/**
 6.8.	查看不在职员工信息
 名称	查询员工详细信息
 描述	根据员工id查询不在职员工详细信息
 验证	根据id验证员工是否存在
 权限	Hr、店长可见
 URL	http://localhost:8081/mgr/staffNotOnJob/showById
 请求方式	get
 请求参数类型	key=value

 请求参数列表：
 参数名	类型	最大长度	允许空	描述
 id	string		否	员工id

 返回数据：
 返回格式	JSON
 成功	{
 "code": 1,
 "data": {
 "id": "员工id",
 "name": "鹿晗",
 "gender": "0",//0：女，1：男
 "birthday": "1990-03-29",
 "phone": "138888888",
 "address": "北京王府井1号",
 "emp_num": "123", //工号
 "hiredate": "2018-06-29", //入职时间
 "dept_id": "部门id",
 "job": "职位",
 "kind": "岗位",
 "status": "在职", //在职状态
 "id_num": "身份证号",
 "type": "全职", //工作类型
 "level": "二星训练员",//级别
 "hour_wage": "16", //时薪，返回为字符串
 "month_wage": "3000", //月薪，返回为字符串
 "bank": "工商银行", //开户行
 "bank_card_num": "20023987413", //银行卡号
 "desc": "不在职原因"
 }
 }
 失败	{
 "code": 0,
 "message": "员工不存在！"
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
        renderJson("{\"code\":1,\"data\":{\"id\":\"员工id\",\"name\":\"鹿晗\",\"gender\":\"0\",\"birthday\":\"1990-03-29\",\"phone\":\"138888888\",\"address\":\"北京王府井1号\",\"emp_num\":\"123\",\"hiredate\":\"2018-06-29\",\"dept_id\":\"部门id\",\"job\":\"职位\",\"kind\":\"岗位\",\"status\":\"在职\",\"id_num\":\"身份证号\",\"type\":\"全职\",\"level\":\"二星训练员\",\"hour_wage\":\"16\",\"month_wage\":\"3000\",\"bank\":\"工商银行\",\"bank_card_num\":\"20023987413\",\"desc\":\"不在职原因\"}}");
    }

}
