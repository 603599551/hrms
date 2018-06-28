package com.hr.train.controllers;

import com.common.controllers.BaseCtrl;

public class TypeCtrl extends BaseCtrl{
    /**
     15.1.	分类列表
     名称	分类列表
     描述	查看分类
     验证	无
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     无

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": [{
     "id": "分类id",
     "name": "分类名称",
     "enable": "1"
     "enable_text": "启用"//参见分类状态数据字典

     }]
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
        renderJson("{\"code\":1,\"data\":[{\"id\":\"分类id\",\"name\":\"分类名称\",\"enable\":\"1\",\"enable_text\":\"启用\"}]}");
    }
    /**
     15.2.	添加分类
     名称	添加分类
     描述	添加分类。
     验证	分类名称不能重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/add
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     name	string		否	分类名称
     parent_id	string		否	上级分类id
     enable	string		否	启用
     sort	string		是	排序
     desc	string		是	描述

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "添加成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写分类名称！"
     }
     或者
     {
     "code": 0,
     "message": "分类名称重复！"
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
     15.3.	修改分类
     名称	修改分类
     描述	修改分类。
     验证	分类名称不能重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/updateById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	分类id
     name	string		否	分类名称
     parent_id	string		否	上级分类id
     enable	string		否	启用
     sort	string		是	排序
     desc	string		是	描述

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "修改成功！"
     }
     失败	{
     "code": 0,
     "message": "请填写分类名称！"
     }
     或者
     {
     "code": 0,
     "message": "分类名称重复！"
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
     15.4.	查看分类
     名称	查看分类
     描述	根据id查询分类信息
     验证	根据传入id判断分类是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	分类id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "134adjfwe",//分类id
     "name": "新人培训",//名称
     "parent_id": "123214sfdadsf",//上级分类id
     "enable": "1",//启用状态，参见数据字典
     "sort": 50,//排序
     "desc": "",//描述
     }
     }
     失败	{
     "code": 0,
     "message": "分类不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        renderJson("{\"code\":1,\"data\":{\"id\":\"134adjfwe\",\"name\":\"新人培训\",\"parent_id\":\"234k5jl234j5lkj24l35j423l5j\",\"enable\":\"1\",\"sort\":50,\"desc\":\"\"}}");
    }
    /**
     15.5.	删除分类
     名称	删除分类
     描述	根据id删除分类信息
     验证	根据传入id判断分类是否存在
     判断该分类下是否有文章，如果有文章给出警告
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/deleteById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	分类id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "删除成功！"
     }
     失败	{
     "code": 0,
     "message": "分类不存在！"
     }
     或者
     {
     "code": 0,
     "message": "该分类下有培训内容，不能删除！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void deleteById(){
        renderJson("{\"code\":1,\"message\":\"删除成功！\"}");
    }
    /**
     15.6.	启用/停用分类
     名称	启用或停用分类
     描述	根据id启用或停用分类
     验证	根据传入id判断分类是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/train/type/enableById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	分类id
     enable	string		否	1：启用
     0：禁用

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "启用（或者禁用）成功！"
     }
     失败	{
     "code": 0,
     "message": "分类不存在！"
     }
     或者
     {
     "code": 0,
     "message": "给出操作失败信息"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void enableById(){
        renderJson("{\"code\":1,\"message\":\"启用（或者禁用）成功！\"}");
    }

    /**
     21.3.	获取所有分类接口
     名称	获取门店字典值
     描述	获取所有门店字典值
     验证
     权限
     URL	http://localhost:8081/mgr/train/type/getTypeDict
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述



     返回数据：
     返回格式	JSON
     成功	{
     "code":1,
     "data":[
     {
     "name":"A分类",
     "value":"234k5jl234j5lkj24l35j423l5j"
     },
     {
     "name":" B分类",
     "value":"4a8d594591ea4c1eb708fcc8a5c67c47"
     },
     {
     "name":" C分类",
     "value":"c95a33cf41a9433d9dbca1ba84603358"
     },
     {
     "name":" D分类",
     "value":"e1866af6ec1a4342aed66b0a71f0a6ee"
     }
     ]
     }
     失败	{
     "code": 0,
     "message": "分类不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     */
    public void getTypeDict(){
        renderJson("{\"code\":1,\"data\":[{\"name\":\"请选择分类\",\"value\":\"-1\"},{\"name\":\"A分类\",\"value\":\"234k5jl234j5lkj24l35j423l5j\"},{\"name\":\" B分类\",\"value\":\"4a8d594591ea4c1eb708fcc8a5c67c47\"},{\"name\":\" C分类\",\"value\":\"c95a33cf41a9433d9dbca1ba84603358\"},{\"name\":\" D分类\",\"value\":\"e1866af6ec1a4342aed66b0a71f0a6ee\"}]}");
    }
}
