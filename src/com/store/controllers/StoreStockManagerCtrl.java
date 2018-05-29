package com.store.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.UserSessionUtil;
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
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String storeId=usu.getUserBean().getDeptId();
        String sql="select id,code,name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text ,number from store_stock a where store_id=? order by type_1,type_2,code,id";
        List<Record> list=Db.find(sql,storeId);
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
