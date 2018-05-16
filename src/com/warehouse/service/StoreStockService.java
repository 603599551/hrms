package com.warehouse.service;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;

import java.util.ArrayList;
import java.util.List;

public class StoreStockService {

    @Before(Tx.class)
    public void saveAndUpdateStoreStock(List<Record> saveList, List<Record> updateList, List<Record> storeStocktakingList, UserSessionUtil usu){
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_stock", saveList, saveList.size());
            for(Record r : saveList){
                Record storeStocktaking = new Record();
                storeStocktaking.setColumns(r);
            }
        }
        if(updateList != null && updateList.size() > 0){
            for(Record r : updateList){
                Db.update("store_stock", r);
            }
        }
        if(storeStocktakingList != null && storeStocktakingList.size() > 0){
            for(Record r : storeStocktakingList){
                r.set("creater_id", usu.getUserId());
                r.set("modifier_id", usu.getUserId());
                String time = DateTool.GetDateTime();
                r.set("creater_time", time);
                r.set("modifier_time", time);
            }
            Db.batchSave("store_stocktaking", storeStocktakingList, storeStocktakingList.size());
        }
    }

}
