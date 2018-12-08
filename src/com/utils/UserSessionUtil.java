package com.utils;

import com.bean.UserBean;
import com.jfinal.Config;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UserSessionUtil {
    String userId ="";
    String realName ="";
    String username;
    boolean login=false;

    UserBean userBean;

    /**
     * 创建获取当前登录信息的对象
     * @param request request内置对象
     */
    public UserSessionUtil(HttpServletRequest request){
        HttpSession session=request.getSession();
        userBean=(UserBean)session.getAttribute(KEY.SESSION_USER);
        if (false) {//
            userBean=new UserBean();
            //长大店长
//            Record r = Db.findFirst("select * from staff where id=?", "60a6f36a65f341c78ee07c9fc250e916");
            //红旗街店长
//            Record r = Db.findFirst("select * from staff where id=?", "713765d2815845efbbdeafc6ede3310c");
            Record r = Db.findFirst("select sta.*, sto.city city, sto.store_color store_color from h_admin sta left join h_store sto on sta.dept_id=sto.id and sta.id=?", "1");
            userBean.setId(r.get("id"));
            userBean.setName(r.getStr("username"));
            userBean.setRealName(r.getStr("name" ));
            userBean.setDeptId(r.getStr("dept_id"));
            userBean.setDeptName(r.getStr("dept_name"));
            Object job=r.get("job");
            if(job==null)
                job="";
            else
                job=job+"";
            userBean.setJobId((String)job);
            userBean.setJobName(r.getStr("job_name"));
            userBean.put("store_id", r.getStr("dept_id"));
            userBean.put("store_color", r.getStr("store_color"));
            userBean.put("city", r.getStr("city"));

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
