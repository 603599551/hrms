package com.logistics.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.StoreOrderManagerSrv;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流接收门店订单
 */
public class StoreOrderCtrl extends BaseCtrl {


    /*
    物流查询门店订单
     */
    public void queryOrders(){
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        String select="select *,substr(create_time,1,16) as create_time_short,(select name from store where store.id=store_order.store_id) as store_text," +
                "(select name from dictionary where dictionary.value=store_order.type and dictionary.parent_id='7') as type_text," +
                "(select name from dictionary where dictionary.value=store_order.status and dictionary.parent_id='1') as status_text";

//        StringBuilder sqlExceptSelect=new StringBuilder("  where ?<=arrive_date and arrive_date<=? and type=? and store_id=? and status=? ");
        try {

//            JSONObject jsonObject = RequestTool.getJson(getRequest());

//            String startDate=jsonObject.getString("startDate");
//            String endDate=jsonObject.getString("endDate");
//            String orderType=jsonObject.getString("type");
//            String storeId=jsonObject.getString("storeId");
//            String status=jsonObject.getString("status");
//            String pageNumStr=getPara("pageNum");
//            String pageSizeStr=getPara("pageSize");
            Map paraMap=getParaMap();
            String[] arrivalDate=getParaValues("arrivalDate");
            String orderType=getPara("orderType");
            String orderCode=getPara("orderCode");
            String storeId=getPara("store");
            String status=getPara("state");
            String pageNumStr=getPara("pageNum");
            String pageSizeStr=getPara("pageSize");

            int pageNum= NumberUtils.parseInt(pageNumStr,1);
            int pageSize=NumberUtils.parseInt(pageSizeStr,10);

            SQLUtil sqlUtil = new SQLUtil(" from store_order ");
            if(arrivalDate!=null) {
                sqlUtil.addWhere("and ?<=arrive_date", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, arrivalDate[0]);
                sqlUtil.addWhere("and arrive_date<=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, arrivalDate[1]);
            }
            sqlUtil.addWhere("and type=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderType);
            sqlUtil.addWhere("and store_id=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, storeId);
            sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
            sqlUtil.addWhere("and order_number=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderCode);
            sqlUtil.order(" order by arrive_date ");
            String sqlExceptSelect=sqlUtil.toString();
            Page<Record> page=Db.paginate(pageNum, pageSize,select,sqlExceptSelect,sqlUtil.getParameterArray());
            jhm.putCode(1).put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /*

     */
    public void modifyOrder(){

    }
}
