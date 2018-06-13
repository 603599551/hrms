package com.warehouse.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.SelectUtil;
import com.utils.UnitConversion;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 仓库库存
 */
public class WarehouseStockCtrl extends BaseCtrl {

    /**
     * 显示当前库存数量
     * 只显示大于0的库存
     */
    public void queryBalanceList(){
        JsonHashMap jhm=new JsonHashMap();
        String warehouseId=getPara("warehouseId");
        if("-1".equals(warehouseId))warehouseId="";
        String sql="select id,(select name from warehouse where warehouse.id=a.warehouse_id) as warehouse_text,code,name,batch_code,number,out_unit,box_attr,box_attr_num,unit_big,unit,unit_num from warehouse_stock a ";
        SelectUtil selectUtil=new SelectUtil(sql);
        selectUtil.addWhere("and a.number>0");
        selectUtil.addWhere("and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
        selectUtil.order("order by warehouse_id,material_id,batch_code");
        List<Record> list=Db.find(selectUtil.toString(),selectUtil.getParameters());
        for(Record r:list){
            try {
                String attr = UnitConversion.getAttrByOutUnit(r);
                r.set("attribute_2_text", attr);
            }catch (Exception e){
//                e.printStackTrace();
            }
            r.set("unit_text",r.get("out_unit"));
        }
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
}
