package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.StoreOrderReceiveSrv;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UnitConversion;
import com.utils.UserSessionUtil;
import easy.util.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接收门店订单
 */
public class StoreOrderReceiverCtrl extends BaseCtrl {
    static final String showDetailByIdSQL="SELECT a1.*,(case when b1.number is null then 0 end )as stock_num FROM ( SELECT a.id,a.code,( SELECT name FROM goods_attribute WHERE id=a.attribute_2) AS attribute_2_text,a.name,( SELECT name FROM goods_unit WHERE id=a.unit) AS unit_text,a.send_num AS order_material_num,b.send_num as receive_num,a.material_id,a.sort,a.store_order_id FROM store_order_material a,warehouse_out_order_material b,warehouse_out_order c WHERE a.store_order_id=c.store_order_id AND c.id=b.warehouse_out_order_id AND a.material_id=b.material_id) a1 LEFT JOIN store_stock b1 ON a1.material_id=b1.material_id where a1.store_order_id=? ORDER BY a1.sort  ";

    /**
     * 根据订单id查询详细信息
     * 根据出库订单id显示出库详细信息
     *
     *
    返回list中的数据有：
    id: '原材料id-批号',
    tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
    label: '原材料名称(批号)',
    search_text: '原材料名称-批号-拼音头', // 搜索条件
    material_id: '原材料id',
    name: '原材料名称',
    code: '编号',
    attribute_2_text: '规格',
    unit_text: '单位',
    want_num: '订货数量',
    security_stock: '安存数量',
    batch_code: '批号',
    send_number: '出货数量',
    warehouseStockNumber: '库存数量',
    isEdit: true, // 三级树中加就行
    warehouse_stock_id:''//库存（warehouse_stock）id。子数组第一个元素的warehouse_stock_id，赋给外部数组

     */
    public void showDetailById(){
        String id=getPara("id");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String storeId=usu.getUserBean().getDeptId();

        JsonHashMap jhm=new JsonHashMap();

        List<Map> reList=new ArrayList();
        Record warehouseOutOrderR= Db.findById("warehouse_out_order",id);
//        String statusOfWarehouseOutOrderR=warehouseOutOrderR.getStr("status");

        /*
        查询该订单的出库原材料
         */
        String sql="select a.*,b.want_num,b.id as store_order_material_id from warehouse_out_order_material_detail a,store_order_material b where a.store_order_id=b.store_order_id and a.material_id=b.material_id and b.store_order_id=? order by a.material_id,a.batch_code ";
        List<Record> warehouseOutOrderMaterialDetailList=Db.find(sql,id);
        /*
        根据上面的记录查询库存
         */
        List<Record> stockList=queryStock(warehouseOutOrderMaterialDetailList,storeId);

        String lastMaterialId=null;
        /*
        获取当前出库订单中原材料的安存数量，并放入到该条记录中
         */
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialDetailList){
            String storeOrderMaterialId=warehouseOutOrderMaterialR.getStr("store_order_material_id");
            String materialId=warehouseOutOrderMaterialR.getStr("material_id");
            String batchCode=warehouseOutOrderMaterialR.getStr("batch_code");
            String name=warehouseOutOrderMaterialR.getStr("name");
//            String pinyin=warehouseOutOrderMaterialR.getStr("pinyin");
            String code=warehouseOutOrderMaterialR.getStr("code");
//            String attribute2=warehouseOutOrderMaterialR.getStr("attribute_2");
            String wantNum=warehouseOutOrderMaterialR.getStr("want_num");
            int sendNumber=warehouseOutOrderMaterialR.get("send_num");//此处用的是warehouse_out_order_material_detail表的send_num
            String outUnit=warehouseOutOrderMaterialR.getStr("out_unit");//出库单位
            String boxAttr=warehouseOutOrderMaterialR.getStr("box_attr");//装箱单位
            Object boxAttrNumObj=warehouseOutOrderMaterialR.get("box_attr_num");//大单位换算成箱的数值
            String unitBig=warehouseOutOrderMaterialR.getStr("unit_big");//大单位
            String unit=warehouseOutOrderMaterialR.getStr("unit");//最小单位
            Object unitNumObj=warehouseOutOrderMaterialR.get("unit_num");//小单位换算成大单位的数值

            int boxAttrNum= NumberUtils.parseInt(boxAttrNumObj,0);
            int unitNum= NumberUtils.parseInt(unitNumObj,0);

            int stockNum=getStockNumberByMaterialId(materialId,stockList);

            Map map=new HashMap();
            map.put("id",storeOrderMaterialId);
            map.put("material_id",materialId);
            map.put("name",name);
            map.put("code",code);
            map.put("out_unit",outUnit);
            map.put("want_num",wantNum);
            map.put("send_number",sendNumber);
            map.put("isEdit",true);
            map.put("stock_num",stockNum);
            map.put("unit",unit);

            try {
                String attribute2 = UnitConversion.getAttrByOutUnit(unit, unitNum, unitBig, boxAttrNum, boxAttr, outUnit);
                map.put("attribute_2_text", attribute2);
            }catch (Exception e){
                e.printStackTrace();
            }

            if(materialId.equals(lastMaterialId)){//如果与上一个相同
                /*
                如果与上一个相同，取出reList最后一个元素，将上面的map，放入到该元素中
                 */
                Map elementMap=reList.get(reList.size()-1);
                List warehouseStockMapList=(List)elementMap.get("orderInfo");
                warehouseStockMapList.add(map);
            }else{//如果与上一个不同
                Map elementMap=new HashMap();
                elementMap.putAll(map);

                elementMap.put("id",storeOrderMaterialId);

                List warehouseStockMapList=new ArrayList();
                warehouseStockMapList.add(map);

                elementMap.put("orderInfo",warehouseStockMapList);

                reList.add(elementMap);
            }

            lastMaterialId=materialId;
        }

