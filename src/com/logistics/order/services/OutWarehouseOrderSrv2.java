package com.logistics.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class OutWarehouseOrderSrv2 {

    /**
     * 生成出库单
     */
    /**
     * 保存并出库
     * 将数据保存到warehouse_out_order_material表
     * 将数据保存到warehouse_out_order_material_detail表
     * 将数据保存到store_order_material表
     *
     * 更新warehouse_out_order、store_order状态
     *
     * @param id 订单编号
     * @param array 提交的数据
     * @return
     */
    @Before(Tx.class)
    public JsonHashMap buildOutWarehouseOrder(String id, JSONArray array){
        JsonHashMap jhm=new JsonHashMap();
        Record warehouseOutOrderR= Db.findById("warehouse_out_order",id);
        String storeOrderId=null;
        String status=null;
        if(warehouseOutOrderR==null){
            jhm.putCode(0).putMessage("无此订单记录！");
            return jhm;
        }else{
            // 下面代码为校验代码，为了debug调试，所以注释掉，调试成功后，必须取消注释下面代码 author:mym
            status=warehouseOutOrderR.getStr("status");
            storeOrderId=warehouseOutOrderR.getStr("store_order_id");
            if("30".equals(status)){
                jhm.putCode(0).putMessage("此订单已经出库，不能再次出库！");
                return jhm;
            }
            if("40".equals(status)){
                jhm.putCode(0).putMessage("此订单已经完成，不能再次出库！");
                return jhm;
            }
        }


        List<Record> allList=new ArrayList<>();
        List<OutWarehouseOrderSrv.WarehouseOutOrderMaterialDetailBean> allBeanList=new ArrayList<>();
        int sort=1;
        List<String> materialIdList=new ArrayList();
        for(Object obj:array){
            JSONObject json=(JSONObject)obj;
            JSONArray subArray=json.getJSONArray("warehouseStockInfo");
            for(Object subObj:subArray) {
                JSONObject subJson=(JSONObject)subObj;
                String warehouseStockId = subJson.getString("warehouse_stock_id");
                String materialId = subJson.getString("material_id");
                String warehouseId = subJson.getString("warehouse_id");

//            String warehouseStockNumber=subJson.getString("warehouseStockNumber");
                String send_number = subJson.getString("send_number");
                int sendNumberInt= NumberUtils.parseInt(send_number,0);
                String batchCode = subJson.getString("batch_code");
                String warehouseOutOrderMaterialId = subJson.getString("warehouseOutOrderMaterialId");
                String code = subJson.getString("code");
                String name = subJson.getString("name");
//                String wantNum = subJson.getString("want_num");

                materialIdList.add(materialId);

                Record warehouseOutOrderMaterialDetailR = new Record();
                warehouseOutOrderMaterialDetailR.set("id", UUIDTool.getUUID());
                warehouseOutOrderMaterialDetailR.set("warehouse_out_order_id", id);
                warehouseOutOrderMaterialDetailR.set("warehouse_out_order_material_id", warehouseOutOrderMaterialId);
                warehouseOutOrderMaterialDetailR.set("warehouse_stock_id", warehouseStockId);
                warehouseOutOrderMaterialDetailR.set("warehouse_id", warehouseId);
//                warehouseOutOrderMaterialDetailR.set("store_id", storeId);
                warehouseOutOrderMaterialDetailR.set("material_id", materialId);
                warehouseOutOrderMaterialDetailR.set("batch_code", batchCode);
                warehouseOutOrderMaterialDetailR.set("code", code);
                warehouseOutOrderMaterialDetailR.set("name", name);
                warehouseOutOrderMaterialDetailR.set("send_num", sendNumberInt);
                warehouseOutOrderMaterialDetailR.set("sort", sort);
                warehouseOutOrderMaterialDetailR.set("status", "1");

                sort++;

//                OutWarehouseOrderSrv.WarehouseOutOrderMaterialDetailBean bean=new OutWarehouseOrderSrv.WarehouseOutOrderMaterialDetailBean();
//                bean.materialId =materialId;
//                bean.sendNum=NumberUtils.parseInt(send_number,0);
//                bean.batchCode=batchCode;
//
//                allList.add(warehouseOutOrderMaterialDetailR);
//                allBeanList.add(bean);
            }
        }


        return jhm;
    }
}
