package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 显示数据字典
 */
public class DictionaryCtrl extends BaseCtrl {

    /**
     * 第一项为“全部”
     * 传输参数返回list
     * 参数是字典值
     */
    public void getDictIncludeAll(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
            Record all = new Record();
            all.set("value", "-1");
            all.set("name", "全部");
            list.add(0, all);
            jhm.putCode(1).put("data", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 第一项为“请选择”
     */
    public void getDictIncludeChoose(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
            Record all = new Record();
            all.set("value", "-1");
            all.set("name", "请选择");
            list.add(0, all);
            jhm.putCode(1).put("data", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /**
     * 只返回数据库中的字典值
     */
    public void getDict(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
            jhm.putCode(1).put("data", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

}
