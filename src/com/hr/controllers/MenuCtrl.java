package com.hr.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.service.MenuService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.List;
import java.util.Map;

/**
 * 获取菜单
 */
public class MenuCtrl extends BaseCtrl {

    public void index() {

        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JsonHashMap jhm = new JsonHashMap();
        try {
            List reList=null;
            if ("admin".equals(usu.getUsername())) {
                List<Record> list = Db.find("select id,name as title,ifnull(url,'') as link,parent_id,sort,icon as iconName,type from h_menu order by sort");
                reList = MenuService.getMe().sort(list);
            } else {
                String jobId = usu.getUserBean().getJobId();
                List<Record> list = Db.find("select m.id,m.name as title,ifnull(url,'') as link,m.parent_id,m.sort,m.icon as iconName,m.type from h_menu m,h_author_job_menu ajm where m.ID=ajm.menu_id and ajm.job_id=? and ajm.access='1' order by sort", jobId);
                reList = MenuService.getMe().sort(list);
            }

            if(reList!=null && !reList.isEmpty()){
                Map map=(Map)reList.get(0);
                String link=(String)map.get("link");
                jhm.put("defaultLink",link);
            }
            jhm.putCode(1).put("data", reList);
            renderJson(jhm);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
