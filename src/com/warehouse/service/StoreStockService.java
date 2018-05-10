package com.warehouse.service;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.List;

public class StoreStockService {

    @Before(Tx.class)
    public void saveAndUpdateStoreStock(List<Record> saveList, List<Record> updateList){
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_stock", saveList, saveList.size());
        }
        if(updateList != null && updateList.size() > 0){
            for(Record r : updateList){
                Db.update("store_stock", r);
            }
        }
    }

}
