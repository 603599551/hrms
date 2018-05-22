package com.warehouse.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import com.warehouse.service.WarehouseMovementSrv;
import utils.bean.JsonHashMap;

import java.util.List;

/**
 * 移库操作的controller
 */
public class WarehouseMovementCtrl extends BaseCtrl {

    /**
     * 显示当前库存数量
     */
    public void queryBalanceList(){
        JsonHashMap jhm=new JsonHashMap();
        //前台传入仓库id
        String warehouseId=getPara("outWarehouseId");
        String type2=getPara("type2");
        String keyword=getPara("keyword");

        String sql="select id,(select name from material_type where material_type.id=a.type_2) as type_2_text,code,name,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,batch_code,number from warehouse_stock a ";
        SelectUtil selectUtil=new SelectUtil(sql);
        selectUtil.addWhere("and a.warehouse_id=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,warehouseId);
        selectUtil.addWhere("and a.type_2=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,type2);
        if(keyword!=null && !"".equals(keyword)) {
            selectUtil.addWhere("and (a.code like ? or a.name like ?) ",  keyword+"%");
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
            service.save(jsonObject);
            jhm.putCode(1).putMessage("保存成功！");
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
            service.submit(jsonObject);
            jhm.putCode(1).putMessage("提交成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

}
