package com.ss.stock.controllers;

import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.SecurityStockService;
import utils.bean.JsonHashMap;

import java.util.List;

public class SecurityStockCtrl extends BaseCtrl {

    public void getSecurityStock(){
        JsonHashMap jhm = new JsonHashMap();
        SecurityStockService service = enhance(SecurityStockService.class);
        List<Record> result = service.getSecurityStockList();
        jhm.putCode(1).put("list", result);
        renderJson(jhm);
    }

}
