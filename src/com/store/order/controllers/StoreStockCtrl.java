package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.MaterialAndMaterialTypeTreeService;
import com.utils.Constants;
import com.utils.HanyuPinyinHelper;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import com.warehouse.service.StoreStockService;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;

public class StoreStockCtrl extends BaseCtrl implements Constants{

    public void getMaterialAll(){
        JsonHashMap jhm=new JsonHashMap();
        List<Record> materialTypeList = Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from material_type order by sort");
        List<Record> materialList = Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,(select name from goods_attribute ga where ga.id=material.attribute_2) attribute_2,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=material.unit) as unit_text,0 as stock_number from material order by sort");
//        List<Record> materialList = Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,(select name from goods_attribute ga where ga.id=material.attribute_2) attribute_2,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=material.unit) as unit_text,0 as stock_number from material where status=1 order by sort");
        Map<String, Record> materialMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialMap.put(r.getStr("id"), r);
                //search_text: '原材料名称-编号-拼音头'
                r.set("search_text",r.getStr("name") + "-" + r.get("code") + "-" + r.get("pinyin"));
                r.set("stock", "0");
                r.set("number", 0);
            }
        }
        if(materialTypeList != null && materialTypeList.size() > 0){
            for(Record r : materialTypeList){
                r.set("search_text",r.getStr("name") + "-" + HanyuPinyinHelper.getFirstLettersLo(r.getStr("name")));
            }
        }
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        List<Record> storeStockList = Db.find("select * from store_stock where store_id=? order by sort", usu.getUserBean().get("store_id"));
        if(storeStockList != null && storeStockList.size() > 0){
            for(Record r : storeStockList){
                Record materialR = materialMap.get(r.getStr("material_id"));
                if(materialR == null){
                    System.out.println(r.getStr("material_id"));
                }
                materialR.set("stock", r.getStr("number"));
                materialR.set("number", r.getStr("number"));
            }
        }
        //构建树
        MaterialAndMaterialTypeTreeService service = MaterialAndMaterialTypeTreeService.getMe();
        //构建原材料分类数
        List materialTypeList2 = service.sort(materialTypeList);
        //将原材料挂载到原材料分类树下
        List resultList = service.addMaterial2MaterialType(materialTypeList2, materialList);
        jhm.putCode(1).put("tree", resultList);
        renderJson(jhm);
    }

    public void editStock(){
        JSONObject jsonObject = RequestTool.getJson(getRequest());
        JSONArray list = jsonObject.getJSONArray("list");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        List<Record> materialList = Db.find("select * from material where status=1");
        Map<String, Record> materialMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialMap.put(r.getStr("id"), r);
            }
        }
        List<Record> storeStockList = Db.find("select * from store_stock where store_id=?", usu.getUserBean().get("store_id"));
        Map<String, Record> storeStockMap = new HashMap<>();
        if(storeStockList != null && storeStockList.size() > 0){
            for(Record r : storeStockList){
                storeStockMap.put(r.getStr("material_id"), r);
            }
        }
        if(list != null && list.size() > 0){
            List<Record> saveList = new ArrayList<>();
            List<Record> updateList = new ArrayList<>();
            List<Record> storeStocktakingList = new ArrayList<>();
            for(int i = 0; i < list.size(); i++){
                JSONObject json = list.getJSONObject(i);
                Record r = storeStockMap.get(json.getString("id"));
                if(r == null){
                    r = materialMap.get(json.getString( "id"));
                    r.remove("id","yield_rate","purchase_price","balance_price","wm_type","type_1","type_2","creater_id","modifier_id","create_time","modify_time","status","desc","shelf_life","storage_condition");
                    r.set("material_id", json.getString("id"));
                    r.set("store_id", usu.getUserBean().get("store_id"));
                    r.set("modify_time", DateTool.GetDate());
                    r.set("id", UUIDTool.getUUID());
                    Record storeStocktaking = new Record();
                    storeStocktaking.setColumns(r);
                    storeStocktaking.set("old_number", 0);
                    storeStocktaking.set("number", json.getString("number"));
                    storeStocktaking.set("id", UUIDTool.getUUID());
                    storeStocktakingList.add(storeStocktaking);
                    r.set("number", json.getString("number"));
                    saveList.add(r);
                }else{
                    r.set("material_id", json.getString("id"));
                    r.set("store_id", usu.getUserBean().get("store_id"));
                    r.set("modify_time", DateTool.GetDate());
                    Record storeStocktaking = new Record();
                    storeStocktaking.setColumns(r);
                    storeStocktaking.set("old_number", r.get("number"));
                    storeStocktaking.set("number", json.getString("number"));
                    storeStocktaking.set("id", UUIDTool.getUUID());
                    storeStocktakingList.add(storeStocktaking);
                    r.set("number", json.getString("number"));
                    updateList.add(r);
                }
            }
            StoreStockService storeStockService = enhance(StoreStockService.class);
            storeStockService.saveAndUpdateStoreStock(saveList, updateList, storeStocktakingList, new UserSessionUtil(this.getRequest()));
        }
        JsonHashMap jhm=new JsonHashMap();
        jhm.putMessage("盘点成功！");
        renderJson(jhm);
    }

}
