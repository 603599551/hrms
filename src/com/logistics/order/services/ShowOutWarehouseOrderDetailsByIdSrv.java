package com.logistics.order.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.stock.services.SecurityStockService;
import easy.util.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据出库订单id显示出库详细信息
 */
public class ShowOutWarehouseOrderDetailsByIdSrv {
    /**
     * 根据出库订单id显示出库详细信息
     * @param id
     * @return
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
    public List<Map> showDetailsById(String id){
        List<Map> reList=new ArrayList();
        Record warehouseOutOrderR= Db.findById("warehouse_out_order",id);
//        String statusOfWarehouseOutOrderR=warehouseOutOrderR.getStr("status");

        /*
        查询该订单的出库原材料
         */
        String sql="select a.*,b.want_num,c.number from warehouse_out_order_material_detail a inner join store_order_material b on a.material_id=b.material_id and a.store_order_id=b.store_order_id left join warehouse_stock c on a.warehouse_stock_id=c.id where a.warehouse_out_order_id=? order by material_id,batch_code ";
        List<Record> warehouseOutOrderMaterialDetailList=Db.find(sql,id);

        //获取安存
        Map securityStock= SecurityStockService.getSecurityStockMap();

        String lastMaterialId=null;
        /*
        获取当前出库订单中原材料的安存数量，并放入到该条记录中
         */
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialDetailList){
            String materialId=warehouseOutOrderMaterialR.getStr("material_id");
            String batchCode=warehouseOutOrderMaterialR.getStr("batch_code");
            String name=warehouseOutOrderMaterialR.getStr("name");
            String pinyin=warehouseOutOrderMaterialR.getStr("pinyin");
            String code=warehouseOutOrderMaterialR.getStr("code");
//            String attribute2=warehouseOutOrderMaterialR.getStr("attribute_2");
            String wantNum=warehouseOutOrderMaterialR.getStr("want_num");
            String sendNumber=warehouseOutOrderMaterialR.getStr("send_num");
            int warehouseStockNumber=warehouseOutOrderMaterialR.get("number");
            String warehouseStockId=warehouseOutOrderMaterialR.getStr("warehouse_stock_id");
            String outUnit=warehouseOutOrderMaterialR.getStr("out_unit");//出库单位
            String boxAttr=warehouseOutOrderMaterialR.getStr("box_attr");//装箱单位
            Object boxAttrNumObj=warehouseOutOrderMaterialR.get("box_attr_num");//大单位换算成箱的数值
            String unitBig=warehouseOutOrderMaterialR.getStr("unit_big");//大单位
            String unit=warehouseOutOrderMaterialR.getStr("unit");//最小单位
            Object unitNumObj=warehouseOutOrderMaterialR.getStr("unit_num");//小单位换算成大单位的数值

            int boxAttrNum= NumberUtils.parseInt(boxAttrNumObj,0);
            int unitNum= NumberUtils.parseInt(unitNumObj,0);



            //取出安存数量
            int security_stock=0;
            Record securityStockR=(Record)securityStock.get(materialId);
            if(securityStockR!=null) {
                security_stock = securityStockR.getInt("security_stock");
            }

            Map map=new HashMap();
            map.put("id",materialId+"-"+batchCode);
            map.put("tid","");
            map.put("label",name+"("+batchCode+")");
            map.put("search_text",name+"-"+batchCode+pinyin);
            map.put("material_id",materialId);
            map.put("name",name);
            map.put("code",code);
            map.put("unit_text",outUnit);
            map.put("want_num",wantNum);
            map.put("security_stock",security_stock);//安存数量
            map.put("batch_code",batchCode);
            map.put("send_number",sendNumber);
            map.put("warehouseStockNumber",warehouseStockNumber);
            map.put("isEdit",true);
            map.put("warehouse_stock_id",warehouseStockId);

            String attribute2="";
            if(outUnit.equals(boxAttr)){
                attribute2=boxAttrNum+unitBig+"/"+boxAttr;
            }else if(outUnit.equals(unitBig)){
                attribute2=unitNum+unit+"/"+unitBig;
            }else if(outUnit.equals(unit)){
                attribute2=unit;
            }
            map.put("attribute_2_text",attribute2);

            if(materialId.equals(lastMaterialId)){//如果与上一个相同
                /*
                如果与上一个相同，取出reList最后一个元素，将上面的map，放入到该元素中
                 */
                Map elementMap=reList.get(reList.size()-1);
                List warehouseStockMapList=(List)elementMap.get("warehouseStockInfo");
                warehouseStockMapList.add(map);
            }else{//如果与上一个不同
                Map elementMap=new HashMap();
                elementMap.putAll(map);

                elementMap.put("id",materialId+"-"+batchCode);
                elementMap.put("label",name+"("+batchCode+")");
                elementMap.put("batch_code",batchCode);
                elementMap.put("warehouse_stock_id",warehouseStockId);

                List warehouseStockMapList=new ArrayList();
                warehouseStockMapList.add(map);

                elementMap.put("warehouseStockInfo",warehouseStockMapList);

                reList.add(elementMap);
            }

            lastMaterialId=materialId;
        }

        return reList;
    }
}
