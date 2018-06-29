package com.common.controllers;

import com.alibaba.fastjson.JSONObject;
import com.common.service.JobService;
import com.jfinal.KEY;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobCtrl extends BaseCtrl {

    @Before(Tx.class)
    public void add(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record json = this.getParaRecord();
            String name = json.getStr("name");
            String desc = json.getStr("desc");
            String menuIds = json.getStr("menuIds");
            if(name==null || "".equals(name)){
                jhm.putCode(-1);
                jhm.putMessage("名称不能为空！");
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
            boolean b= Db.save("h_job", jobR);
            if(StringUtils.isNotEmpty(menuIds)){
                saveJobMenu(menuIds.split(","),uuid,time,usu);
            }
            jhm.putCode(1).putMessage("操作成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());

        }
        renderJson(jhm);
    }
    private void saveJobMenu(String[] array,String jobId,String time,UserSessionUtil usu){
        if(array!=null && array.length>0){
            for(Object menuId:array){
                Record authorMenuJob=new Record();
                authorMenuJob.set("id", UUIDTool.getUUID());
                authorMenuJob.set("menu_id",menuId);
                authorMenuJob.set("job_id",jobId);
                authorMenuJob.set("access",1);
                authorMenuJob.set("creator",usu.getUserId());
                authorMenuJob.set("creator_name",usu.getRealName());
                authorMenuJob.set("create_time",time);
                Db.save("h_author_job_menu",authorMenuJob);
            }
        }
    }
    public void showResource(){

        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JsonHashMap jhm = new JsonHashMap();
        try {
            List<Record> list = Db.find("select id,name as label,ifnull(url,'') as link,parent_id,sort,icon as iconName,type from h_menu order by sort");
//            List<Record> list = Db.find("select id,name as label,ifnull(url,'') as link,parent_id,sort,icon as iconName,type from h_menu order by sort");
            List reList = JobService.getMe().sort(list);
            jhm.putCode(1);
            jhm.put("data", reList);
            renderJson(jhm);
        } catch (Exception e) {
            e.printStackTrace();
            Map ret = new HashMap();
            ret.put("code", KEY.CODE.ERROR);
            ret.put("msg", e.toString());
            renderJson(ret);
        }
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
        String sql="select j.id,j.name,ifnull(j.desc,'') as 'desc',ifnull( (select group_concat(name) from h_staff where h_staff.job=j.id and h_staff.status='5'),'')  as staffs,(select count(id) from h_staff where h_staff.job=j.id and h_staff.status='5') as staffs_count from h_job j order by create_time ";
        try {
            List<Record> list = Db.find(sql);
            jhm.putCode(1);
            jhm.put("data",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
    @Before(Tx.class)
    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record json = this.getParaRecord();
            if(json==null){
                jhm.putCode(-1);
                jhm.putMessage("请传入数据！");
                renderJson(jhm);
                return;
            }
            String jobId=json.getStr("id");
            String name = json.getStr("name");
            String desc = json.getStr("desc");
            String menuIds = json.getStr("menuIds");
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

            UserSessionUtil usu = new UserSessionUtil(getRequest());

            String time = DateTool.GetDateTime();

            Record jobR = new Record();
            jobR.set("id", jobId);
            jobR.set("name", name);
            jobR.set("desc", desc);
            jobR.set("modify_time", time);
            jobR.set("modifier", usu.getUserId());
            jobR.set("modifier_name", usu.getRealName());
            boolean b= Db.update("h_job", jobR);

            //先将原有的job_menu数据删除
            int sqlNum= Db.update("delete from h_author_job_menu where job_id=?",jobId);
            if(StringUtils.isNotEmpty(menuIds)) {
                saveJobMenu(menuIds.split(","), jobId, time, usu);
            }
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
        try{
            Record r= Db.findFirst("select id,name,ifnull(`desc`,'') as `desc` from h_job where id=?",id);
            jhm.put("job",r);

            List list= Db.query("select menu_id from h_author_job_menu a, h_menu m where a.job_id=? and m.icon is null and a.menu_id=m.id ",id);
            jhm.put("menuList",list);

//            List<Record> treeList = Db.find("select id,name as label,CONCAT('/',url) as link,parent_id,sort,icon as iconName,type from h_menu order by sort");
//            List treeList2 = JobService.getMe().sort(treeList);
//            jhm.put("tree",treeList2);

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
        Record r= Db.findFirst("select count(id) as count,group_concat(name) as names from h_staff where job=? and h_staff.status='5'",jobId);
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
        boolean b= Db.deleteById("h_job",jobId);
        int sqlNum= Db.update("delete from h_author_job_menu where job_id=?",jobId);
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
            List<Record> list = Db.find("select id,name from h_job order by create_time");
            Record r=new Record();
            r.set("id","0");
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
