package com.store.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.SelectUtil;
import com.utils.UnitConversion;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 门店接收入库
 */
public class StoreOrderReceiveSrv {

    /**
     * 门店接收入库
     *
     * 查询该门店库存中是否有入库的原材料记录，如果有就更新数量，如果没有就新增记录
     *
     * @param storeOrderId
     * @param jsonArray
     * @param usu
     * @throws Exception
     */
    @Before(Tx.class)
    public JsonHashMap accept(String storeOrderId, JSONArray jsonArray, UserSessionUtil usu) throws Exception{
        JsonHashMap jhm=new JsonHashMap();
        Record storeOrderR=Db.findById("store_order",storeOrderId);
        String status=storeOrderR.getStr("status");
        if("50".equals(status)){
            jhm.putCode(0).putMessage("已经接收入库，不能重复接收！");
            return jhm;
        }
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
            int receiveNum=jsonInArray.getInteger("receive_num");//装箱单位
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

        jhm.putCode(1).putMessage("接收成功");
        return jhm;
    }
    /*
    根据前台提交的数据查询warehouse_out_order_material_detail表的记录
     */
    private List<Record> queryOutWarehouseDetail(String storeOrderId,List<Record> storeOrderMaterialList){
        List materialIdList=new ArrayList();
        for(Record r:storeOrderMaterialList){
            String materialIdInElem=r.getStr("material_id");
            materialIdList.add(materialIdInElem);
        }
        SelectUtil selectUtil=new SelectUtil("select * from warehouse_out_order_material_detail ");
        selectUtil.addWhere("and store_order_id=?",storeOrderId);
        selectUtil.in("and material_id in ",materialIdList.toArray());
        List<Record> reList=Db.find(selectUtil.toString(),selectUtil.getParameters());
        return reList;
    }
    private Record getByMaterialId(String materialId,List<Record> list){
        for(Record r:list){
            String materialIdDb=r.getStr("material_id");
            if(materialIdDb.equals(materialId)){
                return r;
            }
        }

        return null;
    }
    /*
    更新库存数。库存数单位是最小单位，物流发送的数量时提货单位，所以需要换算成最小单位
    如果订单中的原材料，在本店库存中有，就执行更新数量的操作
    如果没有，就执行保存操作
     */
    private void doUpdateStoreStock(String storeOrderId,UserSessionUtil usu){
        /*
        更新门店库存
         */
        //门店订单原材料
//        List<Record> storeOrderMaterialList=Db.find("select * from store_order_material where store_order_id=?",storeOrderId);
        List<Record> warehouseOutOrderMaterialDetailList=Db.find("select * from warehouse_out_order_material_detail where store_order_id=?",storeOrderId);
        //当前门店库存list
        List<Record> storeStockList=Db.find("select * from store_stock where store_id=?",usu.getUserBean().getDeptId());


        List<Object[]> updateDataList=new ArrayList();
        List<Record> insertDataList=new ArrayList();


        buildData(usu,warehouseOutOrderMaterialDetailList,storeStockList,insertDataList,updateDataList,storeOrderId);

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
     * @param warehouseOutOrderMaterialDetailList
     * @param storeStockList
     * @param insertDataList
     * @param updateDataList
     */
    private void buildData(UserSessionUtil usu,List<Record> warehouseOutOrderMaterialDetailList,List<Record> storeStockList,List<Record> insertDataList,List<Object[]> updateDataList,String storeOrderId){
        String datetime= DateTool.GetDateTime();
        String store_id=usu.getUserBean().getDeptId();

//        List<Record> warehouseOutOrderMaterialDetailList=queryOutWarehouseDetail(storeOrderId,storeOrderMaterialList);

        /*
        查询当前门店的库存最大序号
         */
        int maxSort=getMaxSortOfStoreStock(usu.getUserBean().getDeptId());

        for(Record warehouseOutOrderMaterialDetailR:warehouseOutOrderMaterialDetailList){
            String materialIdOfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("material_id");
            String codeOfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("code");
            String nameOfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("name");
            String pinyinOfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("pinyin");
            String attribute1OfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("attribute_1");
            String attribute2OfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("attribute_2");
            String unitOfWarehouseOutOrderMaterialDetailR=warehouseOutOrderMaterialDetailR.getStr("unit");
//            int receiveNum=storeOrderMaterialR.getInt("receive_num");
            int sendNum=warehouseOutOrderMaterialDetailR.getInt("send_num");//发货数量
//            int wantNum=storeOrderMaterialR.getInt("want_num");//门店想要的量，单位是最小单位

//            Record warehouseOutOrderMaterialDetailR=getByMaterialId(materialIdOfStoreOrderMaterialR,warehouseOutOrderMaterialDetailList);
            Object boxAttrNumObj=warehouseOutOrderMaterialDetailR.get("box_attr_num");
            Object unitNumObj=warehouseOutOrderMaterialDetailR.get("unit_num");
            String unit=warehouseOutOrderMaterialDetailR.getStr("unit");//小单位
            String unitBig=warehouseOutOrderMaterialDetailR.getStr("unit_big");//大单位
            String boxAttr=warehouseOutOrderMaterialDetailR.getStr("box_attr");//装箱规格单位
            String outUnit=warehouseOutOrderMaterialDetailR.getStr("out_unit");//提货单位

            int boxAttrNum=NumberUtils.parseInt(boxAttrNumObj,-1);
            int unitNum=NumberUtils.parseInt(unitNumObj,-1);

            /*
            门店库存的数量是最小单位，物流发送的单位是提货单位，此处换算成最小单位
             */
            int smallUnitNum= UnitConversion.outUnit2SmallUnit(sendNum,unit,unitBig,unitNum,boxAttr,boxAttrNum,outUnit);

            boolean has=false;
            for(Record storeStockR:storeStockList){
                String idOfStoreStockR=storeStockR.getStr("id");
                String materialIdOfStoreStockR=storeStockR.getStr("material_id");
                int numberOfStoreStockR=storeStockR.get("number");
                if(materialIdOfWarehouseOutOrderMaterialDetailR.equals(materialIdOfStoreStockR)){//如果相同，就放入到updateDataList里，用于后续的更新
                    has=true;
                    updateDataList.add(new Object[]{smallUnitNum+numberOfStoreStockR,idOfStoreStockR});
                    break;
                }
            }

            if(!has) {
                Record r = new Record();
                r.set("id", UUIDTool.getUUID());
                r.set("store_id", store_id);
                r.set("store_color", usu.getUserBean().get("store_color"));
                r.set("material_id", materialIdOfWarehouseOutOrderMaterialDetailR);
                r.set("code", codeOfWarehouseOutOrderMaterialDetailR);
                r.set("name", nameOfWarehouseOutOrderMaterialDetailR);
                r.set("pinyin", pinyinOfWarehouseOutOrderMaterialDetailR);
                r.set("attribute_1", attribute1OfWarehouseOutOrderMaterialDetailR);
                r.set("attribute_2", attribute2OfWarehouseOutOrderMaterialDetailR);
                r.set("unit", unitOfWarehouseOutOrderMaterialDetailR);
                r.set("type_1", warehouseOutOrderMaterialDetailR.get("type_1"));
                r.set("type_2", warehouseOutOrderMaterialDetailR.get("type_2"));
                r.set("unit_num", warehouseOutOrderMaterialDetailR.get("unit_num"));
                r.set("unit_big", warehouseOutOrderMaterialDetailR.get("unit_big"));
                r.set("box_attr_num", warehouseOutOrderMaterialDetailR.get("box_attr_num"));
                r.set("box_attr", warehouseOutOrderMaterialDetailR.get("box_attr"));
                r.set("out_unit", warehouseOutOrderMaterialDetailR.get("out_unit"));
                r.set("shelf_life_num", warehouseOutOrderMaterialDetailR.get("shelf_life_num"));
                r.set("shelf_life_unit", warehouseOutOrderMaterialDetailR.get("shelf_life_unit"));
                r.set("storage_condition", warehouseOutOrderMaterialDetailR.get("storage_condition"));
                r.set("order_type", warehouseOutOrderMaterialDetailR.get("order_type"));
                r.set("is_out_unit", warehouseOutOrderMaterialDetailR.get("is_out_unit"));
                r.set("model", warehouseOutOrderMaterialDetailR.get("model"));
                r.set("size", warehouseOutOrderMaterialDetailR.get("size"));
                r.set("brand", warehouseOutOrderMaterialDetailR.get("brand"));
                r.set("number", smallUnitNum);
                r.set("modify_time", datetime);
                r.set("sort", maxSort);
                maxSort++;
                insertDataList.add(r);
            }
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
