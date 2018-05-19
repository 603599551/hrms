package com.logistics.order.services;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流处理门店订单
 */
public class StoreScrapSrv {

    @Before(Tx.class)
    public int accept(UserSessionUtil usu, String orderId){
        int result = 0;
        String datetime = DateTool.GetDateTime();
        Record storeScrap = Db.findById("select * from store_scrap where id=?", orderId);
        if(storeScrap != null){
            String store_id = storeScrap.getStr("store_id");
            List<Record> storeScrapMaterialList = Db.find("select * from store_scrap_material where store_scrap_id=?", orderId);
            List<Record> storeStackMaterialList = Db.find("select * from store_stock where store_id=?", store_id);
            Map<String, Record> storeStockMaterialMap = new HashMap<>();
            if(storeStackMaterialList != null && storeStackMaterialList.size() > 0){
                for(Record r : storeStackMaterialList){
                    storeStockMaterialMap.put(r.getStr("material_id"), r);
                }
            }
            if(storeScrapMaterialList != null && storeScrapMaterialList.size() > 0){
                List<Record> updateList = new ArrayList<>();
                for(Record r : storeScrapMaterialList){
                    Record storeStockMaterial = storeStockMaterialMap.get(r.getStr("material_id"));
                    double number = new Double(String.format("%.2f", getDouble(storeStockMaterial.getDouble("number") - r.getDouble("number"))));
                    Record updateSSM = new Record();
                    updateSSM.set("id", storeStockMaterial.getStr("id"));
                    updateSSM.set("number", number);
                    updateList.add(updateSSM);
                }
                for(Record r : updateList){
                    Db.update("store_stock", r);
                }
            }
            result= Db.update("update store_scrap set status=?,logistics_modifier_id=?,logistics_modifier_time=? where id=?", 2, usu.getUserId(),datetime,orderId);
        }
        return result;
    }
    /**
     * 将obj转化成int类型
     *      如果为空返回0
     *      如果是double类型，将double转化成int
     * @param obj
     * @return
     */
    private double getDouble(Object obj){
        if(obj != null && obj.toString().trim().length() > 0 && !"null".equalsIgnoreCase(obj.toString())){
            if(obj instanceof Double){
                double result = new Double(obj.toString());
                return result;
            }else if(obj instanceof Integer){
                return new Double(obj.toString());
            }
        }
        return 0;
    }
}
