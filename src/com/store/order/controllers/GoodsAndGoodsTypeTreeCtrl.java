package com.store.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.GoodsAndGoodsTypeTreeService;
import utils.bean.JsonHashMap;

import java.util.List;

public class GoodsAndGoodsTypeTreeCtrl extends BaseCtrl {

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        List goodsTypeList=Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from goods_type order by sort");

        List goodsList=Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,price,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,(select name from goods_attribute where goods_attribute.id=goods.attribute_1) as attribute_1_text,attribute_2,(select name from goods_attribute where goods_attribute.id=goods.attribute_2) as attribute_2_text,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=goods.unit) as unit_text,0 as stock_number from goods order by sort");

        GoodsAndGoodsTypeTreeService service=GoodsAndGoodsTypeTreeService.getMe();
        List goodsTypeList2=service.sort(goodsTypeList);
        List resultList=service.addGoods2GoodsType(goodsTypeList2,goodsList);
        jhm.putCode(1).put("tree",resultList);
        renderJson(jhm);
    }
}
