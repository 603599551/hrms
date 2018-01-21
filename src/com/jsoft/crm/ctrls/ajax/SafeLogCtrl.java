package com.jsoft.crm.ctrls.ajax;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import utils.NumberUtils;
import utils.bean.JsonHashMap;

public class SafeLogCtrl extends Controller{

    public void index(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm=new JsonHashMap();
        try{
            Page page=Db.paginate(pageNum,pageSize,"select * "," from log order by create_time desc ");
            jhm.putCode(1);
            jhm.put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
