package com.hr.question.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class QuestionCtrl extends BaseCtrl {

    /**
     * 16.1.	考题列表
     * 名称	考题列表
     * 描述	查询考题列表
     * 根据keyword模糊查询标题
     * 验证	无
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/question/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * keyword	string		是	关键字
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "totalRow": 1,
     * "pageNumber": 1,
     * "firstPage": true,
     * "lastPage": true,
     * "totalPage": 1,
     * "pageSize": 10,
     * "list": [{
     * "id": "考题id",
     * "title": "考题标题",
     * "datetime": "2018-6-26 13:52",//最后一次修改日期
     * "creater_name": "马云"//发布人
     * }]
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "失败原因！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void list() {
        JsonHashMap jhm = new JsonHashMap();
        String keyword = getPara("keyword").trim();
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");
        //如果为空时赋给默认值
        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);
        try {
            String select = "select h_question.id id,h_question.title title,(select d.name from h_dictionary d where d.parent_id='3000'and d.value=h_question.kind_id) kind_id,(select t.name from h_question_type t where t.id=h_question.type_id) type_id,h_question.modify_time datetime,h_staff.name creater_name ";
            String sql = "from h_question left join h_staff on h_question.creater_id=h_staff.id where 1=1 ";
            //参数集合
            List<Object> params = new ArrayList<>();
            if (!StringUtils.isEmpty(keyword)) {
                keyword = "%" + keyword + "%";
                sql += " and (title like ? ) ";
                params.add(keyword);
            }
            //按考题最后修改日期倒序排序，即最新修改的考题在最前面
            sql += " order by h_question.modify_time desc,id";
            Page<Record> page = Db.paginate(pageNum, pageSize, select, sql, params.toArray());
            jhm.put("data", page);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 16.2.	添加考题
     * 名称	添加考题
     * 描述	添加考题。
     * 验证
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/question/add
     * 请求方式	post
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * title	string		否	考题名称
     * content	string		是	内容
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "添加成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "请填写考题标题！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "考题标题重复！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void add() {
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        Record question = this.getParaRecord();
        String title = question.getStr("title").trim();
        String typeId = getPara("type_id");
        String kindId = getPara("kind_id");
        if (StringUtils.isEmpty(typeId) || StringUtils.equals(typeId, "-1")) {
            jhm.putCode(0).putMessage("请选择考题分类！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(kindId) || StringUtils.equals(kindId, "-1")) {
            jhm.putCode(0).putMessage("请选择岗位名称！");
            renderJson(jhm);
            return;
        }
        //考题标题不允许为空也不允许为空格
        if (StringUtils.isEmpty(title) || StringUtils.isBlank(title)) {
            jhm.putCode(0).putMessage("请填写考题标题！");
            renderJson(jhm);
            return;
        }
        //考题标题不能重复
        try {
            String sql = "select count(*) c from h_question where title=?";
            Record record = Db.findFirst(sql, title);
            if (record.getInt("c") != 0) {
                jhm.putCode(0).putMessage("考题标题重复！");
            } else {
                question.set("id", UUIDTool.getUUID());
                //前台需要修改增加的参数
                question.set("creater_id", usu.getUserId());
                question.set("modifier_id", usu.getUserId());
                String time = DateTool.GetDateTime();
                question.set("create_time", time);
                question.set("modify_time", time);
                //保存数据到数据库
                boolean flag = Db.save("h_question", question);
                if (flag) {
                    //添加成功
                    jhm.putCode(1).putMessage("添加成功！");
                } else {
                    //添加失败
                    jhm.putCode(0).putMessage("添加失败！");
                }
            }
        } catch (Exception e) {
            jhm.putCode(-1).putMessage("服务器发生异常！");
            e.printStackTrace();
        }
        renderJson(jhm);
    }

    /**
     * 16.3.	修改考题
     * 名称	修改考题
     * 描述	修改考题。
     * 验证	验证标题是否重复
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/question/updateById
     * 请求方式	post
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	考题id
     * title	string		否	考题标题
     * content	string		是	内容
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "修改成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "请填写考题标题！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "考题标题重复！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void updateById() {
        JsonHashMap jhm = new JsonHashMap();
        Record question = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String typeId = getPara("type_id");
        String kindId = getPara("kind_id");
        if (StringUtils.isEmpty(question.getStr("id"))) {
            jhm.putCode(0).putMessage("考题id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(typeId) || StringUtils.equals(typeId, "-1")) {
            jhm.putCode(0).putMessage("请选择考题分类！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(kindId) || StringUtils.equals(kindId, "-1")) {
            jhm.putCode(0).putMessage("请选择岗位名称！");
            renderJson(jhm);
            return;
        }
        String title = question.getStr("title").trim();
        //标题不允许为空也不允许为空格
        if (StringUtils.isEmpty(question.getStr("title")) || StringUtils.isBlank(title)) {
            jhm.putCode(0).putMessage("请填写考题标题！");
            renderJson(jhm);
            return;
        }
        //考题标题不能重复
        try {
            String sql = "select count(*) c from h_question where title=? and id <>?";
            String id = question.getStr("id");
            Record record = Db.findFirst(sql, title, id);
            if (record.getInt("c") != 0) {
                jhm.putCode(0).putMessage("考题标题重复！");
            } else {
                String time = DateTool.GetDateTime();
                question.set("modifier_id", usu.getUserId());
                question.set("modify_time", time);
                question.remove("datetime");
                question.remove("creater_name");
                boolean flag = Db.update("h_question", question);
                if (flag) {
                    jhm.putCode(1).putMessage("修改成功！");
                } else {
                    jhm.putCode(0).putMessage("修改失败！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 16.4.	查看考题
     * 名称	查看考题
     * 描述	查看考题。
     * 验证
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/question/showById
     * 请求方式	post
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	考题id
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "id": "考题id",
     * "title": "考题标题",
     * "datetime": "2018-6-26 13:52",//最后一次修改日期
     * "creater_name": "马云"//发布人
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "查看失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void showById() {
        JsonHashMap jhm = new JsonHashMap();
        //获取当前考题id
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("查看失败！");
            renderJson(jhm);
            return;
        }
        try {
            String sql = "select h_question.id id,h_question.title title,h_question.kind_id kind_id,h_question.content content,h_question.type_id type_id,h_question.modify_time datetime,h_staff.name creater_name from h_question left join h_staff  on h_staff.id=h_question.creater_id where h_question.id=? ";
            Record record = Db.findFirst(sql, id);
            if (record != null) {
                jhm.put("data", record);
            } else {
                jhm.putCode(0).putMessage("考题不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 16.5.	删除考题
     * 名称	删除考题
     * 描述	删除考题。
     * 验证
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/question/deleteById
     * 请求方式	post
     * 请求参数类型	key=value
     * <p>
     * 请求参数：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	考题id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "删除成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "考题不存在！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void deleteById() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("考题不存在！");
            renderJson(jhm);
            return;
        }
        try {
            boolean flag = Db.deleteById("h_question", id);
            if (flag) {
                jhm.putCode(1).putMessage("删除成功！");
            } else {
                jhm.putCode(0).putMessage("删除失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    public void getTypeByKind() {
        String dict = getPara("dict");
        JsonHashMap jhm = new JsonHashMap();
        try {
            List<Record> list;
            list = Db.find("select t.name name,t.id value from h_question_type t where t.kind_id=? order by t.id", dict);
            jhm.putCode(1).put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

}
