package com.hr.staff.controllers;

import com.common.controllers.BaseCtrl;

public class StaffCtrl extends BaseCtrl {

/**
 *
 6.1.	员工列表
 名称	员工列表
 描述	显示在职、借调入员工信息列表。根据查询条件进行查询，查询条件及查询方式如下：
 1.	关键字：根据姓名、电话号码、拼音模糊查询
 2.	性别：完全匹配查询
 3.	职位：完全匹配查询
 4.	岗位：完全匹配查询
 5.	工作类型：完全匹配查询
 验证	无
 权限	Hr、店长可见
 URL	http://localhost:8081/mgr/staff/list
 请求方式	get
 请求参数类型	key=value

 请求参数列表：
 参数名	类型	最大长度	允许空	描述
 keyword	string		是	关键字
 gender	string		是	性别id
 dept_id	string		是	门店（部门）的id。
 job	string		否	职位id
 kind	array		是	岗位id
 type	string		否	工作类型id

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
 "status_text": "在职状态"
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
        String result = "{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"员工id\",\"store_id\":\"门店id\",\"store_name\":\"门店名\",\"name\":\"姓名\",\"gender\":\"性别\",\"phone\":\"电话号码\",\"job\":\"职位名称\",\"kind\":\"岗位名称\",\"wage\":\"时薪/月薪\",\"type\":\"工作类型名称\",\"status_text\":\"在职状态\"}]}}";
        renderJson(result);
    }
/**
 6.2.	录入员工
 名称	店员入职
 描述	输入员工信息保存。
 信息有：姓名、性别、生日、电话号、住址、工号、入职日期、门店、岗位、工种（多选）、在职状态、身份证号、工作类型、级别、时薪、月薪、开户行、银行卡

 验证	姓名不能相同、手机号不能相同
 权限	Hr可见
 URL	http://localhost:8081/mgr/staff/add
 请求方式	Post
 请求参数类型	JSON

 请求参数列表：
 参数名	类型	最大长度	允许空	描述
 name	string		否	姓名
 gender	string		否	性别
 birthday	string		是	生日
 phone	string		否	电话号码
 address	string		是	住址
 emp_num	string		是	工号
 hiredate	string		否	入职时间
 dept_id	string		否	所在门店（部门）的id
 job	string		否	职位。保存数据字典id
 内容详见数据字典的职位
 kind	string		是	岗位。保存数据字典id
 内容详见数据字典的岗位
 status	string		否	在职状态。保存数据字典id。
 内容详见数据字典的在职状态
 id_num	string		是	身份证号
 work_type	string		否	工作类型。
 内容详见数据字典的工作类型
 level	string		是	级别
 内容详见数据字典的级别
 hour_wage	string		是	时薪。
 当工作类型是兼职时，必填此项
 month_wage	string		是	月薪
 当工作类型是全职时，必填此项
 bank	string		是	开户行
 bank_card_num	string		是	银行卡号

 返回数据：
 返回格式	JSON
 成功	{
 "code": 1,
 "message": "保存成功！"
 }
 失败	{
 "code": 0,
 "message": "请输入姓名！"
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
    public void add(){
        renderJson("{\"code\":1,\"message\":\"保存成功！\"}");
    }
/**
 6.3.	查看员工信息
 名称	查询员工详细信息
 描述	根据员工id查询员工详细信息
 验证	根据id验证员工是否存在
 权限	Hr、店长可见
 URL	http://localhost:8081/mgr/staff/showById
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
 "bank_card_num": "20023987413" //银行卡号
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
        renderJson("{\"code\":1,\"data\":{\"id\":\"员工id\",\"name\":\"鹿晗\",\"gender\":\"0\",\"birthday\":\"1990-03-29\",\"phone\":\"138888888\",\"address\":\"北京王府井1号\",\"emp_num\":\"123\",\"hiredate\":\"2018-06-29\",\"dept_id\":\"部门id\",\"job\":\"职位\",\"kind\":\"岗位\",\"status\":\"在职\",\"id_num\":\"身份证号\",\"type\":\"全职\",\"level\":\"二星训练员\",\"hour_wage\":\"16\",\"month_wage\":\"3000\",\"bank\":\"工商银行\",\"bank_card_num\":\"20023987413\"}}");
    }
/**
 6.4.	修改员工信息
 名称	修改后保存员工信息
 描述	修改后保存员工信息
 信息有：姓名、性别、生日、电话号、住址、工号、入职日期、门店、岗位、工种（多选）、在职状态、身份证号、工作类型、级别、时薪、月薪、开户行、银行卡

 验证	姓名不能相同、手机号不能相同
 权限	Hr可见
 URL	http://localhost:8081/mgr/staff/updateById
 请求方式	Post
 请求参数类型	JSON

 请求参数列表：
 参数名	类型	最大长度	允许空	描述
 id	string		否	员工id
 name	string		否	姓名
 gender	string		否	性别
 birthday	string		是	生日
 phone	string		否	电话号码
 address	string		是	住址
 emp_num	string		是	工号
 hiredate	string		否	入职时间
 dept_id	string		否	所在门店（部门）的id
 job	string		否	职位。保存数据字典id
 内容详见数据字典的职位
 kind	string		是	岗位。保存数据字典id
 内容详见数据字典的岗位
 status	string		否	在职状态。保存数据字典id。
 内容详见数据字典的在职状态
 id_num	string		是	身份证号
 type	string		否	工作类型。
 内容详见数据字典的工作类型
 level	string		是	级别
 内容详见数据字典的级别
 hour_wage	string		是	时薪。
 当工作类型是兼职时，必填此项
 month_wage	string		是	月薪
 当工作类型是全职时，必填此项
 bank	string		是	开户行
 bank_card_num	string		是	银行卡号

 返回数据：
 返回格式	JSON
 成功	{
 "code": 1,
 "message": "修改成功！"
 }
 失败	{
 "code": 0,
 "message": "请输入姓名！"
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
    public void updateById(){
        renderJson("{\"code\":1,\"message\":\"修改成功！\"}");
    }


/**

 6.9.	录入下周可上班时间（王泽）

 */

}
