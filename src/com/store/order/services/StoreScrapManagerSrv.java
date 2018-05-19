package com.store.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.common.services.OrderNumberGenerator;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;

import java.util.*;

public class StoreScrapManagerSrv {

    /**
     * 整理数据，向store_scrap和store_scrap_goods表出入数据
     * @param map 前台传入参数
     * @param storeScrapUUID store_scrap的主键
     * @param userBean 用户信息
     */
    @Before(Tx.class)
    public void addStoreScrapAndStoreScrapGoods(Map map, String storeScrapUUID, UserBean userBean){
        JSONObject jsonObject=(JSONObject)map.get("jsonObject");
        JSONArray goodsArray=jsonObject.getJSONArray("list");

        String dateTime= DateTool.GetDateTime();
        OrderNumberGenerator orderNumberGenerator = new OrderNumberGenerator();
        String scrap_time = DateTool.GetDateTime();
        Record storeScrapR=new Record();
        storeScrapR.set("id",storeScrapUUID);
        storeScrapR.set("order_number",orderNumberGenerator.getStoreScrapNumber());
        storeScrapR.set("create_time",dateTime);
        storeScrapR.set("status","1");
        storeScrapR.set("type", "day");
        storeScrapR.set("city", userBean.get("city"));
        storeScrapR.set("store_id",userBean.get("store_id"));
        storeScrapR.set("creater_id",userBean.getId());
        storeScrapR.set("scrap_time", scrap_time);
        Db.save("store_scrap",storeScrapR);

        List<Record> goodsList = Db.find("select * from goods");
        Map<String, Record> goodsMap = new HashMap<>();
        for(Record r : goodsList){
            goodsMap.put(r.getStr("id"), r);
        }

        List<Record> storeScrapGoodsList = new ArrayList<>();
        for(int i = 0; i < goodsArray.size(); i++){
            JSONObject jsonObj = goodsArray.getJSONObject(i);
            String goodsId = jsonObj.getString("id");
            Record goods = goodsMap.get(goodsId);
            double number = jsonObj.getDouble("number");
            Object store_id = userBean.get("store_id");
            Record ssg = new Record();
            ssg.set("id", UUIDTool.getUUID());
            ssg.set("store_scrap_id", storeScrapUUID);
            ssg.set("goods_id", goodsId);
            ssg.set("store_id", store_id);
            ssg.set("code", goods.getStr("code"));
            ssg.set("name", goods.getStr("name"));
            ssg.set("pinyin", goods.getStr("pinyin"));
            ssg.set("price", goods.getStr("price"));
            ssg.set("wm_type", goods.getStr("wm_type"));
            ssg.set("attribute_1", goods.getStr("attribute_1"));
            ssg.set("attribute_2", goods.getStr("attribute_2"));
            ssg.set("unit", goods.getStr("unit"));
            ssg.set("sort", i);
            ssg.set("type_1", goods.getStr("type_1"));
            ssg.set("type_2", goods.getStr("type_2"));
            ssg.set("number", number);
            ssg.set("creater_id", userBean.getId());
            ssg.set("create_time", scrap_time);
            ssg.set("modifier_id", userBean.getId());
            ssg.set("modify_time", scrap_time);
            storeScrapR.set("scrap_time", scrap_time);
            storeScrapGoodsList.add(ssg);

        }
        Db.batchSave("store_scrap_goods", storeScrapGoodsList, storeScrapGoodsList.size());

    }
    /**
     * 添加订单原材料门店修改过的数据
     *      订单原材料已经存放到数据库中，但是门店修改后还要修改数量，其实这个方法是一个修改方法
     * 参数：
     *       stroe_order_material_id
     *       number：want_num和send_num字段数据。send_num数据还需要物流再次修改，但是要将门店数据提交，所以这里也添加
     *       nextOneNum：next1_order_num字段数据，方便以后查询
     *       nextTwoNum：next2_order_num字段数据，方便以后查询
     */
    @Before(Tx.class)
    public void addStoreScrapMaterial(JSONObject jsonObject, UserSessionUtil usu) throws Exception{
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        List<Record> currentList = null;
        Map<String, Record> currentMap = new HashMap<>();
        String orderId = "";
        String sql = "select * from store_scrap_material ";
        if(jsonArray != null && jsonArray.size() > 0){
            sql += " where id in (null,";
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject json = jsonArray.getJSONObject(i);
                if(json.get("id") != null){
                    sql += "'" + json.getString("stroe_order_material_id") + "',";
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ")";
            currentList = Db.find(sql);
        }
        if(currentList != null && currentList.size() > 0){
            orderId = currentList.get(0).getStr("store_order_id");
            for(Record r : currentList){
                currentMap.put(r.getStr("id"), r);
            }
        }

        List<Record> saveList = new ArrayList<>();
        List<Record> updateList = new ArrayList<>();
        List<Record> materialList = Db.find("select * from material");
        Map<String, Record> materialMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialMap.put(r.getStr("id"), r);
            }
        }
        UserBean userBean = usu.getUserBean();
        if(jsonArray != null && jsonArray.size() > 0){
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject json = jsonArray.getJSONObject(i);
                if("0".equals(json.getString("number"))){
                    continue;
                }
                if(json.getString("stroe_order_material_id") != null && json.getString("stroe_order_material_id").length() > 0){
                    Record saveR = currentMap.get(json.getString("stroe_order_material_id"));
                    saveR.set("number", json.get("number"));
                    saveR.set("modifier_id", userBean.getId());
                    saveR.set("modify_time", DateTool.GetDateTime());
                    saveList.add(saveR);
                }else{
                    Record saveR = materialMap.get(json.getString("id"));
                    String id = UUIDTool.getUUID();
                    String time = DateTool.GetDateTime();
                    saveR.set("id", id);
                    saveR.set("store_scrap_id", orderId);
                    saveR.set("store_id", usu.getUserBean().get("store_id"));
                    saveR.set("material_id", json.getString("id"));
                    saveR.set("number", json.getString("number"));
                    saveR.set("creater_id", userBean.getId());
                    saveR.set("create_time", time);
                    saveR.set("modifier_id", userBean.getId());
                    saveR.set("modify_time", time);
                    saveR.set("scrap_time", time);

                    saveR.remove("desc");
                    saveR.remove("unitname");
                    saveR.remove("storage_condition");
                    saveR.remove("type");
                    saveR.remove("city");
                    saveR.remove("shelf_life");
                    saveList.add(saveR);
                }
            }
        }
        String deleteSql = "delete from store_scrap_material where store_scrap_id=?";
        Db.delete(deleteSql, orderId);
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_scrap_material", saveList, saveList.size());
        }
    }

    public void cancelOrder(String orderId) throws Exception{
        Record record = new Record();
        record.set("id", orderId);
        record.set("status", 4);
        Db.update("store_scrap", record);
    }
}
