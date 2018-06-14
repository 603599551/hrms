package com.logistics.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.SelectUtil;
import com.utils.UnitConversion;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 出库单查询
 */
public class OutWarehouseOrderStatisticsCtrl extends BaseCtrl {


    /**
     * 统计出库单
     */
    public void statisticsList(){
        Map map=getParaMap();
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        String[] date=(String[])getParaValues("date");
        String orderNum=getPara("orderCode");
        String outDate=getPara("outDate");
        String storeId=getPara("store");
        String warehouseId=getPara("depot");
        String state=getPara("state");
        String printFlag=getPara("printFlag");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        if("-1".equals(storeId))storeId="";
        if("-1".equals(warehouseId))warehouseId="";
        if("-1".equals(state))state="";
        if("-1".equals(printFlag))printFlag="";

        JsonHashMap jhm=new JsonHashMap();
        String select="select a.id, a.order_number, (select name from store where store.id = a.store_id) as store_text,(select store_color from store where store.id = a.store_id) as store_color, (select name from warehouse where warehouse.id = a.warehouse_id) as warehourse_text, a.out_time, a.status, case a.status when '10' then '新建' when '20' then '保存' when '30' then '出库' when '40' then '完成' end as status_text, (select d.status_color status_color from dictionary d where d.parent_id='1' and d.value=a.status) status_color, b.want_date, b.arrive_date,IFNULL((select sort from print_details where order_id = a.id order by sort desc limit 1,1),0) as print_time";
        String sql=" from warehouse_out_order a inner join store_order b on a.store_order_id = b.id";
        try{
            SelectUtil selectSQL=new SelectUtil(sql);
            if(StringUtils.isNotEmpty(orderNum)) {
                selectSQL.addWhere(" and a.order_number like ?", SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING, "%"+orderNum.toUpperCase() + "%");
            }
            if(date!=null){
                if(date.length==2) {
                    String startTime=date[0];
                    String endTime=date[1];
                    if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                        startTime=startTime+" 00:00:00";
                        endTime=endTime+" 23:59:59";
                        selectSQL.addWhere(" and ?<=a.out_time and a.out_time<=? ", new String[]{startTime,endTime});
                    }
                }else if(date.length==1){
                    String startTime=date[0];
                    if(StringUtils.isNotBlank(startTime) ) {
                        startTime=startTime+" 00:00:00";
                        selectSQL.addWhere(" and ?<=a.out_time  ", SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING, startTime);
                    }
                }
            }
            selectSQL.addWhere(" and substr(a.create_time,1,10)=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,outDate);
            selectSQL.addWhere(" and a.store_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,storeId);
            selectSQL.addWhere(" and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
            selectSQL.addWhere("and a.status=? ",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,state);
            // 下面代码为debug模式，调试成功后，必须注释下面代码，放开上面代码 author:mym
//            selectSQL.in("and status in ",new Object[]{"10","20","30","40"});
            String where=selectSQL.toString();
            Object[] array=selectSQL.getParameters();
            String sqlAll=" from ("+select+where +") a where 1=1 ";
            if("0".equals(printFlag)){
                sqlAll=sqlAll+" and print_time=0";
            }else if("1".equals(printFlag)){
                sqlAll=sqlAll+" and print_time>0";
            }
            sqlAll=sqlAll+" order by out_time desc ";
            Page<Record> result = Db.paginate(pageNum, pageSize,"select * ",sqlAll,array);
            jhm.putCode(1).put("data",result);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示详细信息
     * 订单信息：出库单号、门店、出库日期，仓库名称
     * 出库商品信息：物料代码、名称、规格、单位、批号、数量
     */
    public void showDetailById(){
        String warehouseOutOrderId=getPara("id");//出库订单id
        JsonHashMap jhm=new JsonHashMap();

        Record r=Db.findFirst("select a.order_number,(select name from store where store.id=a.store_id ) as store_name,out_time,(select name from warehouse where warehouse.id=a.warehouse_id) as warehouse_name ,IFNULL((select sort from print_details where order_id = a.id order by sort desc limit 1,1),0) as print_time,IFNULL((select return_reason from store_order where store_order.id=a.store_order_id),'') as return_reason from warehouse_out_order a where id=?",warehouseOutOrderId);

        List<Record> list=Db.find("select code,name,out_unit,box_attr,box_attr_num,unit_big,unit,unit_num,batch_code,send_num from warehouse_out_order_material_detail where warehouse_out_order_id=? order by sort,material_id,batch_code",warehouseOutOrderId);
        List<Map> dataList=new ArrayList<>(list.size());
        for(Record warehouseDetailR:list){
            Map map=new HashMap();
            String attr=UnitConversion.getAttrByOutUnit(warehouseDetailR);
            warehouseDetailR.set("attribute_2_text",attr);

            map.put("code",warehouseDetailR.get("code"));
            map.put("name",warehouseDetailR.get("name"));
            map.put("attribute_2_text",attr);
            map.put("out_unit",warehouseDetailR.get("out_unit"));
            map.put("batch_code",warehouseDetailR.get("batch_code"));
            map.put("send_num",warehouseDetailR.get("send_num"));
            dataList.add(map);
        }
        jhm.putCode(1).put("order",r).put("list",dataList);
        renderJson(jhm);
    }
}
