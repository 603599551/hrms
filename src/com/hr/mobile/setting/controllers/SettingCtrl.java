package com.hr.mobile.setting.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.List;

public class SettingCtrl  extends BaseCtrl{

    /**
     * @author zhanjinqi
     * @date 2018-08-01
    名称	    修改手机号
    描述	    输入新的手机号进行修改
    验证    手机号不能重复，根据id修改信息
    权限  	Hr可见
    URL	    http://localhost:8081/mgr/mobile/setting/modifyPhone
    请求方式    	post
    请求参数类型	    key=value

    请求参数：
    参数名	    类型	    最大长度	允许空	描述
    newphone    string		    否	    新手机号

    返回数据：
    返回格式	JSON
    成功	{
    "code": 1,
    "message": "修改成功！"
    }
    失败	{
    "code": 0,
    "message": "用户不存在！"
    }
    报错	{
    "code": -1,
    "message": "服务器发生异常！"
    }

     */
    public void modifyPhone(){
        JsonHashMap jhm=new JsonHashMap();
        String staffId=getPara("staffid");
        String newphone=getPara("newphone");


        //非空验证
        if(StringUtils.isEmpty(staffId)){
            jhm.putCode(0).putMessage("人员id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(newphone)){
            jhm.putCode(0).putMessage("新手机号不能为空！");
            renderJson(jhm);
            return;
        }
        String sql="select * from h_staff where phone=? ";
        List<Record> staffList=Db.find(sql,newphone);
        if(staffList!=null&&staffList.size()>0){
            Record r=staffList.get(0);
            String idDb=r.getStr("id");
            if(idDb.equals(staffId)){
                jhm.putCode(0).putMessage("请输入新的手机号！");
            }else{
                jhm.putCode(0).putMessage("该手机号已被绑定！");
            }
        }else{
            try{
                int i=Db.update("update h_staff set phone=? where id=?",newphone,staffId);
                if(i>0){
                    jhm.putCode(1).putMessage("修改成功！");
                }else{
                    jhm.putCode(0).putMessage("修改失败！");
                }

            }catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        }
        renderJson(jhm);
    }

    /**
     * @author zhanjinqi
     * @date 2018-08-01
    名称	    修改密码
    描述	    输入新密码，确认密码正确后发送请求
    验证    手机号不能重复，根据id修改信息
    权限  	Hr可见
    URL	    http://localhost:8081/mgr/mobile/setting/modifyPwd
    请求方式    	post
    请求参数类型	    key=value

    请求参数：
    参数名	    类型	    最大长度	允许空	描述
    newpassword string		    否	    新密码

    返回数据：
    返回格式	JSON
    成功	{
    "code": 1,
    "message": "修改成功！"
    }
    失败	{
    "code": 0,
    "message": "用户不存在！"
    }
    报错	{
    "code": -1,
    "message": "服务器发生异常！"
    }

     */
    public void modifyPwd(){
        JsonHashMap jhm=new JsonHashMap();
        String staffId=getPara("staffid");
        String newPassword=getPara("newpassword");

        //非空验证
        if(StringUtils.isEmpty(staffId)){
            jhm.putCode(0).putMessage("人员id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(newPassword)){
            jhm.putCode(0).putMessage("新密码不能为空！");
            renderJson(jhm);
            return;
        }
        String sql="select * from h_staff where password=? and id=?";
        List<Record> staffList=Db.find(sql,newPassword,staffId);
        if(staffList!=null&&staffList.size()>0){
            jhm.putCode(0).putMessage("请输入新的密码！");
        }else{
            try{
                int i=Db.update("update h_staff set password=? where id=?",newPassword,staffId);
                if(i>0){
                    jhm.putCode(1).putMessage("修改成功！");
                }else{
                    jhm.putCode(0).putMessage("修改失败！");
                }

            }catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        }
        renderJson(jhm);
    }
}
