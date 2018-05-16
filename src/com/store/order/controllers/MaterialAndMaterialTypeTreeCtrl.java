package com.store.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.MaterialAndMaterialTypeTreeService;
import com.store.order.services.StoreOrderManagerSrv;
import com.utils.Constants;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;

public class MaterialAndMaterialTypeTreeCtrl extends BaseCtrl  implements Constants {

    public void index() {
        JsonHashMap jhm=new JsonHashMap();
        try {
            //查询原材料分类
            List materialTypeList = Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from material_type order by sort");
            //查询原材料
            List<Record> materialList = Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,attribute_2,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=material.unit) as unit_text,0 as stock_number from material order by sort");

            String orderId = getPara("id");
//        //TODO 测试数据
//        orderId = (String) getSession().getAttribute("store_order_id");

            List<Record> storeOrderGoodsList = Db.find("select sog.*, so.want_date sowant_date, so.arrive_date sarrive_date from store_order_goods sog, store_order so where so.id=sog.store_order_id and store_order_id=?", orderId);
            String wantDateStr = storeOrderGoodsList.get(0).getStr("sowant_date");
            Date wantDate = sdf.parse(wantDateStr);
            StoreOrderManagerSrv storeOrderManagerSrv = StoreOrderManagerSrv.getMe();
            String paramDateOne = sdf.format(new Date(wantDate.getTime() + ONE_DAY_TIME));
            String paramDateTwo = sdf.format(new Date(wantDate.getTime() + ONE_DAY_TIME * 2));

            String sql = "select som.* from store_order so, store_order_material som where so.id=som.store_order_id and arrive_date=? ";
            List<Record> oneList = Db.find(sql, paramDateOne);
            List<Record> twoList = Db.find(sql, paramDateTwo);
            Map<String, Record> oneMap = new HashMap<>();
            Map<String, Record> twoMap = new HashMap<>();
            if(oneList != null && oneList.size() > 0){
                for(Record r : oneList){
                    oneMap.put(r.getStr("material_id"), r);
                }
            }
            if(twoList != null && twoList.size() > 0){
                for(Record r : twoList){
                    twoMap.put(r.getStr("material_id"), r);
                }
            }

            if (materialList != null && materialList.size() > 0) {
                for (Record r : materialList) {
                    //整理昨天和前天的数据
                    Record one = oneMap.get(r.getStr("id"));
                    Record two = twoMap.get(r.getStr("id"));
                    r.set("isEdit", true);
                    r.set("actual_order", "0");
                    r.set("stock", "0");
                    if (one != null) {
                        r.set("nextOneNum", one.getStr("use_num"));
                        r.set("nextOneGetNum", one.getStr("send_num"));
                    } else {
                        r.set("nextOneNum", "0");
                        r.set("nextOneGetNum", "0");
                    }
                    if (two != null) {
                        r.set("nextTwoNum", two.getStr("use_num"));
                        r.set("nextTwoGetNum", two.getStr("send_num"));
                    } else {
                        r.set("nextTwoNum", "0");
                        r.set("nextTwoGetNum", "0");
                    }
                }
            }
            //构建树
            MaterialAndMaterialTypeTreeService service = MaterialAndMaterialTypeTreeService.getMe();
            //构建原材料分类数
            List materialTypeList2 = service.sort(materialTypeList);
            //将原材料挂载到原材料分类树下
            List resultList = service.addMaterial2MaterialType(materialTypeList2, materialList);
            jhm.putCode(1).put("tree", resultList);
        }catch (ParseException e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("查询失败！");
        }
        renderJson(jhm);
    }
}
