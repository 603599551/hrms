package com.ss.goods.controllers;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化表单数据
 */
public class GoodsInitFormCtrl extends BaseCtrl {

    public void showUnitList(){
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select id,name from goods_unit order by sort ,modify_time");
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    /**
     * 显示商品库存类型
     */
    public void showWmTypeList(){
        String type=getPara("type");
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select id,name from wm_type where type='goods' order by sort ");
            if("query".equals(type)){
                Record first=new Record();
                first.set("id","0");
                first.set("name","请选择库存类型");
                list.add(0,first);
            }
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示原材料库存类型
     */
    public void showMaterialWmTypeList(){
        String type=getPara("type");
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select id,name from wm_type order by sort ");
            if("query".equals(type)){
                Record first=new Record();
                first.set("id","0");
                first.set("name","请选择库存类型");
                list.add(0,first);
            }
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示商品配方是否编辑
     */
    public void showBomStatusList(){
        JsonHashMap jhm=new JsonHashMap();
        List list=new ArrayList();
        Map a1=new HashMap();
        a1.put("id","-1");
        a1.put("name","请选择配方状态");

        Map a=new HashMap();
        a.put("id","0");
        a.put("name","未配置");
        Map b=new HashMap();
        b.put("id","1");
        b.put("name","已配置");
        list.add(a1);
        list.add(a);
        list.add(b);
        jhm.putCode(1).put("list",list);
        renderJson(jhm);

    }
}
