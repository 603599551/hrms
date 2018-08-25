package com.hr.controllers;

import com.bean.UserBean;
import com.common.controllers.BaseCtrl;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;

public class LoginCtrl extends BaseCtrl {

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        try{
            String username = getPara("username");
            String password = getPara("password");

            if(StringUtils.isEmpty(username)){
                jhm.putCode(-1).putMessage("用户名不能为空！");
                renderJson(jhm);
                return;
            }
            if(StringUtils.isEmpty(password)){
                jhm.putCode(-1).putMessage("密码不能为空！");
                renderJson(jhm);
                return;
            }
            Record r = Db.findFirst("select *, (select store_color from h_store s where s.id=h_staff.dept_id) store_color,(select city from h_store s where s.id=h_staff.dept_id) city from h_staff where username=? and password=?", username, password);
            if (r != null) {
                String status=r.get("status");
                if("6".equals(status)){
                    jhm.putCode(-1).putMessage("离职员工不能登录！");
                    renderJson(jhm);
                    return;
                }
                if (!(StringUtils.equals(username,"admin")||StringUtils.equals(r.get("job"),"store_manager"))){
                    jhm.putCode(-1).putMessage("员工不能登录！");
                    renderJson(jhm);
                    return;
                }

                UserBean ub=new UserBean();
                ub.setId(r.get("id"));
                ub.setName(r.getStr("username"));
                ub.setRealName(r.getStr("name" ));
                ub.setDeptId(r.getStr("dept_id"));
                ub.setDeptName(r.getStr("dept_name"));
                ub.put("store_id", r.getStr("dept_id"));
                ub.put("store_color", r.getStr("store_color"));
                ub.put("city", r.getStr("city"));
                Object job=r.get("job");
                if(job==null)
                    job="";
                else
                    job=job+"";
                ub.setJobId((String)job);
                ub.setJobName(r.getStr("job_name"));
                setSessionAttr(KEY.SESSION_USER,ub);
                setCookie("userId", r.get("id"), 60 * 60 * 24 * 3);

                Record user = new Record();
                user.set("name", ub.getRealName());
                user.set("id", ub.getId());
                user.set("roles", new ArrayList<>());
                if (StringUtils.equals(username,"admin")){
                    user.set("dept_id","");
                }else {
                    user.set("dept_id",ub.getDeptId());
                }

                jhm.put("data", user);
                jhm.put("sessionId",getSession().getId());
                jhm.putMessage("登录成功！");
                this.setCookie("Admin-Token", String2Unicode(ub.getRealName()), 60 * 60 * 24 * 3);
            } else {
                jhm.putCode(-1);
                jhm.putMessage("用户名或密码错误！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    private String String2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }
}
