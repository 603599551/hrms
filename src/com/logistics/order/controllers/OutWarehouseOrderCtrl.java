package com.logistics.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.logistics.order.services.OutWarehouseOrderSrv;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 物流出库单
 */
public class OutWarehouseOrderCtrl extends BaseCtrl{

    /**
     * 查询出库单
     */
    public void queryList(){
        String orderNum=getPara("orderCode");
        String outDate=getPara("outDate");
        String storeId=getPara("store");
        String warehouseId=getPara("depot");

        if("-1".equals(storeId))storeId="";
        if("-1".equals(warehouseId))warehouseId="";

        JsonHashMap jhm=new JsonHashMap();
        String sql="select a.id, a.order_number, (select name from store where store.id = a.store_id) as store_text, (select name from warehouse where warehouse.id = a.warehouse_id) as warehourse_text, a.out_time, a.status, case a.status when '10' then '新建' when '20' then '保存' when '30' then '出库' when '40' then '完成' end as status_text, (select d.status_color status_color from dictionary d where d.parent_id='1' and d.value=a.status) status_color, b.want_date, b.arrive_date from warehouse_out_order a inner join store_order b on a.store_order_id = b.id";
        try{
            SelectUtil selectSQL=new SelectUtil(sql);
            if(StringUtils.isNotEmpty(orderNum)) {
                selectSQL.addWhere(" and a.order_number like ?", SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING, "%"+orderNum.toUpperCase() + "%");
            }
            selectSQL.addWhere(" and substr(a.out_time,1,10)=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,outDate);
            selectSQL.addWhere(" and a.store_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,storeId);
            selectSQL.addWhere(" and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
            selectSQL.in("and a.status in ",new Object[]{"10","20"});
            // 下面代码为debug模式，调试成功后，必须注释下面代码，放开上面代码 author:mym
//            selectSQL.in("and status in ",new Object[]{"10","20","30","40"});
            selectSQL.order(" order by a.out_time");
            String sqlAll=selectSQL.toString();
            List<Record> list=Db.find(sqlAll,selectSQL.getParameters());
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 根据出库单id显示详细信息
     */
    public void showDetailsById(){
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            OutWarehouseOrderSrv service = enhance(OutWarehouseOrderSrv.class);
            List list = service.showDetailsById(id);
            jhm.putCode(1).put("list", list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }

    /**
     * 保存出库单中的物料出库数量
     */
    public void save(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            JSONObject json=RequestTool.getJson(getRequest());
            String id=json.getString("id");
            JSONArray array=json.getJSONArray("list");
            if(array==null || array.isEmpty()){
                jhm.putCode(0).putMessage("传入list数据为空！");
            }else {
                OutWarehouseOrderSrv service = enhance(OutWarehouseOrderSrv.class);
                jhm = service.save(id, array);
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /**
     * 保存出库单中的物料出库数，并出库
     */
    public void out(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            JSONObject json=RequestTool.getJson(getRequest());
            String id=json.getString("id");
            JSONArray array=json.getJSONArray("list");
            OutWarehouseOrderSrv service = enhance(OutWarehouseOrderSrv.class);
            jhm=service.out(id,array);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 取消出库
     */
    public void cancelOut(){

    }

    /**
     * 打印出库单
     * 记录打印人的信息，并生成pdf
     */
    public void print(){

    }

    /**
     * 显示打印历史信息
     */
    public void showPrintHistoryList(){

    }

    /**
     * 统计出库单
     */
    public void statisticsList(){

    }
}
