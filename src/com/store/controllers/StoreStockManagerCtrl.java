package com.store.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 门店库存管理
 */
public class StoreStockManagerCtrl extends BaseCtrl{
    public void index(){

    }

    /**
     * 显示库存余额
     */
    public void showBalance(){
        JsonHashMap jhm=new JsonHashMap();
        String storeId=getPara("store_id");
        String sql="select id,code,name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text ,number from store_stock a where store_id=? order by code,id";
        List<Record> list=Db.find(sql,storeId);
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
