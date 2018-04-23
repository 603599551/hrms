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
    传输参数返回list
     */
    public void showList(){
        String dict=getPara("dict");
        JsonHashMap jhm=new JsonHashMap();
        List<Record> list=Db.find("select * from dictionary where parent_id=(select id from dictionary where value=?) order by sort",dict);
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
