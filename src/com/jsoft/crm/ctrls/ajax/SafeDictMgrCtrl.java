package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.NumberUtils;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class SafeDictMgrCtrl extends Controller {
    List dictList=new ArrayList();
    public SafeDictMgrCtrl(){
        dictList.add("stff_status");
        dictList.add("std_status");
        dictList.add("sourcee");
        dictList.add("contact_type");
        dictList.add("school");
        dictList.add("speciality");



    }
    public void add(){
//        String dict=getPara("dict");
        String pid=getPara("pid");
        String name=getPara("name");
        String value=getPara("value");
        String sortStr=getPara("sort");
        JsonHashMap jhm=new JsonHashMap();
//        if(dict==null || "".equalsIgnoreCase(dict)){
//            jhm.putCode(-1);
//            jhm.putMessage("dict不能为空！");
//            renderJson(jhm);
//            return;
//        }
        if(pid==null || "".equalsIgnoreCase(pid)){
            jhm.putCode(-1);
            jhm.putMessage("pid不能为空！");
            renderJson(jhm);
            return;
        }
        if(name==null || "".equalsIgnoreCase(name)){
            jhm.putCode(-1);
            jhm.putMessage("name不能为空！");
            renderJson(jhm);
            return;
        }
        if(sortStr==null || "".equalsIgnoreCase(sortStr)){
            jhm.putCode(-1);
            jhm.putMessage("sort不能为空！");
            renderJson(jhm);
            return;
        }
        boolean b=sortStr.matches("[0-9]*");
        if(!b){
            jhm.putCode(-1);
            jhm.putMessage("sort请输入数字！");
            renderJson(jhm);
            return;
        }
        int sort= NumberUtils.parseInt(sortStr,0);

        try {
            Record r = new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("parent_id", pid);
            r.set("name", name);
            r.set("value", value);
            r.set("sort", sort);
            b = Db.save("dictionary", r);
            if (b) {
                jhm.putCode(1);

            } else {
                jhm.putCode(-1);
                jhm.putMessage("保存失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示顶级数据字典
     */
    public void parentList(){
        //不显示职务
        List<Record> list=Db.find("select id,name from dictionary where parent_id='0' and id<>'1' order by sort");
        String selected="";
        if(list!=null && !list.isEmpty()){
            selected=list.get(0).get("id");
        }
        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1);
        jhm.put("list",list);
        jhm.put("selected",selected);
        renderJson(jhm);
    }

    /**
     * 传入pid，查询子数据字典
     */
    public void list(){
        String pid=getPara("pid");
        JsonHashMap jhm=new JsonHashMap();
        if(pid==null || "".equals(pid)){
//            Record r=Db.findFirst("select id,name from dictionary where parent_id='0' and id<>'1' order by sort");
        }
        List<Record> list=Db.find("select id,name,sort from dictionary where parent_id=? order by sort",pid);
        renderJson(list);
    }
    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equals(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空");
            renderJson(jhm);
            return;
        }
        Record r=Db.findById("dictionary",id);
        if(r!=null){
            r.set("id",id);
            String value=r.get("value");
            if(value==null){
                r.set("value","");
            }
        }
        renderJson(r);
    }

    public void deleteById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equals(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空");
            renderJson(jhm);
            return;
        }
        boolean b=Db.deleteById("dictionary",id);
        if(b){
            jhm.putCode(1);
            jhm.putMessage("删除成功！");
        }else{
            jhm.putCode(-1);
            jhm.putMessage("删除失败！");
        }
        renderJson(jhm);
    }
    public void updateById(){
//        String dict=getPara("dict");
        String id=getPara("id");
        String pid=getPara("pid");
        String name=getPara("name");
        String value=getPara("value");
        String sortStr=getPara("sort");
        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equalsIgnoreCase(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        if(pid==null || "".equalsIgnoreCase(pid)){
            jhm.putCode(-1);
            jhm.putMessage("pid不能为空！");
            renderJson(jhm);
            return;
        }
        if(name==null || "".equalsIgnoreCase(name)){
            jhm.putCode(-1);
            jhm.putMessage("name不能为空！");
            renderJson(jhm);
            return;
        }
        if(sortStr==null || "".equalsIgnoreCase(sortStr)){
            jhm.putCode(-1);
            jhm.putMessage("sort不能为空！");
            renderJson(jhm);
            return;
        }
        boolean b=sortStr.matches("[0-9]*");
        if(!b){
            jhm.putCode(-1);
            jhm.putMessage("sort请输入数字！");
            renderJson(jhm);
            return;
        }
        int sort= NumberUtils.parseInt(sortStr,0);
        try{
            Record r=new Record();
            r.set("id", id);
            r.set("parent_id",pid);
            r.set("name",name);
            r.set("value",value);
            r.set("sort",sort);
            b=Db.update("dictionary",r);
            if (b) {
                jhm.putCode(1);

            } else {
                jhm.putCode(-1);
                jhm.putMessage("更新失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
