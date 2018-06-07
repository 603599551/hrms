package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.logistics.order.services.StoreOrderSrv;
import com.ss.controllers.BaseCtrl;
import com.utils.SQLUtil;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
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
                "(select name from dictionary where dictionary.value=store_order.status and dictionary.parent_id='1') as status_text,"+
                "IFNULL((select sort from print_details where order_id = store_order.id order by sort desc limit 1,1),0) as print_time," +
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
            sqlUtil.addWhere("and type=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderType);
            sqlUtil.addWhere("and store_id=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, storeId);
            if(status != null && status.length() > 0){
                if("-1".equals(status)){
                    sqlUtil.addWhere("and status<>?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, "5");
                }else{
                    sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
                }
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
     * 接收订单
     */
    public void accept(){
        String id=getPara("id");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String datetime=DateTool.GetDateTime();
        try {
            Record r=Db.findFirst("select * from store_order where id=?",id);
            if(r==null){
                jhm.putCode(0).putMessage("查无此订单！");
                renderJson(jhm);
                return;
            }else{
                String status=r.getStr("status");
                if("20".equals(status)){
                    jhm.putCode(0).putMessage("已经接收订单！");
                    renderJson(jhm);
                    return;
                }else if("30".equals(status)){
                    jhm.putCode(0).putMessage("已经生成出库单！");
                    renderJson(jhm);
                    return;
                }
            }
            int i=Db.update("update store_order set status=?,accepter_id=?,accept_time=? where id=?", 20, usu.getUserId(),datetime,id);
            if(i>0) {
                jhm.putCode(1).putMessage("接收成功！");
            }else{
                jhm.putCode(0).putMessage("接收失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 生成出库单
     * 要根据原材料所在仓库，分别生成出库单
     */
    public void buildOutOrder(){
        /*
        门店订单id
         */
        String storeOrderId=getPara("id");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();

        try {
            StoreOrderSrv storeOrderSrv = enhance(StoreOrderSrv.class);
            jhm=storeOrderSrv.buildOutWarehouse(storeOrderId,usu);

            renderJson(jhm);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putMessage(e.toString());

            renderJson(jhm);
        }
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
            List<Record> list=Db.find("select *,(select name from goods_unit where goods_unit.id=store_order_material.unit) as unit_text,(select name from goods_attribute where store_order_material.attribute_2=goods_attribute.id) as attribute_2_text from store_order_material where store_order_id=? order by sort ",id);
            jsonHashMap.putCode(1).put("order",r).put("orderDetailsList",list);
        }catch (Exception e){
            e.printStackTrace();
            jsonHashMap.putCode(-1).putMessage(e.toString());
        }
        renderJson(jsonHashMap);
    }

    /**
     * 关闭订单
     */
    public void closeOrder(){
        String id=getPara("id");
        JsonHashMap jsonHashMap=new JsonHashMap();
        try{
            int n=Db.update("update store_order set status=? where id=?",5,id);
            if(n>0){
                jsonHashMap.putCode(1).putMessage("关闭订单成功！");
            }else{
                jsonHashMap.putCode(1).putMessage("查无此订单！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonHashMap.putMessage("发生错误："+e.toString());
        }
        renderJson(jsonHashMap);
    }

    /**
     * 获得批号、第一级规格和第二级规格
     */
    public void getBatchAndAttribute(){
        List<Record> warehouseStockList = Db.find("select ws.*, (select number from goods_attribute where goods_attribute.id=ws.attribute_1) attribute_1_number, (select number from goods_attribute where goods_attribute.id=ws.attribute_2) attribute_2_number from warehouse_stock ws where number > 0");
        Map<String, Map<String, List<Integer>>> materialBatchMap = new HashMap<>();
        if(warehouseStockList != null && warehouseStockList.size() > 0){
            for(Record r : warehouseStockList){
                Map<String, List<Integer>> batchMap = materialBatchMap.get(r.getStr("material_id"));
                if(batchMap == null){
                    batchMap = new HashMap<>();
                    materialBatchMap.put(r.getStr("material_id"), batchMap);
                }
                List<Integer> numberList = new ArrayList<>();
                int number1 = 1;
                if(r.get("attribute_1_number") != null && r.getStr("attribute_1_number").length() > 0){
                    number1 = r.getInt("attribute_1_number");
                }
                int number2 = 1;
                if(r.get("attribute_2_number") != null && r.getStr("attribute_2_number").length() > 0){
                    number2 = r.getInt("attribute_2_number");
                }
                numberList.add(number1);
                numberList.add(number2);
                batchMap.put(r.getStr("batch_code"), numberList);
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", materialBatchMap);
        renderJson(jhm);
    }

}
