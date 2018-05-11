package com.store.order.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 门店接收入库
 */
public class StoreOrderReceiveSrv {

    /**
     * 门店接收入库
     * @param storeOrderId
     * @param jsonArray
     * @param usu
     * @throws Exception
     */
    @Before(Tx.class)
    public void accept(String storeOrderId, JSONArray jsonArray, UserSessionUtil usu) throws Exception{
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
        int jsonArraySize=jsonArray.size();
        Object[][] dataArray=new Object[jsonArraySize][2];
        int i=0;
        for(Object elemInArray:jsonArray){
            JSONObject jsonInArray=(JSONObject)elemInArray;
            String idInElem=jsonInArray.getString("id");
            String materialIdInElem=jsonInArray.getString("material_id");
            int receiveNum=jsonInArray.getInteger("receive_num");
            dataArray[i][0]=receiveNum;
            dataArray[i][1]=idInElem;
            i++;
        }
        String sql="update store_order_material set receive_num=? where id=?";
        int[] numArray=Db.batch(sql,dataArray,100);
        int sum=0;
        for(int numTemp:numArray){
            sum=sum+numTemp;
        }
        doUpdateStoreStock(storeOrderId,usu);
    }
    /*
    保存门店订单接收的数量
    如果订单中的原材料，在本店库存中有，就执行更新数量的操作
    如果没有，就执行保存操作
     */
    private void doUpdateStoreStock(String storeOrderId,UserSessionUtil usu){
        /*
        更新门店库存
         */
        //门店订单原材料
        List<Record> storeOrderMaterialList=Db.find("select * from store_order_material where store_order_id=?",storeOrderId);
        //当前门店库存list
        List<Record> storeStockList=Db.find("select * from store_stock where store_id=?",usu.getUserBean().getDeptId());


        List<Object[]> updateDataList=new ArrayList();
        List<Record> insertDataList=new ArrayList();
        buildData(usu,storeOrderMaterialList,storeStockList,insertDataList,updateDataList);

        if(updateDataList!=null && !updateDataList.isEmpty()){
            Object[][] dataArray=new Object[updateDataList.size()][2];
            int i=0;
            for(Object[] arrayTemp:updateDataList){
                dataArray[i]=arrayTemp;
                i++;
            }
            int[] numArray=Db.batch("update store_stock set number=? where id=?",dataArray,100);
            int numSum=0;
            for(int numTemp:numArray){
                numSum=numSum+numTemp;
            }
        }
        if(insertDataList!=null && !insertDataList.isEmpty()){
            int[] numArray=Db.batchSave("store_stock",insertDataList,100);
            int numSum=0;
            for(int numTemp:numArray){
                numSum=numSum+numTemp;
            }
        }
    }

    /**
     * 构建数据
     * 如果订单中的原材料，在本店库存中有，就将该记录放入到updateDataList中
     如果没有，就将记录放入到insertDataList中
     * @param usu
     * @param storeOrderMaterialList
     * @param storeStockList
     * @param insertDataList
     * @param updateDataList
     */
    private void buildData(UserSessionUtil usu,List<Record> storeOrderMaterialList,List<Record> storeStockList,List<Record> insertDataList,List<Object[]> updateDataList){
        String datetime= DateTool.GetDateTime();
        String store_id=usu.getUserBean().getDeptId();
        /*
        查询当前门店的库存最大序号
         */
        int maxSort=getMaxSortOfStoreStock(usu.getUserBean().getDeptId());

        for(Record storeOrderMaterialR:storeOrderMaterialList){
            String materialIdOfStoreOrderMaterialR=storeOrderMaterialR.getStr("material_id");
            String codeOfStoreOrderMaterialR=storeOrderMaterialR.getStr("code");
            String nameOfStoreOrderMaterialR=storeOrderMaterialR.getStr("name");
            String pinyinOfStoreOrderMaterialR=storeOrderMaterialR.getStr("pinyin");
            String attribute1OfStoreOrderMaterialR=storeOrderMaterialR.getStr("attribute_1");
            String attribute2OfStoreOrderMaterialR=storeOrderMaterialR.getStr("attribute_2");
            String unitOfStoreOrderMaterialR=storeOrderMaterialR.getStr("unit");
            int receiveNum=storeOrderMaterialR.getInt("receive_num");
            for(Record storeStockR:storeStockList){
                String idOfStoreStockR=storeStockR.getStr("id");
                String materialIdOfStoreStockR=storeStockR.getStr("material_id");
                String numberOfStoreStockR=storeStockR.getStr("number");
                if(materialIdOfStoreOrderMaterialR.equals(materialIdOfStoreStockR)){//如果相同，就放入到updateDataList里，用于后续的更新
                    updateDataList.add(new Object[]{receiveNum+numberOfStoreStockR,idOfStoreStockR});
                    continue;
                }
            }

            Record r=new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("store_id", store_id);
            r.set("material_id", materialIdOfStoreOrderMaterialR);
            r.set("code", codeOfStoreOrderMaterialR);
            r.set("name", nameOfStoreOrderMaterialR);
            r.set("pinyin", pinyinOfStoreOrderMaterialR);
            r.set("attribute_1", attribute1OfStoreOrderMaterialR);
            r.set("attribute_2", attribute2OfStoreOrderMaterialR);
            r.set("unit", unitOfStoreOrderMaterialR);
            r.set("number", receiveNum);
            r.set("modify_time", datetime);
            r.set("sort", maxSort);
            maxSort++;
            insertDataList.add(r);
        }
    }

    /**
     * 根据store_id获取该门店库存的最大sort
     * @param storeId
     * @return
     */
    private int getMaxSortOfStoreStock(String storeId){
        Record maxR=Db.findFirst("select max(sort) as max from store_stock where store_id=?",storeId);
        int maxSort=1;
        if(maxR!=null){
            Object maxSortObj=maxR.get("max");
            maxSort= NumberUtils.parseInt(maxSortObj,0);
        }
        if(maxSort==0)maxSort=1;
        return maxSort;
    }
}
