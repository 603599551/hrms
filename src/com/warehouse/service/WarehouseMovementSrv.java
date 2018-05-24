package com.warehouse.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.services.OrderNumberGenerator;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 移库操作service
 */
public class WarehouseMovementSrv {
    String datetime= DateTool.GetDateTime();
    /**
     * 保存操作
     * 新增、更新
     * @param jsonObject
     * @throws Exception
     */
    @Before(Tx.class)
    public JsonHashMap save(JSONObject jsonObject) throws Exception{
        JsonHashMap jhm=new JsonHashMap();
        String id=jsonObject.getString("id");
        DataBean db=buildData(jsonObject,false);
        if(!db.checked){
            jhm.putCode(0).putMessage(db.message);
            return jhm;
        }
        if(db.isCreate){

            Db.save("warehouse_movement_order",db.warehouseMovementOrderR);
        }else{
            Db.delete("delete from warehouse_movement_order_material where warehouse_movement_order_id=?",id);
            Db.update("warehouse_movement_order",db.warehouseMovementOrderR);
        }
        Db.batchSave("warehouse_movement_order_material",db.warehouseMovementOrderMaterialList,30);
        jhm.putCode(1).put("id",db.warehouseMovementOrderR.getStr("id"));
        return jhm;
    }


    /**
     *
     * @param jsonObject
     * @param isSubmit false:保存，true：提交
     * @return
     */
    private DataBean buildData(JSONObject jsonObject,boolean isSubmit){

        DataBean db=new DataBean();
        String id=jsonObject.getString("id");
        JSONArray jsonArray=jsonObject.getJSONArray("list");
        UserSessionUtil usu=(UserSessionUtil )jsonObject.get("usu");

        String outWarehouseId=null;
        String inWarehouseId=null;


        int status=10;
        Record warehouseMovementOrderR=null;
        if(id==null || "".equals(id)){
            id=UUIDTool.getUUID();
            db.isCreate=true;


            outWarehouseId=jsonObject.getString("outWarehouseId");
            inWarehouseId=jsonObject.getString("inWarehouseId");
            String reasonj=jsonObject.getString("reason");

            OrderNumberGenerator ong=new OrderNumberGenerator();

            warehouseMovementOrderR=new Record();
            warehouseMovementOrderR.set("id",id);
            warehouseMovementOrderR.set("out_warehouse_id",outWarehouseId);
            warehouseMovementOrderR.set("in_warehouse_id",inWarehouseId);
            warehouseMovementOrderR.set("order_number",ong.getWarehouseMovementOrderNumber());
            warehouseMovementOrderR.set("creater_id",usu.getUserId());
            warehouseMovementOrderR.set("modifier_id",usu.getUserId());
            warehouseMovementOrderR.set("create_time",datetime);
            warehouseMovementOrderR.set("modify_time",datetime);
        }else{
            db.isCreate=false;

            warehouseMovementOrderR=Db.findById("warehouse_movement_order",id);
            outWarehouseId=warehouseMovementOrderR.getStr("out_warehouse_id");
            inWarehouseId=warehouseMovementOrderR.getStr("in_warehouse_id");

            //回填数据
            jsonObject.put("outWarehouseId",outWarehouseId);
            jsonObject.put("inWarehouseId",inWarehouseId);

            int statusOfR=warehouseMovementOrderR.get("status");
            if(statusOfR==30){
                db.checked=false;
                db.message="已经提交的订单，不能再次修改或提交！";
                return db;

            }
            warehouseMovementOrderR.set("modifier_id",usu.getUserId());
            warehouseMovementOrderR.set("modify_time",datetime);

        }
        if(isSubmit){//如果是提交
            status=30;
            warehouseMovementOrderR.set("finish_time",datetime);

        }else{//如果是保存
            status=20;

        }
        warehouseMovementOrderR.set("status",status);






        /*
        从提交的数据中取出id，进行查询
         */
        List<String> idList=new ArrayList<>();
        for(Object obj:jsonArray) {
            JSONObject json = (JSONObject) obj;
            String idOfObj=json.getString("id");
            idList.add(idOfObj);
        }
        /*
        根据提交的原材料id，查询移出仓库的库存信息
         */
        SelectUtil selectUtil=new SelectUtil("select * from warehouse_stock");
        selectUtil.addWhere("and warehouse_id=?",outWarehouseId);
        selectUtil.in("and id in ",idList.toArray());
        List<Record> list=Db.find(selectUtil.toString(),selectUtil.getParameters());
        db.outWarehouseStockList=list;
        /*

         */
        List<Record> saveList=new ArrayList();
        int sort=1;
        for(Object obj:jsonArray){
            JSONObject jsonObjectOfJson = (JSONObject) obj;
            String idOfJson = jsonObjectOfJson.getString("id");
            int movementNum = jsonObjectOfJson.getInteger("movement_num");

            for(Record r:list) {
                String idOfR=r.getStr("id");
                String nameOfR=r.getStr("name");
                int warehouseStockNum=r.getInt("number");//库存数量
                if (idOfR.equals(idOfJson)) {
                    if(warehouseStockNum<movementNum){//如果库存数小于移库的数量，提示错误
                        db.checked=false;
                        String format="[%s]的库存数是%s，移出数是%s，移出数不能大于库存数！";
                        db.message=String.format(format,nameOfR,warehouseStockNum,movementNum);
                        return db;
                    }
                    Record warehouseMovementOrderMaterialR = new Record();
                    warehouseMovementOrderMaterialR.set("id", UUIDTool.getUUID());
                    warehouseMovementOrderMaterialR.set("warehouse_movement_order_id", id);
                    warehouseMovementOrderMaterialR.set("material_id", r.getStr("material_id"));
                    warehouseMovementOrderMaterialR.set("batch_code", r.getStr("batch_code"));
                    warehouseMovementOrderMaterialR.set("code", r.getStr("code"));
                    warehouseMovementOrderMaterialR.set("name", nameOfR);
                    warehouseMovementOrderMaterialR.set("pinyin", r.getStr("pinyin"));
                    warehouseMovementOrderMaterialR.set("attribute_1", r.getStr("attribute_1"));
                    warehouseMovementOrderMaterialR.set("attribute_2", r.getStr("attribute_2"));
                    warehouseMovementOrderMaterialR.set("type_1", r.getStr("type_1"));
                    warehouseMovementOrderMaterialR.set("type_2", r.getStr("type_2"));
                    warehouseMovementOrderMaterialR.set("number", movementNum);//移动库存数量
                    warehouseMovementOrderMaterialR.set("sort", sort);//排序
                    warehouseMovementOrderMaterialR.set("warehouse_stock_id", idOfR);

                    saveList.add(warehouseMovementOrderMaterialR);
                    sort++;
                    break;
                }
            }
        }


        db.warehouseMovementOrderR=warehouseMovementOrderR;
        db.warehouseMovementOrderMaterialList=saveList;
        return db;
    }


