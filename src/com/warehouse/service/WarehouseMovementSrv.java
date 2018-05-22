package com.warehouse.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 移库操作service
 */
public class WarehouseMovementSrv {
    @Before(Tx.class)
    public void save(JSONObject jsonObject) throws Exception{
        String outWarehouseId=jsonObject.getString("outWarehouseId");
        String inWarehouseId=jsonObject.getString("inWarehouseId");
        JSONArray jsonArray=jsonObject.getJSONArray("list");
        String reasonj=jsonObject.getString("reason");
        UserSessionUtil usu=(UserSessionUtil )jsonObject.get("usu");

        String warehouseMovementOrderId= UUIDTool.getUUID();
        String datetime= DateTool.GetDateTime();

        Record warehouseMovementOrderR=new Record();
        warehouseMovementOrderR.set("id",warehouseMovementOrderId);
        warehouseMovementOrderR.set("out_warehouse_id",outWarehouseId);
        warehouseMovementOrderR.set("in_warehouse_id",inWarehouseId);
        warehouseMovementOrderR.set("order_number","待生成");
        warehouseMovementOrderR.set("creater_id",usu.getUserId());
        warehouseMovementOrderR.set("create_time",datetime);
        warehouseMovementOrderR.set("status",20);

        Db.save("warehouse_movement_order",warehouseMovementOrderR);

        List<Record> saveList=new ArrayList();
        for(Object obj:jsonArray){
            JSONObject json=(JSONObject)obj;

            Record warehouseMovementOrderMaterialR=new Record();
            warehouseMovementOrderMaterialR.set("id",UUIDTool.getUUID());
            warehouseMovementOrderMaterialR.set("warehouse_movement_order_id",warehouseMovementOrderId);
            warehouseMovementOrderMaterialR.set("material_id",json.getString("material_id"));
            warehouseMovementOrderMaterialR.set("batch_code",json.getString("batch_code"));
            warehouseMovementOrderMaterialR.set("code",json.getString("code"));
            warehouseMovementOrderMaterialR.set("name",json.getString("name"));
            warehouseMovementOrderMaterialR.set("pinyin",json.getString("pinyin"));
            warehouseMovementOrderMaterialR.set("attribute_1",json.getString("attribute_1"));
            warehouseMovementOrderMaterialR.set("attribute_2",json.getString("attribute_2"));
            warehouseMovementOrderMaterialR.set("type_1",json.getString("type_1"));
            warehouseMovementOrderMaterialR.set("type_2",json.getString("type_2"));
            warehouseMovementOrderMaterialR.set("number",json.getString("number"));

            saveList.add(warehouseMovementOrderMaterialR);
        }
        Db.batchSave("warehouse_movement_order_material",saveList,30);

    }

    @Before(Tx.class)
    public void submit(JSONObject jsonObject) throws Exception{
        String outWarehouseId=jsonObject.getString("outWarehouseId");
        String inWarehouseId=jsonObject.getString("inWarehouseId");
        JSONArray jsonArray=jsonObject.getJSONArray("list");
        String reasonj=jsonObject.getString("reason");
        UserSessionUtil usu=(UserSessionUtil )jsonObject.get("usu");

        String warehouseMovementOrderId= UUIDTool.getUUID();
        String datetime= DateTool.GetDateTime();

        Record warehouseMovementOrderR=new Record();
        warehouseMovementOrderR.set("id",warehouseMovementOrderId);
        warehouseMovementOrderR.set("out_warehouse_id",outWarehouseId);
        warehouseMovementOrderR.set("in_warehouse_id",inWarehouseId);
        warehouseMovementOrderR.set("order_number","待生成");
        warehouseMovementOrderR.set("creater_id",usu.getUserId());
        warehouseMovementOrderR.set("create_time",datetime);
        warehouseMovementOrderR.set("status",20);

        Db.save("warehouse_movement_order",warehouseMovementOrderR);

        Object[][] array=new Object[jsonArray.size()][3];
        int index=0;
        for(Object obj:jsonArray){
            JSONObject json=(JSONObject)obj;

            Record warehouseMovementOrderMaterialR=new Record();
            warehouseMovementOrderMaterialR.set("id",UUIDTool.getUUID());
            warehouseMovementOrderMaterialR.set("warehouse_movement_order_id",warehouseMovementOrderId);
            warehouseMovementOrderMaterialR.set("material_id",json.getString("material_id"));
            warehouseMovementOrderMaterialR.set("batch_code",json.getString("batch_code"));
            warehouseMovementOrderMaterialR.set("code",json.getString("code"));
            warehouseMovementOrderMaterialR.set("name",json.getString("name"));
            warehouseMovementOrderMaterialR.set("pinyin",json.getString("pinyin"));
            warehouseMovementOrderMaterialR.set("attribute_1",json.getString("attribute_1"));
            warehouseMovementOrderMaterialR.set("attribute_2",json.getString("attribute_2"));
            warehouseMovementOrderMaterialR.set("type_1",json.getString("type_1"));
            warehouseMovementOrderMaterialR.set("type_2",json.getString("type_2"));
            warehouseMovementOrderMaterialR.set("number",json.getString("number"));
            Db.save("warehouse_movement_order_material",warehouseMovementOrderMaterialR);

            array[index][0]=1;
        }

    }
}
