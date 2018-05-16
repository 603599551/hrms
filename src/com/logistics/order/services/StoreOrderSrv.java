package com.logistics.order.services;

import com.common.services.OrderNumberGenerator;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.*;

/**
 * 物流处理门店订单
 */
public class StoreOrderSrv {
    private UserSessionUtil usu;
    /**
     * 生成出库单
     * 要根据原材料所在仓库，分别生成出库单
     *
     * 生成记录保存在store_order表，store_order_material表
     * 不向warehouse_out_order_material_detail插入记录
     */
    @Before(Tx.class)
    public JsonHashMap buildOutWarehouse(String storeOrderId,UserSessionUtil usu) throws Exception{
        this.usu=usu;
        JsonHashMap jhm=new JsonHashMap();
        try{
            Record storeOrderRecord= Db.findById("store_order",storeOrderId);
            if(storeOrderRecord==null){
                jhm.putCode(0).putMessage("查无此订单信息！");
                return jhm;
            }
            String status=storeOrderRecord.getStr("status");
            String orderNumber=storeOrderRecord.getStr("order_number");
            if("10".equals(status)){
                jhm.putCode(0).putMessage("请先接收该订单，然后生成出库单！");
                return jhm;
            }
            if("30".equals(status)){
                jhm.putCode(0).putMessage("已经生成出库单，不能重复生成！");
                return jhm;
            }
            if("40".equals(status)){
                jhm.putCode(0).putMessage("已经出库，不能生成！");
                return jhm;
            }
            if("50".equals(status)){
                jhm.putCode(0).putMessage("已经完成此订单，不能生成！");
                return jhm;
            }
            if("100".equals(status)){
                jhm.putCode(0).putMessage("已经关闭此订单，不能生成！");
                return jhm;
            }
            /*
            查询门店订单表（原材料明细表）
             */
//            String sql="select a.store_id,a.material_id,a.code,a.name,a.want_num,b.warehouse_id,b.batch_code,b.number as warehouse_stock_num from store_order_material a left join warehouse_stock b on a.material_id=b.material_id where store_order_id=? order by sort";
            /*
             * 库存表warehouse_stock，同一原材料有不同的批号，所以此sql语句要根据material_id过滤重复的记录
             */
            String sql="select a.store_id,a.material_id,a.code,a.name,a.want_num,b.id as warehouse_stock_id,b.warehouse_id from store_order_material a left join (select * from warehouse_stock group by material_id )  b on a.material_id=b.material_id where store_order_id=? order by sort";
            List<Record> list=Db.find(sql,storeOrderId);
            Map resultMap= process(storeOrderRecord,list);

            Db.update("update store_order set status=? where id=?","30",storeOrderId);
            jhm.putCode(1).putMessage("生成成功！");
        }catch (Exception e){
            throw e;
        }
        return jhm;
    }

    /**
     * 根据记录，查询原材料所在仓库，生成各个仓库的订单
     * @param storeOrderRecord
     * @param recordList
     * @return
     */
    private Map process(Record storeOrderRecord, List<Record> recordList){
        Map reMap=new HashMap();

        List<String> warehouseIdList=new ArrayList();
        Map<String,List<Record>> warehouseOutOrderMaterialListMap=new HashMap();

        String storeId=null;
        for(Record r:recordList){
            String materialId=r.getStr("material_id");
            Object warehouse_stock_id=r.get("warehouse_stock_id");//库存数量
            storeId=r.get("store_id");//门店
            if(warehouse_stock_id==null){//库存中没有该原材料

            }else{
                /*
                 * 取出仓库id，并放入list中，便于后面生成各个仓库的订单表
                 */
                String warehouseId=r.getStr("warehouse_id");
                if(!warehouseIdList.contains(warehouseId)){
                    warehouseIdList.add(warehouseId);
                }
                /*
                生成出库明细的record，并放入的集合中
                 */
                List<Record> warehouseOutOrderMaterialList=warehouseOutOrderMaterialListMap.get(warehouseId);
                if(warehouseOutOrderMaterialList==null){
                    warehouseOutOrderMaterialList=new ArrayList<>();
                    warehouseOutOrderMaterialListMap.put(warehouseId,warehouseOutOrderMaterialList);
                }

                Record warehouseOutOrderMaterialR=buildWarehouseOutOrderMaterialRecord(r);
                warehouseOutOrderMaterialList.add(warehouseOutOrderMaterialR);
            }
        }
        doSave(storeOrderRecord,warehouseIdList,warehouseOutOrderMaterialListMap);
        return reMap;
    }

    /**
     * 构建出库订单明细表（原材料）
     * @param r
     * @return
     */
    private Record buildWarehouseOutOrderMaterialRecord(Record r){
        String uuid= UUIDTool.getUUID();

        String warehouseId=r.getStr("warehouse_id");
        String storeId=r.getStr("store_id");
        String materialId=r.getStr("material_id");
        String code=r.getStr("code");
        String name=r.getStr("name");
        String want_num=r.getStr("want_num");
        String unit=r.getStr("unit");

        /*
        构建出库订单明细表（原材料）
         */
        Record warehouseOutOrderMaterialR=new Record();
        warehouseOutOrderMaterialR.set("id",uuid);
        warehouseOutOrderMaterialR.set("warehouse_id",warehouseId);
        warehouseOutOrderMaterialR.set("store_id",storeId);
        warehouseOutOrderMaterialR.set("material_id",materialId);
        warehouseOutOrderMaterialR.set("code",code);
        warehouseOutOrderMaterialR.set("name",name);
        warehouseOutOrderMaterialR.set("send_num",want_num);
        warehouseOutOrderMaterialR.set("status","1");//1表示门店订货
        warehouseOutOrderMaterialR.set("unit",unit);
        warehouseOutOrderMaterialR.set("want_num",want_num);

        return warehouseOutOrderMaterialR;
    }

