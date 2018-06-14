package com.store.order.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.utils.*;
import easy.util.DateTool;
import easy.util.NumberUtils;
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

            List<Record> goodsMaterialList = (List<Record>) dailySummaryService.dataGoodsIdMap.get(goodsId).get("materialList");
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
                "(select name from dictionary where dictionary.value=store_order.status and dictionary.parent_id='1') as status_text," +
                "(select status_color from dictionary where dictionary.value=store_order.status and dictionary.parent_id='1') as status_color";

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
            String[] arrivalDate=getParaValues("arrivalDate");
            String orderType=getPara("orderType");
            String orderCode=getPara("orderCode");
            String storeId=getPara("store");
            String status=getPara("state");
            String pageNumStr=getPara("pageNum");
            String pageSizeStr=getPara("pageSize");

            int pageNum= NumberUtils.parseInt(pageNumStr,1);
            int pageSize=NumberUtils.parseInt(pageSizeStr,10);

//            if(StringUtils.isEmpty(status)){
//                status="1";
//            }

            SelectUtil sqlUtil = new SelectUtil(" from store_order ");
            if(arrivalDate!=null) {
                if(arrivalDate.length==1){
                    sqlUtil.addWhere("and ?<=arrive_date", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, arrivalDate[0]);
                }else if(arrivalDate.length==2) {
                    sqlUtil.addWhere("and ?<=arrive_date", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, arrivalDate[0]);
                    sqlUtil.addWhere("and arrive_date<=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, arrivalDate[1]);
                }
            }
            if(orderType != null && orderType.length() > 0 && !"-1".equals(orderType)){
                sqlUtil.addWhere("and type=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderType);
            }
            sqlUtil.addWhere("and store_id=?", usu.getUserBean().getDeptId());
            if(status != null && status.length() > 0 && !"-1".equals(status)){
                sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
            }else{
                sqlUtil.addWhere("and status<>?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, "5");
            }
            sqlUtil.addWhere("and order_number=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderCode);
            sqlUtil.order(" order by arrive_date desc , create_time desc");
            String sqlExceptSelect=sqlUtil.toString();
            Page<Record> page=Db.paginate(pageNum, pageSize,select,sqlExceptSelect,sqlUtil.getParameterList().toArray());
            jhm.putCode(1).put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /**
     * 查看订单详细信息
     */
    public void showOrderDetailsById(){
        String id=getPara("id");
        JsonHashMap jsonHashMap=new JsonHashMap();
        String sql="select *,(select name from store where store.id=store_order.store_id) as store_text,substr(create_time,1,16) as create_time_short,IFNULL((select sort from print_details where order_id = store_order.id order by sort desc limit 1,1),0) as print_time from store_order where id=?";
        try{
            Record r=Db.findFirst(sql,id);
            String returnReason=r.getStr("return_reason");
            if(returnReason==null){
                r.set("return_reason","");
            }
            String status=r.getStr("status");
            boolean showCancelButton=false;
            if("5".equals(status) || "10".equals(status)) {
                showCancelButton=true;
            }
            List<Record> list=Db.find("select * from store_order_material where store_order_id=? order by sort ",id);
            for(Record tempR:list){
                String attr= UnitConversion.getAttrByOutUnit(tempR);
                tempR.set("attribute_2_text",attr);
            }
            jsonHashMap.putCode(1).put("order",r).put("orderDetailsList",list).put("showCancelButton",showCancelButton);
        }catch (Exception e){
            e.printStackTrace();
            jsonHashMap.putCode(-1).putMessage(e.toString());
        }
        renderJson(jsonHashMap);
    }
    /**
     * 撤销订单
     */
    public void cancelOrder(){
        String id=getPara("id");
        JsonHashMap jsonHashMap=new JsonHashMap();
        try{
            Record storeOrderR=Db.findById("store_order",id);
            if(storeOrderR==null){
                jsonHashMap.putCode(0).putMessage("查无此订单！");
                renderJson(jsonHashMap);
                return;
            }
            String status=storeOrderR.getStr("status");
            if("110".equals(status)){
                jsonHashMap.putCode(1).putMessage("已经撤销！");
            }else if("120".equals(status)){
                jsonHashMap.putCode(1).putMessage("物流已经退回！");
            }else if("5".equals(status) || "10".equals(status)) {
                int n = Db.update("update store_order set status=? where id=?", "110", id);
                if (n > 0) {
                    jsonHashMap.putCode(1).putMessage("撤销订单成功！");
                } else {
                    jsonHashMap.putCode(0).putMessage("撤销订单失败！");
                }
            }else {
                jsonHashMap.putCode(0).putMessage("物流已经接收订单，您不能撤销！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonHashMap.putCode(-1).putMessage("发生错误："+e.toString());
        }
        renderJson(jsonHashMap);
    }
}
