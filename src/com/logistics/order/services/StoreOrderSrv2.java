package com.logistics.order.services;

import com.common.services.OrderNumberGenerator;
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

import java.util.*;

/**
 * 物流处理门店订单
 */
public class StoreOrderSrv2 {
    private UserSessionUtil usu;
    private Record storeOrderRecord;
    private OrderNumberGenerator ong=new OrderNumberGenerator();
    private String datetime= DateTool.GetDateTime();
    /**
     * 生成出库单
     * 根据门店订单生成出库单，原材料所在不同的仓库，要分别生成不同的出库单，出库时，要先出批号较早的原材料，
     * 并且要计算提货单位，假如提货单位是箱，那么最少要出库1箱
     *
     * 生成记录保存在store_order表，store_order_material表
     * 不向warehouse_out_order_material_detail插入记录
     */
    @Before(Tx.class)
    public JsonHashMap buildOutWarehouse(String storeOrderId,UserSessionUtil usu) throws Exception{
        System.out.println(this.getClass().getSimpleName());
        this.usu=usu;
        JsonHashMap jhm=new JsonHashMap();
        try{
            storeOrderRecord= Db.findById("store_order",storeOrderId);
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
                jhm.putCode(0).putMessage("已经出库，不能生成出库单！");
                return jhm;
            }
            if("50".equals(status)){
                jhm.putCode(0).putMessage("已经完成此订单，不能生成出库单！");
                return jhm;
            }
            if("110".equals(status)){
                jhm.putCode(0).putMessage("门店已经撤销，不能生成出库单！");
                return jhm;
            }
            if("120".equals(status)){
                jhm.putCode(0).putMessage("物流已经退回此订单，不能生成出库单！");
                return jhm;
            }
            /*
             * 根据订单id查询store_order_material表
             */
            String storeOrderMaterialSQL="select * from store_order_material where store_order_id=? order by sort ";
            List<Record> storeOrderMaterialList=Db.find(storeOrderMaterialSQL,storeOrderId);

            /*
            根据门店订单的原材料查询库存（warehouse_stock）记录
             */
            List<Record> warehouseStockList= queryWarehouseStockList(storeOrderMaterialList);

            /*
            遍历门店订单中的每一个原材料，对比库存list，返回该原材料需要的库存原材料的批号、数量，并构建成record对象，放入list中
             */
            List<Record> allWarehouseMaterialDetailList=new ArrayList<>();
            for(Record storeOrderMaterialR:storeOrderMaterialList){
                String materialId=storeOrderMaterialR.getStr("material_id");
                List<Record> warehouseMaterialDetailList=process(storeOrderMaterialR,warehouseStockList);
                allWarehouseMaterialDetailList.addAll(warehouseMaterialDetailList);
            }

            List<Record> warehouseOutOrderList=buildWarehouseOutOrderList(allWarehouseMaterialDetailList, usu);

            int[] aArray=Db.batchSave("warehouse_out_order",warehouseOutOrderList,10);
            int[] iArray=Db.batchSave("warehouse_out_order_material_detail",allWarehouseMaterialDetailList,30);

            Db.update("update store_order set status=? where id=?","30",storeOrderId);
            jhm.putCode(1).putMessage("生成成功！");
        }catch (Exception e){
            throw e;
        }
        return jhm;
    }
    /*
    将allWarehouseMaterialDetailList集合的数据，根据所在仓库，生成不同的仓库订单
    并回填将allWarehouseMaterialDetailList集合的数据集合的warehouse_out_order_id字段，该字段是出库订单id，只有生成订单时才会有
     */
    private List<Record> buildWarehouseOutOrderList(List<Record> allWarehouseMaterialDetailList, UserSessionUtil usu){
        Map<String,Record> warehouseOutOrderMap=new HashMap<>();//key是warehouse_id，value是ware_house_out_order的record
        for(Record r:allWarehouseMaterialDetailList){
            String warehouseId=r.getStr("warehouse_id");

            Record warehouseOutOrderR=warehouseOutOrderMap.get(warehouseId);
            /*
            第一次遇到该仓库的原材料，要构建ware_house_out_order的record，ware_house_out_order的List
             */
            if(warehouseOutOrderR==null){
                warehouseOutOrderR=buildWarehouseOutOrder(r);
                warehouseOutOrderMap.put(warehouseId,warehouseOutOrderR);
            }
            String warehouseOutOrderId=warehouseOutOrderR.getStr("id");
            r.set("warehouse_out_order_id",warehouseOutOrderId);//回填缺失的字段
            r.set("warehouse_out_order_number",warehouseOutOrderR.get("order_number"));//回填缺失的字段
        }
        List<Record> reList=new ArrayList<>();
        Iterator<Map.Entry<String,Record>> it=warehouseOutOrderMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Record> en=it.next();
            en.getValue().set("store_color", usu.getUserBean().get("store_color"));
            reList.add(en.getValue());
        }
        return reList;
    }

    /**
     * 构建warehouse_out_order表记录
     * @param warehouseMaterialDetail
     * @return
     */
    private Record buildWarehouseOutOrder(Record warehouseMaterialDetail){

        Record warehouseOutOrderR=new Record();
        warehouseOutOrderR.set("id",UUIDTool.getUUID());
        warehouseOutOrderR.set("store_id",warehouseMaterialDetail.get("store_id"));
        warehouseOutOrderR.set("warehouse_id",warehouseMaterialDetail.getStr("warehouse_id"));
        warehouseOutOrderR.set("order_number",ong.getOutWarehouseOrderNumber());
        warehouseOutOrderR.set("store_order_id",storeOrderRecord.get("id"));
        warehouseOutOrderR.set("store_order_number",storeOrderRecord.get("order_number"));
        warehouseOutOrderR.set("city",storeOrderRecord.get("city"));
        warehouseOutOrderR.set("creater_id",usu.getUserId());
        warehouseOutOrderR.set("create_time",datetime);
        warehouseOutOrderR.set("status",10);//出库单状态：新建
        warehouseOutOrderR.set("type",storeOrderRecord.get("type"));

        return warehouseOutOrderR;
    }
    /**
    根据门店订单的原材料查询库存（warehouse_stock）记录
     */
    private List<Record> queryWarehouseStockList(List<Record> storeOrderMaterialList){
        /*
         * 把该订单原材料的material_id取出来，放入list中
         */
        List materialIdList=new ArrayList();
        for(Record storeOrderMaterialR:storeOrderMaterialList){
            String materialId=storeOrderMaterialR.getStr("material_id");
            materialIdList.add(materialId);
        }
        /*
        根据门店订单的原材料查询库存（warehouse_stock）记录
         */
        SelectUtil selectUtil=new SelectUtil("select * from warehouse_stock ");
        selectUtil.in(" and material_id in",materialIdList.toArray());
        selectUtil.addWhere("and number>?",0);
        selectUtil.order("order by material_id,batch_code,id");
        List<Record> warehouseStockList=Db.find(selectUtil.toString(),selectUtil.getParameters());
        return warehouseStockList;
    }


    /**
     * 将门店想要的原材料数量（want_num，是最小单位），换算成提货单位，不能整除的+1
     * @param storeOrderMaterialR
     * @return
     */
    private int wantNumFromUnitToOutUnit(Record storeOrderMaterialR){

        int wantNum=storeOrderMaterialR.getInt("want_num");//门店想要的量，单位是最小单位
        Object boxAttrNumObj=storeOrderMaterialR.get("box_attr_num");
        Object unitNumObj=storeOrderMaterialR.get("unit_num");
        String unit=storeOrderMaterialR.getStr("unit");//小单位
        String unitBig=storeOrderMaterialR.getStr("unit_big");//大单位
        String boxAttr=storeOrderMaterialR.getStr("box_attr");//装箱规格单位
        String outUnit=storeOrderMaterialR.getStr("out_unit");//提货单位

        int boxAttrNum=NumberUtils.parseInt(boxAttrNumObj,-1);
        int unitNum=NumberUtils.parseInt(unitNumObj,-1);

        return UnitConversion.smallUnit2outUnit(wantNum,unit,unitBig,unitNum,boxAttr,boxAttrNum,outUnit);
    }

    /**
     *
     * 传入门店订单中的一个原材料，和库存list，返回该原材料需要的库存原材料的批号、数量
     * 返回数据类型是record，准备保存到warehouse_out_order_material_detail表
     * 根据门店订单计算需要库存原材料的批号、数量、体货箱
     *
     * @param storeOrderMaterialR 门店订单中的原材料
     * @param warehouseStockList 库存原材料，此处的list，必须按照material_id,batch_code排序
     */
    private List<Record> process(Record storeOrderMaterialR,List<Record> warehouseStockList){
        String materialId=storeOrderMaterialR.getStr("material_id");

        /*
        将门店想要的原材料数量（want_num，是最小单位），换算成提货单位，不能整除的+1
        换算后，门店想要数量就是发货数，单位是：提货单位。页面回显用
         */
        int wantNumOutUnit=wantNumFromUnitToOutUnit(storeOrderMaterialR);
        int sum=0;
        int lastSum=0;
        List<Record> warehouseMaterialDetailList=new ArrayList<>();

        /*
        从库存list中取出记录，与门店订单原材料对比，如果material_id相同，构建warehouse_out_order_material_detail记录
        并放入到list中
         */
        for(Record warehouseStockR:warehouseStockList){
            String materialIdOfWarehouseStockR=warehouseStockR.getStr("material_id");
            if(materialId.equals(materialIdOfWarehouseStockR)){
                //库存数量
                int numberOfWarehouseStockR=warehouseStockR.getInt("number");
                lastSum=sum;
                sum=sum+numberOfWarehouseStockR;
                if(wantNumOutUnit<=sum){
                    if(warehouseMaterialDetailList.isEmpty()){//如果是第一次执行
                        Record warehouseMaterialDetail = buildWarehouseMaterialDetail(storeOrderMaterialR,warehouseStockR, wantNumOutUnit);
                        warehouseMaterialDetailList.add(warehouseMaterialDetail);
                    }else {//如果不是第一次执行
                        Record warehouseMaterialDetail = buildWarehouseMaterialDetail(storeOrderMaterialR,warehouseStockR, wantNumOutUnit-lastSum);
                        warehouseMaterialDetailList.add(warehouseMaterialDetail);
                    }
                    break;
                }else{
                    Record warehouseMaterialDetail=buildWarehouseMaterialDetail(storeOrderMaterialR,warehouseStockR,numberOfWarehouseStockR);
                    warehouseMaterialDetailList.add(warehouseMaterialDetail);
                }
            }
        }


        return warehouseMaterialDetailList;
    }

    /**
     * 根据传入的数据构建warehouse_material_detail表的record对象
     * 此对象缺少字段：warehouse_out_order_id，warehouse_out_order_number、warehouse_out_order_material_id（废弃），sort
     * @param storeOrderMaterialR
     * @param warehouseStockR
     * @param sendNum
     * @return
     */
    private Record buildWarehouseMaterialDetail(Record storeOrderMaterialR,Record warehouseStockR,int sendNum){
        Record r=new Record();
        r.set("id", UUIDTool.getUUID());
        r.set("warehouse_stock_id", warehouseStockR.get("id"));
        r.set("warehouse_id", warehouseStockR.get("warehouse_id"));
        r.set("store_order_number", storeOrderRecord.get("order_number"));
        r.set("store_order_id", storeOrderRecord.get("id"));
        r.set("store_id", storeOrderRecord.get("store_id"));
        r.set("material_id", warehouseStockR.get("material_id"));
        r.set("batch_code", warehouseStockR.get("batch_code"));
        r.set("code", warehouseStockR.get("code"));
        r.set("name", warehouseStockR.get("name"));
        r.set("pinyin", warehouseStockR.get("pinyin"));
        r.set("yield_rate", warehouseStockR.get("yield_rate"));
        r.set("purchase_price", warehouseStockR.get("purchase_price"));
        r.set("balance_price", warehouseStockR.get("balance_price"));
        r.set("wm_type", warehouseStockR.get("wm_type"));
        r.set("attribute_1", warehouseStockR.get("attribute_1"));
        r.set("attribute_2", warehouseStockR.get("attribute_2"));
        r.set("unit", warehouseStockR.get("unit"));
        r.set("type_1", warehouseStockR.get("type_1"));
        r.set("type_2", warehouseStockR.get("type_2"));
        r.set("send_num", sendNum);
        r.set("status", "1");
        r.set("unit_num", warehouseStockR.get("unit_num"));
        r.set("unit_big", warehouseStockR.get("unit_big"));
        r.set("box_attr_num", warehouseStockR.get("box_attr_num"));
        r.set("box_attr", warehouseStockR.get("box_attr"));
        r.set("out_unit", warehouseStockR.get("out_unit"));
        r.set("out_price", warehouseStockR.get("out_price"));
        r.set("shelf_life_num", warehouseStockR.get("shelf_life_num"));
        r.set("shelf_life_unit", warehouseStockR.get("shelf_life_unit"));
        r.set("storage_condition", warehouseStockR.get("storage_condition"));
        r.set("security_time", warehouseStockR.get("security_time"));
        r.set("order_type", warehouseStockR.get("order_type"));
        r.set("is_out_unit", warehouseStockR.get("is_out_unit"));
        r.set("model", warehouseStockR.get("model"));
        r.set("size", warehouseStockR.get("size"));
        r.set("brand", warehouseStockR.get("brand"));

        return r;
    }

