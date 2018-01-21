package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.utils.SQLUtil;
import com.jsoft.crm.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.DateTool;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SafeClassStudentCtrl extends Controller {
    /**
     * 添加学生到班级
     */
    public void addStudentToClass(){
        String student_ids=getPara("student_ids");
        String classId=getPara("class_id");
        JsonHashMap jhm=new JsonHashMap();
        if(StringUtils.isBlank(student_ids)){
            jhm.putCode(-1).putMessage("请选择要分配的学员！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isBlank(classId)){
            jhm.putCode(-1).putMessage("请选择班级！");
            renderJson(jhm);
            return;
        }
        String className="";
        try{
            Record r=Db.findFirst("select name from class where id=?",classId);
            if(r==null){
                jhm.putCode(-1).putMessage("班级不存在，请选择存在的班级！");
                renderJson(jhm);
                return;
            }else{
                className=r.getStr("name");
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        StringBuilder sql=new StringBuilder(" update student set jr_class=? where id in (");
        List paraList=new ArrayList();
        paraList.add(classId);
        String[] studentIdArray=student_ids.split(",");
        for(int i=0,length=studentIdArray.length;i<length;i++){
            sql.append("?");
            paraList.add(studentIdArray[i]);
            if(i<=(length-2)){
                sql.append(",");
            }
        }
        sql.append(")");
        try{
            int i=Db.update(sql.toString(),paraList.toArray());
            if(i>0){
                jhm.putCode(1).putMessage("操作成功！").put("result",i);
                addLogDetail(studentIdArray,classId,className);
            }else{
                jhm.putCode(-1).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     *  将学生操作记录添加到log_detail表中
     * @param studentIdArray 学生id数组
     * @param classId 班级id
     */
    private void addLogDetail(String[] studentIdArray,String classId,String className) {
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        SQLUtil sql=new SQLUtil(" select id,name from student where ");
        sql.in("id",studentIdArray);
        String datetime= DateTool.GetDateTime();
        String logUUID=UUIDTool.getUUID();
        try{
            StringBuilder studentNames=new StringBuilder();
            List<Record> list=Db.find(sql.toString(),sql.getParameterArray());
            if(list!=null && !list.isEmpty()){
                for(Record r:list){
                    String studentId=r.get("id");
                    String studentName=r.get("name");

                    studentNames.append(studentName+",");

                    Record logDetail=new Record();
                    logDetail.set("id", UUIDTool.getUUID());
                    logDetail.set("content",usu.getRealName()+"将 "+studentName+" 分配到 "+className);
                    logDetail.set("student_id", studentId);
                    logDetail.set("student_name", studentName);
                    logDetail.set("creator", usu.getUserId());
                    logDetail.set("creator_name", usu.getRealName());
                    logDetail.set("create_time", datetime);
                    Db.save("log_detail",logDetail);
                }

                studentNames.delete(studentNames.length()-1,studentNames.length());
                Record log=new Record();
                log.set("id", UUIDTool.getUUID());
                log.set("content",usu.getRealName()+"将 "+studentNames.toString()+" 分配到 "+className);
                log.set("creator", usu.getUserId());
                log.set("creator_name", usu.getRealName());
                log.set("create_time", datetime);
                Db.save("log",log);

            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


}
