package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidateAjaxCtrl extends Controller{
    /*
    验证是否有重复记录
    dbObj 表名，可以被fields内的dbObj覆盖
    where 过滤条件。如果是修改功能，此处需要过滤掉自己的id
    fields 查询字段名，必须，json对象，里面的dbObj、where可以不填写，
            fields:[{"field":"name","value":"鹿晗","dbObj":"user","where":"id!=2322"}]
     */
    public void duplicate(){
        try{
            String dbObj=getPara("dbObj");
            String fields=getPara("fields");
            String where=getPara("where");

            JSONArray fieldsJsonArray=JSON.parseArray(fields);
            //String[] fieldsArray=fields.split(",");
            Map ret=new HashMap();
            List list=new ArrayList();
            for(int i=0;i<fieldsJsonArray.size();i++) {
                JSONObject fieldsJson=fieldsJsonArray.getJSONObject(i);
                String dbObjFields=fieldsJson.getString("dbObj");
                if(dbObjFields==null){
                    dbObjFields=dbObj;
                }
                String field=fieldsJson.getString("field");
                String value=fieldsJson.getString("value");
                String whereFields=fieldsJson.getString("where");
                if(whereFields==null ){
                    whereFields=where;
                }

                String sql="select count(*) as count from " +  dbObjFields+ " where "+field+"='"+value+"'";
                if(whereFields!=null && !"".equals(whereFields)){
                    sql=sql+" and "+whereFields;
                }
                Record r=Db.findFirst(sql);
                Object countObj=r.get("count");
                int count= NumberUtils.parseInt(countObj,0);
                if(count>0){
                    Map map=new HashMap();
                    map.put("field",field);
                    map.put("value",value);
                    map.put("dbObj",dbObj);
                    map.put("count",count);

                    list.add(map);
                }
            }
            ret.put("list",list);
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
