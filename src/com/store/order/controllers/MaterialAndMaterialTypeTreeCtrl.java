package com.store.order.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.MaterialAndMaterialTypeTreeService;
import utils.bean.JsonHashMap;

import java.util.List;

public class MaterialAndMaterialTypeTreeCtrl extends BaseCtrl {

    public void index(){
        JsonHashMap jhm=new JsonHashMap();
        //查询原材料分类
        List materialTypeList= Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from material_type order by sort");
        //查询原材料
        List materialList=Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,attribute_2,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=material.unit) as unit_text,0 as stock_number from material order by sort");
        if(materialList != null && materialList.size() > 0){
            for(Object obj : materialList){
                Record r = (Record) obj;
                r.set("isEdit", true);
            }
        }
        //构建树
        MaterialAndMaterialTypeTreeService service=MaterialAndMaterialTypeTreeService.getMe();
        //构建原材料分类数
        List materialTypeList2=service.sort(materialTypeList);
        //将原材料挂载到原材料分类树下
        List resultList=service.addMaterial2MaterialType(materialTypeList2,materialList);
        jhm.putCode(1).put("tree",resultList);
        renderJson(jhm);
    }

}