    /**
     * 执行保存操作
     * @param storeOrderRecord
     * @param warehouseIdList
     * @param warehouseOutOrderMaterialListMap
     */
//    private void doSave(Record storeOrderRecord,List<String> warehouseIdList,Map<String,List<Record>> warehouseOutOrderMaterialListMap){
//        String datetime= DateTool.GetDateTime();
//        String storeOrderId=storeOrderRecord.getStr("id");
//        String storeId=storeOrderRecord.getStr("store_id");
//        String storeOrderNumber=storeOrderRecord.getStr("order_number");
//        String city=storeOrderRecord.getStr("city");
//        String type=storeOrderRecord.getStr("type");
//        for(String warehouseId :warehouseIdList){
//            String warehouseOutOrderUUID=UUIDTool.getUUID();
//            String outOrderNumber=buildOrderNumber(warehouseId);
//
//            Record warehouseOutOrderR=new Record();
//            warehouseOutOrderR.set("id",warehouseOutOrderUUID);
//            warehouseOutOrderR.set("store_id",storeId);
//            warehouseOutOrderR.set("warehouse_id",warehouseId);
//            warehouseOutOrderR.set("order_number",outOrderNumber);
//            warehouseOutOrderR.set("out_time",datetime);
//            warehouseOutOrderR.set("store_order_id",storeOrderId);
//            warehouseOutOrderR.set("store_order_number",storeOrderNumber);
//            warehouseOutOrderR.set("city",city);
//            warehouseOutOrderR.set("creater_id",usu.getUserId());
//            warehouseOutOrderR.set("create_time",datetime);
//            warehouseOutOrderR.set("status",10);//出库单状态：新建
//            warehouseOutOrderR.set("type",type);
//            /*
//            保存出库订单
//             */
//            Db.save("warehouse_out_order",warehouseOutOrderR);
//
//            /*
//            保存出库订单明细表（原材料）
//             */
//            List<Record> warehouseOutOrderMaterialList=(List<Record>)warehouseOutOrderMaterialListMap.get(warehouseId);
//            for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
//                warehouseOutOrderMaterialR.set("warehouse_out_order_id",warehouseOutOrderUUID);
//                Db.save("warehouse_out_order_material",warehouseOutOrderMaterialR);
//            }
//        }
//    }
    private void doSave(Record storeOrderRecord,List<String> warehouseIdList,Map<String,List<Record>> warehouseOutOrderMaterialListMap){
        String datetime= DateTool.GetDateTime();
        String storeOrderId=storeOrderRecord.getStr("id");
        String storeId=storeOrderRecord.getStr("store_id");
        String storeOrderNumber=storeOrderRecord.getStr("order_number");
        String city=storeOrderRecord.getStr("city");
        String type=storeOrderRecord.getStr("type");

        List<Record> saveWarehouseOutOrderList=new ArrayList();
        List<Record> saveWarehouseOutOrderMaterialList=new ArrayList();
        for(String warehouseId :warehouseIdList){
            String warehouseOutOrderUUID=UUIDTool.getUUID();
            String outOrderNumber=buildOrderNumber(warehouseId);

            Record warehouseOutOrderR=new Record();
            warehouseOutOrderR.set("id",warehouseOutOrderUUID);
            warehouseOutOrderR.set("store_id",storeId);
            warehouseOutOrderR.set("warehouse_id",warehouseId);
            warehouseOutOrderR.set("order_number",outOrderNumber);
            warehouseOutOrderR.set("out_time",datetime);
            warehouseOutOrderR.set("store_order_id",storeOrderId);
            warehouseOutOrderR.set("store_order_number",storeOrderNumber);
            warehouseOutOrderR.set("city",city);
            warehouseOutOrderR.set("creater_id",usu.getUserId());
            warehouseOutOrderR.set("create_time",datetime);
            warehouseOutOrderR.set("status",10);//出库单状态：新建
            warehouseOutOrderR.set("type",type);
            /*
            保存出库订单
             */
//            Db.save("warehouse_out_order",warehouseOutOrderR);
            saveWarehouseOutOrderList.add(warehouseOutOrderR);
            /*
            保存出库订单明细表（原材料）
             */
            List<Record> warehouseOutOrderMaterialList=(List<Record>)warehouseOutOrderMaterialListMap.get(warehouseId);
            for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
                warehouseOutOrderMaterialR.set("warehouse_out_order_id",warehouseOutOrderUUID);
//                Db.save("warehouse_out_order_material",warehouseOutOrderMaterialR);
                saveWarehouseOutOrderMaterialList.add(warehouseOutOrderMaterialR);
            }
        }

        Db.batchSave("warehouse_out_order",saveWarehouseOutOrderList,50);
        Db.batchSave("warehouse_out_order_material",saveWarehouseOutOrderMaterialList,50);

    }
    private String buildOrderNumber(String warehouseId){
        OrderNumberGenerator service=new OrderNumberGenerator();
        return service.getOutWarehouseOrderNumber();
    }
}
