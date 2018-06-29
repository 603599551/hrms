package com.hr.train.controllers;

import com.common.controllers.BaseCtrl;

public class ArticleCtrl extends BaseCtrl {
    /**
     15.7.	文章列表
     名称	文章列表
     描述	查询文章列表
     根据keyword模糊查询文章标题
     验证	无
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/article/list
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
     "id": "文章id",
     "title": "文章名称",
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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"文章id\",\"title\":\"文章名称\",\"datetime\":\"2018-6-26 13:52\",\"creater_name\":\"马云\"}]}}");
    }
    /**
     15.8.	添加文章
     名称	添加文章
     描述	添加文章。
     验证	文章标题不能重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/article/add
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     title	string		否	文章名称
     content	string		是	内容

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "添加成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写文章标题！"
     }
     或者
     {
     "code": 0,
     "message": "文章标题重复！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void add(){
        String content = getPara("content");
        System.out.println(content);
        renderJson("{\"code\":1,\"message\":\"添加成功！\"}");
    }
    /**
     15.9.	修改文章
     名称	修改文章
     描述	根据id修改文章。
     验证	文章标题不能重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/article/updateById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	文章id
     class_id  string  否  分类id
     title	string		否	文章名称
     content	string		是	内容

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "修改成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写文章标题！"
     }
     或者
     {
     "code": 0,
     "message": "文章标题重复！"
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
     15.10.	查看文章
     名称	查看文章
     描述	根据id查询文章信息
     验证	根据传入id判断文章是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/article/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	文章id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "134adjfwe",//文章id
     "title": "餐具的摆放",//标题
     "class_id": "234k5jl234j5lkj24l35j423l5j",//分类id
     "content": "<hr><h1>sdfsdfd</h1>",//内容
     "create_time": "2018-06-28",
     "author": "作者"
     }
     }
     失败	{
     "code": 0,
     "message": "文章不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"id\":\"134adjfwe\",\"title\":\"餐具的摆放\",\"class_id\":\"234k5jl234j5lkj24l35j423l5j\",\"content\":\"<hr><h1>sdfsdfd</h1>\",\"create_time\":\"2018-06-28\",\"author\":\"作者\"}}");
    }
    /**
     15.11.	删除文章
     名称	删除文章
     描述	根据id删除文章
     验证	根据传入id判断文章是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/article/deleteById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	文章id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "删除成功！"
     }
     失败	{
     "code": 0,
     "message": "文章不存在！"
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
