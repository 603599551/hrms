package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 显示select控件的数据
 */
public class SelectDataBuilderCtrl extends BaseCtrl{
    /**
     * 显示仓库
     */
    public void showWarehourseList(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select id,code,name from warehouse where status=1 order by create_time");
            Record r = new Record();
            r.set("name", "全部");
            r.set("id", "-1");
            list.add(0, r);
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
