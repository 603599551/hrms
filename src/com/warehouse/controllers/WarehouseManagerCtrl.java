package com.warehouse.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.sun.prism.impl.Disposer;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.List;
import java.util.Map;

/**
 * 仓库管理
 */
public class WarehouseManagerCtrl extends BaseCtrl {
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        String uuid=UUIDTool.getUUID();

        try{
            JSONObject jsonObject=RequestTool.getJson(getRequest());
            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String desc=jsonObject.getString("desc");

            if(StringUtils.isEmpty(name)){
                jhm.putCode(0).putMessage("请输入名称！");
                return;
            }
            Record r=new Record();
            r.set("id", uuid);
            r.set("code", code);
            r.set("name", name);
            r.set("desc", desc);
            r.set("status", 1);
            r.set("creater_id", usu.getUserId());
            r.set("modifier_id", usu.getUserId());
            r.set("create_time", datetime);
            r.set("modify_time", datetime);

            boolean b=Db.save("warehouse",r);
            if(b){
                jhm.putCode(1).putMessage("添加成功！");
            }else{
                jhm.putCode(0).putMessage("添加失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void deleteById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            boolean b=Db.deleteById("warehouse",id);
            if(b){
                jhm.putCode(1).putMessage("删除成功！");
            }else{
                jhm.putCode(0).putMessage("删除失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void showById() {
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");
        try {
            Record r = Db.findById("warehouse", id);
            if (r != null) {
                jhm.putCode(1).put("data", r);
            } else {
                jhm.putCode(0).putMessage("查无此记录！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void updateById() {
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();

        try{
            JSONObject jsonObject=RequestTool.getJson(getRequest());
            String id=jsonObject.getString("id");
            String name=jsonObject.getString("name");
            String desc=jsonObject.getString("desc");

            if(StringUtils.isEmpty(name)){
                jhm.putCode(0).putMessage("请输入名称！");
                return;
            }
            Record r=new Record();
            r.set("id", id);
            r.set("name", name);
            r.set("desc", desc);
            r.set("status", 1);
            r.set("modifier_id", usu.getUserId());
            r.set("modify_time", datetime);

            boolean b=Db.update("warehouse",r);
            if(b){
                jhm.putCode(1).putMessage("修改成功！");
            }else{
                jhm.putCode(0).putMessage("修改失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void list() {
        String keyword=getPara("keyword");
        JsonHashMap jhm=new JsonHashMap();
        try{
            SelectUtil selectUtil=new SelectUtil("select id,code,name,`desc`,creater_id,modifier_id,create_time,modify_time,concat(status,'') as status,case status when 1 then '启用' when 0 then '停用' end as status_text from warehouse");
            selectUtil.addWhere("name=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,keyword);
            selectUtil.order("order by status desc,create_time");
            String sql=selectUtil.toString();
            List<Record> list=Db.find(sql,selectUtil.getParameterList().toArray());
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void enabel(){
        String id=getPara("id");
        String status=getPara("status");
        JsonHashMap jhm=new JsonHashMap();
        if(StringUtils.isBlank(id)){
            jhm.putCode(0).putMessage("请输入ID！");
            return;
        }
        if(StringUtils.isBlank(status)){
            jhm.putCode(0).putMessage("请输入状态！");
            return;
        }
        try {
            int i=Db.update("update warehouse set status=? where id=?",status,id);
            if(i>0){
                jhm.putCode(1).putMessage("操作成功！");
            }else{
                jhm.putCode(0).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    @Override
    public void query() {
        super.query();
    }

    @Override
    public void index() {
        super.index();
    }
}
