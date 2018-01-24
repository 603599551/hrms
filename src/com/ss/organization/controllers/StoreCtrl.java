package com.ss.organization.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.Map;

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
            sort=nextSort(DbUtil.queryMaxSort("Store","sort"));
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
        String id=getPara("id");


    }
    public void showById(){

    }
    public void updateById(){

    }
    public void list(){

    }
    public void query(){

    }
    public void index(){

    }
}
