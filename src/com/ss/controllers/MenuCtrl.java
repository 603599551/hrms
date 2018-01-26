package com.ss.controllers;

import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.services.MenuService;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuCtrl extends BaseCtrl {

    public void index() {

        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JsonHashMap jhm = new JsonHashMap();
        try {
            if ("admin".equals(usu.getUsername())) {
                List<Record> list = Db.find("select id,name as title,CONCAT('/',ifnull(url,'')) as link,parent_id,sort,icon as iconName,type from menu order by sort");
                List reList = MenuService.getMe().sort(list);
                jhm.putCode(1);
                jhm.put("list", reList);
            } else {
                String jobId = usu.getUserBean().getJobId();
                List<Record> list = Db.find("select m.id,m.name as title,CONCAT('/',ifnull(m.url,'')) as link,m.parent_id,m.sort,m.icon as iconName,m.type from menu m,author_job_menu ajm where m.ID=ajm.menu_id and ajm.job_id=? and ajm.access='1' order by sort", jobId);
                List reList = MenuService.getMe().sort(list);
                jhm.putCode(1);
                jhm.put("list", reList);
            }
            renderJson(jhm);
        } catch (Exception e) {
            e.printStackTrace();
            Map ret = new HashMap();
            ret.put("code", KEY.CODE.ERROR);
            ret.put("msg", e.toString());
            renderJson(ret);
        }
    }
}
