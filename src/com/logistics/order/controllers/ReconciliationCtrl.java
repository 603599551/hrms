package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 门店对账
 * 查看门店入库原材料种类、数量，和物流出库原材料种类、数量
 */
public class ReconciliationCtrl extends BaseCtrl{
    public void index(){
        JsonHashMap jhm=new JsonHashMap();

        String startDate=getPara("startDate");
        String endDate=getPara("endDate");
        String storeId=getPara("storeId");

        String startDateTime=startDate+" 00:00:00";
        String endDateTime=endDate+" 23:59:59";

//        String sql1="select a.code,a.name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,a.receive_num from store_order_material a inner join store_order b on a.store_order_id=b.id where ?<b.create_time and b.create_time<=? and b.status<>'100' and b.id=? ";
        String sql1="select e.code,e.name,e.attribute_text,e.unit_text,case when e.receive_num_sum  is null then 0 end as receive_num_sum,f.send_num_sum,send_num_sum-receive_num_sum as sub_num from ( select a.code,a.name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_text, (select name from goods_unit where goods_unit.id=a.unit) as unit_text,sum(a.receive_num) as receive_num_sum from store_order_material a inner join store_order b on a.store_order_id=b.id where ?<b.create_time and b.create_time<=? and b.store_id=? and b.status<>'100' group by a.code ) e,(select c.code,c.name,(select name from goods_attribute where goods_attribute.id=c.attribute_2) as attribute_text,  (select name from goods_unit where goods_unit.id=c.unit) as unit_text,sum(c.send_num) as send_num_sum from warehouse_out_order_material c inner join   (select a.id from warehouse_out_order a inner join store_order b on a.store_order_id=b.id where ?<b.create_time and b.create_time<=? and b.store_id=? and b.status<>'100') d where c.warehouse_out_order_id=d.id group by c.code ) f where e.code=f.code";

        List<Record> list=Db.find(sql1,startDateTime,endDateTime,storeId,startDateTime,endDateTime,storeId);
        jhm.putCode(1).put("list",list);

        renderJson(jhm);
    }
}
