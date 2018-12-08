package com.hr.mobile.examine;

import com.common.controllers.BaseCtrl;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.ContentTransformationUtil;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ExamineCtrl class
 * @date 2018-08-11
 * @author zhanjinqi
 */
public class ExamineCtrl extends BaseCtrl{

    /**
     * 将职位英文名翻译成中文
     */
    public String translate(String kind){
        String unSQL3="SELECT name FROM h_dictionary WHERE value=?";
        Record r3=Db.findFirst(unSQL3,kind);
        return r3.getStr("name");
    }

    /**
     * 经理查询考核申请列表
     */
    public void showCheckList(){
        JsonHashMap jhm=new JsonHashMap();

        try {
            //经理id
            String staffId=getPara("staff_id");

            //查找未处理记录untreated
            //步骤1：从 h_exam表中获取该经理未审核的人的 ①考核id ②员工id ③岗位名（单个）
            String unSQL1="SELECT id,staff_id,kind_id as job FROM h_exam WHERE examiner_id=? AND result='0' ORDER BY create_time DESC";
            List<Record> unList=Db.find(unSQL1,staffId);
            //步骤2：遍历步骤1所得的staff_id 查h_staff表 得到①员工姓名name ②员工姓名首字母firstname ③员工电话phone
            String unSQL2="SELECT name,left(pinyin,1) as firstname,phone FROM h_staff WHERE id=?";
            if (unList!=null&&unList.size()>0){
                for (Record unR:unList){
                    Record r1=Db.findFirst(unSQL2,unR.getStr("staff_id"));
                    if (r1!=null){
                        unR.set("name",r1.getStr("name"));
                        unR.set("job",translate(unR.getStr("job")));
                        unR.set("firstname",r1.getStr("firstname"));
                        unR.set("phone",r1.getStr("phone"));
                    }else{
                        jhm.putCode(0).putMessage("未在staff表中找到该员工！");
                    }
                }
            }

            //查找已处理记录pocessed
            //步骤1：从 h_exam表中获取该经理已审核的人的 ①考核id ②员工id ③岗位名（单个）④通过状态
            String sql1="SELECT id,staff_id,kind_id as job,result as status FROM h_exam WHERE examiner_id=? AND result!='0' ORDER BY review_time DESC";
            List<Record> list=Db.find(sql1,staffId);
            //步骤2：遍历步骤1所得的staff_id 查h_staff表 得到①员工姓名name ②员工姓名首字母firstname ③员工电话phone
            if (list!=null&&list.size()>0){
                for (Record r:list){
                    Record r2=Db.findFirst(unSQL2,r.getStr("staff_id"));
                    if (r2!=null){
                        r.set("name",r2.getStr("name"));
                        r.set("job",translate(r.getStr("job")));
                        r.set("firstname",r2.getStr("firstname"));
                        r.set("phone",r2.getStr("phone"));
                    }else{
                        jhm.putCode(0).putMessage("未在staff表中找到该员工！");
                    }
                }
            }

            jhm.putCode(1);
            jhm.put("untreated",unList);
            jhm.put("pocessed",list);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 经理查询员工考核列表项
     */
    public void showCheckListItem(){
        JsonHashMap jhm=new JsonHashMap();


        try {
            //考核id
            String id=getPara("id");

            //步骤0：通过考核id查exam表的kind_id
            String sql0="SELECT kind_id FROM h_exam WHERE id=?";
            Record r0=Db.findFirst(sql0,id);
            if (r0==null){
                jhm.putCode(0).putMessage("exam表的kind_id为空！");
                renderJson(jhm);
                return;
            }
            String kindId=r0.getStr("kind_id");
            String job=translate(kindId);
            //步骤1：通过exam表的kind_id查询question_type表的kind_id ->主键id 分类名称name
            String sql1="SELECT id as typeId,name as title FROM h_question_type WHERE kind_id=?";
            List<Record> list1=Db.find(sql1,kindId);
            //步骤2：通过步骤1得到的主键id查question表的type_id  -> 题目title 题目内容content
            String sql2="SELECT id as question_id,title as question,content FROM h_question WHERE type_id=?";
            //步骤4：创建一个新list ----detail
            List<Record> finalList=new ArrayList<>();

            if (list1!=null&&list1.size()>0){
                for (Record r1:list1){
                    //"detail"中的record
                    Record nr1=new Record();
                    //分类名称title
                    nr1.set("title",r1.getStr("title"));
                    //name：题目list
                    List<Record> list2=Db.find(sql2,r1.getStr("typeId"));
                    nr1.set("name",list2);
                    finalList.add(nr1);
                }
            }
            jhm.putCode(1);
            jhm.put("job",job);
            jhm.put("detail",finalList);


        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 经理提交审核结果
     */
    public void doCheckSubmit(){
        JsonHashMap jhm=new JsonHashMap();

        try{
            UserSessionUtil usu = new UserSessionUtil(getRequest());
            String senderId = usu.getUserId();
            String createTime = DateTool.GetDateTime();
            //考核id
            String id=getPara("id");
            if (id==null){
                jhm.putCode(0).putMessage("考核id为空！");
                renderJson(jhm);
                return;
            }

            //每小题的通过结果
            String name=getPara("name");
            jhm.put("name",name);
            if (name==null){
                jhm.putCode(0).putMessage("小题结果为空！");
                renderJson(jhm);
                return;
            }
            JSONArray jsonArray = JSONArray.fromObject(name);

            //考试通过结果
            String status=getPara("status");
            if (status==null){
                jhm.putCode(0).putMessage("考试通过结果为空！");
                renderJson(jhm);
                return;
            }else {
                //更新exam表中的status
                Db.update("UPDATE h_exam SET result=?  WHERE id=?",status,id);
                //更新staff_train表的status
                String sql5="UPDATE h_staff_train SET status=? WHERE staff_id=? AND type_2=?";
                //通过考核id查询staffid和typeid
                String sql6="SELECT staff_id,type_id FROM h_exam WHERE id=?";
                Record r6=Db.findFirst(sql6,id);
                if (r6==null){
                    jhm.putCode(0).putMessage("无该考核记录！");
                    renderJson(jhm);
                    return;
                }
                String staffId=r6.getStr("staff_id");
                String typeId=r6.getStr("type_id");
                List<String> questions=new ArrayList<>();
                if ("2".equals(status)){
                    //通过考核
                    Db.update(sql5,"1",staffId,typeId);
                }else if ("1".equals(status)){
                    //未通过考核
                    Db.update(sql5,"0",staffId,typeId);
                }

                //遍历jsonArray，通过考核id（exam_id）更新exam_question表的数据
                for (int i=0;i<jsonArray.size();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    if (jsonObject.size()!=0){
                        String questionId=jsonObject.getString("question_id");
                        String result=jsonObject.getString("status");
                        Record r=new Record();
                        r.set("id",UUIDTool.getUUID());
                        r.set("question_id",questionId);
                        r.set("exam_id",id);
                        r.set("result",result);
                        Db.save("h_exam_question",r);
                    }
                }

                if ("1".equals(status)){
                    for (int i=0;i<jsonArray.size();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        if (jsonObject.size()!=0){
                            String questionId=jsonObject.getString("question_id");
                            Record qr=Db.findFirst("SELECT h_question.title FROM h_question,h_exam_question WHERE h_question.id=? AND h_exam_question.exam_id=? AND h_exam_question.question_id=h_question.id AND h_exam_question.result='0'",questionId,id);
                            if (qr==null){
                                continue;
                            }
                            String qTitle=qr.getStr("title");
                            questions.add(qTitle);
                        }
                    }
                }


                //向notice表存信息 发往员工端
                Record notice=new Record();
                notice.set("id",UUIDTool.getUUID());
                notice.set("title","岗位考核情况");
                if ("2".equals(status)){
                    notice.set("content","考核已通过！");
                }else {
                    String s=String.join(",", questions);
                    notice.set("content",s);
                }
                notice.set("sender_id",senderId);
                notice.set("receiver_id",staffId);
                notice.set("create_time",createTime);
                notice.set("status","0");
                notice.set("type","check");
                notice.set("fid",id);
                Db.save("h_notice",notice);

                Db.update("UPDATE h_exam SET review_time=? WHERE id=?",createTime,id);

                jhm.putCode(1).putMessage("提交成功！");
            }


        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

    /**
     * 经理查看未通过审核的员工列表项
     */
    public void showUnpassedList(){
        JsonHashMap jhm=new JsonHashMap();

        try{
            //考核id
            String id=getPara("id");

            if (id==null){
                jhm.putCode(0).putMessage("考核id为空！");
            }

            //步骤0：通过考核id查exam表的kind_id
            String sql0="SELECT kind_id FROM h_exam WHERE id=?";
            Record r0=Db.findFirst(sql0,id);
            if (r0==null){
                jhm.putCode(0).putMessage("exam表的kind_id为空！");
                renderJson(jhm);
                return;
            }
            String kindId=r0.getStr("kind_id");
            String job=translate(kindId);
            //步骤1：通过exam表的kind_id查询question_type表的kind_id ->主键id 分类名称name
            String sql1="SELECT id as typeId,name as title FROM h_question_type WHERE kind_id=?";
            List<Record> list1=Db.find(sql1,kindId);
            //步骤2：通过步骤1得到的主键id查question表的type_id  -> 题目title 题目内容content
            String sql2="SELECT id as question_id,title as question,content FROM h_question WHERE type_id=?";
            //步骤3：根据考核id和步骤2得到的question_id查询exam_question表得到 result
            String sql33="SELECT result FROM h_exam_question WHERE exam_id=? AND question_id=?";
            //步骤5：创建一个新list ----detail
            List<Record> finalList=new ArrayList<>();

            if (list1!=null&&list1.size()>0){
                for (Record r1:list1){
                    //"detail"中的record
                    Record nr1=new Record();
                    //分类名称title
                    nr1.set("title",r1.getStr("title"));
                    //name：题目list
                    List<Record> list2=Db.find(sql2,r1.getStr("typeId"));
                    if (list2!=null&&list2.size()>0){
                        for (Record r2:list2){
                            Record resultRecord=Db.findFirst(sql33,id,r2.getStr("question_id"));
                            if (resultRecord!=null){
                                r2.set("result",resultRecord.getStr("result"));
                            }else {
                                jhm.putCode(0).putMessage("没有答题结果记录");
                            }
                        }
                    }

                    nr1.set("name",list2);
                    finalList.add(nr1);
                }
            }

            jhm.putCode(1);
            jhm.put("job",job);
            jhm.put("detail",finalList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

    public void checkStaff(){
        JsonHashMap jhm=new JsonHashMap();

        try{
            String staffId=getPara("staff_id");
            String kindId=getPara("kind_id");
            if (staffId==null||kindId==null){
                jhm.putCode(0).putMessage("参数有误！");
                renderJson(jhm);
                return;
            }
            //查找h_staff表得入职时间和店铺id
            String sql1="SELECT hiredate,dept_id FROM h_staff WHERE id=?";
            Record r1=Db.findFirst(sql1,staffId);
            String hiredate="";
            String examinerId="";
            String deptId="";
            if (r1!=null){
                hiredate=r1.getStr("hiredate");
                deptId=r1.getStr("dept_id");
                //通过店铺id和经理职位查询经理id
                String sql2="SELECT id FROM h_staff WHERE dept_id=? AND job='store_manager'";
                Record r2=Db.findFirst(sql2,deptId);
                examinerId=r2.getStr("id");
            }

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String createTime=sdf.format(d);

            Record r=new Record();
            r.set("id",UUIDTool.getUUID());
            r.set("staff_id",staffId);
            r.set("create_time",createTime);
            r.set("hiredate",hiredate);
            r.set("kind_id",kindId);
            r.set("examiner_id",examinerId);
            r.set("result","0");
            r.set("type_id","0");

            Db.save("h_exam",r);

            jhm.putCode(1).putMessage("提交成功！");

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
