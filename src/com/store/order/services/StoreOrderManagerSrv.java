package com.store.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.common.services.OrderNumberGenerator;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.*;

public class StoreOrderManagerSrv {

    static StoreOrderManagerSrv me=new StoreOrderManagerSrv();

    public static StoreOrderManagerSrv getMe() {
        return me;
    }

    public void goodsToMaterialTypes(Map map, String storeOrderUUID, UserBean userBean, String type){
        if("day".equals(type)){
            goodsToMaterialTypeDay(map, storeOrderUUID, userBean);
        }else if("week".equals(type)){
            goodsToMaterialTypeWeek(map, storeOrderUUID, userBean);
        }else if("month".equals(type)){
            goodsToMaterialTypeMonth(map, storeOrderUUID, userBean);
        }else if("rush".equals(type)){
            goodsToMaterialTypeRush(map, storeOrderUUID, userBean);
        }
    }

    public void goodsToMaterialTypeDay(Map map, String storeOrderUUID, UserBean userBean){
        goodsToMaterial(map, storeOrderUUID, userBean, "day");
    }

    public void goodsToMaterialTypeWeek(Map map, String storeOrderUUID, UserBean userBean){
        goodsToMaterial(map, storeOrderUUID, userBean, "week");
    }

    public void goodsToMaterialTypeMonth(Map map, String storeOrderUUID, UserBean userBean){
        goodsToMaterial(map, storeOrderUUID, userBean, "month");
    }

    public void goodsToMaterialTypeRush(Map map, String storeOrderUUID, UserBean userBean){
        goodsToMaterial(map, storeOrderUUID, userBean, "rush");
    }

    /**
     * 整理数据，向store_order和store_order_goods表出入数据
     * @param map 前台传入参数
     * @param storeOrderUUID store_order的主键
     * @param userBean 用户信息
     */
    @Before(Tx.class)
    private void goodsToMaterial(Map map, String storeOrderUUID, UserBean userBean, String type){
        JSONObject jsonObject=(JSONObject)map.get("jsonObject");
        String arriveDate=jsonObject.getString("arriveDate");
        String wantDate=jsonObject.getString("wantDate");
        JSONArray goodsArray=jsonObject.getJSONArray("list");

        String dateTime= DateTool.GetDateTime();
        OrderNumberGenerator orderNumberGenerator = new OrderNumberGenerator();

        Record storeOrderR=new Record();
        storeOrderR.set("id",storeOrderUUID);
        storeOrderR.set("order_number",orderNumberGenerator.getStoreOrderNumber());
        storeOrderR.set("arrive_date",arriveDate);
        storeOrderR.set("want_date",wantDate);
        storeOrderR.set("create_time",dateTime);
        storeOrderR.set("status","5");
        storeOrderR.set("type", type);
        storeOrderR.set("city", userBean.get("city"));
        storeOrderR.set("store_id",userBean.get("store_id"));
        storeOrderR.set("store_color",userBean.get("store_color"));
        storeOrderR.set("creater_id",userBean.getId());
        Db.save("store_order",storeOrderR);


        Map<String,Record> materialMap=new LinkedHashMap();
        List<Record> goodsList = Db.find("select * from goods");
        Map<String, Record> goodsMap = new HashMap<>();
        for(Record r : goodsList){
            goodsMap.put(r.getStr("id"), r);
        }

        List<Record> storeOrderGoodsList = new ArrayList<>();
        for(int i = 0; i < goodsArray.size(); i++){
            JSONObject jsonObj = goodsArray.getJSONObject(i);
            String goodsId = jsonObj.getString("id");
            Record goods = goodsMap.get(goodsId);
            int number = jsonObj.getInteger("number");
            Object store_id = userBean.get("store_id");
            Record sog = new Record();
            sog.set("id", UUIDTool.getUUID());
            sog.set("store_order_id", storeOrderUUID);
            sog.set("store_id", store_id);
            sog.set("goods_id", goodsId);
            sog.set("code", goods.getStr("code"));
            sog.set("name", goods.getStr("name"));
            sog.set("pinyin", goods.getStr("pinyin"));
            sog.set("price", goods.getStr("price"));
            sog.set("wm_type", goods.getStr("wm_type"));
            sog.set("attribute_1", goods.getStr("attribute_1"));
            sog.set("attribute_2", goods.getStr("attribute_2"));
            sog.set("unit", goods.getStr("unit"));
            sog.set("sort", i);
            sog.set("type_1", goods.getStr("type_1"));
            sog.set("type_2", goods.getStr("type_2"));
            sog.set("number", number);
            storeOrderGoodsList.add(sog);
        }
        if(storeOrderGoodsList != null && storeOrderGoodsList.size() > 0){
            Db.batchSave("store_order_goods", storeOrderGoodsList, storeOrderGoodsList.size());
        }

    }
    /**
     * 添加订单原材料门店修改过的数据
     *      订单原材料已经存放到数据库中，但是门店修改后还要修改数量，其实这个方法是一个修改方法
     * 参数：
     *       stroe_order_material_id
     *       number：want_num和send_num字段数据。send_num数据还需要物流再次修改，但是要将门店数据提交，所以这里也添加
     *       nextOneNum：next1_order_num字段数据，方便以后查询
     *       nextTwoNum：next2_order_num字段数据，方便以后查询
     *
     */
    @Before(Tx.class)
    public void addStoreOrderMaterial(JSONObject jsonObject, UserSessionUtil usu) throws Exception{
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        List<Record> currentList = null;
        Map<String, Record> currentMap = new HashMap<>();
        String orderId = "";
        String sql = "select * from store_order_material ";
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
        List<Record> materialList = Db.find("select * from material");
        Map<String, Record> materialMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialMap.put(r.getStr("id"), r);
            }
        }
        if(jsonArray != null && jsonArray.size() > 0){
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject json = jsonArray.getJSONObject(i);
                if(json.getString("stroe_order_material_id") != null && json.getString("stroe_order_material_id").length() > 0){
                    Record updateR = currentMap.get(json.getString("stroe_order_material_id"));
                    updateR.set("want_num", json.get("number"));
                    /*
                    因为发货单位发生变化，所以发货数量要按照发货单位计算，此时不能修改
                     */
                    //updateR.set("send_num", json.get("number"));
                    updateR.set("next1_order_num", json.get("nextOneNum"));
                    updateR.set("next2_order_num", json.get("nextTwoNum"));
                    Record saveR = new Record();
                    saveR.setColumns(updateR);
                    saveR.set("id", UUIDTool.getUUID());
                    saveList.add(saveR);
                }else{
                    Record saveR = materialMap.get(json.getString("id"));
                    String id = UUIDTool.getUUID();
                    saveR.set("id", id);
                    saveR.set("store_order_id", orderId);
                    saveR.set("store_id", usu.getUserBean().get("store_id"));
                    saveR.set("material_id", json.getString("id"));
                    saveR.set("use_num", json.getString("number"));
                    /*
                    因为发货单位发生变化，所以发货数量要按照发货单位计算，此时不能修改
                     */
                    //saveR.set("send_num", json.getString("number"));
                    saveR.set("status", 10);
                    //saveR.set("type", "day");
                    //saveR.set("city", usu.getUserBean().get("city"));
                    saveR.set("want_num", json.getString("number"));
                    saveR.set("next1_order_num", json.getString("nextOneNum"));
                    saveR.set("next2_order_num", json.getString("nextTwoNum"));

                    saveR.remove("creater_id");
                    saveR.remove("modifier_id");
                    saveR.remove("create_time");
                    saveR.remove("modify_time");
                    saveR.remove("desc");
                    saveR.remove("unitname");
                    saveR.remove("storage_condition");
                    saveR.remove("shelf_life");
                    saveList.add(saveR);
                }
            }
        }
        Db.delete("delete from store_order_material where store_order_id=?",orderId);
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_order_material", saveList, saveList.size());
        }
        Db.update("update store_order set status=10 where id=?", orderId);
