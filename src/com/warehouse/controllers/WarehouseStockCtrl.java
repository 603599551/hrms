package com.warehouse.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.SelectUtil;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 仓库库存
 */
public class WarehouseStockCtrl extends BaseCtrl {

    /**
     * 显示当前库存数量
     */
    public void queryBalanceList(){
        JsonHashMap jhm=new JsonHashMap();
        String warehouseId=getPara("warehouseId");
        String sql="select id,(select name from warehouse where warehouse.id=a.warehouse_id) as warehouse_text,code,name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,batch_code,number from warehouse_stock a ";
        SelectUtil selectUtil=new SelectUtil(sql);
        selectUtil.addWhere("and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
        selectUtil.order("order by warehouse_id,material_id,batch_code");
        List<Record> list=Db.find(selectUtil.toString(),selectUtil.getParameters());
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