        jhm.putCode(1).put("list",reList);
        renderJson(jhm);
    }

    /**
     * 查询库存
     * @param list
     */
    private List<Record> queryStock(List<Record> list,String storeId){
        List materialIdList=new ArrayList();
        for(Record r:list){
            String materialId=r.get("material_id");
            if(!materialIdList.contains(materialIdList)) {
                materialIdList.add(materialId);
            }
        }
        SelectUtil selectUtil=new SelectUtil("select material_id,number from store_stock ");
        selectUtil.addWhere(" and store_id=?",storeId);
        selectUtil.in("and material_id in ",materialIdList.toArray());
        List<Record> storeStockList=Db.find(selectUtil.toString(),selectUtil.getParameters());
        return storeStockList;
    }
    private int getStockNumberByMaterialId(String materialId,List<Record> stockList){
        int num=0;
        for(Record r:stockList){
            String materialIdDb=r.get("material_id");
            int number=r.get("number");
            if(materialId.equals(materialIdDb)){
                num=number;
                break;
            }
        }

        return num;
    }

    /**
     * 查询接收订单
     * 必须条件：当前登录人所在门店id
     */
    @Override
    public void query() {
        String orderCode=getPara("order_code");
        String wantDate=getPara("wantDate");
        String arriveDate=getPara("arriveDate");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();

        String sql="select b.id as store_order_id,b.order_number as store_order_number,substr(b.create_time,1,16) as create_time_short,b.arrive_date,substr(b.out_time,1,16) as out_time from  store_order b  ";
        try{
            SelectUtil selectUtil=new SelectUtil(sql);
//            selectUtil.like("and order_number like ?",SelectUtil.NONE,orderCode,SelectUtil.WILDCARD_PERCENT);
//            selectUtil.addWhere("and want_date=? ",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wantDate);
//            selectUtil.addWhere("and arrive_date=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,arriveDate);
            selectUtil.addWhere("and b.status=?",40);
            selectUtil.addWhere("and b.store_id=?",usu.getUserBean().getDeptId());
            selectUtil.order("order by b.arrive_date,b.out_time");
            String sqlExe=selectUtil.toString();
            List<Record> list=Db.find(sqlExe,selectUtil.getParameters());
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 接收入库
     */
    public void accept(){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String storeOrderId=jsonObject.getString("id");
            JSONArray jsonArray=jsonObject.getJSONArray("list");

            StoreOrderReceiveSrv service=enhance(StoreOrderReceiveSrv.class);
            jhm=service.accept(storeOrderId,jsonArray,usu);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }
}
