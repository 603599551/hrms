package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 显示数据字典
 */
public class DictionaryCtrl extends BaseCtrl {

    /**
     * 第一项为“全部”
    传输参数返回list
     */
    public void showList(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select * from dictionary where parent_id=(select id from dictionary where value=?) order by sort", dict);
            Record all = new Record();
            all.set("id", "-1");
            all.set("name", "全部");
            list.add(0, all);
            jhm.putCode(1).put("list", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 第一项为“请选择”
     */
    public void showList2(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select * from dictionary where parent_id=(select id from dictionary where value=?) order by sort", dict);
//            Record all = new Record();
//            all.set("id", "-1");
//            all.set("name", "请选择");
//            all.set("value", "-1");
//            list.add(0, all);
            jhm.putCode(1).put("list", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}