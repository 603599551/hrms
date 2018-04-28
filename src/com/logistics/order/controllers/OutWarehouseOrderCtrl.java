package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.logistics.order.services.OutWarehouseOrderSrv;
import com.ss.controllers.BaseCtrl;
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
        String orderNum=getPara("orderNum");
        String outDate=getPara("outDate");
        String storeId=getPara("storeId");
        String warehouseId=getPara("warehouseId");

        JsonHashMap jhm=new JsonHashMap();
        String sql="select id,order_number,(select name from store where store.id=warehouse_out_order.store_id) as store_text ,(select name from warehouse where warehouse.id=warehouse_out_order.warehouse_id) as warehourse_text ,out_time,status,case status when '10' then '新建' when '20' then '出库' end as status_text from warehouse_out_order";
        try{
            SelectUtil selectSQL=new SelectUtil(sql);
            if(StringUtils.isNotEmpty(orderNum)) {
                selectSQL.addWhere(" and order_number like ?", SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING, orderNum + "%");
            }
            selectSQL.addWhere(" and substr(out_time,1,10)=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,outDate);
            selectSQL.addWhere(" and store_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,storeId);
            selectSQL.addWhere(" and warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
            selectSQL.order(" order by out_time");
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
            OutWarehouseOrderSrv service = new OutWarehouseOrderSrv();
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

    }
    /**
     * 保存出库单中的物料出库数，并出库
     */
    public void out(){

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
    public void showPrintHistory(){

    }
}
