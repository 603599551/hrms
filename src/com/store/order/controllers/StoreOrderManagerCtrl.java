package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.StoreOrderManagerSrv;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门店订单管理
 */
public class StoreOrderManagerCtrl extends BaseCtrl{

    /**
     * 商品转原材料
     */
    public void goodsToMaterial(){
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {

            JSONObject jsonObject = RequestTool.getJson(getRequest());

            String arriveDate=jsonObject.getString("arriveDate");
            String wantDate=jsonObject.getString("wantDate");

            Map paraMap=new HashMap();
            paraMap.put("jsonObject",jsonObject);

            StoreOrderManagerSrv service = enhance(StoreOrderManagerSrv.class);
            Map resultMap=service.goodsToMaterial(paraMap);
            List list=(List)resultMap.get("list");

            jhm.putCode(1).put("arriveDate",arriveDate).put("wantDate",wantDate).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
