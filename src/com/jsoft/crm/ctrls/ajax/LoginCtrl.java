package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.jsoft.crm.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
用于处理admin和user登录，不需要过滤
登录后将用户信息保存到session中

登录成功：返回admin表的json记录，但不包含password，为了保密
 */
public class LoginCtrl extends Controller {

    public void index(){
        String username = getPara("username");
        String password = getPara("password");
        JsonHashMap jhm=new JsonHashMap();
        try {
            Record r = Db.findFirst("select * from staff where username=? and password=?", username, password);
            if (r != null) {
                UserBean ub=new UserBean();
                ub.setId(r.get("id"));
                ub.setName(r.getStr("username"));
                ub.setRealName(r.getStr("name" ));
                ub.setDeptId(r.getStr("dept"));
                ub.setDeptName(r.getStr("dept_name"));
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
                jhm.putMessage("登录失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
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

    public void admin(){
        try {
            String username = getPara("username");
            String password = getPara("password");
            Record r = Db.findFirst("select * from admin where username=? and password=?", username, password);

            Map ret = new HashMap();
            if (r != null) {
                Map dataMap=r.getColumns();
                dataMap.remove("password");
                setSessionAttr(KEY.SESSION_ADMIN, dataMap);
                ret.put("code",1);
                ret.put("user",dataMap);
                renderJson(ret);
            } else {
                ret.put("code", 0);
                ret.put("msg", "用户名不存在或密码错误");
                renderJson(ret);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void user(){
//        getResponse().addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS,DELETE,PUT");
//        getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");

        String username=getPara("username");
        String password=getPara("password");
        if(username==null || "".equals(username)){
            Map ret = new HashMap();
            ret.put("code",KEY.USER.LOGIN_NAME_NOT_EMPTY);
            ret.put("msg","登录名不能为空！");
            renderJson(ret);
            return;
        }
        if(password==null || "".equals(password)){
            Map ret = new HashMap();
            ret.put("code",KEY.USER.PASSWORD_NOT_EMPTY);
            ret.put("msg","密码不能为空！");
            renderJson(ret);
            return;
        }
        try {
            Record r = Db.findFirst("select * from staff where username=? and password=?", username, password);
            Map ret = new HashMap();
            if(r!=null){
                Map dataMap=r.getColumns();
                dataMap.remove("password");
                UserBean userbean=new UserBean();
                userbean.putAll(dataMap);

                setSessionAttr(KEY.SESSION_USER,userbean);

                ret.put("code",1);
                ret.put("user",dataMap);
                renderJson(ret);
            }else{
                ret.put("code",0);
                ret.put("msg","用户名不存在或密码错误");
                renderJson(ret);
            }
        }catch(Exception e){
            e.printStackTrace();
            Map ret = new HashMap();
            ret.put("msg",e.toString());
            renderJson(ret);
        }

    }
    public void byLoginCode(){
        String phone=getPara("phone");
        String loginCode=getPara("loginCode");

        String phoneSession=getSessionAttr("phone");
        String loginCodeSession=getSessionAttr("loginCode");
        Date loginCodeTimeSession= getSessionAttr("loginCodeTime");

        Date now=new Date();

        Map ret = new HashMap();
        if(phone==null || "".equals(phone)){
            ret.put("code",-100);
            ret.put("msg","请输入手机号！");
        }else if(loginCode==null || "".equals(loginCode)){
            ret.put("code",-101);
            ret.put("msg","请输入验证码！");
        }else if(phoneSession==null || "".equals(phoneSession) || loginCodeSession==null || "".equals(loginCodeSession) || loginCodeTimeSession==null){
            ret.put("code",-102);
            ret.put("msg","请重新获取登录验证码！");
        }else if((now.getTime()-loginCodeTimeSession.getTime())>60*10*1000){
            ret.put("code",-103);
            ret.put("msg","登录验证码失效，请重新获取登录验证码！");
        }else if(phone.equals(phoneSession) && loginCode.equals(loginCodeSession)){

            Record r=Db.findFirst("select * from user where phone=?",phone);
            if(r==null){
                ret.put("code",-104);
                ret.put("msg","数据库没有此手机号！");
            }else {
                Map dataMap = r.getColumns();
                dataMap.remove("password");
                setSessionAttr(KEY.SESSION_USER, dataMap);
                ret.put("code",1);
                ret.put("msg","登录成功！");
            }
        }else{
            ret.put("code",-105);
            ret.put("msg","手机号或验证码填写错误，请重新输入手机号、验证码，并点击登录！");
        }

        renderJson(ret);
    }
}
