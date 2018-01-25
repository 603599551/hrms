package com.ss.organization.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.StringUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.List;

/**
 * 门店管理
 */
public class StoreCtrl extends BaseCtrl {
    public void add(){
        JsonHashMap jhm=new JsonHashMap();
        JSONObject json;
        try {
            json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请上传数据！");
                renderJson(jhm);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String sortStr=json.getString("sort");
        int sort=1;
        try{
            sort=Integer.parseInt(sortStr);
        }catch (Exception e){
            sort=nextSort(DbUtil.queryMax("Store","sort"));
        }
        String uuid= UUIDTool.getUUID();
        String dateTime= DateTool.GetDateTime();
        Record record=new Record();
        record.set("id",uuid);
        record.set("name",json.getString("name"));
        record.set("address",json.getString("address"));
        record.set("phone",json.getString("phone"));
        record.set("desc",json.getString("desc"));
        record.set("sort",sort);
        record.set("creater_id",usu.getUserId());
        record.set("modifier_id",usu.getUserId());
        record.set("create_time",dateTime);
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.save("store", record);
            if (b) {
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }


    private int nextSort(int sort){
        int i=sort;
        while(true){
            i++;
            if(i%10==0){
                break;
            }
        }
        return i;
    }
    public void deleteById(){

    }

    /**
     * 停用门店
     */
    public void stop(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(org.apache.commons.lang.StringUtils.isEmpty(id)){
            jhm.putCode(-1).putMessage("id不能为空！");
            renderJson(jhm);
            return ;
        }
        try {
            int i = Db.update("update store set status=? where id=?", 3,id);
            if(i>0){
                jhm.putCode(1).putMessage("停用操作成功！");
            }else{
                jhm.putCode(-1).putMessage("停用操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(org.apache.commons.lang.StringUtils.isEmpty(id)){
            jhm.putCode(-1).putMessage("id不能为空！");
            renderJson(jhm);
            return ;
        }
        try {
            Record storeRecord=Db.findById("store",id);
            if(storeRecord!=null){
                jhm.putCode(1).put("data",storeRecord);
            }else{
                jhm.putCode(-1).putMessage("查询失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();
        JSONObject json;
        try {
            json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请上传数据！");
                renderJson(jhm);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String sortStr=json.getString("sort");
        int sort=1;
        try{
            sort=Integer.parseInt(sortStr);
        }catch (Exception e){
            sort=nextSort(DbUtil.queryMax("Store","sort"));
        }
        String uuid= json.getString("id");
        String dateTime= DateTool.GetDateTime();
        Record record=new Record();
        record.set("id",uuid);
        record.set("name",json.getString("name"));
        record.set("address",json.getString("address"));
        record.set("phone",json.getString("phone"));
        record.set("desc",json.getString("desc"));
        record.set("sort",sort);
//        record.set("creater_id",usu.getUserId());
        record.set("modifier_id",usu.getUserId());
//        record.set("create_time",dateTime);
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.update("store", record);
            if (b) {
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void list(){

    }
    public void query(){
        String key=getPara("name");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm=new JsonHashMap();
        try {
            SQLUtil sql = SQLUtil.initSelectSQL("from store");
            sql.addWhere(" and name=? ", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, key);
            sql.addWhere(" or phone=? ", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, key);
            sql.append(" order by modify_time desc ");
            Page<Record> page = Db.paginate(pageNum, pageSize, "select id,name,ifnull(address,'') as address,ifnull(phone,'') as phone", sql.toString(), sql.getParameterArray());
            jhm.putCode(1).put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void index(){

    }
}
