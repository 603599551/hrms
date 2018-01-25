package com.ss.goods.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.sun.deploy.panel.JHighDPITable;
import utils.bean.JsonHashMap;

import java.util.List;

public class GoodsUnitCtrl extends BaseCtrl {
    @Override
    public void add() {
        super.add();
    }

    @Override
    public void deleteById() {
        super.deleteById();
    }

    @Override
    public void showById() {
        super.showById();
    }

    @Override
    public void updateById() {
        super.updateById();
    }

    @Override
    public void query() {
        JsonHashMap jhm=new JsonHashMap();
        List<Record> list=Db.find("select * from goods_unit order by sort ");
        jhm.putCode(1).put("list",list);
        renderJson(list);
    }
}
