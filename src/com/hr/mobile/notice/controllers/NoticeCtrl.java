package com.hr.mobile.notice.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import easy.util.NumberUtils;
import easy.util.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoticeCtrl extends BaseCtrl{

    public void showType(){
        JsonHashMap jhm=new JsonHashMap();
        String staffId =getPara("staff_id");
        try{
            //leaveList
            String leaveSQL1="select count(*) as c from h_notice where type='leave'and status=0 and receiver_id=? limit 0,30";
            //数据类型有可能是int long 等等
            Object cObj=Db.findFirst(leaveSQL1,staffId).get("c");
            //将object转化为int
            int c=NumberUtils.parseInt(cObj,0);
            String leaveSQL2="select * from h_notice where type='leave' and receiver_id=? order by create_time desc limit 0,1";
            String leaveSQL3="select h_staff_leave_info.status from h_staff_leave_info,h_notice where h_notice.type='leave' and h_notice.receiver_id=? and h_notice.receiver_id=h_staff_leave_info.staff_id and h_staff_leave_info.status!='0' order by h_notice.create_time desc limit 0,1";
            Record leaveR=new Record();
            leaveR.set("type","leaveList");
            leaveR.set("number",c);

            //数据有可能为空 所以用object存
            Record object=Db.findFirst(leaveSQL2,staffId);
            if(object==null){
                leaveR.set("date",null);
            }else{
                String date=object.getStr("create_time");
                leaveR.set("date",date);
            }

            Record statusRecord=Db.findFirst(leaveSQL3,staffId);
            if(statusRecord == null ){
                leaveR.set("states",null);
            }else{
                String states=statusRecord.getStr("status");
                leaveR.set("states",states);
            }

            //resignList
            String resignSQL1="select count(*) as c from h_notice where type='resign'and status=0 and receiver_id=? limit 0,30";
            String resignSQL2="select * from h_notice where type='resign'and receiver_id=? order by create_time desc limit 0,1";
            String resignSQL3="select h_staff_leave_info.status from h_staff_leave_info,h_notice where h_notice.type='resign'and h_notice.receiver_id=? and h_notice.receiver_id=h_staff_leave_info.staff_id and h_resign.status!='0' order by h_notice.create_time desc limit 0,1";
            Record resignR=new Record();
            resignR.set("type","resignList");

            //数据类型有可能是int long 等等
            Object countObj=Db.findFirst(resignSQL1,staffId).get("c");
            //将object转化为int
            int count=NumberUtils.parseInt(countObj,0);
            resignR.set("number",count);

            //数据有可能为空
            Record date2=Db.findFirst(resignSQL2,staffId);
            if(date2==null){
                resignR.set("date",null);
            }else{
                String date3=date2.getStr("create_time");
                resignR.set("date",date3);
            }

            Record status2=Db.findFirst(resignSQL3,staffId);
            if(status2==null){
                resignR.set("states",null);
            }else{
                String states2=status2.getStr("status");
                resignR.set("states",states2);
            }

            List<Record> list=new ArrayList<>();
            list.add(leaveR);
            list.add(resignR);

            jhm.putCode(1);
            jhm.put("data",list);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器出现异常！");
        }
        renderJson(jhm);
    }

    public void showListByType(){
        JsonHashMap jhm=new JsonHashMap();
        String staffId=getPara("staff_id");
        String type=getPara("type");
        List<Record> list;
        try{
            //请假提醒
            if (type.equals("leaveList")){
                //根据接收到的staffId和type查询最近30事件的date、states、content
                String sql1="select h_notice.create_time as date,h_staff_leave_info.status as states,h_staff_leave_info.result as content from h_staff_leave_info,h_notice where h_notice.type='leave' and h_notice.receiver_id=?  and h_staff_leave_info.status!='0' and h_notice.receiver_id=h_staff_leave_info.staff_id and h_notice.fid=h_staff_leave_info.id order by h_notice.create_time desc limit 0,30";
                list=Db.find(sql1,staffId);
                if(list!=null&&list.size()>0){
                    for (Record r:list){
                        r.set("reason",null);
                    }
                }
                //未读转已读
                Db.update("update h_notice set status=? where type='leave' and receiver_id=?",1,staffId);
            }//离职提醒
            else if(type.equals("resignList")){
                //根据接收到的staffId和type查询最近事件的date、states、reason(clothes)、content
                String sql2="select h_notice.create_time as date,h_resign.reply as content,h_resign.status as states,h_resign.id from h_resign,h_notice where h_notice.receiver_id=? and h_notice.type='resign' and h_notice.receiver_id=h_resign.applicant_id and h_notice.fid=h_resign.id";
                String sql3="select name as itemName,status as itemStatus from h_resign_return where resign_id=? ";
                String resign_id="";
                String itemName="";
                String itemStatus="";
                List<Record> list2;
                HashMap<String,String> dictionary=new HashMap<>();
                list=Db.find(sql2,staffId);
                if(list!=null&&list.size()>0){
                    for (Record r:list){
                        resign_id=r.getStr("id");
                        list2=Db.find(sql3,resign_id);
                        if(list2!=null&&list2.size()>0){
                            for (Record r2:list2){
                                itemName=r2.getStr("itemName");
                                itemStatus=r2.getStr("itemStatus");
                                dictionary.put(itemName,itemStatus);
                            }
                        }
                        r.set("reason",dictionary);
                        r.remove("id");
                    }
                }
                //未读转已读
                Db.update("update h_notice set status=? where type='resign' and receiver_id=?",1,staffId);
            }else{
                list=null;
            }

            jhm.putCode(1);
            jhm.put("data",list);

        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器出现异常！");
        }
        renderJson(jhm);
    }


    /**
     名称	店长端消息页未读数量回显
     描述	店长端消息页未读数量回显
     验证
     权限	店长可见
     URL	http://localhost:8081/mgr/notice/showNRMessageNum
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staffid	string		否	店长id



     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "check": 2,
     "leave": 5,
     "quit": 4
     }
     备注
     check  考核（暂时不做，返回空即可）
     leave  请假
     quit   离职
     }
     失败	{
     "code": 0,
     "message": "此记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": "操作失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }


     */

    public void showNRMessageNum(){
        JsonHashMap jhm=new JsonHashMap();
        String staffId=getPara("staffid");
        try{
            //查询请假消息未读数量
            String sql="select count(*) as c from h_notice where receiver_id=? and type='leave' and status='0'";
            //数据类型有可能是int long ....
            Object cObj1=Db.findFirst(sql,staffId).get("c");
            //将Object转为int
            int count1=NumberUtils.parseInt(cObj1,0);

            //查询离职消息未读数量
            String sql2="select count(*) as c from h_notice where receiver_id=? and type='resign' and status='0'";
            //数据类型有可能是int long ....
            Object cObj2=Db.findFirst(sql2,staffId).get("c");
            //将Object转为int
            int count2=NumberUtils.parseInt(cObj2,0);

            jhm.putCode(1);
            jhm.put("check",null);
            jhm.put("leave",count1);
            jhm.put("quit",count2);
        }catch (Exception e){
            jhm.putCode(-1);
            jhm.putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
