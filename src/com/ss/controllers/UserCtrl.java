package com.ss.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;
import utils.jfinal.RecordUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前用户操作
 */
public class UserCtrl extends BaseCtrl {


    /**
     * 修改自己密码
     */
    public void modifyMyPwd(){
        JsonHashMap jhm=new JsonHashMap();
        try{
            JSONObject json= RequestTool.getJson(getRequest());
            String currentPwd=json.getString("currentPwd");
            String confirmPwd=json.getString("confirmPwd");
            if(currentPwd==null || "".equalsIgnoreCase(currentPwd)){
                jhm.putCode(-1);
                jhm.putMessage("请输入原密码！");
                renderJson(jhm);
                return;
            }
            if(confirmPwd==null || "".equalsIgnoreCase(confirmPwd)){
                jhm.putCode(-1);
                jhm.putMessage("请输入新密码！");
                renderJson(jhm);
                return;
            }
            UserSessionUtil usu=new UserSessionUtil(getRequest());
            Record r= Db.findFirst("select * from staff where username=? and password=?",usu.getUsername(),currentPwd);
            if(r!=null){
                int sqlNum=Db.update("update staff set password=? where id=? ",confirmPwd,usu.getUserId());
                if(sqlNum>0){
                    jhm.putCode(1);
                    jhm.putMessage("更新成功！");
                }else{
                    jhm.putCode(-1);
                    jhm.putMessage("更新失败！");
                }
            }else{
                jhm.putCode(-1);
                jhm.putMessage("密码错误！");
            }
        }catch(Exception e){
            e.printStackTrace();

            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void showMyDetail(){
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        if(!usu.isLogin()){
            jhm.putCode(-1);
            jhm.putMessage("请先登录！");
            renderJson(jhm);
            return ;
        }
        try{
            Record r=Db.findFirst("select s.*,case gender when 1 then '男' when 0 then '女' end as gender_text,(select name from dictionary where id=s.status) as status_text,(select name from job where id=s.job) as job_text,dept.name as dept_text from staff s  left join (select id,name from dept  union all select id,name from store ) as dept on s.dept=dept.id where s.id=?",usu.getUserId());
            r.remove("password");
            RecordUtils.obj2str(r);
            jhm.putCode(1).put("data",r);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void getUserInfo(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            UserSessionUtil usu = new UserSessionUtil(getRequest());
            if(!usu.isLogin()){
                jhm.putCode(-1);
                jhm.putMessage("请先登录！");
                renderJson(jhm);
                return ;
            }
            jhm.put("id",usu.getUserId());
            jhm.put("name",usu.getRealName());
            jhm.put("deptId",usu.getUserBean().getDeptId());
            jhm.put("type","1");
            jhm.put("job",usu.getUserBean().getJobId());
            jhm.putCode(1);
            jhm.putMessage("");
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());

        }
        renderJson(jhm);
    }

    public void loginout(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            HttpSession session = getSession();
            session.removeAttribute(KEY.SESSION_USER);
            session.removeAttribute(KEY.SESSION_ADMIN);
            session.invalidate();
            session = null;
            //清空cookie
            Cookie cookies[] = getCookieObjects();
            for(int i=0;i<cookies.length;i++){
                cookies[i].setMaxAge(0);
                setCookie(cookies[i]);
            }
            jhm.putCode(1);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
