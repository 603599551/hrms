package com.jsoft.crm.utils;

import com.jfinal.Config;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UserSessionUtil {
    String userId ="";
    String realName ="";
    String username;
    boolean login=false;

    UserBean userBean;
    public UserSessionUtil(HttpServletRequest request){
        HttpSession session=request.getSession();
        userBean=(UserBean)session.getAttribute(KEY.SESSION_USER);
        if (Config.devMode) {//
            userBean=new UserBean();
            Record r = Db.findFirst("select * from staff where id=?", 10);
            userBean.setId(r.get("id"));
            userBean.setName(r.getStr("username"));
            userBean.setRealName(r.getStr("name" ));
            userBean.setDeptId(r.getStr("dept"));
            userBean.setDeptName(r.getStr("dept_name"));
            Object job=r.get("job");
            if(job==null)
                job="";
            else
                job=job+"";
            userBean.setJobId((String)job);
            userBean.setJobName(r.getStr("job_name"));

            userId = userBean.getId();
            realName = userBean.getRealName();
            username=userBean.getName();
            login=true;
        }else{
            if(userBean!=null) {
                userId = userBean.getId();
                realName = userBean.getRealName();
                username=userBean.getName();
                login=true;
            }else{
                login=false;
            }
        }
    }
    public boolean isLogin(){
        return login;
    }

    public String getUserId() {
        return userId;
    }

    public String getRealName() {
        return realName;
    }

    public String getUsername() {
        return username;
    }
    public UserBean getUserBean() {
        return userBean;
    }
}
