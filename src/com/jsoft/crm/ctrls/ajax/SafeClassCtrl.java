package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.utils.RequestTool;
import com.jsoft.crm.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.DateTool;
import utils.NumberUtils;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class SafeClassCtrl extends Controller {

    public void add(){


        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        String dateTime= DateTool.GetDateTime();
        try{
            JSONObject jsonObject=RequestTool.getJson(getRequest());
            String name=jsonObject.getString("name");
            String remark=jsonObject.getString("remark");

            if(!validate(jsonObject)){
                return;
            }

            Record r=new Record();
            r.set("id",uuid);
            r.set("name",name);
            r.set("remark",remark);
            r.set("creator",usu.getUserId());
            r.set("creator_name",usu.getUsername());
            r.set("create_time",dateTime);
            boolean b=Db.save("class",r);
            if(b){
                jhm.putCode(1).putMessage("添加成功！");
            }else{
                jhm.putCode(-1).putMessage("添加失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void showById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record r = Db.findById("class", id);
            jhm.putCode(1).put("data",r);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    private boolean validate(JSONObject jsonObject){
        JsonHashMap jhm=new JsonHashMap();
        String name=jsonObject.getString("name");
        if(StringUtils.isBlank(name)){
            jhm.putCode(-1).putMessage("班级名称不能为空！");
            renderJson(jhm);
            return false;
        }
        return true;
    }
    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();

        JSONObject jsonObject=RequestTool.getJson(getRequest());
        String name=jsonObject.getString("name");
        String remark=jsonObject.getString("remark");
        String id= jsonObject.getString("id");

        if(!validate(jsonObject)){
            return;
        }

        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String dateTime= DateTool.GetDateTime();
        try{
            Record r=new Record();
            r.set("id",id);
            r.set("name",name);
            r.set("remark",remark);
            boolean b=Db.update("class",r);
            if(b){
                jhm.putCode(1).putMessage("修改成功！");
            }else{
                jhm.putCode(-1).putMessage("修改失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void deleteById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();

        if(StringUtils.isBlank(id)){
            jhm.putCode(-1).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }

        try {
            boolean b = Db.deleteById("class", id);
            if(b){
                jhm.putCode(1).putMessage("删除成功！");
            }else{
                jhm.putCode(-1).putMessage("删除失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void finish(){
        JsonHashMap jhm=new JsonHashMap();

        String id= getPara("id");
        String finish= getPara("finish");

        if(StringUtils.isBlank(id)){
            jhm.putCode(-1).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isBlank(finish)){
            jhm.putCode(-1).putMessage("必须设置是否完成！");
            renderJson(jhm);
            return;
        }

        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String dateTime= DateTool.GetDateTime();
        try{
            Record r=new Record();
            r.set("id",id);
            r.set("finish",finish);
            boolean b=Db.update("class",r);
            if(b){
                jhm.putCode(1).putMessage("设置成功！");
            }else{
                jhm.putCode(-1).putMessage("设置失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void query(){
        String name=getPara("name");
        String finish=getPara("finish");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);


        JsonHashMap jhm=new JsonHashMap();
        StringBuilder sql=new StringBuilder(" from class c where 1=1 ");
        List paraList=new ArrayList();
        if(StringUtils.isNotBlank(name)){
            sql.append(" and name=? ");
            paraList.add(name);
        }
        if(StringUtils.isNotBlank(finish)){
            sql.append(" and finish=? ");
            paraList.add(finish);
        }
        sql.append(" order by finish ,create_time desc");
        try{
            Page<Record> page= Db.paginate(pageNum,pageSize," select c.id,c.name,c.finish,case c.finish when '0' then '上课中' when '1' then '已结课' end as finish_text,(select count(*) from student where student.jr_class=c.id) as student_count ",sql.toString(),paraList.toArray());
            jhm.putCode(1).put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void showClassNum(){
        JsonHashMap jhm=new JsonHashMap();
        StringBuilder msg=new StringBuilder();
        try{
            Record r=Db.findFirst(" select count(id) as count from class");
            Object countObj=r.get("count");
            msg.append("共有班级 "+countObj+" 个");
            int count=NumberUtils.parseInt(countObj,0);
            List<Record> list=Db.find(" select finish,count(id) as count from class group by finish order by finish");
            if(list!=null && !list.isEmpty()){
                for(Record t:list){
                    String finishT=t.get("finish");
                    Object countObjT=t.get("count");
                    if("0".equals(finishT)){
                        msg.append("，上课班级 "+countObjT+" 个");
                    }else{
                        msg.append("，结课班级 "+countObjT+" 个");
                    }
                }
//                Record t=list.get(0);

            }
            jhm.putCode(1).putMessage(msg.toString());
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
