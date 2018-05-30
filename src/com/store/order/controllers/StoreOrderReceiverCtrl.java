package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.StoreOrderReceiveSrv;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 接收门店订单
 */
public class StoreOrderReceiverCtrl extends BaseCtrl {
    static final String showDetailByIdSQL="SELECT a1.*,(case when b1.number is null then 0 end )as stock_num FROM ( SELECT a.id,a.code,( SELECT name FROM goods_attribute WHERE id=a.attribute_2) AS attribute_2_text,a.name,( SELECT name FROM goods_unit WHERE id=a.unit) AS unit_text,a.send_num AS order_material_num,b.send_num as receive_num,a.material_id,a.sort,a.store_order_id FROM store_order_material a,warehouse_out_order_material b,warehouse_out_order c WHERE a.store_order_id=c.store_order_id AND c.id=b.warehouse_out_order_id AND a.material_id=b.material_id) a1 LEFT JOIN store_stock b1 ON a1.material_id=b1.material_id where a1.store_order_id=? ORDER BY a1.sort  ";
    /**
     * 根据订单id查询详细信息
     */
    public void showDetailById() {

        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find(showDetailByIdSQL,id);
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 查询接收订单
     * 必须条件：当前登录人所在门店id
     */
    @Override
    public void query() {
        String orderCode=getPara("order_code");
        String wantDate=getPara("wantDate");
        String arriveDate=getPara("arriveDate");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();

        String sql="select a.id as warehouse_out_order_id,b.id as store_order_id,a.order_number as warehouse_out_order_number,substr(a.out_time,1,16) as out_time,b.order_number as store_order_number,substr(b.create_time,1,16) as create_time_short,b.arrive_date from warehouse_out_order a left join store_order b on a.store_order_id=b.id ";
        try{
            SelectUtil selectUtil=new SelectUtil(sql);
//            selectUtil.like("and order_number like ?",SelectUtil.NONE,orderCode,SelectUtil.WILDCARD_PERCENT);
//            selectUtil.addWhere("and want_date=? ",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wantDate);
//            selectUtil.addWhere("and arrive_date=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,arriveDate);
            selectUtil.addWhere("and b.status=?",40);
            selectUtil.addWhere("and b.store_id=?",usu.getUserBean().getDeptId());
            selectUtil.order("order by b.arrive_date");
            String sqlExe=selectUtil.toString();
            List<Record> list=Db.find(sqlExe,selectUtil.getParameters());
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 接收入库
     */
    public void accept(){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String storeOrderId=jsonObject.getString("id");
            JSONArray jsonArray=jsonObject.getJSONArray("list");

            StoreOrderReceiveSrv service=enhance(StoreOrderReceiveSrv.class);
            service.accept(storeOrderId,jsonArray,usu);
            jhm.putCode(1).putMessage("操作成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }
}
