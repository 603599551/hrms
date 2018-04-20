package com.ss.controllers;

import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class LoginCtrl extends BaseCtrl{

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        try{
            JSONObject json=RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请求数据不能为空！");
                renderJson(jhm);
                return;
            }
            String username=json.getString("username");
            String password=json.getString("password");

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
            Record r = Db.findFirst("select * from staff where username=? and password=?", username, password);
            if (r != null) {
                String status=r.get("status");
                if("6".equals(status)){
                    jhm.putCode(-1).putMessage("离职员工不能登录！");
                    renderJson(jhm);
                    return;
                }
                UserBean ub=new UserBean();
                ub.setId(r.get("id"));
                ub.setName(r.getStr("username"));
                ub.setRealName(r.getStr("name" ));
                ub.setDeptId(r.getStr("dept"));
                ub.setDeptName(r.getStr("dept_name"));
                ub.put("store_id", r.getStr("dept"));
                Object job=r.get("job");
                if(job==null)
                    job="";
                else
                    job=job+"";
                ub.setJobId((String)job);
                ub.setJobName(r.getStr("job_name"));
                setSessionAttr(KEY.SESSION_USER,ub);
                setCookie("userId", r.get("id"), 60 * 60 * 24 * 3);

                jhm.putCode(1);
                jhm.put("userId",r.get("id"));
                jhm.put("sessionId",getSession().getId());
                jhm.putMessage("登录成功！");
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


}
