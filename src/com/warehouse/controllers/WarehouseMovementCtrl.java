package com.warehouse.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import com.warehouse.service.WarehouseMovementSrv;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 移库操作的controller
 */
public class WarehouseMovementCtrl extends BaseCtrl {
    /**
     * 移库列表页面
     */
    public void queryList(){
        JsonHashMap jhm=new JsonHashMap();
        String orderNumber=getPara("orderNumber");
        String outWarehouseId=getPara("outWarehouseId");
        String inWarehouseId=getPara("inWarehouseId");
        String date=getPara("date");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        if("-1".equals(status))status="";
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        String sql="select id,order_number,create_time,(select name from warehouse where warehouse.id=a.out_warehouse_id) as out_warehouse_name ,(select name from warehouse where warehouse.id=a.in_warehouse_id) as in_warehouse_name, case status when '20' then '已保存' when '30' then '已确定' end as status_text,(select name from staff where staff.id=a.creater_id) as creater_name ,status  ";
        SelectUtil selectUtil=new SelectUtil("from warehouse_movement_order a");
        selectUtil.addWhere("and out_warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,outWarehouseId);
        selectUtil.addWhere("and in_warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,inWarehouseId);
        selectUtil.addWhere("and substr(create_time,1,10)=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,date);
        selectUtil.addWhere("and status=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,status);
        selectUtil.like("and order_number like ? ",SelectUtil.NONE,orderNumber,SelectUtil.WILDCARD_ASTERISK);
        selectUtil.order("order by create_time desc ,out_warehouse_id,id ");
        String sqlExt=selectUtil.toString();
        Object[] parameterArray=selectUtil.getParameters();
        Page<Record> page=Db.paginate(pageNum,pageSize,sql,sqlExt,parameterArray);
        for(Record r:page.getList()){
            int statusOfR=r.getInt("status");
            boolean isEdit=true;
            if(statusOfR==20){
                isEdit=true;
            }else if(statusOfR==30){
                isEdit=false;
            }
            r.set("isEdit",isEdit);
        }
        jhm.putCode(1).put("data",page);
        renderJson(jhm);
    }
    /**
     * 显示当前库存数量
     */
    public void queryBalanceList(){
        JsonHashMap jhm=new JsonHashMap();
        //前台传入仓库id
        String warehouseId=getPara("outWarehouseId");
        String type2=getPara("type2");
        String keyword=getPara("keyword");
        String ids=getPara("ids");

        if("-1".equals(type2))type2=null;

        if(warehouseId==null || "".equals(warehouseId)){
            jhm.putCode(0).putMessage("请选择移出仓库！");
            renderJson(jhm);
            return;
        }

        String sql="select id,(select name from material_type where material_type.id=a.type_2) as type_2_text,code,name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,batch_code,number as warehouse_stock_num,0 as movement_num from warehouse_stock a ";
        SelectUtil selectUtil=new SelectUtil(sql);
        selectUtil.addWhere("and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
        selectUtil.addWhere("and a.type_2=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,type2);
        if(keyword!=null && !"".equals(keyword)) {
            String keyword2=keyword+"%";
            selectUtil.addWhere("and (a.code like ? or a.name like ? or a.pinyin like ?) ",  new String[]{keyword2,keyword2,keyword2});
        }
        if(StringUtils.isNotBlank(ids)){
            String[] idArray=ids.split(",");
            selectUtil.in("and id not in ",idArray);
        }
        selectUtil.order("order by warehouse_id,material_id,batch_code");
        List<Record> list= Db.find(selectUtil.toString(),selectUtil.getParameters());
        jhm.putCode(1).put("list",list);
        renderJson(jhm);
    }
    public void save(){
        JSONObject jsonObject=RequestTool.getJson(getRequest());
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        jsonObject.put("usu",usu);

        JsonHashMap jhm=new JsonHashMap();

        WarehouseMovementSrv service=enhance(WarehouseMovementSrv.class);
        try {
            jhm=service.save(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void submit(){
        JSONObject jsonObject=RequestTool.getJson(getRequest());
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        jsonObject.put("usu",usu);

        JsonHashMap jhm=new JsonHashMap();

        WarehouseMovementSrv service=enhance(WarehouseMovementSrv.class);
        try {
            jhm=service.submit(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 根据id显示详细信息
     */
    public void showDetailById(){
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");
//        Record r=Db.findById("warehouse_movement_order",id);
        Record r=Db.findFirst("select *,(select name from warehouse where warehouse.id=a.in_warehouse_id) as in_warehouse_name,(select name from warehouse where warehouse.id=a.out_warehouse_id) as out_warehouse_name,(select name from staff where staff.id=a.creater_id) as creater_name  from warehouse_movement_order a where id=?",id);
        String sql="SELECT a.id,(SELECT name FROM material_type WHERE material_type.id=a.type_2) AS type_2_text,a.code,a.name,(SELECT name FROM goods_attribute WHERE goods_attribute.id=a.attribute_2) AS attribute_2_text,(SELECT name FROM goods_unit WHERE goods_unit.id=a.unit) AS unit_text,a.batch_code,a.number AS warehouse_stock_num,b.number AS movement_num FROM warehouse_stock a inner JOIN warehouse_movement_order_material b ON a.material_id=b.material_id and a.batch_code=b.batch_code WHERE warehouse_movement_order_id=? and a.warehouse_id=? ORDER BY b.sort,b.material_id,b.batch_code";
//        String sql="select *,number as movement_num from warehouse_movement_order_material where warehouse_movement_order_id=? order by sort,material_id,batch_code";
        List<Record> list=Db.find(sql,id,r.getStr("out_warehouse_id"));

        String nbsp="　　　　　";
        StringBuilder msg=new StringBuilder("");
        msg.append("移库单号：");
        msg.append(r.getStr("order_number"));
        msg.append(nbsp);
        msg.append("移出仓库：");
        msg.append(r.getStr("out_warehouse_name"));
        msg.append(nbsp);
        msg.append("移入仓库：");
        msg.append(r.getStr("in_warehouse_name"));
        msg.append(nbsp);
        msg.append("创建人：");
        msg.append(r.getStr("creater_name"));

        jhm.putCode(1).put("warehouse_movement_order",r).put("list",list).put("info",msg.toString());
        renderJson(jhm);;
    }

    /**
     * 撤销确定
     */
    public void revokeSubmit(){
        String id=getPara("id");
        UserSessionUtil usu=new UserSessionUtil(getRequest());

        JsonHashMap jhm=new JsonHashMap();

        WarehouseMovementSrv service=enhance(WarehouseMovementSrv.class);
        try {
            jhm=service.revokeSubmit(id,usu);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
