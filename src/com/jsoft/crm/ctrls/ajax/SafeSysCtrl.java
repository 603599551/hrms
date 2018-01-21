package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.services.MenuService;
import com.jsoft.crm.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.*;

public class SafeSysCtrl extends Controller {

    public void menu(){
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        try {
            if("admin".equals(usu.getUsername())){
                List<Record> list=Db.find("select * from sys_menu order by sort");
                for(Record r:list){
                    r.set("title",r.get("name"));
                    r.remove("name");
                    r.set("link",r.get("url"));
                    r.remove("url");
                }
                List reList= MenuService.getMe().sort(list);
                jhm.putCode(1);
                jhm.put("list",reList);
            }else{
                String jobId=usu.getUserBean().getJobId();
                List<Record> list=Db.find("select m.* from sys_menu m,author_job_menu ajm where m.ID=ajm.menu_id and ajm.job_id=? and ajm.access='1' order by sort",jobId);
                for(Record r:list){
                    r.set("title",r.get("name"));
                    r.remove("name");
                    r.set("link",r.get("url"));
                    r.remove("url");
                }
                List reList= MenuService.getMe().sort(list);
                jhm.putCode(1);
                jhm.put("list",reList);
            }
            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            Map ret=new HashMap();
            ret.put("code",KEY.CODE.ERROR);
            ret.put("msg",e.toString());
            renderJson(ret);
        }
    }

}
