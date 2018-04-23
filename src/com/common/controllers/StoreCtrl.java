package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import jdk.nashorn.internal.ir.CatchNode;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 显示门店
 */
public class StoreCtrl extends BaseCtrl {

    /**
    传输参数返回list
     */
    public void showList(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select * from store where status in ('1','0') order by sort");
            for (Record r : list) {
                int status = r.getInt("status");
                String name = r.getStr("name");
                if (status == 0) {//停用
                    name = name + "(停用)";
                    r.set("name", name);
                    r.set("disabled", "true");
                }
            }
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
}
