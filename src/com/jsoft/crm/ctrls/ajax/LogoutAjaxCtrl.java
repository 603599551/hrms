package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/*
退出登录
 */
public class LogoutAjaxCtrl extends Controller{
    public void index(){
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
            Map reMap = new HashMap();
            reMap.put("code", 1);
            renderJson(reMap);
        }catch (Exception e){
            e.printStackTrace();
            Map reMap = new HashMap();
            reMap.put("code", -1);
            reMap.put("msg",e.toString());
            renderJson(reMap);
        }
    }
}
