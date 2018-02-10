package com.ss.goods.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.ss.services.BaseService;
import com.utils.RequestTool;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

public class BomMgrService extends BaseService {

    public void save(String goodsId, Object totalPriceObj, JSONArray list) {

        //先清空原数据
        int j=Db.delete("delete from goods_material where goods_id=? ", goodsId);

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                JSONObject jsonObj = list.getJSONObject(i);
                String mid = jsonObj.getString("id");
                Object net_numObj = jsonObj.get("net_num");
                Object gross_numObj = jsonObj.get("gross_num");
                Object total_priceObj = jsonObj.get("total_price");

                double net_num = NumberUtils.parseDouble(net_numObj, 0);
                double gross_num = NumberUtils.parseDouble(gross_numObj, 0);
                double total_price = NumberUtils.parseDouble(total_priceObj, 0);

                Record r = new Record();
                r.set("id", UUIDTool.getUUID());
                r.set("goods_id", goodsId);
                r.set("material_id", mid);
                r.set("net_num", net_num);
                r.set("gross_num", gross_num);
                r.set("total_price", total_price);
                r.set("sort", i + 1);
                Db.save("goods_material", r);
            }
            int n=Db.update("update goods set bom_status=? , bom_time=? ,total_bom_price=? where id=?", 1, DateTool.GetDateTime(), totalPriceObj, goodsId);
        } else {
            int n=Db.update("update goods set bom_status=? , bom_time=? ,total_bom_price=? where id=?", 0, null, totalPriceObj, goodsId);
        }
        Record r=new Record();
        r.set("id",UUIDTool.getUUID()+"123");
        Db.save("menu",r);
    }
}
