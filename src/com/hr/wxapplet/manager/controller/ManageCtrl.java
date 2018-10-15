package com.hr.wxapplet.manager.controller;

import com.common.controllers.BaseCtrl;
import com.hr.wxapplet.manager.service.ManageSrv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ManageCtrl extends BaseCtrl{

    /**
     * url:https://ip:port/context/wx/manager/showTrainTypeList
     * 2000.A.消息回显（最新50条）
     */
    public void queryNotice(){
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");

        try{

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
//        JsonHashMap jhm=new JsonHashMap();
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"list\":[{\"id\":\"123\",\"name\":\"张久鹏\",\"job\":\"传菜员\",\"phone\":13130005589,\"status\":\"0\",\"entryTime\":\"2018-01-01\",\"applyTime\":\"2018-01-01\",\"checkTime\":\"2018-01-01\",\"detail\":[{\"type\":\"接管岗位前\",\"list\":[{\"title\":\"检查仪容仪表\",\"des\":\"要求……\"},{\"title\":\"准备工作\",\"des\":\"要求……\"}]}]}]}");

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
//        JsonHashMap jhm=new JsonHashMap();
//        String userId=getPara("userId");
//        String checkId=getPara("checkId");
//
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"message\":\"提交成功！\"}");
    }


}
