package com.common.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import sun.swing.StringUIClientPropertyKey;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示数据字典
 */
public class DictionaryCtrl extends BaseCtrl {

    /**
     * 第一项为“全部”
     * 传输参数返回list
     * 参数是字典值
     */
    public void getDictIncludeAll() {
        String dict = getPara("dict");
        JsonHashMap jhm = new JsonHashMap();
        try {
            List<Record> list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
            Record all = new Record();
            all.set("value", "-1");
            all.set("name", "全部");
            list.add(0, all);
            jhm.putCode(1).put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 第一项为“请选择”
     */
    public void getDictIncludeChoose() {
        String dict = getPara("dict");
        JsonHashMap jhm = new JsonHashMap();
        try {
            List<Record> list;
            //如果是消息管理查看，只显示部分数据
            if (!StringUtils.equals(dict, "notice_type")) {
                list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
                //绩效考核的时候去掉”请选择“选项
                if (!StringUtils.equals(dict, "performance_type")) {
                    Record all = new Record();
                    all.set("value", "-1");
                    all.set("name", "请选择");
                    list.add(0, all);
                }
                //添加员工页面的在职状态只显示请选择和在职
                if(StringUtils.equals(dict,"job_type_disabled")){
                    list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", "job_type");
                    for(int i = 0 ; i < list.size() ; ++i){
                        if(!StringUtils.equals(list.get(i).getStr("value"),"on")){
                            list.get(i).set("disabled",true);
                        }
                    }
                }
            } else {
                list = Db.find("select name, value from h_dictionary where (value = 'apply_movein' or value = 'movein_notice') and parent_id=(select id from h_dictionary where value=? )",dict);
                Record all = new Record();
                all.set("value", "-1");
                all.set("name", "请选择");
                list.add(0, all);
            }

            jhm.putCode(1).put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 只返回数据库中的字典值
     */
    public void getDict() {
        String dict = getPara("dict");
        JsonHashMap jhm = new JsonHashMap();
        try {
            List<Record> list = Db.find("select name, value from h_dictionary where parent_id=(select id from h_dictionary where value=?) order by sort", dict);
            jhm.putCode(1).put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.toString());
        }
        renderJson(jhm);
    }

}
