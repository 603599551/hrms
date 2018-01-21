package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import easy.util.UUIDTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
admin操作类
 */
public class SafeAdminAjaxCtrl extends Controller {
    private static final String db_tb = "admin";
    /*
    添加管理员
     */
    public void save(){
        try {
            Map paraMap = RequestTool.getParameterMap(getRequest());
            String id= UUIDTool.getUUID();
            String time= DateTool.GetDateTime();

            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                r.set(en.getKey(),en.getValue());
            }
            r.remove("dbObj");
            r.set("id",id);
            r.set("create_time",time);
            r.set("modify_time",time);
            Db.save(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",0);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
}
