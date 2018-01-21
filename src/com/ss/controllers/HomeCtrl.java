package com.ss.controllers;

import com.jfinal.core.Controller;

public class HomeCtrl extends Controller {
    public void index(){
        redirect("/login.html");
    }
}
