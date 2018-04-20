package com.store.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.RequestTool;
import easy.util.DateTool;
import easy.util.UUIDTool;

import java.util.*;

public class StoreOrderManagerSrv {

    static StoreOrderManagerSrv me=new StoreOrderManagerSrv();

    public static StoreOrderManagerSrv getMe() {
        return me;
    }

    @Before(Tx.class)
    public Map goodsToMaterial(Map map){
        Map resultMap=new HashMap();
        JSONObject jsonObject=(JSONObject)map.get("data");
        String arriveDate=jsonObject.getString("arriveDate");
        String wantDate=jsonObject.getString("wantDate");
        JSONArray goodsArray=jsonObject.getJSONArray("list");

        String storeOrderUUID= UUIDTool.getUUID();
        String dateTime= DateTool.GetDateTime();

        Record storeOrderR=new Record();
        storeOrderR.set("id",storeOrderUUID);
        storeOrderR.set("order_number","");
        storeOrderR.set("arrive_date",arriveDate);
        storeOrderR.set("want_date",wantDate);
        storeOrderR.set("create_time",dateTime);
        storeOrderR.set("status","1");
        Db.save("store_order",storeOrderR);


        Map<String,Record> materialMap=new LinkedHashMap();
        for(Object obj:goodsArray){
            JSONObject jsonObj=(JSONObject)obj;
            String id=jsonObj.getString("id");
            int number=jsonObj.getInteger("number");

            List<Record> materialList= Db.find("select a.net_num,a.gross_num,a.total_price,b.id, b.name,b.code,b.attribute_2,b.unit,(select name from goods_unit where goods_unit.id=b.id) from goods_material a,material b where a.goods_id=? and a.material_id=b.id ");

            process(materialMap,materialList,number);
        }
        List<Record> reList=new ArrayList<>();
        Iterator<Map.Entry<String,Record>> it=materialMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry=it.next();
            reList.add((Record)entry.getValue());
        }
        resultMap.put("list",reList);

        return resultMap;
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
