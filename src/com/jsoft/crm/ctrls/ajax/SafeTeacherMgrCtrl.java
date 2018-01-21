package com.jsoft.crm.ctrls.ajax;

import com.jfinal.Config;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SafeTeacherMgrCtrl extends Controller {

    /**
     * 教师查看正式学员池
     */
    public void studentPool(){
        Map map=getParaMap();
        String keyword=getPara("keyword");
        String school=getPara("school");
//        String follow=getPara("follow");
        String tags=getPara("tags");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        String jr_class=getPara("jr_class");

        UserSessionUtil usu=new UserSessionUtil(getRequest());

        List paraList=new ArrayList();

        StringBuilder sql=new StringBuilder(" select * from student where 1=1 ");
        if(status==null || "".equals(status)){
            sql.append(" and status='10' ");
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
        if(school!=null && !"".equals(school)){
            sql.append(" and school=? ");
            paraList.add(school);
        }
        if(StringUtils.isNotEmpty(jr_class)){
            sql.append(" and jr_class=? ");
            paraList.add(jr_class);
        }


        StringBuilder where=new StringBuilder(" from ( ");
        where.append(sql);
        where.append(") as s ");


//        where.append(" inner join student_owner so on s.id=so.student_id ");
//        where.append(" and so.staff_id=? ");
//        paraList.add(usu.getUserId());
//
//        //查询关注，必须得有session
//        if(follow!=null && !"".equals(follow)){
//            where.append(" and so.follow=? ");
//            paraList.add(follow);
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
        where.append(" order by jr_class, s.modify_time desc");
        StringBuilder select =new StringBuilder();
        select.append("select distinct s.id,s.name," +
                "case s.gender when 1 then '男' when 0 then '女' end as gender," +
                "s.jr_class,s.status," +
                "(select d.name from dictionary d where d.id=s.STATUS) as status_name ," +
                "s.school," +
                "(select name from dictionary  where id=s.school) as school_name," +
                "s.school_year,s.class,s.room_num,s.phone,s.speciality," +
                "(select d.name from dictionary d where d.id=s.speciality) as speciality_name ," +
                "email,qq," +
                "(select group_concat(student_tag.tag_id)  from student_tag where student_tag.student_id=s.id ) as tag," +
                "(select group_concat(tag.name)  from student_tag ,tag  where student_tag.student_id=s.id and student_tag.tag_id=tag.id) as tag_name ");
//        select.append("  so.follow as follow ");
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
                if(tagName!=null && !"".equals(tagName)){
                    String[] array=tagName.split(",");
                    r.set("tag_name",array);
                }else{
                    r.set("tag_name",new String[]{});
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

}
