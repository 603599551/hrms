package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.sun.deploy.panel.JHighDPITable;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.ArrayList;
import java.util.List;

public class GoodsUnitCtrl extends BaseCtrl {
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        JSONObject json;
        try {
            json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请上传数据！");
                renderJson(jhm);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        List<Record> list = Db.find("select * from goods_unit where name=?", json.getString("name"));
        if(list != null && list .size() > 0){
            jhm.putCode(-1).putMessage("单位名称重复！");
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String sortStr=json.getString("sort");
        int sort=1;
        try{
            sort=Integer.parseInt(sortStr);
        }catch (Exception e){
            sort=nextSort(DbUtil.queryMax("goods_unit","sort"));
        }
        String uuid= UUIDTool.getUUID();
        String dateTime= DateTool.GetDateTime();
        Record record=new Record();
        record.set("id",uuid);
        record.set("name",json.getString("name"));
        record.set("sort",sort);
        record.set("creater_id",usu.getUserId());
        record.set("modifier_id",usu.getUserId());
        record.set("create_time",dateTime);
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.save("goods_unit", record);
            if (b) {
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    private int nextSort(int sort){
        int i=sort;
        while(true){
            i++;
            if(i%10==0){
                break;
            }
        }
        return i;
    }

    @Override
    public void deleteById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(id==null || "".equals(id)){
            jhm.putCode(-1);
            jhm.putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        //验证material中的两个类别是否占用，unit
        List<Record> hasList = Db.find("select * from material where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被原料引用，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证sale_goods_material中的两个类别是否占用，unit
        hasList = Db.find("select * from sale_goods_material where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被销售商品原料引用，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证scrap_goods_material中的两个类别是否占用，unit
        hasList = Db.find("select * from scrap_goods_material where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被报废商品原料引用，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证goods中的两个类别是否占用，unit
        hasList = Db.find("select * from goods where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被商品表引用，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证sale_goods中的两个类别是否占用，unit
        hasList = Db.find("select * from sale_goods where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被销售商品引用，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证scrap_goods中的两个类别是否占用，unit
        hasList = Db.find("select * from scrap_goods where unit=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该单位被报废商品引用，不能删除！");
            renderJson(jhm);
            return;
        }
        try {
            Db.deleteById("goods_unit", id);
            jhm.putCode(1);
            jhm.putMessage("删除成功！");
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void showById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        if(org.apache.commons.lang.StringUtils.isEmpty(id)){
            jhm.putCode(-1).putMessage("id不能为空！");
            renderJson(jhm);
            return ;
        }
        try {
            Record storeRecord=Db.findById("goods_unit",id);
            if(storeRecord!=null){
                jhm.putCode(1).put("data",storeRecord);
            }else{
                jhm.putCode(-1).putMessage("查询失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void updateById() {
        JsonHashMap jhm=new JsonHashMap();
        JSONObject json;
        try {
            json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请上传数据！");
                renderJson(jhm);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        List<Record> list = Db.find("select * from goods_unit where id<>? and name=?", json.getString("id"), json.getString("name"));
        if(list != null && list .size() > 0){
            jhm.putCode(-1).putMessage("单位名称重复！");
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String uuid= json.getString("id");
        String dateTime= DateTool.GetDateTime();
        Record record=new Record();
        record.set("id",uuid);
        record.set("name",json.getString("name"));
        record.set("modifier_id",usu.getUserId());
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.update("goods_unit", record);
            if (b) {
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void query() {
        String key=getPara("name");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize= NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm=new JsonHashMap();
        try {
            SQLUtil sql = SQLUtil.initSelectSQL("from goods_unit ");
            sql.addWhere(" and name=? ", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, key);
            sql.append(" order by sort ");
            Page<Record> page = Db.paginate(pageNum, pageSize, "select * ", sql.toString(), sql.getParameterArray());
            jhm.putCode(1).put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void sort(){
        JsonHashMap jhm=new JsonHashMap();
        JSONObject json;
        try {
            json = RequestTool.getJson(getRequest());
            if(json==null){
                jhm.putCode(-1).putMessage("请上传数据！");
                renderJson(jhm);
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            return;
        }
        //{"pid":"1","sort":["3","4","5","6"]}
        JSONArray sortArr = json.getJSONArray("sort");
        int sort = 10;
        List<Record> recordList = new ArrayList<>();
        for(Object s : sortArr){
            if(s != null){
                Record r = new Record();
                r.set("id", s.toString());
                r.set("sort", sort);
                recordList.add(r);
                sort += 10;
            }
        }
        for(Record r : recordList){
            Db.update("goods_unit", r);
        }
        renderJson(jhm);
    }
}
