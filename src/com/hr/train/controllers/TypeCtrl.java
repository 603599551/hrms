package com.hr.train.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void list() {
//        renderJson("{\"code\":1,\"data\":[{\"id\":\"分类id\",\"name\":\"分类名称\",\"enable\":\"1\",\"enable_text\":\"启用\"}]}");
        JsonHashMap jhm = new JsonHashMap();
        String sql = "select id as value, case enable WHEN '1' THEN '启用' WHEN '0' THEN '停用'  END enable_text ,enable, id, parent_id, name, sort,`desc`, creater_id, create_time, modifier_id, modify_time from h_train_type where parent_id = -1 and enable <> 0 order by sort desc";
        String sonSql = "select id as value, case enable WHEN '1' THEN '启用' WHEN '0' THEN '停用'  END enable_text ,enable, id, parent_id, name, sort,`desc`, creater_id, create_time, modifier_id, modify_time from h_train_type where parent_id <> -1 and enable <> 0  order by sort desc";
        try {
            List<Record> list = new ArrayList<>();
            List<Record> dictList = Db.find(sql);
            List<Record> sonList = Db.find(sonSql);
            if(dictList.size() > 0 && dictList != null ){
                for (int i = dictList.size()-1; i >= 0; i--) {
                    dictList.get(i) .set("name", "┗"+dictList.get(i).getStr("name"));
                    String pid = dictList.get(i).getStr("id");
                    list.add(dictList.get(i));
                    for (int j = sonList.size() - 1; j >= 0; j--) {
                        if(StringUtils.equals(pid,sonList.get(j).getStr("parent_id"))){
                            sonList.get(j) .set("name", "　　┣"+sonList.get(j).getStr("name"));
                            list.add(sonList.get(j));
                            sonList.remove(j);
                        }
                    }
                }
            }else{
                jhm.putCode(0).putMessage("无记录！");
            }
            jhm.put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器出错！");
        }
        renderJson(jhm);
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
//        renderJson("{\"code\":1,\"message\":\"添加成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        Record type = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //根据接口要求进行非空验证
        if(StringUtils.isBlank(type.getStr("name"))){
            jhm.putCode(0).putMessage("分类名称不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(type.getStr("parent_id"))){
            jhm.putCode(0).putMessage("上级分类不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(type.getStr("sort"))){
            type.set("sort",0);
        }
        if(StringUtils.isEmpty(type.getStr("enable"))){
            type.set("enable",1);
        }
        String sql = "select count(*)c from h_train_type where name = ? ";
        String name = type.getStr("name");
        Record typeSearch = Db.findFirst(sql,name);
        if(typeSearch.getInt("c") != 0){
            jhm.putCode(0).putMessage("分类名称重复！");
            renderJson(jhm);
        }else{
            try {
                type.set("id", UUIDTool.getUUID());//获取主键（UUID）的通用方法
                type.set("creater_id", usu.getUserId());
                type.set("modifier_id", usu.getUserId());
                String time = DateTool.GetDateTime();//获取时间的通用方法，yyyy-MM-dd HH:mm:ss   这个类中也有其他格式的获取方法
                type.set("create_time", time);
                type.set("modify_time", time);
                boolean flag = Db.save("h_train_type", type);//保存数据到数据库
                if(flag){
                    jhm.putCode(1).putMessage("添加成功！");
                }else{
                    jhm.putCode(0).putMessage("添加失败！");
                }
            }catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器出错！");
            }
        }
        renderJson(jhm);
    }
    /**
     15.3.	修改分类
     名称	修改分类
     描述	修改分类。
     验证	分类名称不能重复
     权限	Hr可见
     URL	http://localhost:8081/mgr/type/type/updateById
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
//        renderJson("{\"code\":1,\"message\":\"修改成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        Record type = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());

        //对传入数据进行非空验证
        if(StringUtils.isBlank(type.getStr("name"))){
            jhm.putCode(0).putMessage("分类名称不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(type.getStr("parent_id"))){
            jhm.putCode(0).putMessage("上级分类不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(type.getStr("sort"))){
            type.set("sort",0);
        }
        if(StringUtils.isEmpty(type.getStr("enable"))){
            type.set("enable",1);
        }

        //分类名称重复验证
        String sql = "select count(*)c from h_train_type where name = ? and id<>? ";
        String name = type.getStr("name");
        String id = type.getStr("id");
        Record typeSearch = Db.findFirst(sql,name,id);
        if(typeSearch.getInt("c") != 0){
            jhm.putCode(0).putMessage("分类名重复！");
        }else{
            try {
                type.set("modifier_id", usu.getUserId());
                String time = DateTool.GetDateTime();//获取时间的通用方法，yyyy-MM-dd HH:mm:ss   这个类中也有其他格式的获取方法
                type.set("modify_time", time);
                boolean flag = Db.update("h_train_type",type);
                if(flag){
                    jhm.putCode(1).putMessage("修改成功！");
                }else{
                    jhm.putCode(0).putMessage("修改失败！");
                }
            }catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器出错！");
            }
        }
        renderJson(jhm);
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
//        renderJson("{\"code\":1,\"data\":{\"id\":\"134adjfwe\",\"name\":\"新人培训\",\"parent_id\":\"234k5jl234j5lkj24l35j423l5j\",\"enable\":\"1\",\"sort\":50,\"desc\":\"\"}}");
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        try {
            Record type = Db.findById("h_train_type",id);
            if(type != null){
                type.remove("creater_id");
                type.remove("create_time");
                type.remove("modifier_id");
                type.remove("modifier_time");
                jhm.putCode(1).put("data",type);
            }else{
                jhm.putCode(0).putMessage("分类不存在！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器出错！");
        }
        renderJson(jhm);
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
//        renderJson("{\"code\":1,\"message\":\"删除成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("删除失败！");
            renderJson(jhm);
            return;
        }
        Record type = Db.findById("h_train_type",id);
        if(type == null){
            jhm.putCode(0).putMessage("分类不存在！");
        }else{
            String sql = "select count(*)c from h_train_article where type_id = ?";
            String sonSearch = "select count(*)c from h_train_type where parent_id = ?";
            try {
                Record article = Db.findFirst(sql, type.getStr("id"));
                Record sonRecord = Db.findFirst(sonSearch, id);
                if(sonRecord.getInt("c") != 0){
                    jhm.putCode(0).putMessage("该分类下有子分类，不能删除！");
                }else{
                    if(article.getInt("c") != 0){
                        jhm.putCode(0).putMessage("该分类下有培训文章，不能删除！");
                    }
                    else{
                        type.set("enable",0);
                        boolean flag = Db.update("h_train_type",type);
                        if(flag){
                            jhm.putCode(1).putMessage("删除成功！");
                        }else{
                            jhm.putCode(0).putMessage("删除失败！");
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器出错！");
            }
        }
        renderJson(jhm);

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
//        renderJson("{\"code\":1,\"data\":[{\"name\":\"请选择分类\",\"value\":\"-1\"},{\"name\":\"A分类\",\"value\":\"234k5jl234j5lkj24l35j423l5j\"},{\"name\":\" B分类\",\"value\":\"4a8d594591ea4c1eb708fcc8a5c67c47\"},{\"name\":\" C分类\",\"value\":\"c95a33cf41a9433d9dbca1ba84603358\"},{\"name\":\" D分类\",\"value\":\"e1866af6ec1a4342aed66b0a71f0a6ee\"}]}");
//        renderJson("{\"code\":1,\"data\":[{\"name\":\"请选择分类\",\"value\":\"-1\"}");
        JsonHashMap jhm = new JsonHashMap();
        String sql = "select id as value,name from h_train_type where parent_id = -1";
        try {
            List<Record> dictList = Db.find(sql);
            if(dictList==null){
                jhm.putCode(0).putMessage("无记录！");
            }else{
                jhm.putCode(1);
                Record r=new Record();
                r.set("name","创建一级分类");
                r.set("value","-1");
                dictList.add(0,r);
                jhm.put("data",dictList);
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器出错！");
        }
        renderJson(jhm);

    }
}
