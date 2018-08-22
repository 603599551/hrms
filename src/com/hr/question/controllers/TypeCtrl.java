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

public class TypeCtrl extends BaseCtrl{
    /**
     * 考题分类列表
     */
    public void list(){
        JsonHashMap jhm = new JsonHashMap();
        String keyword = getPara("keyword").trim();
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");
        //如果为空时赋给默认值
        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);
        try {
            String select = "select h_question_type.id id,h_question_type.name name,h_question_type.kind_id kind,h_question_type.modify_time date,h_staff.name creater_name ";
            String sql = "from h_question_type left join h_staff on h_question_type.creater_id=h_staff.id where 1=1 ";
            //参数集合
            List<Object> params = new ArrayList<>();
            if (!StringUtils.isEmpty(keyword)) {
                keyword = "%" + keyword + "%";
                sql += " and (h_question_type.name like ? ) ";
                params.add(keyword);
            }
            //按考题最后修改日期倒序排序，即最新修改的考题在最前面
            sql += " order by h_question_type.modify_time desc,h_question_type.id";
            Page<Record> page = Db.paginate(pageNum, pageSize, select, sql, params.toArray());
            jhm.put("data", page);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
    /**
     * 添加考题分类
     */

    public void add(){
        JsonHashMap jhm = new JsonHashMap();
        Record type = this.getParaRecord();
        String name = type.getStr("name").trim();
        String kind=type.getStr("kind").trim();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //考题分类名称不允许为空也不允许为空格
        if(StringUtils.isEmpty(name)|| StringUtils.isBlank(name)){
            jhm.putCode(0).putMessage("请填写考题分类名称！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(kind)|| StringUtils.isBlank(kind)){
            jhm.putCode(0).putMessage("请选择岗位名称！");
            renderJson(jhm);
            return;
        }

        try {

                type.set("id", UUIDTool.getUUID());
                type.set("name",name);
                type.set("kind_id",kind);
                type.set("creater_id", usu.getUserId());
                type.set("modifier_id", usu.getUserId());
                String time = DateTool.GetDateTime();
                type.set("create_time", time);
                type.set("modify_time", time);
                type.remove("kind");
                //保存数据到数据库
                boolean flag = Db.save("h_question_type", type);
                if (flag) {
                    //添加成功
                    jhm.putCode(1).putMessage("添加成功！");
                } else {
                    //添加失败
                    jhm.putCode(0).putMessage("添加失败！");
                }
        } catch (Exception e) {
            jhm.putCode(-1).putMessage("服务器发生异常！");
            e.printStackTrace();
        }
        renderJson(jhm);
    }

    /**
     *
     * 修改考题分类
     */
    public void updateById(){
        JsonHashMap jhm = new JsonHashMap();
        Record type = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        if (StringUtils.isEmpty(type.getStr("id"))) {
            jhm.putCode(0).putMessage("考题分类id不能为空！");
            renderJson(jhm);
            return;
        }
        String name = type.getStr("name").trim();
        String kind=type.getStr("kind").trim();
        //分类名称不允许为空也不允许为空格
        if (StringUtils.isEmpty(name)|| StringUtils.isBlank(name)) {
            jhm.putCode(0).putMessage("请填写考题分类名称！");
            renderJson(jhm);
            return;
        }

        if (StringUtils.isEmpty(kind) || StringUtils.isBlank(kind)) {
            jhm.putCode(0).putMessage("请填写考题岗位名称！");
            renderJson(jhm);
            return;
        }
        try {
                String time = DateTool.GetDateTime();
                type.set("name",name);
                type.set("kind_id",kind);
                type.set("modifier_id", usu.getUserId());
                type.set("modify_time", time);
                type.remove("kind");
                boolean flag = Db.update("h_question_type", type);
                if (flag) {
                    jhm.putCode(1).putMessage("修改成功！");
                } else {
                    jhm.putCode(0).putMessage("修改失败！");
                }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     *
     * 查看考题
     */
    public void showById(){
        JsonHashMap jhm = new JsonHashMap();
        //获取当前考题分类id
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("查看失败！");
            renderJson(jhm);
            return;
        }
        try {
            String sql = "select h_question_type.id id,h_question_type.name name,h_question_type.kind_id kind from h_question_type where h_question_type.id=? ";
            Record record = Db.findFirst(sql, id);
            if (record != null) {
                jhm.put("data", record);
            } else {
                jhm.putCode(0).putMessage("考题分类不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
    /**
     * 删除考题分类
     */
    public void delete(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("考题分类id不能为空！");
            renderJson(jhm);
            return;
        }
        try {
            String sql = "select count(*) c from h_question_type where id=?";
            Record record = Db.findFirst(sql, id);
            if (record.getInt("c") == 0) {
                jhm.putCode(0).putMessage("考题分类不存在！");
                renderJson(jhm);
                return;
            }
            boolean flag = Db.deleteById("h_question_type", id);
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
}
