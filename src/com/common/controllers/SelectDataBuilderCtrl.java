package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
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

    /**
     * 显示原材料分类，仅显示二级分类
     */
    public void showMaterialType(){
        JsonHashMap jhm=new JsonHashMap();
        List<Record> list =Db.find("select id,code,name from material_type where parent_id<>? order by sort",0);
        Record r = new Record();
        r.set("name", "全部");
        r.set("id", "-1");
        list.add(0, r);
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
