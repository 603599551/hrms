package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.sun.prism.impl.Disposer;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SafeTagCtrl extends Controller {
    public void save(){
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }

        String name=getPara("name");
        String staffId=getPara("staff_id");
        Record r=new Record();
        r.set("id", UUIDTool.getUUID());
        r.set("name",name);
        r.set("staff_id",creator);
        r.set("staff_name",creator_name);
        try {
            Db.save("tag", r);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }

    public void query(){
        JsonHashMap jhm=new JsonHashMap();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        StringBuilder sql=new StringBuilder("select * from tag where 1=1 ");
        List paraList=new ArrayList();
//        if(creator!=null && !"".equals(creator)){
//            sql.append(" and staff_id=?");
//            paraList.add(creator);
//        }
        sql.append(" order by create_time desc ");
        try {
            List<Record> list = Db.find(sql.toString(), paraList.toArray());
            for (Record r : list) {
                r.set("value", r.get("name"));
                r.remove("name");
            }
            renderJson(list);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
