package com.jsoft.crm.ctrls;

import com.jfinal.Config;
import com.jfinal.core.Controller;

public class Index extends Controller {
    public void index(){

        if(Config.devMode){
            String sessionId=getSession().getId();
            System.out.println("sessionId="+sessionId);
        }
        render("/index.html");
    }
}
