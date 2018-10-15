package com.hr.wxapplet.staff.controller;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.oreilly.servlet.DaemonHttpServlet;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class StaffCtrl extends BaseCtrl {
    /**
     * url:https://ip:port/context/wx/staff/showTrainTypeList
     * 1000.A.获取培训类别（紧急a）
     */
    public void showTrainTypeList(){
//        JsonHashMap jhm=new JsonHashMap();
//
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"list\":[{\"id\":\"abc\",\"name\":\"产品培训\",\"sum\":20},{\"id\":\"abc\",\"name\":\"产品培训\",\"sum\":20}]}");
    }

    /**
     * url:https://ip:port/context/wx/staff/queryTrainGoods
     * 1001.A.查询类别下的产品列表及详情
     */
    public void queryTrainGoods(){
//        JsonHashMap jhm=new JsonHashMap();
//        String id=getPara("id");
//
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"list\":[{\"id\":\"123\",\"name\":\"口水鸡\",\"videoSum\":5,\"fileSum\":5,\"status\":\"0\",\"status_text\":\"已通过\",\"detail\":{\"video\":[{\"id\":\"abc\",\"url\":\"\"},{\"id\":\"abc\",\"url\":\"\"}],\"file\":[{\"id\":\"abc\",\"url\":\"\"},{\"id\":\"abc\",\"url\":\"\"}]}}]}");
    }

    /**
     * url:https://ip:port/context/wx/staff/applyCheck
     * 1003.A.申请考核
     */
    public void applyCheck(){
        JsonHashMap jhm=new JsonHashMap();
        String userId=getPara("userId");
        String typeId=getPara("typeId");
        if (StringUtils.isEmpty(userId)){
            jhm.putCode(0).putMessage("员工id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(typeId)){
            jhm.putCode(0).putMessage("岗位id不能为空！");
            renderJson(jhm);
            return;
        }

        String kindSql="SELECT name FROM h_dictionary WHERE value=?";
        String receiverSql="SELECT id FROM h_staff WHERE dept_id=(SELECT dept_id FROM h_staff WHERE id=?) AND job='store_manager'";
        String time=DateTool.GetDateTime();
        try{
            Record notice=new Record();
            notice.set("id", UUIDTool.getUUID());
            //title----申请考核的岗位名
            notice.set("title", Db.findFirst(kindSql,typeId).getStr("name"));
            notice.set("sender_id", userId);
            notice.set("receiver_id", Db.findFirst(receiverSql,userId).getStr("id"));
            notice.set("create_time", time);
            notice.set("modify_time", time);
            notice.set("status", "0");
            notice.set("type", "examine");

            boolean flag=Db.save("h_notice",notice);
            if (flag){
                jhm.putCode(1).putMessage("申请成功！");
            }else{
                jhm.putCode(0).putMessage("申请失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"message\":\"申请成功！\"}");
    }

    /**
     * url:https://ip:port/context/wx/staff/queryNotice
     * 1004.B.消息查询（最新50条）
     */
    public void queryNotice(){
//        JsonHashMap jhm=new JsonHashMap();
//        String userId=getPara("userId");
//
//        try{
//
//        }catch (Exception e){
//            e.printStackTrace();
//            jhm.putCode(-1).putMessage(e.toString());
//        }
//        renderJson(jhm);
        renderJson("{\"code\":1,\"list\":[{\"status\":\"0\",\"status_text\":\"已通过\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"address\":\"面对面长大店\"},{\"status\":\"1\",\"status_text\":\"已同意\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"address\":\"面对面长大店\"},{\"status\":\"2\",\"status_text\":\"未通过\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"address\":\"面对面长大店\"},{\"status\":\"3\",\"status_text\":\"被拒绝\",\"job\":\"传菜员\",\"time\":\"2018-01-01 10:20\",\"reason\":\"没空\"}]}");
    }


}
