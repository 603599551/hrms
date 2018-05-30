package com.store.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.common.services.OrderNumberGenerator;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.ss.stock.services.DailySummaryService;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;

public class StoreScrapManagerSrv {

    /**
     * 整理数据，向store_scrap和store_scrap_goods表出入数据
     * @param map 前台传入参数
     * @param storeScrapUUID store_scrap的主键
     * @param userBean 用户信息
     */
    @Before(Tx.class)
    public void addStoreScrapAndStoreScrapGoods(Map map, String storeScrapUUID, UserBean userBean) throws ParseException {
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
//        storeScrapR.set("type", "day");
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
            ssg.set("scrap_time", scrap_time);
            storeScrapGoodsList.add(ssg);

        }
        Db.batchSave("store_scrap_goods", storeScrapGoodsList, storeScrapGoodsList.size());
        goodsToMaterial(storeScrapUUID, userBean);
    }

    /**
     * 将商品拆分成原材料，并计算数量
     * 参数：
     *      id：store_scrap表的主键。用来查询准备订货的商品相关信息
     *
     * 1、根据商品配方将商品数据拆分成原材料，将所有原材料放置到一个map（materialMAP）中，原材料id是key，具体数据是value。
     *      这样做的目的是为了方便数据整理，因为很多商品有相同的原材料，要将所有原材料的数据统计到一起
     * 3、这个方法还要保存原材料数据到store_order_material表
     *
     * @throws ParseException
     */
    public void goodsToMaterial(String orderId, UserBean usu) throws ParseException {
        //1
        List<Record> storeOrderGoodsList = Db.find("select ssg.goods_id goods_id, ssg.number number from store_scrap ss, store_scrap_goods ssg where ss.id=ssg.store_scrap_id and ss.id=?", orderId);

        List<String> idArr = new ArrayList<>();
        List<Integer> numberArr = new ArrayList<>();
        if(storeOrderGoodsList != null && storeOrderGoodsList.size() > 0){
            for(int i = 0; i < storeOrderGoodsList.size(); i++){
                idArr.add(storeOrderGoodsList.get(i).getStr("goods_id"));
                numberArr.add(storeOrderGoodsList.get(i).getInt("number"));
            }
        }

        List<Record> stockList = Db.find("select * from store_stock where store_id=?", usu.get("store_id"));
        Map<String, Record> stockMap = new HashMap<>();
        if(stockList != null && stockList.size() > 0){
            for(Record r : stockList){
                stockMap.put(r.getStr("material_id"), r);
            }
        }
        DailySummaryService dailySummaryService = DailySummaryService.getMe();
        Map<String, Record> materialMap = new HashMap<>();
        List<Record> result = new ArrayList<>();

        for(int i = 0; i < idArr.size(); i++){
            String goodsId = idArr.get(i);
            int number = numberArr.get(i);

            Map goodsIdMap=dailySummaryService.dataGoodsIdMap.get(goodsId);
            if(goodsIdMap==null){
                continue;
            }
            List<Record> goodsMaterialList = (List<Record>) goodsIdMap.get("materialList");
            for(Record r : goodsMaterialList){
                Record materialR = materialMap.get(r.getStr("mid"));
                Record stockR = stockMap.get(r.getStr("mid"));
                if(materialR != null){
                    //TODO 暂时用r净料数量计算
                    double actual_order = new Double(String.format("%.2f", getDouble(materialR.getDouble("actual_order") + r.getDouble("gmnet_num") * number)));
                    materialR.set("actual_order", actual_order);
                }else{
                    materialR = new Record();
                    materialMap.put(r.getStr("mid"), materialR);
                    materialR.set("id", r.getStr("mid"));
                    materialR.set("name", r.getStr("mname"));
                    materialR.set("code", r.getStr("mcode"));
                    materialR.set("unit_text", r.getStr("munit"));
                    double actual_order = new Double(String.format("%.2f", getDouble(r.getDouble("gmnet_num") * number)));
                    materialR.set("actual_order", actual_order);
                    if(stockR != null){
                        materialR.set("stock", stockR.getInt("number"));
                    }else{
                        materialR.set("stock", 0);
                    }
                    result.add(materialR);
                }
            }
        }
        //1
        //查询商品相关信息，缓存到内存，方便后面读取数据
        List<Record> materialList = Db.find("select m.*, gu.name unitname, gm.net_num net_num, gm.gross_num gross_num, gm.total_price total_price from material m, goods_unit gu, goods_material gm where m.id=gm.material_id and m.unit=gu.id");
        Map<String, Record> materialAllMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialAllMap.put(r.getStr("id"), r);
            }
        }
        String time = DateTool.GetDateTime();
        //3
        List<Record> saveList = new ArrayList<>();
        if(result != null && result.size() > 0){
            for(Record r : result){
                Record saveR = materialAllMap.get(r.getStr("id"));
                String id = UUIDTool.getUUID();
                saveR.set("id", id);
                r.set("store_scrap_material_id", id);
                saveR.set("store_scrap_id", orderId);
                saveR.set("store_id", usu.get("store_id"));
                saveR.set("material_id", r.getStr("id"));
                saveR.set("number", r.getStr("actual_order"));
                saveR.set("creater_id", usu.getId());
                saveR.set("create_time", time);
                saveR.set("modifier_id", usu.getId());
                saveR.set("modify_time", time);
                saveR.set("scrap_time", time);

                saveR.remove("desc");
                saveR.remove("unitname");
                saveR.remove("storage_condition");
                saveR.remove("type");
                saveR.remove("city");
                saveR.remove("shelf_life");
                saveR.remove("status");
                saveList.add(saveR);
            }
        }
        Db.batchSave("store_scrap_material", saveList, saveList.size());
        //3
        //前台需要非0的数据，如果是0这个数字，前台不能显示，所以将数字列转化成字符串传到前台
    }


    /**
     * 添加订单原材料门店修改过的数据
     *      订单原材料已经存放到数据库中，但是门店修改后还要修改数量，其实这个方法是一个修改方法
     * 参数：
     *       store_scrap_material_id
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
                    sql += "'" + json.getString("store_scrap_material_id") + "',";
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
        if(!(orderId != null && orderId.length() > 0)){
            orderId = UUIDTool.getUUID();
        }

        List<Record> saveList = new ArrayList<>();
        List<Record> materialList = Db.find("select m.* from material m ");
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
                if(json.getString("store_scrap_material_id") != null && json.getString("store_scrap_material_id").length() > 0){
                    Record saveR = currentMap.get(json.getString("store_scrap_material_id"));
                    saveR.set("number", json.get("number"));
                    saveR.set("modifier_id", userBean.getId());
                    saveR.set("modify_time", DateTool.GetDateTime());
                    String id = UUIDTool.getUUID();
                    saveR.set("id", id);
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
                    saveR.set("net_num", 0);
                    saveR.set("gross_num", 0);
                    saveR.set("total_price", 0);

                    saveR.remove("desc");
                    saveR.remove("unitname");
                    saveR.remove("storage_condition");
                    saveR.remove("type");
                    saveR.remove("city");
                    saveR.remove("shelf_life");
                    saveR.remove("status");
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
        record.set("close_time", DateTool.GetDateTime());
        Db.update("store_scrap", record);
    }

    @Before(Tx.class)
    public void finishOrder(String orderId, UserSessionUtil usu) throws Exception{
        String time = DateTool.GetDateTime();
        Record record = new Record();
        record.set("id", orderId);
        record.set("status", 3);
        record.set("logistics_modifier_id", usu.getUserId());
        record.set("logistics_modifier_time", time);
        Db.update("store_scrap", record);
        List<Record> storeScrapMaterialList = Db.find("select * from store_scrap_material where store_scrap_id=?", orderId);
        String storeId = null;
        if(storeScrapMaterialList != null && storeScrapMaterialList.size() > 0){
            storeId = storeScrapMaterialList.get(0).getStr("store_id");
        }
        List<Record> storeStockList = Db.find("select * from store_stock where store_id=?", storeId);
        Map<String, Record> storeStockMap = new HashMap<>();
        if(storeStockList != null && storeStockList.size() > 0){
            for(Record r : storeStockList){
                storeStockMap.put(r.getStr("material_id"), r);
            }
        }
        List<Record> warehouseStockList = Db.find("select * from warehouse_stock where warehouse_id=?", "1324081092138412934fpk");
        Map<String, Record> warehouseStockMap = new HashMap<>();
        if(warehouseStockList != null && warehouseStockList.size() > 0){
            for(Record r : warehouseStockList){
                warehouseStockMap.put(r.getStr("material_id"), r);
            }
        }
        List<Record> updateList = new ArrayList<>();
        List<Record> saveList = new ArrayList<>();
        List<Record> warehouseStockUpdateList = new ArrayList<>();
        List<Record> warehouseStockSaveList = new ArrayList<>();
        if(storeScrapMaterialList != null && storeScrapMaterialList.size() > 0){
            for(Record r : storeScrapMaterialList){
                Record storeStockR = storeStockMap.get(r.getStr("material_id"));
                Record storeStocktakingR = new Record();
                storeStocktakingR.setColumns(storeStockR);
                storeStocktakingR.set("id", UUIDTool.getUUID());
                storeStocktakingR.set("old_number", storeStocktakingR.getInt("number"));
                storeStocktakingR.set("create_time", time);
                storeStocktakingR.set("modify_time", time);
                storeStocktakingR.set("creater_id", usu.getUserId());
                storeStocktakingR.set("modifier_id", usu.getUserId());
                saveList.add(storeStocktakingR);

                Record updateR = new Record();
                updateR.set("id", storeStockR.getStr("id"));
                int number = storeStockR.getInt("number") - r.getInt("number");
                updateR.set("number", number);
                updateR.set("modify_time", time);
                updateList.add(updateR);

                Record warehouseStock = warehouseStockMap.get(r.getStr("material_id"));
                if(warehouseStock == null){
                    warehouseStock = new Record();
                    warehouseStock.setColumns(r);
                    String[] removeColumns = {"store_scrap_id", "store_id", "net_num", "gross_num", "total_price", "creater_id", "modifier_id", "create_time", "modify_time", "scrap_time", "number"};
                    warehouseStock.remove(removeColumns);
                    warehouseStock.set("creater_id", usu.getUserId());
                    warehouseStock.set("create_time", time);
                    //TODO 1324081092138412934fpk废品库id，暂时写死，以后可以修改成动态获取
                    warehouseStock.set("warehouse_id", "1324081092138412934fpk");
                    warehouseStock.set("number", number);
                    warehouseStockSaveList.add(warehouseStock);
                }else{
                    warehouseStock.set("number", number);
                    warehouseStockUpdateList.add(warehouseStock);
                }
            }
        }
        if(updateList != null && updateList.size() > 0){
            for(Record r : updateList){
                Db.update("store_stock", r);
            }
        }
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_stocktaking", saveList, saveList.size());
        }
        if(warehouseStockUpdateList != null && warehouseStockUpdateList.size() > 0){
            for(Record r : warehouseStockUpdateList){
                Db.update("warehouse_stock", r);
            }
        }
        if(warehouseStockSaveList != null && warehouseStockSaveList.size() > 0){
            Db.batchSave("warehouse_stock", warehouseStockSaveList, warehouseStockSaveList.size());
        }
    }

    /**
     * 将obj转化成int类型
     *      如果为空返回0
     *      如果是double类型，将double转化成int
     * @param obj
     * @return
     */
    private double getDouble(Object obj){
        if(obj != null && obj.toString().trim().length() > 0 && !"null".equalsIgnoreCase(obj.toString())){
            if(obj instanceof Double){
                double result = new Double(obj.toString());
                return result;
            }else if(obj instanceof Integer){
                return new Double(obj.toString());
            }
        }
        return 0;
    }
}
