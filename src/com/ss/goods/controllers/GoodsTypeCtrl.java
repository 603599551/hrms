package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsTypeCtrl extends BaseCtrl {

    public void getFirstType(){
        JsonHashMap result = new JsonHashMap();
        List<Record> list = Db.find("select * from goods_type where parent_id=0 order by sort");
        Record rootRecord = new Record();
        rootRecord.set("id", "0");
        rootRecord.set("name", "添加一级分类");
        list.add(0, rootRecord);
        result.put("data", list);
        renderJson(result);
    }
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
        List<Record> list = Db.find("select * from goods_type where code=? or name=?", json.getString("code"), json.getString("name"));
        if(list != null && list .size() > 0){
            jhm.putCode(-1).putMessage("商品类别编码或者商品类别名称重复！");
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String sortStr=json.getString("sort");
        int sort=1;
        try{
            sort=Integer.parseInt(sortStr);
        }catch (Exception e){
            sort=nextSort(DbUtil.queryMax("goods_type","sort"));
        }
        String uuid= UUIDTool.getUUID();
        String dateTime= DateTool.GetDateTime();
        String parent_id = json.getString("parent_id");
        if(parent_id == null || parent_id.length() <= 0){
            parent_id = "0";
        }
        Record record=new Record();
        record.set("id",uuid);
        record.set("parent_id",parent_id);
        record.set("code",json.getString("code"));
        record.set("name",json.getString("name"));
        record.set("sort",sort);
        record.set("desc",json.getString("desc"));
        record.set("showChild",0);
        record.set("creater_id",usu.getUserId());
        record.set("modifier_id",usu.getUserId());
        record.set("create_time",dateTime);
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.save("goods_type", record);
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
        //验证该分类下是否有子分类，即是否有parent_id和传入id相同的
        List<Record> hasList = Db.find("select * from goods_type where parent_id=?", id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("该分类下有二级分类，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证goods中的两个类别是否占用，type_1和type_2
        hasList = Db.find("select * from goods where type_1=? or type_2=?", id, id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("商品表占用该类别，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证sale_goods中的两个类别是否占用，type_1和type_2
        hasList = Db.find("select * from sale_goods where type_1=? or type_2=?", id, id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("销售商品表占用该类别，不能删除！");
            renderJson(jhm);
            return;
        }
        //验证scrap_goods中的两个类别是否占用，type_1和type_2
        hasList = Db.find("select * from scrap_goods where type_1=? or type_2=?", id, id);
        if(hasList != null && hasList .size() > 0){
            jhm.putCode(-1).putMessage("报废商品表占用该类别，不能删除！");
            renderJson(jhm);
            return;
        }
        try {
            Db.deleteById("goods_type", id);
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
            Record storeRecord=Db.findById("goods_type",id);
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
        List<Record> list = Db.find("select * from goods_type where id<>? and (code=? or name=?)", json.getString("id"), json.getString("code"), json.getString("name"));
        if(list != null && list .size() > 0){
            jhm.putCode(-1).putMessage("商品类别编码或者商品类别名称重复！");
            renderJson(jhm);
            return;
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String uuid= json.getString("id");
        String dateTime= DateTool.GetDateTime();
        String parent_id = json.getString("parent_id");
        if(parent_id == null || parent_id.length() <= 0){
            parent_id = "0";
        }
        Record record=new Record();
        record.set("id",uuid);
        record.set("parent_id",parent_id);
        record.set("code",json.getString("code"));
        record.set("name",json.getString("name"));
        record.set("desc",json.getString("desc"));
        record.set("modifier_id",usu.getUserId());
        record.set("modify_time",dateTime);
        try {
            boolean b = Db.update("goods_type", record);
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
        List<Record> goodsTypeList = Db.find("select * from goods_type order by sort");
        List<Record> firstList = new ArrayList<>();
        Map<String, Record> firstMap = new HashMap<>();
        List<Record> secondList = new ArrayList<>();
        for(Record r : goodsTypeList){
            if(!"0".equals(r.getStr("parent_id"))){
                r.set("showChild", false);
                secondList.add(r);
            }else{
                //一级分类，当showChild为1时，默认展开，0时不展开
                if("1".equals(r.getStr("showChild"))){
                    r.set("showChild", true);
                }else{
                    r.set("showChild", false);
                }
                r.set("children", new ArrayList<>());
                firstList.add(r);
                firstMap.put(r.getStr("id"), r);
            }
        }
        for(Record r : secondList){
            Record firstR = firstMap.get(r.getStr("parent_id"));
            List<Record> list = firstR.get("children");
            if(list != null && list.size() > 0){
                list.add(r);
            }else{
                list = new ArrayList<>();
                list.add(r);
                firstR.set("children", list);
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", firstList);
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
        String pid = json.getString("pid");
        JSONArray sortArr = json.getJSONArray("sort");
        int sort = 10;
        List<Record> recordList = new ArrayList<>();
        boolean isFirst = "0".equals(pid);
        for(Object s : sortArr){
            if(s != null){
                Record r = new Record();
                r.set("id", s.toString());
                r.set("sort", sort);
                if(isFirst){
                    r.set("showChild", "1");
                    isFirst = false;
                }else{
                    r.set("showChild", "0");
                }
                recordList.add(r);
                sort += 10;
            }
        }
        for(Record r : recordList){
            Db.update("goods_type", r);
        }
        renderJson(jhm);
    }

}