    /**
     * 提交
     * @param jsonObject
     * @throws Exception
     */
    @Before(Tx.class)
    public JsonHashMap submit(JSONObject jsonObject) throws Exception{
        JsonHashMap jhm=new JsonHashMap();
        String id=jsonObject.getString("id");

        DataBean db=buildData(jsonObject,true);
        if(!db.checked){
            jhm.putCode(0).putMessage(db.message);
            return jhm;
        }
        if(db.isCreate){

            Db.save("warehouse_movement_order",db.warehouseMovementOrderR);
        }else{
            Db.delete("delete from warehouse_movement_order_material where warehouse_movement_order_id=?",id);
            Db.update("warehouse_movement_order",db.warehouseMovementOrderR);
        }
        Db.batchSave("warehouse_movement_order_material",db.warehouseMovementOrderMaterialList,30);
        /*
         * 更新出库库存表
         */
        updateOutWarehouseStock(jsonObject,db);
        /*
        更新入库库存表
         */
        updateInWarehouseStock(jsonObject,db);
        jhm.putCode(1).putMessage("提交成功！");
        return jhm;
    }

    /**
     * 更新出库库存表
     */
    private void updateOutWarehouseStock(JSONObject jsonObject,DataBean db){
        String outWarehouseId=jsonObject.getString("outWarehouseId");
        Object[][] dataArray=new Object[db.warehouseMovementOrderMaterialList.size()][4];
        int i=0;
        for(Record r:db.warehouseMovementOrderMaterialList){
            String numberStr=r.getStr("number");
            String material_id=r.getStr("material_id");
            String batch_code=r.getStr("batch_code");
            int number= NumberUtils.parseInt(numberStr,0);
            dataArray[i][0]=number;
            dataArray[i][1]=material_id;
            dataArray[i][2]=batch_code;
            dataArray[i][3]=outWarehouseId;

            i++;
        }
        Db.batch("update warehouse_stock set number=number-? where material_id=? and batch_code=? and warehouse_id=?",dataArray,30);
    }
    /**
     * 更新入库库存表
     * 根据前台提交的数据，查询移入仓库的库存，如果存在该原材料，就做更新数量操作，
     * 如果不存在，就做添加记录操作
     */
    private void updateInWarehouseStock(JSONObject jsonObject,DataBean db){
//        String outWarehouseId=jsonObject.getString("outWarehouseId");
        String inWarehouseId=jsonObject.getString("inWarehouseId");
        JSONArray jsonArray=jsonObject.getJSONArray("list");
        /*
        从提交的数据中取出id，进行查询
         */
        List<String> idList=new ArrayList<>();
        for(Object obj:jsonArray) {
            JSONObject json = (JSONObject) obj;
            String idOfObj=json.getString("id");
            idList.add(idOfObj);
        }
        /*
        根据提交的原材料id，查询入库的库存信息
         */
        SelectUtil selectUtil=new SelectUtil("select * from warehouse_stock");
        selectUtil.addWhere("and warehouse_id=?",inWarehouseId);
        selectUtil.in("and id in ",idList.toArray());
        List<Record> list=Db.find(selectUtil.toString(),selectUtil.getParameters());

        /*
        封装需要添加到入库的原材料信息
         */
        List<Record> saveList=new ArrayList();
        /*
        移入的原材料，在入库仓库中已经存在，此时仅需要更新数量即可，将这些信息封装到该list中
         */
        List<Object[]> updteList=new ArrayList<>();

        for(Object obj:jsonArray) {
            JSONObject json = (JSONObject) obj;
            String idOfJson=json.getString("id");
            int movementNum = json.getInteger("movement_num");
            /*
            前台提交的数据，在移入仓库中存在，就更新数量，如果不存在，就从移出list中找出该原材料的信息
             */
            boolean has=false;
            for(Record r:list){
                String idOfR=r.getStr("id");
                if(idOfJson.equals(idOfR)){
                    String material_id=r.getStr("material_id");
                    String batch_code=r.getStr("batch_code");
                    Object[] dataArray=new Object[4];
                    dataArray[0]=movementNum;
                    dataArray[1]=material_id;
                    dataArray[2]=batch_code;
                    dataArray[3]=inWarehouseId;

                    updteList.add(dataArray);

                    has=true;
                    break;
                }
            }

            if(!has){
                for(Record r:db.outWarehouseStockList){
                    String idOfR=r.getStr("id");
                    if(idOfJson.equals(idOfR)){
                        r.set("id",UUIDTool.getUUID());
                        r.set("warehouse_id",inWarehouseId);
                        r.set("create_time",datetime);
                        r.set("number",movementNum);
                        saveList.add(r);
                        break;
                    }
                }

            }
        }

        Db.batchSave("warehouse_stock",saveList,30);

        Object[][] dataArray=new Object[updteList.size()][];
        int i=0;
        for(Object[] array:updteList){
            dataArray[i]=array;
        }
        Db.batch("update warehouse_stock set number=number+? where material_id=? and batch_code=? and warehouse_id=?",dataArray,30);
    }

    /**
     * 撤回提交
     * @param id
     * @param usu
     * @return
     */
    public JsonHashMap revokeSubmit(String id,UserSessionUtil usu){
        JsonHashMap jhm=new JsonHashMap();


        return jhm;
    }
    class DataBean{
        /**
         * 是否是新建订单
         * true：新建订单
         * false：更新订单
         */
        boolean isCreate=true;
        Record warehouseMovementOrderR;
        List<Record> warehouseMovementOrderMaterialList;
        /**
         * 移出原材料的库存信息
         */
        List<Record> outWarehouseStockList;
        /**
         * 校验标记
         * true：正常
         * false：不能继续执行
         */
        boolean checked=true;
        String message;
    }
}
