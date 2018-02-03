package com.ss.organization.controllers;

import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.*;

public class StaffCtrl extends Controller{
    public void add(){
        Map paraMap= RequestTool.getParameterMap(getRequest());
        String username=(String)paraMap.get("username");
        //判断用户登录名是否重复
        JsonHashMap jhm=new JsonHashMap();
        try{
            Record r=Db.findFirst("select count(id) as count from staff where username=?",username);
            Object countObj=r.get("count");
            int count=NumberUtils.parseInt(countObj,0);
            if(count>0){
                jhm.putCode(1);
                jhm.putMessage("登录名重复，请更换其他登录名！");
                renderJson(jhm);
                return;
            }

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        String status=getPara("status");
        String uuid= UUIDTool.getUUID();
        String time= DateTool.GetDateTime();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        Record r=new Record();
        r.set("id",uuid);

        Set<Map.Entry<String,String>> set=paraMap.entrySet();
        for(Map.Entry<String,String> en:set){
//            Object result=processValue(paraMap,en.getKey(),en.getValue());
            r.set(en.getKey(),en.getValue());
        }
        String password=getPara("password");
        if(password==null || "".equals(password)){
            r.set("password","123456");
        }
        if(status==null || "".equals(status)){
            status="5";
            r.set("status",status);
        }
        jhm=processStaffRecord(paraMap,r);
        int code=jhm.getCode();
        if(code!=1){
            renderJson(jhm);
            return;
        }

        r.set("creator",creator);
        r.set("creator_name",creator_name);
        r.set("create_time",time);
        r.set("modify_time",time);
        r.set("creator",creator);
        r.set("creator_name",creator_name);
        try{
            Db.save("staff",r);
            Map reMap=r.getColumns();
            reMap.put("id",uuid);
            renderJson(reMap);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap errorJhm=new JsonHashMap();
            errorJhm.putCode(KEY.CODE.ERROR);
            errorJhm.putMessage(e.toString());
            renderJson(errorJhm);
        }
    }

    /**
     * 处理字段
     * @param paraMap
     * @param r
     * @return
     */
    private JsonHashMap processStaffRecord(Map paraMap,Record r){
        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1);
        //处理部门
        String dept=(String)paraMap.get("dept");
        if(StringUtils.isNotEmpty(dept)) {
            Record deptRecord = Db.findFirst("select * from dept where id=?", dept);
            if (deptRecord != null) {
                String deptName = deptRecord.get("name");
                r.set("dept_name", deptName);
            } else {
                jhm.putCode(KEY.CODE.ERROR);
                jhm.putMessage("没有此部门！");
                return jhm;
            }
        }
        //处理职务
        String jobId=(String)paraMap.get("job");
        if(StringUtils.isNotEmpty(jobId)) {
            Record jobRecord=Db.findFirst("select * from job where id=?",jobId);
            if(jobRecord!=null ){
                String jobName=jobRecord.get("name");
                r.set("job_name",jobName);
            }else{
                jhm.putCode(KEY.CODE.ERROR);
                jhm.putMessage("没有此职务！");
                return jhm;
            }
        }

        //处理员工状态
        String statusId=(String)paraMap.get("status");
        if(StringUtils.isNotEmpty(statusId)) {
            Record jobRecord=Db.findFirst("select * from dictionary where id=?",statusId);
            if(jobRecord!=null ){
                String jobName=jobRecord.get("name");
                r.set("status_name",jobName);
            }else{
                jhm.putCode(KEY.CODE.ERROR);
                jhm.putMessage("没有此员工状态！");
                return jhm;
            }
        }
        return jhm;
    }
    public void updateById(){
        Map paraMap= RequestTool.getParameterMap(getRequest());
        String id=(String)paraMap.get("id");
        String time= DateTool.GetDateTime();
        Record r=new Record();
        r.set("id",id);

        Set<Map.Entry<String,String>> set=paraMap.entrySet();
        for(Map.Entry<String,String> en:set){
//            Object result=processValue(paraMap,en.getKey(),en.getValue());
            r.set(en.getKey(),en.getValue());
        }
        JsonHashMap jhm=processStaffRecord(paraMap,r);
        int code=jhm.getCode();
        if(code!=1){
            renderJson(jhm);
            return;
        }
        r.set("modify_time",time);
        try{
            boolean b=Db.update("staff",r);
            if(b){
                Map reMap=r.getColumns();
                reMap.put("id",id);
                renderJson(reMap);
                return;
            }else{
                JsonHashMap errorJhm=new JsonHashMap();
                errorJhm.putCode(-1);
                errorJhm.putMessage("更新失败！");
                renderJson(errorJhm);
                return;
            }
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap errorJhm=new JsonHashMap();
            errorJhm.putCode(KEY.CODE.ERROR);
            errorJhm.putMessage(e.toString());
            renderJson(errorJhm);
            return;
        }
//        renderJson(jhm);
    }
    public void deleteById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record r=new Record();
            r.set("id",id);
            r.set("status","6");
            r.set("status_name","离职");
            boolean b=Db.update("staff",r);
            Record result=Db.findById("staff",id);
            if(b){
//                Map map=result.getColumns();
                jhm.putCode(1);
            }else if(result==null){

                jhm.putCode(-1);
                jhm.putMessage("不存在该人员！");
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
    public void query(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb ="staff";
        int pageNum = NumberUtils.parseInt(paraMap.get("pageNum"), 1);
        int pageSize = NumberUtils.parseInt(paraMap.get("pageSize"), 15);
        String keyword=getPara("keyword");
        String deptId=getPara("dept");
        String jobId=getPara("job");
        String statusId=getPara("status");
        String fields = (String) paraMap.get("fields");
        String where = (String) paraMap.get("where");
        String order = (String) paraMap.get("order");
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }
        if(statusId==null || "".equals(statusId)){
            statusId="5";
        }

        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize == 0) {
            pageSize = 15;
        }
        if(pageSize>50){
            pageSize = 50;
        }
        if(where==null){
            where="";
        }
        if(keyword!=null && !"".equals(keyword)){
            keyword = "%"+keyword + "%";
            where=where +" and (phone like '"+keyword+"' or name like '"+keyword+"' or pinyin like '"+keyword+"' )";
        }
        if(deptId!=null && !"".equals(deptId) && !"0".equals(deptId) ){
            where=where +" and dept='"+deptId+"'";
        }
        if(jobId!=null && !"".equals(jobId)&& !"0".equals(jobId)){
            where=where +" and job='"+jobId+"'";
        }
        if(statusId!=null){
            where=where +" and status='"+statusId+"'";
        }
        try {

        /*
        处理select部分
         */
            String select = "select *  ";
            if (fields != null && !"".equals(fields)) {
                select = "select " + fields;
            }
            select = select ;

            String whereEx = " from " + db_tb+" where 1=1 ";
            if (where == null || "".equals(where)) {

            } else {
                where=where.trim();
                //如果传进的where前面是and，则去掉
                if(where.startsWith("and")){
                    where=where.substring("and".length());
                }
                whereEx = whereEx + " and "+where;
            }
            if(order==null || "".equals(order)) {
                order=" order by create_time desc ";
            }
            whereEx = whereEx + " " + order;
            Page<Record> page = Db.paginate(pageNum, pageSize, select, whereEx);
            List<Record> list=page.getList();
            //过滤掉密码
            for(Record r:list){
                Object genderObj=r.get("gender");
                int gender=NumberUtils.parseInt(genderObj,0);
                if(gender==0){
                    r.set("gender","女");
                }else if(gender==1){
                    r.set("gender","男");
                }
                r.remove("password");
            }
            renderJson(page);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }

