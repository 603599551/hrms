package com.jsoft.crm.ctrls.ajax;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.Config;
import com.jfinal.KEY;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jsoft.crm.bean.UserBean;
import com.jsoft.crm.services.StudentService;
import com.jsoft.crm.utils.UserSessionUtil;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import utils.NumberUtils;
import utils.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.*;

public class SafeStudentCtrl extends Controller{

    public void save(){
        JsonHashMap jhm=new JsonHashMap();
//        Map paraMap=null;
//        Map<String,String[]> map=getParaMap();
        JSONObject paraJsonObject=null;
        Map paraMapTemp= RequestTool.getParameterMap(getRequest());
        if(paraMapTemp==null || paraMapTemp.isEmpty()){
            jhm.putCode(-1);
            jhm.putMessage("请传入数据！");
            renderJson(jhm);
            return;
        }
        Iterator it=paraMapTemp.keySet().iterator();
        if(it.hasNext()){
            Object obj=it.next();
            paraJsonObject=JSONObject.parseObject(obj.toString());
        }
        JSONArray tagAarray=paraJsonObject.getJSONArray("tag");
        String ownerStr=paraJsonObject.getString("owner");
        String[] ownerAarray=null;
        if(ownerStr!=null && !"".equals(ownerStr)){
            ownerAarray=ownerStr.split(",");
        }

        String uuid= UUIDTool.getUUID();
        String time= DateTool.GetDateTime();
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String creator="";
        String creator_name="";
        if(userBean!=null) {
            creator = userBean.getId();
            creator_name = userBean.getName();
        }
        /*
        处理数据字典值
         */
        JsonHashMap reJhm=processDict(paraJsonObject);
        if(reJhm.getCode()!=1){
            renderJson(reJhm);
            return;
        }
        //处理报名时间，当学员状态为“已交费”，就认为是新转化吉软学员，自动填上录入时间
        String status=paraJsonObject.getString("status");
        if("10".equals(status) ) {
            paraJsonObject.put("enroll_time",time);
        }
        Set<Map.Entry<String,Object>> set=paraJsonObject.entrySet();
        Record r=new Record();
        for(Map.Entry<String,Object> en:set){
//            Object result=processValue(paraMap,en.getKey(),en.getValue());
            if("owner".equals(en.getKey()) || "tag".equals(en.getKey())){

            }else {
                r.set(en.getKey(), en.getValue());
            }
        }
        r.set("id",uuid);
        r.set("creator",creator);
        r.set("creator_name",creator_name);
        r.set("create_time",time);
        r.set("modify_time",time);
        List<Object> studentList = Db.query("select * from student where phone=? and name=?", r.get("phone"), r.get("name"));
        if(studentList != null && studentList.size() > 0){
            jhm.putCode(-1);
            jhm.putMessage("学员信息重复！");
            renderJson(jhm);
            return;
        }
        try {
            boolean b=Db.save("student",r);

            //保存标签
            String[] tagStringArray=new String[tagAarray.size()];
            tagAarray.toArray(tagStringArray);
            List<Record> tagRecordList=getTagIdArray(tagStringArray);
            if (tagRecordList != null && !tagRecordList.isEmpty()) {
                for (Record tagRecord : tagRecordList) {

                    Record stdTagRe = new Record();
                    stdTagRe.set("id", UUIDTool.getUUID());
                    stdTagRe.set("student_id", uuid);
                    stdTagRe.set("tag_id", tagRecord.get("id"));
                    Db.save("student_tag", stdTagRe);
                }
            }
            //保存所有者
            if(ownerAarray!=null ){
                for(int i=0,size=ownerAarray.length;i<size;i++){
                    String owner=ownerAarray[i];
                    Record ownerR=new Record();
                    ownerR.set("id",UUIDTool.getUUID());
                    ownerR.set("student_id",uuid);
                    ownerR.set("staff_id",owner);
                    Db.save("student_owner",ownerR);
                }
            }
            if(b){
                jhm.putCode(1);
            }else{
                jhm.putCode(-1);
                jhm.putMessage("添加失败！");
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

    /**
     * 处理数据字典值
     * @param paraMap
     */
    private JsonHashMap processDict(JSONObject paraMap){
        JsonHashMap jhm=new JsonHashMap();
        /*
        处理学生状态
        如果为空设置为9，即意向学员
         */
        String status=(String)paraMap.get("status");
        if(status==null || "".equals(status)){
            jhm.putCode(-1);
            jhm.putMessage("请选择学员状态！");
            return jhm;
        }
        Record r=Db.findFirst("select * from dictionary where id=?",status);
        if(r==null){
            jhm.putCode(-1);
            jhm.putMessage("没有此学员状态！");
            return jhm;
        }else{
            String name=r.get("name");
            paraMap.put("status_name",name);
        }

        /*
        --------------------------------------------
        处理学校
         */
        String school=(String)paraMap.get("school");
        if(school==null || "".equals(school)){
//            jhm.putCode(-1);
//            jhm.putMessage("请选择学校！");
//            return jhm;
        }else {
            Record schoolR = Db.findFirst("select * from dictionary where id=?", school);
            if (schoolR == null) {
                jhm.putCode(-1);
                jhm.putMessage("没有此大学！");
                return jhm;
            } else {
                String name = schoolR.get("name");
                paraMap.put("school_name", name);
            }
        }
        /*
        --------------------------------------------
        处理专业
         */
        String speciality=(String)paraMap.get("speciality");
        if(speciality==null || "".equals(speciality)){
            speciality="380";
            paraMap.put("speciality",speciality);
            paraMap.put("speciality_name", "其他");
//            jhm.putCode(-1);
//            jhm.putMessage("请选择专业！");
//            return jhm;
        }else {
            Record specialityR = Db.findFirst("select * from dictionary where id=?", speciality);
            if (specialityR == null) {
                jhm.putCode(-1);
                jhm.putMessage("没有此专业！");
                return jhm;
            } else {
                String name = specialityR.get("name");
                paraMap.put("speciality_name", name);
            }
        }
        /*
        --------------------------------------------------
        处理删除标识
         */
        String deleted=(String)paraMap.get("deleted");
        if(deleted==null || "".equals(deleted)){
            deleted="0";
            paraMap.put("deleted", deleted);
        }

        /*
        --------------------------------------------------
        处理来源
         */
        String source=(String)paraMap.get("source");
        if(source==null || "".equals(source)){

        }else{
            Record record = Db.findFirst("select * from dictionary where id=?", source);
            if (record == null) {
                jhm.putCode(-1);
                jhm.putMessage("没有此来源！");
                return jhm;
            } else {
                String name = record.get("name");
                paraMap.put("source_name", name);
            }
        }

        /*
        --------------------------------------------------
        处理关注
         */
//        String follow=(String)paraMap.get("follow");
//        if(follow==null || "".equals(follow)){
//            follow="0";
//            paraMap.put("follow", follow);
//        }
        return jhm;
    }
    public void updateById(){
        JsonHashMap jhm=new JsonHashMap();
//        Map paraMap=null;
//        Map<String,String[]> map=getParaMap();
        JSONObject paraJsonObject=null;
        Map paraMapTemp= RequestTool.getParameterMap(getRequest());
        if(paraMapTemp==null || paraMapTemp.isEmpty()){
            jhm.putCode(-1);
            jhm.putMessage("请传入数据！");
            renderJson(jhm);
            return;
        }
        Iterator it=paraMapTemp.keySet().iterator();
        if(it.hasNext()){
            Object obj=it.next();
            paraJsonObject=JSONObject.parseObject(obj.toString());
        }
        String uuid= paraJsonObject.getString("id");
        Record studentR=Db.findById("student",uuid);
        String statusDb=studentR.get("status");
        JSONObject formJsonObj=paraJsonObject.getJSONObject("form");
        JSONArray tagAarray=formJsonObj.getJSONArray("tag");
        String ownerStr=formJsonObj.getString("owner");
        String[] ownerAarray=null;
        if(ownerStr!=null && !"".equals(ownerStr)){
            ownerAarray=ownerStr.split(",");
        }

        String time= DateTool.GetDateTime();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        /*
        处理数据字典值
         */
        JsonHashMap reJhm=processDict(formJsonObj);
        if(reJhm.getCode()!=1){
            renderJson(reJhm);
            return;
        }
        /*
        处理报名时间，当学员状态为“已交费”，就认为是新转化吉软学员，自动填上录入时间
        否则情况录入时间
         */
        if("9".equals(statusDb) || "16".equals(statusDb)) {
            String status = formJsonObj.getString("status");
            if ("10".equals(status)) {
                formJsonObj.put("enroll_time", time);
            } else {
                formJsonObj.put("enroll_time", null);
            }
        }
        Set<Map.Entry<String,Object>> set=formJsonObj.entrySet();
        Record r=new Record();
        for(Map.Entry<String,Object> en:set){
//            Object result=processValue(paraMap,en.getKey(),en.getValue());
            if("owner".equals(en.getKey()) || "tag".equals(en.getKey())){

            }else {
                r.set(en.getKey(), en.getValue());
            }
        }
        r.set("id",uuid);
//        r.set("creator",creator);
//        r.set("creator_name",creator_name);
//        r.set("create_time",time);
        r.set("modify_time",time);
        try {
            boolean b=Db.update("student",r);

            //保存标签
            String[] tagStringArray=new String[tagAarray.size()];
            tagAarray.toArray(tagStringArray);
            List<Record> tagRecordList=getTagIdArray(tagStringArray);
            if (tagRecordList != null && !tagRecordList.isEmpty()) {
                //先删除原有的标签
                int clearTagRecordNum=Db.update("delete from student_tag where student_id=?",uuid);

                for (Record tagRecord : tagRecordList) {

                    Record stdTagRe = new Record();
                    stdTagRe.set("id", UUIDTool.getUUID());
                    stdTagRe.set("student_id", uuid);
                    stdTagRe.set("tag_id", tagRecord.get("id"));
                    Db.save("student_tag", stdTagRe);
                }
            }
            //保存所有者
            if(ownerAarray!=null ){
                StudentService.getMe().modifyOwnerByStudentId(ownerAarray,new String[]{uuid},usu);
            }
            if(b){
                jhm.putCode(1);
                jhm.putMessage("修改成功！");
            }else{
                jhm.putCode(-1);
                jhm.putMessage("修改失败！");
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

    /**
     * 归还学员池
     * 有些学员的所有者是多个人，所以其中一个所有者将该学员归还学员池，不能影响其他所有者（不能把学员状态改为无意向学员）
     *
     */
    @Before(Tx.class)
    public void backPool(){
        JsonHashMap jhm=new JsonHashMap();
        String[] studentIdArray=getParaValues("ids");
        if(studentIdArray==null || studentIdArray.length==0){
            jhm.putCode(-1);
            jhm.putMessage("请传入学生id！");
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());

//        List updateParaList=new ArrayList();
        List selectParaList=new ArrayList();
        List deleteParaList=new ArrayList();
//        StringBuilder updateSql=new StringBuilder("update student set status=? where id in (");
//        updateParaList.add("16");//无意向学员
        StringBuilder selectSql=new StringBuilder("select id,name from student where id in (");
        StringBuilder deleteSql=new StringBuilder("delete from student_owner where staff_id=? and student_id in (");
        deleteParaList.add(usu.getUserId());
        for(int i=0,size=studentIdArray.length;i<size;i++){
//            updateSql.append(" ? ");
            selectSql.append(" ? ");
            deleteSql.append(" ? ");
//            updateParaList.add(studentIdArray[i]);
            selectParaList.add(studentIdArray[i]);
            deleteParaList.add(studentIdArray[i]);
            if(i<(size-1)){
//                updateSql.append(" , ");
                selectSql.append(" , ");
                deleteSql.append(" , ");
            }
        }
//        updateSql.append(")");
        selectSql.append(")");
        deleteSql.append(")");
        try{
//            int updateSqlNum=Db.update(updateSql.toString(),updateParaList.toArray());
            int deleteSqlNum=Db.update(deleteSql.toString(),deleteParaList.toArray());
            List<Record> studentList=Db.find(selectSql.toString(),selectParaList.toArray());
            StringBuilder message=new StringBuilder();
            message.append(usu.getRealName()+" 将学员 ");
            for(int i=0,size=studentList.size();i<size;i++){
                Record r=studentList.get(i);
                String name=r.getStr("name");
                message.append(name);
                if(i<(size-1)){
                    message.append(",");
                }
            }
            message.append(" 归还学员池！");

            Record messageR=new Record();
            messageR.set("id",UUIDTool.getUUID());
            messageR.set("staff_id","1");
            messageR.set("content",message.toString());
            messageR.set("create_time",DateTool.GetDateTime());
            messageR.set("read","0");
            Db.save("message",messageR);

            jhm.putCode(1);
            jhm.put("std_num",studentList.size());

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            throw e;
        }
        renderJson(jhm);
    }
    /**
     * 删除，把状态修改为无状态学员
     */
    public void deleteById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equals(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        try {
            Db.update("update student set status=? where id=?",16,id);
            jhm.putCode(1);
            jhm.putMessage("删除成功！");
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 销售人员查询属于自己的学生
     */
    public void query(){
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
            int pageNum= NumberUtils.parseInt(pageNumStr,1);
            int pageSize=NumberUtils.parseInt(pageSizeStr,10);

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
            renderJson(page);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
    public void adminQuery(){
        Map map=getParaMap();
        String keyword=getPara("keyword");
        String school=getPara("school");
        String follow=getPara("follow");
        String tags=getPara("tags");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        String sessionId=getSession().getId();
        UserSessionUtil usu=new UserSessionUtil(getRequest());

        List paraList=new ArrayList();

        StringBuilder sql=new StringBuilder(" select * from student where 1=1 ");
        if(status==null || "".equals(status)){//默认全查
//            sql.append(" status<>'16' ");
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
//            where.append(" inner join student_owner so on s.id=so.student_id ");
//            where.append(" and so.staff_id=? ");
//            paraList.add(userIdSession);
//
//            //查询关注，必须得有session
//            if(follow!=null && !"".equals(follow)){
//                sql.append(" and so.follow=? ");
//                paraList.add(follow);
//            }
//        }
        //如果查询关注学员，必须得有所有者，拼接sql
        if("1".equals(follow)){
            where.append(" inner join student_owner so on s.id=so.student_id ");
            where.append(" and so.staff_id=? and so.follow=?");
            paraList.add(usu.getUserId());
            paraList.add(follow);
        }


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
                "(select group_concat(student_owner.staff_id)  from student_owner where student_owner.student_id=s.id ) as owner_id," +
                "(select group_concat(staff.name)  from student_owner ,staff  where student_owner.student_id=s.id and student_owner.staff_id=staff.id) as owner_name," +
                "(select group_concat(student_tag.tag_id)  from student_tag where student_tag.student_id=s.id ) as tag," +
                "(select group_concat(tag.name)  from student_tag ,tag  where student_tag.student_id=s.id and student_tag.tag_id=tag.id) as tag_name ,");
        if("1".equals(follow)){//如果查询关注学员，将所有者拼接sql中，所以有so.follow
            select.append("  so.follow as follow ");
        }else{
            select.append("  '0' as follow ");
        }
        try {
            if(Config.devMode) {
                System.out.println(select.toString());
                System.out.println(where.toString());
                System.out.println(paraList);
            }
            int pageNum= NumberUtils.parseInt(pageNumStr,1);
            int pageSize=NumberUtils.parseInt(pageSizeStr,10);

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
            renderJson(page);
        }catch(Exception e){
            e.printStackTrace();
            JsonHashMap jhm=new JsonHashMap();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
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

    /**
     * 根据标签名查询标签id
     * @param tagNameArray
     * @return
     */
    private String[] findByTagName(String[] tagNameArray){
        List reList=new ArrayList();
        String[] reArray=new String[tagNameArray.length];

        List<Record> list=Db.find("select * from tag ");
        for(int j=0,size=tagNameArray.length;j<size;j++){
            String tagName=tagNameArray[j];
            for(Record r:list){
                String name=r.get("name");
                String staffId=r.get("staff_id");

                if(name.equals(tagName)){
                    reList.add(r.get("id"));
                    break;
                }
            }

        }

        reList.toArray(reArray);
        return reArray;
    }
    /**
     * 关注
     */
    public void follow(){
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");//学生id
        String follow=getPara("follow");
        //
        UserBean userBean=(UserBean)getSessionAttr(KEY.SESSION_USER);
        String userIdSession="";
        String userNameSession="";
        if (Config.devMode ) {
            userIdSession="10";
            userNameSession="王销售";
        }else{
            if(userBean!=null) {
                userIdSession = userBean.getId();
                userNameSession = userBean.getName();
            }else{
                jhm.putCode(-1);
                jhm.putMessage("请先登录！");
                renderJson(jhm);
                return;
            }
        }

        String[] paraArray=new String[]{follow,id,userIdSession};
        try {
            /*
            首先执行更新操作，如果没有记录就执行增加操作
             */
            int i=Db.update("update student_owner set follow=? where student_id=? and staff_id=?",paraArray);
            if(i<1) {
                Record r = new Record();
                r.set("id", UUIDTool.getUUID());
                r.set("student_id", id);
                r.set("staff_id",userIdSession);
                r.set("follow", follow);
                boolean b = Db.save("student_owner", r);
                if(b){
                    jhm.putCode(1);
                    if("0".equals(follow)){
                        jhm.putMessage("取消关注成功！");
                    }else{
                        jhm.putMessage("关注成功！");
                    }
                }else{
                    jhm.putCode(-1);
                    if("0".equals(follow)){
                        jhm.putMessage("取消关注失败！");
                    }else{
                        jhm.putMessage("关注失败！");
                    }
                }
            }else{
                jhm.putCode(1);
                if("0".equals(follow)){
                    jhm.putMessage("取消关注成功！");
                }else{
                    jhm.putMessage("关注成功！");
                }
            }

            renderJson(jhm);
        }catch(Exception e){
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }

    /**
     * 修改标签
     */
    public void modifyTag(){
        Map paraMap=getParaMap();
        String id=getPara("id");
        String tags=getPara("tags");

        JsonHashMap jhm=new JsonHashMap();
        try {
            if (tags != null && !"".equals(tags)) {
                String[] tagsArray = tags.split(",");
                List<Record> list = getTagIdArray(tagsArray);
                Db.update("delete from student_tag where student_id=?", id);
                if (list != null && !list.isEmpty()) {
                    for (Record r : list) {

                        Record stdTagRe = new Record();
                        stdTagRe.set("id", UUIDTool.getUUID());
                        stdTagRe.set("student_id", id);
                        stdTagRe.set("tag_id", r.get("id"));
                        Db.save("student_tag", stdTagRe);
                    }
                }
            }
            jhm.putCode(1);
            jhm.putMessage("成功！");
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 传入标签名，如果数据库中没有就保存，保存后也返回，
     * 如果数据库中已经存在，就返回
     * @param tagNameArray
     * @return
     */
    private List<Record> getTagIdArray(String[] tagNameArray){
        List<Record> list=Db.find("select * from tag ");
        List<Record> reList=new ArrayList();

        for(int j=0,size=tagNameArray.length;j<size;j++){
            String tagName=tagNameArray[j];
            boolean has=false;
            for(Record r:list){
                String name=r.get("name");
                String staffId=r.get("staff_id");

                if(name.equals(tagName)){
                    reList.add(r);
                    has=true;
                    break;
                }
            }

            if(!has){
                String id=UUIDTool.getUUID();
                Record tagRe=new Record();
                tagRe.set("id",id);
                tagRe.set("name",tagName);
                tagRe.set("create_time",DateTool.GetDateTime());
                tagRe.set("num",0);
                Db.save("tag",tagRe);
                tagRe.set("id",id);
                reList.add(tagRe);
            }

        }

        return reList;
    }

    /**
     * 根据学员id查询学员的所有者
     * 用于回显所有者tree
     */
    public void queryOwnerByStdId(){
        String ids=getPara("ids");

    }

    /**
     * 修改学员的所有者
     */

    public void modifyOwnerByStudentId(){
        JsonHashMap jhm=new JsonHashMap();
        Map paraMapTemp=getParaMap();
        try{
            JSONObject paraJsonObject=null;
            if(paraMapTemp==null || paraMapTemp.isEmpty()){
                jhm.putCode(-1);
                jhm.putMessage("请传入数据！");
                renderJson(jhm);
                return;
            }
            UserSessionUtil usu=new UserSessionUtil(getRequest());
            Iterator it=paraMapTemp.keySet().iterator();
            if(it.hasNext()){
                Object obj=it.next();
                paraJsonObject=JSONObject.parseObject(obj.toString());
            }
            //调整学生的id，可能是多个
            JSONArray studentIdJSONArray=paraJsonObject.getJSONArray("student_id");
            //所有者，可能是多个，可能包含自己
            JSONArray ownerIdJSONArray=paraJsonObject.getJSONArray("owner_id");
            if(studentIdJSONArray==null || studentIdJSONArray.size()==0){
                jhm.putCode(-1);
                jhm.putMessage("请传入要修改的学生！");
                renderJson(jhm);
                return;
            }
            if(ownerIdJSONArray==null || ownerIdJSONArray.size()==0){
                jhm.putCode(-1);
                jhm.putMessage("请传入所有者！");
                renderJson(jhm);
                return;
            }
            String[] studentIdArray=new String[studentIdJSONArray.size()];
            studentIdJSONArray.toArray(studentIdArray);
            String[] ownerIdArray=new String[ownerIdJSONArray.size()];
            ownerIdJSONArray.toArray(ownerIdArray);
            StudentService.getMe().modifyOwnerByStudentId(ownerIdArray,studentIdArray,usu);
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
            throw e;
        }
    }
    /**
    根据学生id查询
     */
    public void showById(){
        String id=getPara("id");

        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equals(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空！");
            renderJson(jhm);
            return ;
        }
        try{
            Record r=Db.findById("student",id);
            Map reMap=r.getColumns();
            Iterator<Map.Entry<String, Object>> it=reMap.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String,Object> en=it.next();
                if(en.getValue()==null){
                    en.setValue("");
                }
                if(en.getValue() instanceof Number){
                    en.setValue(en.getValue()+"");
                }
            }
            //获取所有者
            List<Record> ownerRecordList=Db.find("select s.id as id,d.id as pid from student_owner so,staff s,dept d where so.staff_id=s.id and s.dept=d.id and student_id=?",id);
//            for(Record ownerR:ownerRecordList){
//
//            }
            reMap.put("owners",ownerRecordList);
            //查询标签
            List<Record> tagRecordList=Db.find("select t.name from student_tag st,tag t where st.tag_id=t.id and student_id=?",id);
            List tagList=new ArrayList(tagRecordList.size());
            for(Record  tagR:tagRecordList){
                tagList.add(tagR.get("name"));
                reMap.put("tags",tagList);
            }

            renderJson(reMap);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
