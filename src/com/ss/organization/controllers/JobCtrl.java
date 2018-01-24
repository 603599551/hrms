package com.ss.organization.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.KEY;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobCtrl extends Controller{

    @Before(Tx.class)
    public void add(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            JSONObject json = RequestTool.getJson(getRequest());
            String name = json.getString("name");
            String desc = json.getString("desc");
            JSONArray jsonArray = json.getJSONArray("list");
            if(name==null || "".equals(name)){
                jhm.putCode(-1);
                jhm.putMessage("名称不能为空！");
                renderJson(jhm);
                return;
            }
            if(jsonArray==null || jsonArray.isEmpty()){
                jhm.putCode(-1);
                jhm.putMessage("权限不能为空！");
                renderJson(jhm);
                return;
            }
            UserSessionUtil usu = new UserSessionUtil(getRequest());

            String time = DateTool.GetDateTime();
            String uuid = UUIDTool.getUUID();

            Record jobR = new Record();
            jobR.set("id", uuid);
            jobR.set("name", name);
            jobR.set("desc", desc);
            jobR.set("create_time", time);
            jobR.set("creator", usu.getUserId());
            jobR.set("creator_name", usu.getRealName());
            boolean b=Db.save("job", jobR);

            saveJobMenu(jsonArray,uuid,time,usu);
            jhm.putCode(1);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());

        }
        renderJson(jhm);
    }
    private void saveJobMenu(JSONArray jsonArray,String jobId,String time,UserSessionUtil usu){
        if(jsonArray!=null && !jsonArray.isEmpty()){
            /*
            找出顶级节点，放入map中
             */
            Map<String,JSONObject> topMenuMap=new HashMap();
            List<JSONObject> subMenuList=new ArrayList();
            for(Object temp:jsonArray){
                JSONObject jsonTemp=(JSONObject)temp;
                String menuParentId=jsonTemp.getString("parent_id");
                String menuId=jsonTemp.getString("id");
                boolean power=jsonTemp.getBoolean("power");
                if("0".equals(menuParentId)){
                    topMenuMap.put(menuId,jsonTemp);
                }else{
                    subMenuList.add(jsonTemp);
                }
            }
            /*
            判断子节点为true，让其父节点也为true
             */
            for(Object temp:subMenuList){
                JSONObject jsonTemp=(JSONObject)temp;
                String menuParentId=jsonTemp.getString("parent_id");
                String menuId=jsonTemp.getString("id");
                boolean power=jsonTemp.getBoolean("power");
                if(!"0".equals(menuParentId) && power){
                    JSONObject menu=topMenuMap.get(menuParentId);
                    menu.put("power",true);
                }
            }
            for(Object temp:jsonArray){
                JSONObject jsonTemp=(JSONObject)temp;
                String menuParentId=jsonTemp.getString("parent_id");
                String menuId=jsonTemp.getString("id");
                boolean power=jsonTemp.getBoolean("power");
                String access="0";
                if(power){
                    access="1";
                }else{
                    access="0";
                }
                Record authorMenuJob=new Record();
                authorMenuJob.set("id",UUIDTool.getUUID());
                authorMenuJob.set("menu_id",menuId);
                authorMenuJob.set("job_id",jobId);
                authorMenuJob.set("access",access);
                authorMenuJob.set("creator",usu.getUserId());
                authorMenuJob.set("creator_name",usu.getRealName());
                authorMenuJob.set("create_time",time);
                Db.save("author_job_menu",authorMenuJob);
            }
        }
    }
    public void showResource(){

        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list=Db.find("select id,name,parent_id from menu order by sort");
            for(Record r:list){
                r.set("power",false);
            }
            List<Record> reList=sort(list);
            jhm.putCode(1);
            jhm.put("menuList",reList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }

        renderJson(jhm);
    }
    private List sort(List<Record> list){
        List<Record> mylist=new ArrayList(list);
        List<Record> reList=new ArrayList();
        //将一级菜单放入到reList中
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            Map temp=r.getColumns();
            String parentId=r.get("PARENT_ID");
            if("0".equals(parentId)){
                reList.add(r);
                mylist.remove(r);
                i--;
            }
        }
        //将二级菜单放入到一级菜单中
        for(int i=0;i<mylist.size();i++){
            Record r=mylist.get(i);
            String parentId=r.get("PARENT_ID");
            for(int j=0,size=reList.size();j<size;j++){
                Record top=reList.get(j);
                String idTop=(String)top.get("id");
                if(parentId.equals(idTop)){
                    reList.add(j+1,r);
//                    List topList=(List)top.get("list");
//                    topList.add(r.getColumns());
                }
            }
        }
        return reList;
    }
    public void list(){
        JsonHashMap jhm=new JsonHashMap();
        String sql="select j.id,j.name,ifnull(j.desc,'') as 'desc',ifnull( (select group_concat(name) from staff where staff.job=j.id and staff.status='5'),'')  as staffs,(select count(id) from staff where staff.job=j.id and staff.status='5') as staffs_count from job j order by create_time ";
        try {
            List<Record> list = Db.find(sql);
            jhm.putCode(1);
            jhm.put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            JSONObject json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1);
                jhm.putMessage("请传入数据！");
                renderJson(jhm);
                return;
            }
            String jobId=json.getString("id");
            String name = json.getString("name");
            String desc = json.getString("desc");
            JSONArray jsonArray = json.getJSONArray("list");
            if(jobId==null || "".equals(jobId)){
                jhm.putCode(-1);
                jhm.putMessage("id不能为空！");
                renderJson(jhm);
                return;
            }
            if(name==null || "".equals(name)){
                jhm.putCode(-1);
                jhm.putMessage("名称不能为空！");
                renderJson(jhm);
                return;
            }
            if(jsonArray==null || jsonArray.isEmpty()){
                jhm.putCode(-1);
                jhm.putMessage("权限不能为空！");
                renderJson(jhm);
                return;
            }
            UserSessionUtil usu = new UserSessionUtil(getRequest());

            String time = DateTool.GetDateTime();

            Record jobR = new Record();
            jobR.set("id", jobId);
            jobR.set("name", name);
            jobR.set("desc", desc);
            jobR.set("create_time", time);
            jobR.set("creator", usu.getUserId());
            jobR.set("creator_name", usu.getRealName());
            boolean b=Db.update("job", jobR);

            //先将原有的job_menu数据删除
            int sqlNum=Db.update("delete from author_job_menu where job_id=?",jobId);

            saveJobMenu(jsonArray,jobId,time,usu);
            jhm.putCode(1);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());

        }
        renderJson(jhm);
    }
    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        String sql="select id,name,parent_id,case power when 0 then 'false' when 1 then 'true' end as power from " +
                "(" +
                " select id,name,parent_id," +
                " ifnull((select access from author_job_menu where author_job_menu.menu_id=m.id and author_job_menu.job_id=?),0) as power  " +
                ",sort " +
                " from menu m " +
                " ) as a" +
                " order by sort ";
        try{
            Record r=Db.findFirst("select id,name,ifnull(`desc`,'') as `desc` from job where id=?",id);
            jhm.put("job",r);

            List<Record> list=Db.find(sql,id);
            List<Record> reList=sort(list);
            jhm.putCode(1);
            for(Record temp:list){
                String powerStr=temp.getStr("power");
                boolean powerBool=Boolean.parseBoolean(powerStr);
                temp.set("power",powerBool);
            }
            jhm.put("menuList",reList);

            jhm.putCode(1);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void deleteById(){
        String jobId=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(jobId==null || "".equals(jobId)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        Record r=Db.findFirst("select count(id) as count,group_concat(name) as names from staff where job=? and staff.status='5'",jobId);
        if(r!=null){
            Object countObj=r.get("count");
            String names=r.getStr("names");
            int count= NumberUtils.parseInt(countObj,0);
            if(count>0){
                jhm.putCode(-1);
                jhm.putMessage("该职务已经分配给 "+names+" ，请将成员的职务删除，再尝试删除该职务！");
                renderJson(jhm);
                return;
            }
        }
        boolean b=Db.deleteById("job",jobId);
        int sqlNum=Db.update("delete from author_job_menu where job_id=?",jobId);
        if(b){
            jhm.putCode(1);
            jhm.putMessage("删除成功！");
        }else{
            jhm.putCode(-1);
            jhm.putMessage("删除失败！");
        }
        renderJson(jhm);
    }

    public void showJobs(){
        try {
            List<Record> list = Db.find("select id,name from job order by create_time");
            Record r=new Record();
            r.set("id","");
            r.set("name","请选择职务");
            list.add(0,r);
            renderJson(list);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }

    }
}