//        if(updateList != null && updateList.size() > 0){
//            for(Record r : updateList){
//                Db.update("store_order_material", r);
//            }
//        }

        /*
        try{
            if(jsonArray != null && jsonArray.size() > 0){
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject json = jsonArray.getJSONObject(i);
                    Record r = new Record();
                    //TODO 和前台沟通这个字段应该用什么key：stroe_order_material_id ? id
                    r.set("id", json.get("id"));
                    r.set("want_num", json.get("number"));
                    r.set("send_num", json.get("number"));
                    r.set("next1_order_num", json.get("nextOneNum"));
                    r.set("next2_order_num", json.get("nextTwoNum"));
                    Db.update("store_order_material", r);

                }
            }
        }catch (Exception e){
            throw e;
        }
        */
    }

    private void process(Map<String,Record> reMap,List<Record> materialList,int number){
        for(Record materialR:materialList){
            String idMaterialR=materialR.get("id");
            Record rOfMap=reMap.get(idMaterialR);
            if(rOfMap==null){
                int net_num=(int)materialR.get("net_num");
                int gross_num=(int)materialR.get("gross_num");

                materialR.set("net_num",net_num*number);
                materialR.set("gross_num",gross_num*number);
                materialR.set("number",number);
                reMap.put(idMaterialR,materialR);
            }else{

                int net_numOfMap=(int)rOfMap.get("net_num");
                int gross_numOfMap=(int)rOfMap.get("gross_num");
                int numberOfMap=(int)rOfMap.get("number");

                int net_num=(int)materialR.get("net_num");
                int gross_num=(int)materialR.get("gross_num");

                rOfMap.set("net_num",net_numOfMap+net_num*number);
                rOfMap.set("gross_num",gross_numOfMap+gross_num*number);
                rOfMap.set("number",numberOfMap+number);

            }

        }
    }

}
