package com.store.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.UnitConversion;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门店库存管理
 */
public class StoreStockManagerCtrl extends BaseCtrl{
    public void index(){

    }

    /**
     * 显示库存余额
     */
    public void showBalance(){
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String storeId=usu.getUserBean().getDeptId();
        String sql="select id,code,name,number,out_unit,box_attr,box_attr_num,unit_big,unit,unit_num from store_stock a where store_id=? order by type_1,type_2,code,id";
        List<Record> list=Db.find(sql,storeId);
        List<Map> reList=new ArrayList();
        for(Record r:list){
            Map map=new HashMap();
            try {
                String attr = UnitConversion.getAttrByOutUnit(r);
                map.put("attribute_2_text",attr);
            }catch (Exception e){
                e.printStackTrace();
            }
            map.put("id",r.get("id"));
            map.put("code",r.get("code"));
            map.put("name",r.get("name"));
            map.put("number",r.get("number"));
            map.put("unit_text",r.get("unit"));
            reList.add(map);
        }
        jhm.putCode(1).put("list",reList);
        renderJson(jhm);
    }
}
