package com.logistics.order.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.stock.services.SecurityStockService;
import com.utils.SelectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutWarehouseOrderSrv {

    public List<Record> showDetailsById(String id){
        String sql="select material_id,code,name,case when attribute_2 is null then '' else attribute_2 end as attribute_2 ,(select name from goods_unit where goods_unit.id=warehouse_out_order_material.unit) as unit_text,want_num,send_num from warehouse_out_order_material where warehouse_out_order_id=?";
        List<Record> warehouseOutOrderMaterialList=Db.find(sql,id);
        //获取安存
        Map securityStock=SecurityStockService.getSecurityStockMap();
        List materialIdList=new ArrayList();
        /*
        获取当前出库订单中原材料的安存数量，并放入到该条记录中
         */
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
            String materialId=warehouseOutOrderMaterialR.get("material_id");
            Record securityStockR=(Record)securityStock.get(materialId);
            if(securityStockR!=null) {
                int security_stock = securityStockR.getInt("security_stock");
                warehouseOutOrderMaterialR.set("security_stock",security_stock);//安存数量
            }else{
                warehouseOutOrderMaterialR.set("security_stock",0);//安存数量

            }
            materialIdList.add(materialId);
        }
        /*
        查询库存
        注：入库时要录入批号，所以同一个原材料会有多条记录
         */
        SelectUtil selectSQL=new SelectUtil("select * from warehouse_stock  ");
        selectSQL.in(" and material_id in ",materialIdList.toArray());
        selectSQL.order(" order by batch_code ");//按照批号排序
        List warehouseStockList=Db.find(selectSQL.toString(),selectSQL.getParameters());

        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
            String materialId=warehouseOutOrderMaterialR.get("material_id");
            int wantNum=warehouseOutOrderMaterialR.get("want_num");

            /*
            根据该订单原材料的id，获取对应库存原材料的库存数量、批号
             */
            List<Map> warehouseStockMapList=getWarehouseStock(materialId,wantNum,warehouseStockList);
            warehouseOutOrderMaterialR.set("warehouseStockInfo",warehouseStockMapList);
        }

        return warehouseOutOrderMaterialList;
    }

    /**
     * 根据该订单原材料的id，获取对应库存原材料的库存数量、批号
     * 注：入库时要录入批号，所以同一个原材料会有多条记录
     * @return
     */
    private List<Map> getWarehouseStock(String materialId,int wantNum,List<Record> warehouseStockList){
        List<Map> reList=new ArrayList<>();
        int sum=0;
        int lastSum=0;
        for(Record r:warehouseStockList){
            String materialIdDb=r.getStr("material_id");
            if(!materialId.equals(materialIdDb)){
                continue;
            }
            int number=r.get("number");
            String batch_code=r.get("batch_code");

            sum=sum+number;

            if(sum>=wantNum){//如果该原材料的库存数大于需求数
                int num=0;
                Map map=new HashMap();
                map.put("warehouseStockNumber",number);
                if(reList.isEmpty()){
                    map.put("send_number", wantNum);
                }else {
                    map.put("send_number", wantNum - lastSum);
                }
                map.put("batch_code",batch_code);
                reList.add(map);
                break;
            }else{//如果该原材料的库存数小于需求数，把当前原材料的信息放入到list中
                Map map=new HashMap();
                map.put("warehouseStockNumber",number);
                map.put("send_number",number);
                map.put("batch_code",batch_code);
                reList.add(map);
            }
            lastSum=lastSum+number;
        }
        return reList;
    }

}
