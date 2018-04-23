package com.store.order.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.GoodsAndGoodsTypeTreeService;
import com.utils.RequestTool;
import utils.bean.JsonHashMap;

import java.util.List;

public class GoodsAndGoodsTypeTreeCtrl extends BaseCtrl {

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        //查询商品分类
        List goodsTypeList=Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from goods_type order by sort");
        //查询商品
        List goodsList=Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,price,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,(select name from goods_attribute where goods_attribute.id=goods.attribute_1) as attribute_1_text,attribute_2,(select name from goods_attribute where goods_attribute.id=goods.attribute_2) as attribute_2_text,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=goods.unit) as unit_text,0 as number from goods order by sort");
        //构建树
        GoodsAndGoodsTypeTreeService service=GoodsAndGoodsTypeTreeService.getMe();
        //构建商品分类数
        List goodsTypeList2=service.sort(goodsTypeList);
        //将商品挂载到商品分类树下
        List resultList=service.addGoods2GoodsType(goodsTypeList2,goodsList);
        jhm.putCode(1).put("tree",resultList);
        renderJson(jhm);
    }

}
