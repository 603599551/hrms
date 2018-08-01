package com.common.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import java.io.BufferedReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseCtrl extends Controller {

    protected SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
    protected SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
    protected static final long ONE_DAY_TIME = 1000 * 60 * 60 *24;

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

    /**
     * 取Request中的数据对象
     * @return
     * @throws Exception
     */
    protected String getRequestObject() throws Exception {
        StringBuilder json = new StringBuilder();
        BufferedReader reader = this.getRequest().getReader();
        String line = null;
        while((line = reader.readLine()) != null){
            json.append(line);
        }
        reader.close();
        return json.toString();
    }

    /**
     * 将前台的参数整理到Record对象中
     * @return
     */
    protected Map<String, Object> getParaMaps(){
        Map<String, Object> result = null;
        Enumeration<String> namesList = this.getRequest().getParameterNames();
        if(namesList != null && namesList.hasMoreElements()){
            result = new HashMap<>();
            while(namesList.hasMoreElements()){
                String name = namesList.nextElement();
                result.put(name, getPara(name));
            }
        }
        return result;
    }

    protected String nextDay(String date, int nextDay){
        String result = "";
        try {
            Date today = sdf_ymd.parse(date);
            today = new Date(today.getTime() + nextDay * ONE_DAY_TIME);
            result = sdf_ymd.format(today);
        } catch (ParseException e) {
            e.printStackTrace();
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
