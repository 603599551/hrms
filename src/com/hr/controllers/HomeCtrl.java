package com.hr.controllers;

import com.jfinal.core.Controller;
import com.utils.UserSessionUtil;

public class HomeCtrl extends Controller {
    public void index(){
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        if(usu.isLogin()){
            redirect("/index.html");
        }else {
            redirect("/login.html");
        }
    }
}
