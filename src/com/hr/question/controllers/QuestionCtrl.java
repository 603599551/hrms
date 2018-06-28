package com.hr.question.controllers;

import com.common.controllers.BaseCtrl;

public class QuestionCtrl extends BaseCtrl {

    /**
     16.1.	考题列表
     名称	考题列表
     描述	查询考题列表
     根据keyword模糊查询标题
     验证	无
     权限	Hr可见
     URL	http://localhost:8081/mgr/question/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     keyword	string		是	关键字

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
     "id": "考题id",
     "title": "考题标题",
     "datetime": "2018-6-26 13:52",//最后一次修改日期
     "creater_name": "马云"//发布人
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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"考题id\",\"title\":\"考题标题\",\"datetime\":\"2018-6-26 13:52\",\"creater_name\":\"马云\"}]}}");
    }
    /**
     16.2.	添加考题
     名称	添加考题
     描述	添加考题。
     验证
     权限	Hr可见
     URL	http://localhost:8081/mgr/question/add
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     title	string		否	考题名称
     content	string		是	内容

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "添加成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写考题标题！"
     }
     或者
     {
     "code": 0,
     "message": "考题标题重复！"
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
     16.3.	修改考题
     名称	修改考题
     描述	修改考题。
     验证	验证标题是否重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/question/updateById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	考题id
     title	string		否	考题标题
     content	string		是	内容

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "修改成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写考题标题！"
     }
     或者
     {
     "code": 0,
     "message": "考题标题重复！"
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
     16.4.	查看考题
     名称	查看考题
     描述	查看考题。
     验证
     权限	Hr可见
     URL	http://localhost:8081/mgr/question/showById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	考题id
     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "考题id",
     "title": "考题标题",
     "datetime": "2018-6-26 13:52",//最后一次修改日期
     "creater_name": "马云"//发布人
     }
     }
     失败	{
     "code": 0,
     "message": "查看失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"id\":\"考题id\",\"title\":\"考题标题\",\"datetime\":\"2018-6-26 13:52\",\"creater_name\":\"马云\"}}");
    }
    /**
     16.5.	删除考题
     名称	删除考题
     描述	删除考题。
     验证
     权限	Hr可见
     URL	http://localhost:8081/mgr/question/deleteById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	考题id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "删除成功！"
     }
     失败	{
     "code": 0,
     "message": "考题不存在！"
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
