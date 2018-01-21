package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 自定义学员表单
 */
public class SafeStudentFormCtrl extends Controller{

    /**
     * 添加、更新操作
     */
    public void save(){
        JsonHashMap jhm=new JsonHashMap();
        Map paraMap=getParaMap();
        if(paraMap==null || "".equals(paraMap)){
            jhm.putCode(-1);
            jhm.putMessage("提交数据为空！");
            renderJson(jhm);
            return;
        }

        Iterator<String> it=paraMap.keySet().iterator();
        String key=null;
        while(it.hasNext()){
            key=it.next();
        }

        JSONObject jsonObj=JSONObject.parseObject(key);
        JSONArray jsonArray=jsonObj.getJSONArray("content");


        String time= DateTool.GetDateTime();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }


        Record r=new Record();
        r.set("content",jsonArray.toJSONString());
        r.set("creator",creator);
        r.set("creator_name",creator_name);
        r.set("name","student");
        try {
            Record dbR=Db.findFirst("select * from form where name=?","student");
            if(dbR==null){//如果查询不到，执行insert操作
                String uuid= UUIDTool.getUUID();
                r.set("id",uuid);
                r.set("create_time",time);
                r.set("modify_time",time);
                boolean b=Db.save("form",r);
                if(b){
                    jhm.putCode(1);
                }else{
                    jhm.putCode(-1);
                    jhm.putMessage("添加失败！");
                }
            }else{//如果能查询到，执行update操作
                String id=dbR.get("id");
                r.set("id",id);
                r.set("modify_time",time);
                boolean b=Db.update("form",r);
                if(b){
                    jhm.putCode(1);
                }else{
                    jhm.putCode(-1);
                    jhm.putMessage("更新失败！");
                }
            }

            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            JsonHashMap errorJhm=new JsonHashMap();
            errorJhm.putCode(KEY.CODE.ERROR);
            errorJhm.putMessage(e.toString());
            renderJson(errorJhm);
            return;
        }
    }


    public void show(){
        JsonHashMap jhm=new JsonHashMap();
        Record r=Db.findFirst("select * from form where name=?","student");
        if(r==null){
            renderJson(new HashMap());
        }else{
            renderJson(r.getColumns());
        }
    }
    public void query(){

    }
}
