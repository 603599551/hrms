package com.hr.mobile.notice.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.org.apache.regexp.internal.RE;
import easy.util.NumberUtils;
import easy.util.StringUtils;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * NoticeCtrl class
 * @author zhanjinqi
 * @date 2018-08-06
 */
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
            String leaveSQL3="select h_staff_leave_info.status from h_staff_leave_info,h_notice where h_notice.type='leave' and h_notice.receiver_id=? and h_notice.receiver_id=h_staff_leave_info.staff_id and h_notice.fid=h_staff_leave_info.id order by h_notice.create_time desc limit 0,1";
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
            if(statusRecord == null){
                leaveR.set("states",null);
            }else{
                String states=statusRecord.getStr("status");
                leaveR.set("states",states);
            }

            //resignList
            String resignSQL1="select count(*) as c from h_notice where type='resign'and status=0 and receiver_id=? limit 0,30";
            String resignSQL2="select * from h_notice where type='resign'and receiver_id=? order by create_time desc limit 0,1";
            String resignSQL3="select h_resign.status from h_resign,h_notice where h_notice.type='resign'and h_notice.receiver_id=?  and h_notice.receiver_id=h_resign.applicant_id and h_notice.fid=h_resign.id order by h_notice.create_time desc limit 0,1\n";
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

            //examList
            String examSQL1="select count(*) as c from h_notice where type='check' and status='0' and receiver_id=? limit 0,30";
            Record examR=new Record();
            examR.set("type","examList");
            //数据类型有可能是int long 等等
            Object ccObj=Db.findFirst(examSQL1,staffId).get("c");
            //将object转化为int
            int counte=NumberUtils.parseInt(ccObj,0);
            examR.set("number",counte);

            String examSQL2="select * from h_notice where type='check'and receiver_id=? order by create_time desc limit 0,1";
            //数据有可能为空
            Record date2e=Db.findFirst(examSQL2,staffId);
            if(date2e==null){
                examR.set("date",null);
            }else{
                String date3=date2e.getStr("create_time");
                examR.set("date",date3);
            }

            String examSQL3="select h_exam.result from h_exam,h_notice where h_notice.type='check'and h_notice.receiver_id=?  and h_notice.receiver_id=h_exam.staff_id and h_notice.fid=h_exam.id order by h_notice.create_time desc limit 0,1";
            Record status3=Db.findFirst(examSQL3,staffId);
            if(status3==null){
                examR.set("states",null);
            }else{
                String states3=status3.getStr("result");
                examR.set("states",states3);
            }

            List<Record> list=new ArrayList<>();
            list.add(leaveR);
            list.add(resignR);
            list.add(examR);

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
            if ("leaveList".equals(type)){
                //根据接收到的staffId和type查询最近30事件的date、states、content
                String sql1="select h_notice.create_time as date,h_staff_leave_info.status as states,h_staff_leave_info.result as content from h_staff_leave_info,h_notice where h_notice.type='leave' and h_notice.receiver_id=?  and h_staff_leave_info.status!='0' and h_notice.receiver_id=h_staff_leave_info.staff_id and h_notice.fid=h_staff_leave_info.id order by h_notice.create_time ASC limit 0,30";
                list=Db.find(sql1,staffId);
                if(list!=null&&list.size()>0){
                    for (Record r:list){
                        r.set("reason",null);
                    }
                }
                //未读转已读
                Db.update("update h_notice set status=? where type='leave' and receiver_id=?",1,staffId);
            }//离职提醒
            else if("resignList".equals(type)){
                //根据接收到的staffId和type查询最近事件的date、states、reason(clothes)、content
                String sql2="select h_notice.create_time as date,h_resign.reply as content,h_resign.status as states,h_resign.id from h_resign,h_notice where h_notice.receiver_id=? and h_notice.type='resign' and h_notice.receiver_id=h_resign.applicant_id and h_notice.fid=h_resign.id order by h_notice.create_time ASC limit 0,30";
                String sql3="select name as itemName,status as itemStatus from h_resign_return where resign_id=? ";
                String resignId="";
                String itemName="";
                String itemStatus="";
                List<Record> list2;
                HashMap<String,String> dictionary=new HashMap<>();
                list=Db.find(sql2,staffId);
                if(list!=null&&list.size()>0){
                    for (Record r:list){
                        resignId=r.getStr("id");
                        list2=Db.find(sql3,resignId);
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
            }//考核提醒
            else if("examList".equals(type)){
                String sql4="SELECT h_notice.create_time AS date,h_notice.content AS content,h_exam.result AS states FROM h_notice,h_exam\n" +
                        "WHERE h_notice.receiver_id=? AND h_notice.type='check' AND h_notice.fid=h_exam.id order by h_notice.create_time ASC limit 0,30";
                list=Db.find(sql4,staffId);
                //未读转已读
                Db.update("update h_notice set status=? where type='check' and receiver_id=?",1,staffId);
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
        if (staffId==null){
            jhm.putMessage("经理id为空");
            renderJson(jhm);
            return;
        }

        try{
            //查询请假消息未处理数量
            String sql="select count(*) as c from h_staff_leave_info where store_mgr_id=? and status='0'";
            //数据类型有可能是int long ....
            Object cObj1=Db.findFirst(sql,staffId).get("c");
            //将Object转为int
            int count1=NumberUtils.parseInt(cObj1,0);

            //查询离职消息未处理数量
            String sql2="select count(*) as c from h_resign where reviewer_id=? and status='0'";
            //数据类型有可能是int long ....
            Object cObj2=Db.findFirst(sql2,staffId).get("c");
            //将Object转为int
            int count2=NumberUtils.parseInt(cObj2,0);

            //查询考核消息未处理数量
            String sql3="SELECT count(*) AS c FROM h_exam WHERE examiner_id=? AND result='0'";
            //数据类型有可能是int long ....
            Object cObj3=Db.findFirst(sql3,staffId).get("c");
            //将Object转为int
            int count3=NumberUtils.parseInt(cObj3,0);

            //根据经理id查询门店id
            String sql4="SELECT dept_id FROM h_staff WHERE id=?";
            Record r4=Db.findFirst(sql4,staffId);
            if (r4==null){
                jhm.putMessage("该经理没有门店id");
                renderJson(jhm);
                return;
            }
            String storeId=r4.getStr("dept_id");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 可以方便地修改日期格式
            String hehe = dateFormat.format(new Date());
            //查询签到消息未处理数量
            String sql5="SELECT count(*) AS c FROM h_staff_clock WHERE store_id=? AND is_deal='0' AND status='1' AND date=?";
            //数据类型有可能是int long ....
            Object cObj5=Db.findFirst(sql5,storeId,hehe).get("c");
            //将Object转为int
            int count5=NumberUtils.parseInt(cObj5,0);

            jhm.putCode(1);
            jhm.put("check",count3);
            jhm.put("leave",count1);
            jhm.put("quit",count2);
            jhm.put("sign",count5);

        }catch (Exception e){
            jhm.putCode(-1);
            jhm.putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