//    /**
//     * 根据门店订单计算需要库存原材料的批号、数量、体货箱
//     * @param storeOrderMaterialR 门店订单中的原材料
//     * @param warehouseStockList 库存原材料
//     */
//    private void process(Record storeOrderMaterialR,List<Record> warehouseStockList){
//        String materialId=storeOrderMaterialR.getStr("material_id");
//        int wantNum=storeOrderMaterialR.getInt("want_num");//门店想要的量，单位是最小单位
//        int boxAttrNum=storeOrderMaterialR.getInt("box_attr_num");
//        int unitNum=storeOrderMaterialR.getInt("unit_num");
//        String unitBig=storeOrderMaterialR.getStr("unit_big");//大单位
//        String boxAttr=storeOrderMaterialR.getStr("box_attr");//装箱规格单位
//        String outUnit=storeOrderMaterialR.getStr("out_unit");//提货单位
//
//        /*
//        最少提货数是1（单位是提货单位），从提货单位换算成最小单位
//        如果提货单位是装箱单位，那么提货单位换算成最小单位是：numKey=boxAttrNum*unitNum
//        如果提货单位是大单位，那么提货单位换算成最小单位是：numKey=unitNum
//         */
////        int numMinUnit=0;//最少提货数量（最小单位）
////        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
////            numMinUnit=boxAttrNum*unitNum;
////        }
////        if(StringUtils.isNotBlank(unitBig) && outUnit.equals(unitBig)){
////            numMinUnit=unitNum;
////        }
//        /*
//        将门店想要的数量，换算成提货单位，不能整除的+1
//         */
//        int wantNumOutUnit=0;
//        if(StringUtils.isNotBlank(boxAttr) && outUnit.equals(boxAttr)){//如果“提货单位”与“装箱规格单位”相同
//            wantNumOutUnit=(int)Math.ceil((double)wantNum/(double)(boxAttrNum*unitNum));
//        }else if(outUnit.equals(unitBig)){
//            wantNumOutUnit=(int)Math.ceil((double)wantNum/(double)unitNum);
//        }
//        /*
//        计算出库数量（此处数量的单位是提货单位）
//        如果门店想要的数量（此处换算成提货单位），小于等于1个提货数量，那么出库数是1（此处是提货单位）
//        如果门店想要的数量（此处换算成提货单位），大于1个提货数量，那么出库数是wantNumOutUnit（门店想要的数量，已经换算成提货单位）
//         */
//        int sendNumOutUnit=0;//出库数，单位是：提货单位。页面回显用
//        if(wantNumOutUnit<=1){
//            sendNumOutUnit=1;
//        }else{
//            sendNumOutUnit=wantNumOutUnit;
//        }
//
//        for(Record warehouseStockR:warehouseStockList){
//            String materialIdOfWarehouseStockR=warehouseStockR.getStr("material_id");
//            if(materialId.equals(materialIdOfWarehouseStockR)){
//                int numberOfWarehouseStockR=storeOrderMaterialR.getInt("number");
//
//            }
//        }
//
//    }

}
