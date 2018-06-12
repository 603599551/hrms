package com.store.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.GoodsAndGoodsTypeTreeService;
import com.utils.HanyuPinyinHelper;
import utils.bean.JsonHashMap;

import java.util.List;

public class GoodsAndGoodsTypeTreeCtrl extends BaseCtrl {

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        //查询商品分类
        List<Record> goodsTypeList=Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from goods_type order by sort");
        //查询商品
        List<Record> goodsList=Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,price,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,(select name from goods_attribute where goods_attribute.id=goods.attribute_1) as attribute_1_text,attribute_2,(select name from goods_attribute where goods_attribute.id=goods.attribute_2) as attribute_2_text,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=goods.unit) as unit_text,1 as number from goods order by sort");
        if(goodsList != null && goodsList.size() > 0){
            for(Record r : goodsList){
                if(r.get("attribute_1_text") == null){
                    r.set("attribute_1_text", "");
                }
                if(r.get("attribute_1") == null){
                    r.set("attribute_1", "");
                }
                if(r.get("attribute_2_text") == null){
                    r.set("attribute_2_text", "");
                }
                if(r.get("attribute_2") == null){
                    r.set("attribute_2", "");
                }
                r.set("search_text",r.getStr("name") + "-" + r.get("code") + "-" + r.get("pinyin"));
            }
        }
        if(goodsTypeList != null && goodsTypeList.size() > 0){
            for(Record r : goodsTypeList){
                r.set("search_text",r.getStr("name") + "-" + HanyuPinyinHelper.getFirstLettersLo(r.getStr("name")));
            }
        }
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
