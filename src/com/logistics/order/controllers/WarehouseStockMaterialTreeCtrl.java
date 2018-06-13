package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.services.MaterialTypeService;
import com.ss.stock.services.SecurityStockService;
import com.utils.HanyuPinyinHelper;
import com.utils.SelectUtil;
import com.utils.UnitConversion;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 显示仓库库存原材料树
 */
public class WarehouseStockMaterialTreeCtrl extends BaseCtrl{
    /**
     * 查询仓库库存原材料信息
     *
     * id: '原材料id-批号',
     tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
     label: '原材料名称(批号)',
     search_text: '原材料名称-批号-拼音头', // 搜索条件
     material_id: '原材料id',
     name: '原材料名称',
     code: '编号',
     attribute_2: '规格',
     unit_text: '单位',
     want_num: '订货数量',
     security_stock: '安存数量',
     batch_code: '批号',
     send_number: '出货数量',
     warehouseStockNumber: '库存数量',
     isEdit: true, // 三级树中加就行
     warehouse_stock_id:''//库存（warehouse_stock）id
     */
    public void index(){
        String warehouseId=getPara("warehouseId");
        JsonHashMap jhm=new JsonHashMap();
        /*
        查询material_type原材料分类sql
         */
        String sql="select id,parent_id,code,name as label from material_type order by sort,id";
        /*
        查询仓库库存原材料信息
        material表的status字段为1
         */
        String warehouseStockSql="select a.out_unit,a.out_unit as unit_text,a.box_attr,a.box_attr_num,a.unit_big,a.unit,a.unit_num,a.warehouse_id,a.id as warehouse_stock_id,CONCAT(a.material_id,'-',batch_code) as id,'' as tid,a.material_id,a.code,a.name,CONCAT(a.name,'(',batch_code,')') as label,a.batch_code,a.number as warehouseStockNumber,0 as want_num,0 as send_number,CONCAT(a.name,'-',batch_code,'-',b.pinyin) as search_text,b.type_2 from warehouse_stock a left join material b on a.material_id=b.id ";
        try {
            SelectUtil selectUtil=new SelectUtil(warehouseStockSql);
            selectUtil.addWhere("and a.number>0");
            selectUtil.addWhere("and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
            selectUtil.order("order by a.material_id,a.batch_code,a.id");
            /*
            查询分类并给分类加拼音
             */
            List<Record> typeList = Db.find(sql);
            for(Record r:typeList){
                String name=r.getStr("label");
                String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);
                r.set("search_text",name+"-"+pinyin);
            }

            List<Record> warehouseStockList=Db.find(selectUtil.toString(),selectUtil.getParameters());
            //获取安存
            Map securityStock= SecurityStockService.getSecurityStockMap();

            Iterator<Record> it=warehouseStockList.iterator();
            while(it.hasNext()) {
                Record r=it.next();
                r.set("isEdit",true);

                String attribute2text=UnitConversion.getAttrByOutUnit(r);
                r.set("attribute_2_text",attribute2text);
                String materialId=r.getStr("material_id");
                String type2InRecord=r.getStr("type_2");
                r.remove("type_2");
                /*
                放入安存数量
                 */
                Record securityStockR = (Record) securityStock.get(materialId);
                if (securityStockR != null) {
                    int security_stock = securityStockR.getInt("security_stock");
                    r.set("security_stock", security_stock);//安存数量
                } else {
                    r.set("security_stock", 0);//安存数量

                }

                /*
                将原材料加入到菜单中
                 */
                for(Record map:typeList){
                    String idInMap=(String)map.get("id");

                    if(idInMap.equals(type2InRecord)){
                        List childrenList=(List)map.get("children");
                        if(childrenList==null){
                            childrenList=new ArrayList();
                            map.set("children",childrenList);
                        }
                        childrenList.add(r.getColumns());
                    }
                }
            }

//            for(Record map:typeList){
//                String idInMap=(String)map.get("id");
////                for(Record r:warehouseStockList){
//                while(it.hasNext()){
//                    Record r=it.next();
//                    String type2InRecord=r.getStr("type_2");
//                    if(idInMap.equals(type2InRecord)){
//                        List childrenList=(List)map.get("children");
//                        if(childrenList==null){
//                            childrenList=new ArrayList();
//                            map.set("children",childrenList);
//                        }
//                        childrenList.add(r.getColumns());
//                        it.remove();
////                        warehouseStockList.remove(r);
//                    }
//                }
//            }
            List<Map> mtList= MaterialTypeService.getMe().sort(typeList);
            jhm.putCode(1).put("list",mtList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
