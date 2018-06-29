package com.hr.hr.controllers;

import com.common.controllers.BaseCtrl;

public class PerformanceCtrl extends BaseCtrl {

    /**
     14.1.	绩效列表
     名称	绩效列表
     描述	根据查询条件，查询绩效考核，查询条件及查询方式如下：
     1.门店id：完全匹配
     2.开始日期：时间段
     3.结束日期：时间段
     4.关键字：根据姓名、拼音模糊查询
     验证	无
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/performance/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     store_id	string		是	门店id
     start_date	string		是	开始日期
     end_date	string		是	结束日期
     keyword	string		否	关键字

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
     "id": "奖罚记录id",
     "staff_id": "员工id",
     "store_id": "门店id",
     "store_name": "门店名",
     "name": "姓名",
     "type": "奖励或者惩罚",
     "date": "奖罚日期",
     "money": "金额"
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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"奖罚记录id\",\"staff_id\":\"员工id\",\"store_id\":\"门店id\",\"store_name\":\"面对面（长大店）\",\"name\":\"鹿晗\",\"type\":\"奖励\",\"date\":\"2018-02-03\",\"money\":\"200\"}]}}");
    }
    /**
     14.2.	添加奖罚
     姓名、原因（备选项）、金额
     名称	添加奖罚
     描述	添加奖罚。
     验证
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/add
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     staff_id	array		否	员工id
     date	string		否	日期
     type	string		否	奖或者罚，参加数据字典
     money	string		否	金额
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "添加成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择奖罚的员工！"
     }
     或者
     {
     "code": 0,
     "message": "请选择日期！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void add(){
        renderJson("{\"code\":1,\"message\":\"添加成功！\"}");
    }
    /**
     14.3.	查看惩罚
     名称	查看惩罚
     描述	根据id查询惩罚信息
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "id",//记录id
     "names": "鹿晗、吴亦凡",//员工姓名
     "date": "2018-02-04",//时间
     "type": "1",
     "type_text": "奖励",//参见奖罚类别
     "money": "200",//奖罚金额
     "desc": "说明"
     }
     }
     失败	{
     "code": 0,
     "message": "记录不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"id\":\"id\",\"names\":\"鹿晗\",\"date\":\"2018-02-03\",\"type\":\"1\",\"type_text\":\"奖励\",\"money\":\"200\",\"desc\":\"扶老奶奶过马路\"}}");
    }
    /**
     14.4.	修改奖罚
     名称	修改后保存奖罚
     描述	根据id修改奖罚记录
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/updateById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id
     date	string		否	日期
     type	string		否	奖或者罚，参加数据字典
     money	string		否	金额
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "修改成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择奖罚的员工！"
     }
     或者
     {
     "code": 0,
     "message": "请选择日期！"
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
     14.5.	删除奖罚
     名称	删除奖罚
     描述	根据id删除奖罚记录
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/deleteById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "删除成功！"
     }
     失败	{
     "code": 0,
     "message": "失败的信息"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     */
    public void deleteById(){
        renderJson("{\"code\":1,\"message\":\"删除成功！\"}");
    }

}
