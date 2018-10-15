package com.hr.wxapplet.manager.controller;

import com.common.controllers.BaseCtrl;
import com.hr.wxapplet.manager.service.ManageSrv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.*;

public class ManageCtrl extends BaseCtrl{

    public void queryNotice2(){
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");
        if (StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("经理id不能为空!");
            renderJson(jhm);
            return;
        }

        String sql="SELECT n.title AS job,n.content AS address,n.create_time AS `time`,n.`status` FROM h_notice n WHERE receiver_id=? AND `type`='examine'  ORDER BY n.create_time DESC LIMIT 50";

        try{
            List<Record> list=Db.find(sql,id);
            if (list!=null){
                for (Record r:list){
                    String status=r.getStr("status");
                    if (StringUtils.equals(status,"3")){
                        r.set("status_text","已通过");
                    }else if (StringUtils.equals(status,"1")){
                        r.set("status_text","已同意");
                    }else if (StringUtils.equals(status,"2")){
                        r.set("status_text","被拒绝");
                    }else if (StringUtils.equals(status,"4")){
                        r.set("status_text","未通过");
                    }
                }
            }
            jhm.putCode(1);
            jhm.put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /**
     * url:https://ip:port/context/wx/manager/queryNotice
     * 2000.A.消息回显（最新50条）
     */
    public void queryNotice(){
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");
        if (StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("经理id不能为空!");
            renderJson(jhm);
            return;
        }

        String sql="SELECT n.id, n.title AS job,s.name,n.create_time AS `time`,n.`status` FROM h_notice n,h_staff s WHERE receiver_id=? AND `type`='examine' AND n.sender_id=s.id AND n.`status` IN('0','1','2')  ORDER BY n.create_time DESC  LIMIT 50";

        try{
            List<Record> list=Db.find(sql,id);
            if (list!=null){
                for (Record r:list){
                    String status=r.getStr("status");
                    if (StringUtils.equals(status,"0")){
                        r.set("status_text","待同意");
                    }else if (StringUtils.equals(status,"1")){
                        r.set("status_text","已同意");
                    }else if (StringUtils.equals(status,"2")){
                        r.set("status_text","已拒绝");
                    }
                }
            }
            jhm.putCode(1);
            jhm.put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"list\":[{\"id\":\"123\",\"name\":\"张久鹏\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"status\":\"0\",\"status_text\":\"待同意\"},{\"name\":\"张久鹏\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"status\":\"1\",\"status_text\":\"已同意\"}]}");
    }

    /**
     * url:https://ip:port/context/wx/manager/querycheckList
     * 2001.A.考核列表查询
     */
    public void querycheckList(){
        JsonHashMap jhm=new JsonHashMap();
        //经理id
        String id=getPara("id");
        if (StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("经理id不能为空!");
            renderJson(jhm);
            return;
        }

        //staff、exam、dictionary、question_type、question 五表查询
        String sql="SELECT e.id,s.name,s.phone,d.name AS job,e.result AS `status`,s.hiredate AS entryTime,e.create_time AS applyTime,e.review_time AS checkTime,qt.name AS `type`,q.title,q.content AS des FROM h_exam e,h_staff s,h_dictionary d,h_question_type qt,h_question q WHERE e.examiner_id=? AND e.staff_id=s.id AND d.value=e.kind_id AND e.kind_id=qt.kind_id AND q.type_id=qt.id ORDER BY e.create_time DESC ,qt.name DESC";

        try{
            List<Record> initialList=Db.find(sql,id);
            List<Record> checkList=new ArrayList<>();
            if (initialList!=null){
                int iLen=initialList.size();
                List<Record> detailList=new ArrayList<>();
                List<Record> questionList=new ArrayList<>();
                Record detail=new Record();
                Record question=new Record();
                Record check=new Record();

                //按照接口要求将数据归类
                for (int i=0;i<iLen;i++){
                    Record examR=initialList.get(i);
                    String type=examR.getStr("type");
                    String title=examR.getStr("title");
                    String des=examR.getStr("des");
                    String status=examR.getStr("status");
                    String applyTime=examR.getStr("applyTime");
                    String name=examR.getStr("name");
                    Record lastExamR=initialList.get(i-1);
                    String lastName=lastExamR.getStr("name");
                    String lastApplyTime=lastExamR.getStr("applyTime");
                    String lastType=lastExamR.getStr("type");

                    //首条记录
                    if (i==0){
                        check=examR;
                        if (StringUtils.isEmpty(status)){
                            check.set("status","0");
                        }else {
                            check.set("status","1");
                        }
                        detail.set("type",type);
                        question.set("title",title);
                        question.set("des",des);
                        questionList.add(question);

                        //当本条记录和上条记录属于同个考核记录
                    }else if (StringUtils.equals(applyTime,lastApplyTime)&&StringUtils.equals(name,lastName)){
                        //当本条记录和上条记录的考题分类相同
                        if (StringUtils.equals(type,lastType)){
                            question=new Record();
                            question.set("title",title);
                            question.set("des",des);
                            questionList.add(question);
                        }else {
                            detail.set("list",questionList);
                            detailList.add(detail);
                            detail=new Record();
                            questionList=new ArrayList<>();
                            question=new Record();
                            detail.set("type",type);
                            question.set("title",title);
                            question.set("des",des);
                            questionList.add(question);
                        }
                    }else{//当本条记录和上条记录不属于同个考核记录
                        detail.set("list",questionList);
                        detailList.add(detail);
                        check.set("detail",detailList);
                        check.remove("type");
                        check.remove("title");
                        check.remove("des");
                        checkList.add(check);

                        questionList=new ArrayList<>();
                        detailList=new ArrayList<>();
                        detail=new Record();
                        question=new Record();
                        check=new Record();
                        check=examR;
                        if (StringUtils.isEmpty(status)){
                            check.set("status","0");
                        }else {
                            check.set("status","1");
                        }
                        detail.set("type",type);
                        question.set("title",title);
                        question.set("des",des);
                        questionList.add(question);
                    }
                    if (i==iLen-1){
                        detail.set("list",questionList);
                        detailList.add(detail);
                        check.set("detail",detailList);
                        checkList.add(check);
                        check.remove("type");
                        check.remove("title");
                        check.remove("des");
                    }
                }
            }

            jhm.putCode(1);
            jhm.put("list",checkList);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"list\":[{\"id\":\"123\",\"name\":\"张久鹏\",\"job\":\"传菜员\",\"phone\":13130005589,\"status\":\"0\",\"entryTime\":\"2018-01-01\",\"applyTime\":\"2018-01-01\",\"checkTime\":\"2018-01-01\",\"detail\":[{\"type\":\"接管岗位前\",\"list\":[{\"title\":\"检查仪容仪表\",\"des\":\"要求……\"},{\"title\":\"准备工作\",\"des\":\"要求……\"}]}]}]}");
    }

    /**
     * url:https://ip:port/context/wx/manager/replyCheck
     * 2003.A.回复考核（同意/拒绝）
     */
    public void replyCheck(){
        JsonHashMap jhm=new JsonHashMap();
        String userId=getPara("userId");
        String status=getPara("status");
        String time=getPara("time");
        String address=getPara("address");
        String reason=getPara("reason");
        //回复列表id
        String noticeId=getPara("listId");

        if (StringUtils.isEmpty(userId)){
            jhm.putCode(0).putMessage("经理id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(status)){
            jhm.putCode(0).putMessage("status状态不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(noticeId)){
            jhm.putCode(0).putMessage("回复列表id不能为空！");
            renderJson(jhm);
            return;
        }

        Map paraMap=new HashMap();
        paraMap.put("userId",userId);
        paraMap.put("status",status);
        paraMap.put("noticeId",noticeId);

        //同意情况
        if (StringUtils.equals(status,"0")){
            if (StringUtils.isEmpty(time)){
                jhm.putCode(0).putMessage("考核时间不能为空！");
                renderJson(jhm);
                return;
            }
            if (StringUtils.isEmpty(address)){
                jhm.putCode(0).putMessage("考核地点不能为空！");
                renderJson(jhm);
                return;
            }
            try{
                paraMap.put("time",time);
                paraMap.put("address",address);
                ManageSrv srv=enhance(ManageSrv.class);
                srv.agreeCheck(paraMap);
                jhm.putCode(1).putMessage("回复成功！");
            }catch (ActiveRecordException e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage(e.getMessage());
            }

        }else if(StringUtils.equals(status,"1")){
            //拒绝情况
            if (StringUtils.isEmpty(reason)){
                jhm.putCode(0).putMessage("拒绝原因不能为空！");
                renderJson(jhm);
                return;
            }
            try{
                paraMap.put("reason",reason);
                ManageSrv srv=enhance(ManageSrv.class);
                srv.refuseCheck(paraMap);
                jhm.putCode(1).putMessage("回复成功！");
            }catch (ActiveRecordException e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage(e.getMessage());
            }
        }
        renderJson(jhm);
    }

    /**
     * url:https://ip:port/context/wx/manager/applyCheckResult
     * 2004.A.提交考核结果
     */
    public void applyCheckResult(){
        JsonHashMap jhm=new JsonHashMap();
        /**
         * 接收前端参数
         */
        String userId=getPara("userId");
        String checkId=getPara("checkId");
        String status=getPara("status");
        //非空验证
        if (StringUtils.isEmpty(userId)){
            jhm.putCode(0).putMessage("主考官id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(checkId)){
            jhm.putCode(0).putMessage("记录id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(status)){
            jhm.putCode(0).putMessage("考核结果不能为空！");
            renderJson(jhm);
            return;
        }
        try{
            Map paraMap=new HashMap();
            paraMap.put("userId",userId);
            paraMap.put("checkId",checkId);
            paraMap.put("status",status);
            ManageSrv srv=enhance(ManageSrv.class);
            srv.applyCheckResult(paraMap);
            jhm.putCode(1).putMessage("提交成功");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
        //renderJson("{\"code\":1,\"message\":\"提交成功！\"}");

    }


}
