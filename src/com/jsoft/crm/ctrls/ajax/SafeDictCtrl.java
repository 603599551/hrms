package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class SafeDictCtrl extends Controller {

    public void index(){
        String id=getPara("id");
        String parent_id=getPara("parentId");
        String dbObj=getPara("dbObj");
        JsonHashMap reMap=_query(dbObj,parent_id,id);
        renderJson(reMap);

    }
    private JsonHashMap _query(String dbObj,String parent_id,String id){
        JsonHashMap reMap=new JsonHashMap();
        if(StringUtils.isEmpty(dbObj)){
            reMap.putCode(-1);
            reMap.putMessage("dbObj不能为空！");
            return(reMap);
        }
        StringBuilder where=new StringBuilder("select * from "+dbObj+" where 1=1 ");
        List paraList=new ArrayList();
        if(StringUtils.isNotEmpty(parent_id)){
            where.append(" and parent_id=?");
            paraList.add(parent_id);
        }
        where.append("order by sort");
        List<Record> list= Db.find(where.toString(),paraList.toArray());
        reMap.putCode(1);
        reMap.put("result",list);
        return reMap;
    }
    public void showStudentStatus(){
        JsonHashMap reMap=_query("dictionary","7","");
        List list=(List)reMap.get("result");
        Record r=new Record();
        r.set("name","请选择状态");
        list.add(0,r);
        renderJson(reMap);
    }
    public void showSchool(){
        JsonHashMap reMap=_query("dictionary","24","");
        List list=(List)reMap.get("result");
        Record r=new Record();
        r.set("name","请选择学校");
        list.add(0,r);
        renderJson(reMap);
    }
    public void showSpeciality(){
        JsonHashMap reMap=_query("dictionary","25","");
        renderJson(reMap);
    }
    public void showSource(){
        JsonHashMap reMap=_query("dictionary","12","");
        renderJson(reMap);
    }
    public void showContactType(){
        JsonHashMap reMap=_query("dictionary","18","");
        List list=(List)reMap.get("result");
//        Record r=new Record();
//        r.set("name","请选择联系类型");
//        r.set("id","");
//        list.add(0,r);
        renderJson(reMap);
    }
    public void toSearch(){
        String id=getPara("id");
        List<Record> list= Db.find(" select * from ( " +
                " select * from dictionary where parent_id=? " +
                " union" +
                " select * from dictionary where id=? " +
                ") as a " +
                " order by sort ",id,id);
        if(list!=null && !list.isEmpty()){
            Record r=list.get(0);
            r.set("id","");
            r.set("name","请选择职务");

        }
        renderJson(list);
    }
}
