package com.store.order.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.List;

public class StoreOrderReceiveSrv {

    @Before(Tx.class)
    public void accept(String storeOrderId, JSONArray array) throws Exception{
        /*
         * 更新门店订单状态
         */
        Db.update("update store_order set status=? where id=?",50,storeOrderId);
        /*
        更新出库状态为完成（即门店已接收）
         */
        Db.update("update warehouse_out_order set status=? where store_order_id=?",40,storeOrderId);
        /*
        保存门店订单接收的数量
         */
//        List<Record> list=Db.find("select * from store_order_material where store_order_id=? order by sort",storeOrderId);
//
        List<Object[]> batchDataList=new ArrayList<>();
//        for(Record r:list){
//            String materialId=r.getStr("material_id");
//            for(Object elemInArray:array){
//                JSONObject jsonInArray=(JSONObject)elemInArray;
//                String materialIdInElem=jsonInArray.getString("material_id");
//                int receiveNum=jsonInArray.getInteger("receive_num");
//                if(materialId.equals(materialIdInElem)){
//                    batchDataList.add(new Object[]{receiveNum,materialId,storeOrderId});
//                    break;
//                }
//            }
//        }
//        String sql="update store_order_material set receive_num=? where materialIdInElem=? and store_order_id=?";
        for(Object elemInArray:array){
            JSONObject jsonInArray=(JSONObject)elemInArray;
            String idInElem=jsonInArray.getString("id");
//                String materialIdInElem=jsonInArray.getString("material_id");
            int receiveNum=jsonInArray.getInteger("receive_num");
            batchDataList.add(new Object[]{receiveNum,idInElem});
        }
        String sql="update store_order_material set receive_num=? where id=?";
        int[] numArray=Db.batch(sql,(Object[][])batchDataList.toArray(),100);
        int sum=0;
        for(int numTemp:numArray){
            sum=sum+numTemp;
        }


    }
}