    /**
     * 显示部门及员工树
     */
    public void tree(){
        List<Record> deptList=null;
        List<Record> staffList=null;
        try{
            deptList= Db.find("select id,parent_id,name as label from dept order by sort ");
            staffList=Db.find("select id,name as label,dept from staff where status='5' ");
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
            return;
        }
        List<Map> statffMapList=new ArrayList(staffList.size());
        for(Record r:staffList){
            r.set("_type","staff");
            statffMapList.add(r.getColumns());
        }

        List<Map> deptMapList=new ArrayList(deptList.size());
        for(Record r:deptList){
            r.set("_type","dept");
            deptMapList.add(r.getColumns());
        }

//        List<Record> deptTempList=new ArrayList(deptList);
        List<Map> deptTreeList=new ArrayList();
        buildDeptTree(deptMapList,deptTreeList);


        buildDeptStaffTree(statffMapList,deptTreeList);
        JsonHashMap reMap=new JsonHashMap();
        reMap.putCode(1);
        reMap.put("result",deptTreeList);
        renderJson(reMap);
    }
    private void buildDeptStaffTree(List<Map> staffList,List<Map> deptTreeList){
        if(staffList.isEmpty()){
            return;
        }
        if(deptTreeList.isEmpty()){
            return;
        }
        for(int i=0,size=deptTreeList.size();i<size;i++){
            Map deptMap=deptTreeList.get(i);
            String sign=(String)deptMap.get("_type");
            if("staff".equals(sign)){
                continue;
            }
            String deptId=(String)deptMap.get("id");
            List childrenList=(List)deptMap.get("children");
            if(childrenList==null){
                childrenList=new ArrayList();
                deptMap.put("children",childrenList);
            }
            for(int j=0,staffListSize=staffList.size();j<staffListSize;j++){
                Map staff=staffList.get(j);
                String deptStaff=(String)staff.get("dept");
                if(deptId.equals(deptStaff)){
                    childrenList.add(staff);
                    staffList.remove(staff);
                    staffListSize=staffList.size();
                    j--;
                }
            }
            if(!childrenList.isEmpty()){
                buildDeptStaffTree(staffList,childrenList);
            }
        }
    }
    private void buildDeptTree(List<Map> list, List<Map> treeList){
        if(list.isEmpty()){
            return;
        }
        if(treeList.isEmpty()){//如果为空，取出顶级节点
            for(int i=0;i<list.size();i++){
                Map r=list.get(i);
                String parentId=(String)r.get("parent_id");
                if("0".equalsIgnoreCase(parentId)){
                    treeList.add(r);
                    list.remove(r);
                    i--;
                }
            }
            buildDeptTree(list,treeList);
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
                    Map r=list.get(i);
                    String parentId=(String)r.get("parent_id");
                    if(id.equalsIgnoreCase(parentId)){
                        childrenList.add(r);
//                        childrenRecordList.add(r);
                        list.remove(r);
                        i--;
                    }
                }
                if(!childrenList.isEmpty()) {
                    buildDeptTree(list, childrenList);
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

    /**
     * 修改自己密码
     */
    public void modifyMyPwd(){

        JsonHashMap jhm=new JsonHashMap();
        String currentPwd=getPara("currentPwd");
        String confirmPwd=getPara("confirmPwd");
        if(currentPwd==null || "".equalsIgnoreCase(currentPwd)){
            jhm.putCode(-1);
            jhm.putMessage("请输入原密码！");
            renderJson(jhm);
            return;
        }
        if(confirmPwd==null || "".equalsIgnoreCase(confirmPwd)){
            jhm.putCode(-1);
            jhm.putMessage("请输入新密码！");
            renderJson(jhm);
            return;
        }
        try{
            UserSessionUtil usu=new UserSessionUtil(getRequest());
            Record r=Db.findFirst("select * from staff where username=? and password=?",usu.getUsername(),currentPwd);
            if(r!=null){
                int sqlNum=Db.update("update staff set password=? where id=? ",confirmPwd,usu.getUserId());
                if(sqlNum>0){
                    jhm.putCode(1);
                    jhm.putMessage("更新成功！");
                }else{
                    jhm.putCode(-1);
                    jhm.putMessage("更新成功！");
                }
            }else{
                jhm.putCode(-1);
                jhm.putMessage("密码错误！");
            }
        }catch(Exception e){
            e.printStackTrace();

            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示自己信息
     */
    public void showMe(){
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String sql="select s.*,case s.gender when '1' then '男' when '0' then '女' end as gender_text ,(select name from dept where s.dept=dept.id) as dept_name,(select name from dictionary where s.job=dictionary.id) as job_name from staff s where id=?";
        Record r=Db.findFirst(sql,usu.getUserId());
        if(r!=null){
            r.remove("password");
        }
        renderJson(r);
    }
    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record r = Db.findById("staff", id);
            jhm.putCode(1).put("data",r);
            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
    }
}
