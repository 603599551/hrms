package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.logistics.order.services.StoreScrapSrv;
import com.ss.controllers.BaseCtrl;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流接收门店废弃单
 */
public class StoreScrapCtrl extends BaseCtrl {

    private StoreScrapSrv service = enhance(StoreScrapSrv.class);

    /**
     * 分页查询订单列表
     */
    public void queryListByLogistics(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        String orderNumber = getPara("orderNumber");
        String scrapTime = getPara("scrapTime");
        String status = getPara("state");
        String storeId = getPara("storeId");
        String sql = " from store_scrap where 1=1 ";
        List<Object> params = new ArrayList<>();
        if(orderNumber != null && orderNumber.length() > 0){
            sql += " and order_number=? ";
            params.add(orderNumber);
        }
        if(scrapTime != null && scrapTime.length() > 0){
            sql += " and scrap_time=? ";
            params.add(scrapTime);
        }
        if(status != null && status.length() > 0){
            sql += " and status=? ";
            params.add(status);
        }
        if(storeId != null && storeId.length() > 0){
            sql += " and store_id=? ";
            params.add(storeId);
        }
        Page<Record> result = Db.paginate(pageNum, pageSize, "select * ", sql, params.toArray());
        if(result != null && result.getList().size() > 0){
            for(Record r : result.getList()){
                if("2".equals(r.getStr("status"))){
                    r.set("isEdit", true);
                }else{
                    r.set("isEdit", false);
                }
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", result);
        renderJson(jhm);
    }

    /**
     * 废弃订单详情
     */
    public void showDetailList(){
        String orderId = getPara("id");
        String sql = "select ssm.*, (select name from goods_unit gu where gu.id=ssm.unit) unit_text, (select name from goods_attribute ga where ga.id=ssm.attribute_2) attribute2_text from store_scrap_material ssm where ssm.store_scrap_id=?";
        List<Record> detailList = Db.find(sql, orderId);
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", detailList);
        renderJson(detailList);
    }

    /**
     * 接收订单
     */
    public void accept(){
        String orderId = getPara("id");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        String datetime=DateTool.GetDateTime();
        try {
            Record r=Db.findFirst("select * from store_scrap where id=?",orderId);
            if(r==null){
                jhm.putCode(0).putMessage("查无此订单！");
                renderJson(jhm);
                return;
            }else{
                String status=r.getStr("state");
                if("2".equals(status)){
                    jhm.putCode(0).putMessage("已经接收订单！");
                    renderJson(jhm);
                    return;
                }else if("3".equals(status)){
                    jhm.putCode(0).putMessage("已完成订单不能操作！");
                    renderJson(jhm);
                    return;
                }else if("4".equals(status)){
                    jhm.putCode(0).putMessage("已取消订单不能操作！");
                    renderJson(jhm);
                    return;
                }
            }
            int i = service.accept(usu, orderId);
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
     * 关闭订单
     */
    public void closeOrder(){
        String orderId = getPara("id");
        JsonHashMap jsonHashMap = new JsonHashMap();
        try{
            int n=Db.update("update store_scrap set status=?,close_time=? where id=?",5, DateTool.GetDateTime(), orderId);
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
}
