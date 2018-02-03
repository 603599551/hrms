package com.ss.organization.controllers;

import com.bean.UserBean;
import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.RequestTool;
import easy.util.DateTool;
import utils.bean.JsonHashMap;
import easy.util.NumberUtils;
import easy.util.UUIDTool;

import java.util.*;

public class DeptCtrl extends Controller{


    /*
    dbObj表示数据库中的表名
     */
    public void save(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String parentId=(String)paraMap.get("parent_id");
        String parentName=null;
        String db_tb = "dept";
        String id= UUIDTool.getUUID();
        String time= DateTool.GetDateTime();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        try {
            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                r.set(en.getKey(),en.getValue());
            }
            if(!"0".equals(parentId)){
                Record parentDeptR=Db.findFirst("select name from "+db_tb+" where id=?",parentId);
                if(parentDeptR!=null){
                    parentName=parentDeptR.get("name");
                    r.set("parent_name",parentName);
                }else{
                    Map reMap=new HashMap();
                    reMap.put("code",KEY.CODE.ERROR);
                    reMap.put("msg","上级部门不存在，不能继续添加！");
                    renderJson(reMap);
                    return;
                }
            }
            int sort=1;
            Record maxR=Db.findFirst("select max(sort) as max from "+db_tb);
            Object max=maxR.get("max");
            if(max!=null) {
                sort= NumberUtils.parseInt(max,0)+1;
            }
            r.set("sort",sort);

            r.remove("dbObj");
            r.set("id",id);
            r.set("creator",creator);
            r.set("creator_name",creator_name);
            r.set("create_time",time);
            r.set("modify_time",time);
            Db.save(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
    public void updateById(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String db_tb = (String)paraMap.get("dbObj");
        String parentId=(String)paraMap.get("parent_id");
        String id=(String)paraMap.get("id");
        String time= DateTool.GetDateTime();
        String parentName=null;
        try {
            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                r.set(en.getKey(),en.getValue());
            }
            //如果上级部门不是顶级部门，判断是否存在
            if(!"0".equals(parentId)){
                Record parentDeptR=Db.findFirst("select name from "+db_tb+" where id=?",parentId);
                if(parentDeptR!=null){
                    parentName=parentDeptR.get("name");
                    r.set("parent_name",parentName);
                }else{
                    Map reMap=new HashMap();
                    reMap.put("code",KEY.CODE.ERROR);
                    reMap.put("msg","父节点不存在，不能继续添加！");
                    renderJson(reMap);
                    return;
                }
            }
            r.remove("dbObj");
//            r.set("create_time",time);
            r.set("modify_time",time);
            Db.update(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }


    public void query(){
        String name=getPara("name");
        if(name==null){
            name="";
        }
        name="%"+name+"%";
        List<Record> list=null;
        try{
            list= Db.find("select * from dept where name like ? order by sort ",name);

        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
//        List<Record> dataList=new ArrayList(list);
//        List<Map> treeList=new ArrayList();
//        buildTree(dataList,treeList);
//        List reList=new ArrayList();
//        toWeb(treeList,reList);

        renderJson(list);
    }
    //显示树形结构
    public void tree(){
        List<Record> list=null;
        try{
            list= Db.find("select * from dept order by sort ");

        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
        List<Record> dataList=new ArrayList(list);
        List<Map> treeList=new ArrayList();
        buildTree(dataList,treeList);
        List reList=new ArrayList();
        toWeb(treeList,reList);

        renderJson(reList);
    }
    //显示树形结构
//    public void tree2(){
//        List<Record> list=null;
//        try{
//            list= Db.find("select * from dept order by sort ");
//
//        }catch(Exception e){
//            e.printStackTrace();
//            Map map=new HashMap();
//            map.put("code",-1);
//            map.put("msg",e.toString());
//            renderJson(map);
//        }
//        List<Record> dataList=new ArrayList(list);
//        List<Map> treeList=new ArrayList();
//        buildTree(dataList,treeList);
//        List reList=new ArrayList();
//        toWeb(treeList,reList);
//
//        renderJson(reList);
//    }
        private void buildTree(List<Record> list,List<Map> treeList){
        if(list.isEmpty()){
            return;
        }
        if(treeList.isEmpty()){//如果为空，取出顶级节点
            for(int i=0;i<list.size();i++){
                Record r=list.get(i);
                String parentId=r.get("parent_id");
                if("0".equalsIgnoreCase(parentId)){
                    treeList.add(r.getColumns());
                    list.remove(r);
                    i--;
                }
            }
            buildTree(list,treeList);
        }else{//取出下面的节点
            for(int j=0;j<treeList.size();j++){
                Map treeMap=treeList.get(j);
                String id=(String)treeMap.get("id");
                List childrenList=(List)treeMap.get("children");
                if(childrenList==null){
                    childrenList=new ArrayList();
                    treeMap.put("children",childrenList);
                }
//                List childrenRecordList=new ArrayList();
                for(int i=0;i<list.size();i++){
                    Record r=list.get(i);
                    String parentId=r.get("parent_id");
                    if(id.equalsIgnoreCase(parentId)){
                        childrenList.add(r.getColumns());
//                        childrenRecordList.add(r);
                        list.remove(r);
                        i--;
                    }
                }
                if(!childrenList.isEmpty()) {
                    buildTree(list, childrenList);
                }
            }
        }
//        if(catchList ==null ){
//            catchList=new ArrayList();
//        }
//        if(catchList.containsAll(list)){//防止有失去父节点的节点，造成死循环
//            return;
//        }
//        catchList.clear();
//        catchList.addAll(list);
//        buildTree2(list,treeList);
    }
    private int level=0;
    private void toWeb(List<Map> treeList, List reList){
        for(int j=0 ,size=treeList.size();j<size;j++){
            Map map=(Map)treeList.get(j);
            String name=(String)map.get("name");
            List<Map> children=(List)map.get("children");

            if(level==0){
                map.put("name","┗ "+ name);
            }else{
                String nameTemp="";
                for(int i=0;i<level;i++){
                    nameTemp="　"+nameTemp;
                }
                if(j==size-1){
                    map.put("name",nameTemp+"┗ "+name);
                }else{
                    map.put("name",nameTemp+"┣ "+name);
                }
            }
            Map node=new HashMap();
            node.putAll(map);
            node.remove("children");
            reList.add(node);

            level++;
            if(children!=null) {
                toWeb(children,reList);
            }
            level--;
        }

    }

    public void deleteByid(){
        String id=getPara("id");
        //判断是否存在子部门
        Record r=Db.findFirst("select count(*) as count from dept where parent_id=?",id);
        if(r!=null){
            Object countObj=r.get("count");
            int count=NumberUtils.parseInt(countObj,0);
            if(count>0){
                JsonHashMap jhm=new JsonHashMap();
                jhm.putCode(-1);
                jhm.putMessage("请先删除子部门，然后再删除部门！");
                renderJson(jhm);
                return;
            }
        }
        //判断该部门下是否存在员工
        r=Db.findFirst("select count(*) as count from staff where dept=?",id);
        if(r!=null){
            Object countObj=r.get("count");
            int count=NumberUtils.parseInt(countObj,0);
            if(count>0){
                JsonHashMap jhm=new JsonHashMap();
                jhm.putCode(-1);
                jhm.putMessage("请先删除该部门下的员工，然后再删除部门！");
                renderJson(jhm);
                return;
            }
        }
        try {
            boolean b=Db.deleteById("dept",id);
            if(b){
                JsonHashMap jhm=new JsonHashMap();
                jhm.putCode(1);
                renderJson(jhm);
            }else{
                r=Db.findById("dept",id);
                if(r==null){
                    JsonHashMap jhm=new JsonHashMap();
                    jhm.putCode(KEY.CODE.ERROR);
                    jhm.putMessage("不存在此部门！");
                    renderJson(jhm);
                }else{
                    JsonHashMap jhm=new JsonHashMap();
                    jhm.putCode(KEY.CODE.ERROR);
                    jhm.putMessage("删除失败！");
                    renderJson(jhm);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(KEY.CODE.ERROR);
            jhm.putMessage(e.toString());
            renderJson(jhm);
            return;
        }

    }

    public void toWebSearch(){
        List<Record> list=null;
        try{
            list= Db.find("select * from dept order by sort ");

        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
        List<Record> dataList=new ArrayList(list);
        List<Map> treeList=new ArrayList();
        buildTree(dataList,treeList);
        List reList=new ArrayList();
        toWeb(treeList,reList);

        Map map=new HashMap();
        map.put("name","请选择部门");
        map.put("id","0");
        reList.add(0,map);
        renderJson(reList);
    }

    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record r = Db.findById("dept", id);
            jhm.putCode(1).put("data",r);
            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
    }
}
