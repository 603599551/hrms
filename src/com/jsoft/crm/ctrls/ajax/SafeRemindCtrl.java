package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.Config;
import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.jsoft.crm.utils.UserSessionUtil;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import easy.util.StringUtils;
import utils.NumberUtils;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SafeRemindCtrl extends Controller {
//    public void save(){
//        JsonHashMap jhm=new JsonHashMap();
//        JSONObject paraJsonObject=null;
//        Map paraMapTemp= RequestTool.getParameterMap(getRequest());
//        if(paraMapTemp==null || paraMapTemp.isEmpty()){
//            jhm.putCode(-1);
//            jhm.putMessage("请传入数据！");
//            renderJson(jhm);
//            return;
//        }
//        String uuid = UUIDTool.getUUID();
//        String time = DateTool.GetDateTime();
//        UserBean userBean = (UserBean) getSessionAttr(KEY.SESSION_USER);
//        String creator = "";
//        String creator_name = "";
//        if (Config.devMode ) {
//            creator="10";
//            creator_name="王销售";
//        }else{
//            creator = userBean.getId();
//            creator_name = userBean.getName();
//        }
//        try {
//            Iterator it = paraMapTemp.keySet().iterator();
//            if (it.hasNext()) {
//                Object obj = it.next();
//                paraJsonObject = JSONObject.parseObject(obj.toString());
//            }
//            String stdId=paraJsonObject.getString("std_id");
//
//            /*
//             * 添加提醒
//             */
//
//            String remindContent=paraJsonObject.getString("remind_content");
//            String contactType=paraJsonObject.getString("contact_type");
//            String remindDate=paraJsonObject.getString("remind_date");
//            if(remindDate==null || "".equals(remindDate)){
//                jhm.putCode(-1);
//                jhm.putMessage("请输入提醒时间！");
//                renderJson(jhm);
//                return;
//            }
//            Record remindR=new Record();
//            remindR.set("id",uuid);
//            remindR.set("staff_id",creator);
//            remindR.set("student_id",stdId);
//            remindR.set("track_id","");
//            remindR.set("remind_date",remindDate);
//            remindR.set("content",remindContent);
//            remindR.set("contact_type",contactType);
//            remindR.set("creator",creator);
//            remindR.set("creator_name",creator_name);
//            remindR.set("create_time",time);
//            remindR.set("modify_time",time);
//            remindR.set("finish","0");
//            boolean b=Db.save("remind",remindR);
//
//            if(b){
//                jhm.putCode(1);
//                jhm.putMessage("添加成功！");
//                jhm.put("result",remindR);
//
//            }else{
//                jhm.putCode(-1);
//                jhm.putMessage("添加失败！");
//            }
//            renderJson(jhm);
//        }catch(Exception e){
//            jhm.putCode(-1);
//            jhm.putMessage(e.toString());
//            renderJson(jhm);
//        }
//
//    }

    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();
        JSONObject paraJsonObject=null;
        Map paraMapTemp= RequestTool.getParameterMap(getRequest());
        if(paraMapTemp==null || paraMapTemp.isEmpty()){
            jhm.putCode(-1);
            jhm.putMessage("请传入数据！");
            renderJson(jhm);
            return;
        }
        String time = DateTool.GetDateTime();
        UserBean userBean = (UserBean) getSessionAttr(KEY.SESSION_USER);
        String creator = "";
        String creator_name = "";
        if (Config.devMode ) {
            creator="10";
            creator_name="王销售";
        }else{
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        try {
            Iterator it = paraMapTemp.keySet().iterator();
            if (it.hasNext()) {
                Object obj = it.next();
                paraJsonObject = JSONObject.parseObject(obj.toString());
            }
            String stdId=paraJsonObject.getString("std_id");

            /*
             * 添加提醒
             */

            String uuid = paraJsonObject.getString("id");
            String remindContent=paraJsonObject.getString("remind_content");
            String contactType=paraJsonObject.getString("contact_type");
            String remindDate=paraJsonObject.getString("remind_date");
            if(remindDate==null || "".equals(remindDate)){
                jhm.putCode(-1);
                jhm.putMessage("请输入提醒时间！");
                renderJson(jhm);
                return;
            }
            Record remindR=new Record();
            remindR.set("id",uuid);
//            remindR.set("staff_id",creator);
//            remindR.set("student_id",stdId);
//            remindR.set("track_id","");
            remindR.set("remind_date",remindDate);
            remindR.set("content",remindContent);
            remindR.set("contact_type",contactType);
//            remindR.set("creator",creator);
//            remindR.set("creator_name",creator_name);
//            remindR.set("create_time",time);
            remindR.set("modify_time",time);
//            remindR.set("finish","1");
            boolean b=Db.update("remind",remindR);

            if(b){
                jhm.putCode(1);
                jhm.putMessage("修改成功！");
                jhm.put("result",remindR);

            }else{
                jhm.putCode(-1);
                jhm.putMessage("修改失败！");
            }
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
    public void deleteById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            boolean b=Db.deleteById("remind",id);
            if(b){
                jhm.putCode(1);
                jhm.putMessage("删除成功！");
            }else{
                jhm.putCode(-1);
                jhm.putMessage("删除失败！");
            }
            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
    public void finish(){
        String id=getPara("id");
        String finish=getPara("finish");
        JsonHashMap jhm=new JsonHashMap();
        try{
            int sqlNum=Db.update("update remind set finish=? where id=? ",finish,id);
            if(sqlNum>0){
                jhm.putCode(1);

            }else{
                jhm.putCode(-1);
            }
            if(Config.devMode){
                System.out.println(jhm);
            }
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }

    /**
     * 查询当前登录人，学生的提醒
     */
    public void query(){
        JsonHashMap jhm=new JsonHashMap();
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        String stdId=getPara("std_id");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);

        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String date=DateTool.GetDate();

        StringBuilder sql1=new StringBuilder(" select * from remind where finish='0' and creator=? ");
        StringBuilder sql2=new StringBuilder(" select * from remind where finish='1' and creator=? ");
        List paraList=new ArrayList();
        if(StringUtils.isNotNone(stdId)){
            sql1.append(" and student_id =? ");
            sql2.append(" and student_id =? ");
            paraList.add(usu.getUserId());
            paraList.add(stdId);
            paraList.add(usu.getUserId());
            paraList.add(stdId);
        }else{
            paraList.add(usu.getUserId());
            paraList.add(usu.getUserId());

        }
        sql1.append(" order by remind_date ");
        sql2.append(" order by remind_date desc ");

        String sql="  from ( select * from (" +sql1+" ) as a  union select * from (" +sql2+"  ) as b  ) as c ";

//        StringBuilder where1 =new StringBuilder("select * from remind where finish='0' and creator=? and student_id =? order by remind_date ");
//        StringBuilder where2=new StringBuilder("select * from remind where finish='1' and creator=? and student_id =? order by remind_date desc ");
//        List sqlList=new ArrayList();
//        sqlList.add(where1);
//        sqlList.add(where2);


//        where.append(" and remind_date >= ? ");
//        paraList.add(date);

        try {//-----------------
            if(Config.devMode){
                System.out.println(getClass().getName()+":::query:::"+sql);
                System.out.println(getClass().getName()+":::query:::"+paraList);
            }
            Page<Record> page= Db.paginate(pageNum,pageSize,"select c.*,(select name from student where student.id=c.student_id) as student_name ",sql,paraList.toArray());
            renderJson(page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }

    /**
     * 查询未完成提醒
     */
    public void queryRemind(){ 
        JsonHashMap jhm=new JsonHashMap();
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        String stdId=getPara("std_id");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);

        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String date=DateTool.GetDate();

        StringBuilder sql1=new StringBuilder(" select * from remind where finish='0' and creator=? ");
        List paraList=new ArrayList();
        if(StringUtils.isNotNone(stdId)){
            sql1.append(" and student_id =? ");
            paraList.add(usu.getUserId());
            paraList.add(stdId);
        }else{
            paraList.add(usu.getUserId());

        }
        sql1.append(" order by remind_date ");

        String sql="  from (" +sql1+" ) as a ";

//        StringBuilder where1 =new StringBuilder("select * from remind where finish='0' and creator=? and student_id =? order by remind_date ");
//        StringBuilder where2=new StringBuilder("select * from remind where finish='1' and creator=? and student_id =? order by remind_date desc ");
//        List sqlList=new ArrayList();
//        sqlList.add(where1);
//        sqlList.add(where2);


//        where.append(" and remind_date >= ? ");
//        paraList.add(date);

        try {
            if(Config.devMode){
                System.out.println(getClass().getName()+":::query:::"+sql);
                System.out.println(getClass().getName()+":::query:::"+paraList);
            }
            Page<Record> page= Db.paginate(pageNum,pageSize,"select a.*,(select name from student where student.id=a.student_id) as student_name ",sql,paraList.toArray());
            renderJson(page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
