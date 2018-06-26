package com.common.controllers;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import java.util.Enumeration;

public class BaseCtrl extends Controller {

    /**
     * 将前台的参数整理到Record对象中
     * @return
     */
    protected Record getParaRecord(){
        Record result = null;
        Enumeration<String> namesList = this.getRequest().getParameterNames();
        if(namesList != null && namesList.hasMoreElements()){
            result = new Record();
            while(namesList.hasMoreElements()){
                String name = namesList.nextElement();
                result.set(name, getPara(name));
            }
        }
        return result;
    }

    public void add(){

    }

    public void deleteById(){

    }
    public void showById(){

    }
    public void updateById(){

    }
    public void list(){

    }
    public void query(){

    }
    public void index(){

    }
}
