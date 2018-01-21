package com.jsoft.crm.ctrls.ajax;

import com.jfinal.Config;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.jsoft.crm.ctrls.ajax.SafeStudentCtrl;
import com.jsoft.crm.utils.FileUtil;
import com.jsoft.crm.utils.UserSessionUtil;
import com.jsoft.crm.utils.XlsUtil;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SafeStudentXls extends Controller{
    public void getTemplate(){
        String path = this.getRequest().getSession().getServletContext().getRealPath("WEB-INF/template/studentTemplate.xls");
        try {
            FileUtil.downLoad(path,"模板", this.getResponse(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadErrorFile(){
        String dir = getPara("errorFileDir");
        String name = getPara("errorFileName");
        String path = this.getRequest().getSession().getServletContext().getRealPath("WEB-INF/upload/") + "/" + dir + "/" + name;
//        System.out.println(path);
        try {
            FileUtil.downLoad(path, this.getResponse(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importXls(){
        try {
            String dir = UUIDTool.getUUID();
            String path = this.getRequest().getSession().getServletContext().getRealPath("WEB-INF/upload/" + dir);
            String name = FileUtil.upload(path, this.getRequest());
            UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
            String student = "student";
            String student_owner = "student_owner";
            Map<String, Object> o = XlsUtil.parseXls(path + "/", name, userBean);
            List<Record> studentList = (List<Record>)o.get("studentList");
            List<Record> studentOwnerList = (List<Record>)o.get("studentOwnerList");
            for(Record r : studentList){
                Db.save(student, r);
            }
            for(Record r : studentOwnerList){
                Db.save(student_owner, r);
            }
            List<Record> studentRepetitionList = (List<Record>)o.get("studentRepetitionList");
            List<Object> cellMsgList = (List<Object>)o.get("cellMsgList");

            JsonHashMap result = new JsonHashMap();

            int errorNum = cellMsgList != null ? cellMsgList.size() : 0;
            int repetitionNum = studentRepetitionList != null ? studentRepetitionList.size() : 0;
            int successNum = studentList != null ? studentList.size() : 0;
            int totalNum = successNum + errorNum + repetitionNum;

            String msg = "";
            if(errorNum > 0 || repetitionNum > 0){
                result.putCode(0);
                msg = "共导入" + totalNum + "条记录，其中成功导入" + successNum + "条记录，" + repetitionNum + "条重复，" + errorNum + "条有错误，详情请下载错误文档进行修改！";
                result.put("errorFileDir", dir);
                result.put("errorFileName", "error.xls");
            }else{
                result.putCode(1);
                msg = "共成功导入" + totalNum + "条记录！";
            }
            result.put("msg", msg);
//            System.out.println(result.get("msg"));
//            System.out.println("http://localhost:8080/api/safe/studentXls/downloadErrorFile?errorFileDir=" + result.get("errorFileDir") + "&errorFileName=" + result.get("errorFileName"));
            renderJson(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportXls(){
        Map map=getParaMap();
        String keyword=getPara("keyword");
        String school=getPara("school");
        String follow=getPara("follow");
        String tags=getPara("tags");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        UserSessionUtil usu=new UserSessionUtil(getRequest());

        List paraList=new ArrayList();

        StringBuilder sql=new StringBuilder(" select * from student where 1=1 ");
        if(status==null || "".equals(status)){
            sql.append(" and status<>'16' ");
        }else{
            sql.append(" and status=? ");
            paraList.add(status);
        }
        if(keyword!=null && !"".equals(keyword)) {
            sql.append(" and (name like ? or phone like ? or qq like ? or pinyin like ? )");

            keyword=keyword+"%";
            paraList.add(keyword);
            paraList.add(keyword);
            paraList.add(keyword);
            paraList.add(keyword);
        }
        int sqlNum=paraList.size();
        if(school!=null && !"".equals(school)){
            sql.append(" and school=? ");
            paraList.add(school);
        }



        StringBuilder where=new StringBuilder(" from ( ");
        where.append(sql);
        where.append(") as s ");
//        if(userIdSession!=null && !"".equals(userIdSession)) {
        where.append(" inner join student_owner so on s.id=so.student_id ");
        where.append(" and so.staff_id=? ");
        paraList.add(usu.getUserId());

        //查询关注，必须得有session
        if(follow!=null && !"".equals(follow)){
            where.append(" and so.follow=? ");
            paraList.add(follow);
        }
//        }


        //处理查询标签
        if(tags!=null && !"".equals(tags)){
            where.append(" inner join student_tag on s.id=student_tag.student_id inner join tag on student_tag.tag_id=tag.id ");
            where.append(" and tag.name in ( ");
            String[] tagsArray=tags.split(",");
            for(int i=0,length=tagsArray.length;i<length;i++){
                where.append("?");
                if(i<(length-1)){
                    where.append(",");
                }
                paraList.add(tagsArray[i]);
            }
            where.append(")");
        }
        where.append(" order by s.modify_time desc");
        StringBuilder select =new StringBuilder();
        select.append("select distinct s.id,s.name," +
                "case s.gender when 1 then '男' when 0 then '女' end as gender," +
                "s.status," +
                "(select d.name from dictionary d where d.id=s.STATUS) as status_name ," +
                "s.sfzh,s.school," +
                "(select name from dictionary  where id=s.school) as school_name," +
                "s.school_year,s.class,s.room_num,s.phone,s.speciality," +
                "(select d.name from dictionary d where d.id=s.speciality) as speciality_name ," +
                "email,qq,s.source," +
                "(select d.name from dictionary d where d.id=s.source) as source_name ," +
                "remark," +
                "(select group_concat(student_owner.staff_id)  from student_owner where student_owner.student_id=s.id ) as owner_id," +
                "(select group_concat(staff.name)  from student_owner ,staff  where student_owner.student_id=s.id and student_owner.staff_id=staff.id) as owner_name," +
                "(select group_concat(student_tag.tag_id)  from student_tag where student_tag.student_id=s.id ) as tag," +
                "(select group_concat(tag.name)  from student_tag ,tag  where student_tag.student_id=s.id and student_tag.tag_id=tag.id) as tag_name ,");
//        if(userIdSession!=null && !"".equals(userIdSession)){
        select.append("  so.follow as follow ");
//        }else{
//            select.append("  '0' as follow ");
//        }
        try {
            if(Config.devMode) {
                System.out.println(select.toString());
                System.out.println(where.toString());
                System.out.println(paraList);
            }
            int pageNum= 1;
            int pageSize=100000;

            Page<Record> page = Db.paginate(pageNum, pageSize, select.toString(), where.toString(),paraList.toArray());
            List<Record> list=page.getList();
            for(Record r:list){
                String tagName=r.get("tag_name");
                String ownerId=r.get("owner_id");
                if(tagName!=null && !"".equals(tagName)){
                    String[] array=tagName.split(",");
                    r.set("tag_name",array);
                }else{
                    r.set("tag_name",new String[]{});
                }
                if(ownerId!=null && !"".equals(ownerId)){
                    String[] array=ownerId.split(",");
                    List resultList=buildOwnerIdArray(array);
                    r.set("owner_id_array",resultList);
                }else{
                    r.set("owner_id_array",new String[]{});
                }
            }
            String fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
            String headStr = "attachment; filename=\"" + fileName + "\"";
            HttpServletResponse response = getResponse();
            response.setContentType("APPLICATION/OCTET-STREAM");
            response.setHeader("Content-Disposition", headStr);
            OutputStream out = response.getOutputStream();
            XlsUtil.export(list, out);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
        ///safe/student/modifyOwnerByStudentId
    }

    /*
    传入员工id，返回该员工id、部门id
     */
    private List buildOwnerIdArray(String[] ownerIdArray){
        List reList=new ArrayList();
        StringBuilder sql=new StringBuilder("select s.id as staff_id,d.id as dept_id from staff s,dept d where s.dept=d.id and s.id in (");
        List paraList=new ArrayList();
        for(int i=0,length=ownerIdArray.length;i<length;i++){
            sql.append(" ? ");
            if(i<(length-1)){
                sql.append(",");
            }
            paraList.add(ownerIdArray[i]);
        }
        sql.append(")");
        if(Config.devMode){
            System.out.println("buildOwnerIdArray:::"+sql.toString());
        }
        List<Record> resultList=Db.find(sql.toString(),paraList.toArray());
        for(Record r:resultList){
            Map map=new HashMap();
            map.put("id",r.get("staff_id"));
            map.put("pid",r.get("dept_id"));
            reList.add(map);
        }

        return reList;
    }
}
