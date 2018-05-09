package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 接收门店订单
 */
public class StoreOrderReceiverCtrl extends BaseCtrl {
    /**
     * 根据订单id查询详细信息
     */
    public void showDetailById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select * from store_order_material where store_order_id=? order by sort",id);
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
        try{
            SelectUtil selectUtil=new SelectUtil("select *,substr(create_time,1,16) as create_time_short from store_order ");
            selectUtil.like("and order_number like ?",SelectUtil.NONE,orderCode,SelectUtil.WILDCARD_ASTERISK);
            selectUtil.addWhere("and want_date=? ",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wantDate);
            selectUtil.addWhere("and arrive_date=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,arriveDate);
            selectUtil.addWhere("and status=?",40);
            selectUtil.addWhere("and store_id=?",usu.getUserBean().getDeptId());
            selectUtil.order("order by create_time");
            String sql=selectUtil.toString();
            List<Record> list=Db.find(sql,selectUtil.getParameters());
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
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String storeOrderId=jsonObject.getString("store_order_id");
            JSONArray jsonArray=jsonObject.getJSONArray("list");

            List dataList=new ArrayList();
            for(Object obj:jsonArray){
                JSONObject json=(JSONObject)obj;
                String id=json.getString("id");
                int realSendNum=json.getInteger("real_send_num");
                dataList.add(new Object[]{realSendNum,id});
            }
            int[] numArray=Db.batch("update store_order_material set real_send_num=? where id=?",null,100);
            int sum=0;
            for(int numTemp:numArray){
                sum=sum+numTemp;
            }
            jhm.putCode(1).putMessage("操作成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }
}
