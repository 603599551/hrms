package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSONArray;
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
import utils.NumberUtils;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SafeTrackCtrl extends Controller {
    public void save(){
        JsonHashMap jhm=new JsonHashMap();
        JSONObject paraJsonObject=null;
        Map paraMapTemp= RequestTool.getParameterMap(getRequest());
        if(paraMapTemp==null || paraMapTemp.isEmpty()){
            jhm.putCode(-1);
            jhm.putMessage("请传入数据！");
            renderJson(jhm);
            return;
        }
        String uuid = UUIDTool.getUUID();
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
            String contact_type=paraJsonObject.getString("contact_type");
            if(contact_type==null || "".equals(contact_type)){
                jhm.putCode(-1);
                jhm.putMessage("请选择联系方式！");
                renderJson(jhm);
                return;
            }
            String stdId=paraJsonObject.getString("std_id");
            Record r = new Record();
            r.set("id", uuid);
            r.set("std_id",stdId);
            r.set("remark",paraJsonObject.get("remark"));
            r.set("contact_type",contact_type);
            r.set("creator",creator);
            r.set("creator_name",creator_name);
            r.set("create_time",time);
            r.set("modify_time",time);
            boolean b=Db.save("track", r);
            Map resultMap=r.getColumns();
            resultMap.put("id",uuid);
            Record dictR=Db.findFirst("select * from dictionary where id=?",contact_type);
            String dictName="";
            if(dictR!=null){
                dictName=dictR.getStr("name");
                resultMap.put("contact_type_name",dictName);
            }

            /*
             * 添加提醒
             */

            String remindContent=paraJsonObject.getString("remind_content");
            if(remindContent!=null && !"".equals(remindContent)){
                String remindDate=paraJsonObject.getString("remind_date");
                Record remindR=new Record();
                remindR.set("id",UUIDTool.getUUID());
                remindR.set("student_id",stdId);
                remindR.set("track_id",uuid);
                remindR.set("remind_date",remindDate);
                remindR.set("content",remindContent);
                remindR.set("contact_type",contact_type);
                remindR.set("creator",creator);
                remindR.set("creator_name",creator_name);
                remindR.set("create_time",time);
                remindR.set("modify_time",time);
                remindR.set("finish","0");
                Db.save("remind",remindR);
            }

            if(b){
                jhm.putCode(1);
                jhm.putMessage("添加成功！");
                jhm.put("result",resultMap);

            }else{
                jhm.putCode(-1);
                jhm.putMessage("添加失败！");
            }
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
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
            String contact_type=paraJsonObject.getString("contact_type");
            if(contact_type==null || "".equals(contact_type)){
                jhm.putCode(-1);
                jhm.putMessage("请选择联系方式！");
                renderJson(jhm);
                return;
            }
            String id=paraJsonObject.getString("id");
            if(id==null || "".equals(id)){
                jhm.putCode(-1);
                jhm.putMessage("id不能为空！");
                renderJson(jhm);
                return;
            }
            Record r = new Record();
            r.set("id", id);
//            r.set("std_id",paraJsonObject.get("std_id"));
            r.set("remark",paraJsonObject.get("remark"));
            r.set("contact_type",contact_type);
//            r.set("creator",creator);
//            r.set("creator_name",creator_name);
//            r.set("create_time",time);
            r.set("modify_time",time);
            boolean b=Db.update("track", r);
            Map resultMap=r.getColumns();
            Record dictR=Db.findFirst("select * from dictionary where id=?",contact_type);
            String dictName="";
            if(dictR!=null){
                dictName=dictR.getStr("name");
                resultMap.put("contact_type_name",dictName);
            }

            if(b){
                jhm.putCode(1);
                jhm.putMessage("更新成功！");
                jhm.put("result",resultMap);
            }else{
                jhm.putCode(-1);
                jhm.putMessage("更新失败！");
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

    }
    public void query(){
        String keyword=getPara("keyword");
        String contact_type=getPara("contact_type");
        String stdId=getPara("std_id");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
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
        StringBuilder where =new StringBuilder(" from track t where 1=1");
        List paraList=new ArrayList();
        if(keyword!=null && !"".equals(keyword)){
            where.append(" and remark like ?");
            paraList.add("%"+keyword+"%");
        }
        if(contact_type!=null && !"".equals(contact_type)){
            where.append(" and contact_type=?");
            paraList.add(contact_type);
        }
        if(usu.getUserId()!=null && !"".equals(usu.getUserId())) {
            where.append(" and creator=? ");
            paraList.add(usu.getUserId());
        }
        if(stdId!=null && !"".equals(stdId)){
            where.append(" and std_id=? ");
            paraList.add(stdId);
        }

        where.append(" order by modify_time desc ");

        StringBuilder select=new StringBuilder("");
        select.append("select t.*  ");
        select.append(" ,(select name from dictionary where dictionary.id=t.contact_type) as contact_type_name ");
        try{
            if(Config.devMode){
                System.out.println(getClass().getName()+"::query::"+select+where);
                System.out.println(getClass().getName()+"::query::"+paraList);
            }
            Page<Record> page=Db.paginate(pageNum,pageSize,select.toString(),where.toString(),paraList.toArray());
            renderJson(page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
