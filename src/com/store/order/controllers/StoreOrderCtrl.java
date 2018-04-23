package com.store.order.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreOrderCtrl extends BaseCtrl {

    public void createOrder(){
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        JSONObject json= RequestTool.getJson(getRequest());

        String store_id = (String) usu.getUserBean().get("store_id");

        List<Record> goodsList = Db.find("select * from goods");
        Map<String, Record> goodsMap = new HashMap<>();
        for(Record r : goodsList){
            goodsMap.put(r.getStr("id"), r);
        }

        String[] idArr = (String[]) getSession().getAttribute("ids");
        String[] numberArr = (String[]) getSession().getAttribute("numbers");
        String orderId = (String) getSession().getAttribute("storeOrderUUID");

        DailySummaryService dailySummaryService = DailySummaryService.getMe();
        Map<String, Record> materialMap = new HashMap<>();

        List<Record> storeOrderGoodsList = new ArrayList<>();
        for(int i = 0; i < idArr.length; i++){
            String goodsId = idArr[i];
            Record goods = goodsMap.get(goodsId);
            int number = new Integer(numberArr[i]);
            Record sog = new Record();
            sog.set("id", UUIDTool.getUUID());
            sog.set("store_order_id", orderId);
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

            List<Record> goodsMaterialList = (List<Record>) dailySummaryService.dataMap.get(goodsId).get("materialList");
            for(Record r : goodsMaterialList){
                Record materialR = materialMap.get(r.getStr("id"));
                if(materialR != null){
                    //TODO 暂时用r净料数量计算
                    materialR.set("number", materialR.getDouble("number") + (int)r.get("gmnet_num") * number);
                }else{
                    materialR = new Record();
                    materialMap.put(r.getStr("id"), materialR);

                    materialR.set("id", r.getStr("mid"));
                    materialR.set("name", r.getStr("mname"));
                    materialR.set("code", r.getStr("mcode"));
                    materialR.set("unit_text", r.getStr("munit"));
                    materialR.set("number", (int)r.get("gmnet_num") * number);

                }
            }

        }
        Db.batchSave("store_order_goods", storeOrderGoodsList, storeOrderGoodsList.size());

        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1).put("materialList",materialMap.values());
        renderJson(jhm);

    }
}
