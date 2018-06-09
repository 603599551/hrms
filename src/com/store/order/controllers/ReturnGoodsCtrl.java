package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.store.order.services.MaterialAndMaterialTypeTreeService;
import com.store.order.services.ReturnGoodsService;
import com.utils.Constants;
import com.utils.HanyuPinyinHelper;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;

public class ReturnGoodsCtrl extends BaseCtrl implements Constants{

    private ReturnGoodsService returnGoodsService = enhance(ReturnGoodsService.class);

    /**
     * 通过门店id和状态查询退货单信息
     * 如果没有传status，则只按照门店id查询
     */
    public void queryOrderListByStore(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(this.getRequest());
        Page<Record> orderList = null;
        String status = getPara("status");
        String orderId = getPara("orderCode");

        String sql = " from return_order ro, dictionary d, store s where s.id=ro.store_id and d.value=ro.status and d.parent_id=500 and ro.store_id=?";
        List<Object> params = new ArrayList<>();
        params.add(usu.getUserBean().get("store_id"));
        if(status != null && status.length() > 0 && !"-1".equalsIgnoreCase(status)){
            sql += " and ro.status=? ";
            params.add(status);
        }else{
            sql += " and ro.status<>? ";
            params.add("5");
        }
        if(orderId != null && orderId.length() > 0 && !"null".equalsIgnoreCase(orderId)){
            sql += " and ro.order_number like ? ";
            params.add("%" + orderId + "%");
        }
        sql += " order by ro.return_time desc";
        orderList = Db.paginate(pageNum, pageSize, "select ro.*, d.name dname, d.status_color status_color, s.name store_name ", sql, params.toArray());
        if(orderList != null && orderList.getList().size() > 0){
            for(Record r : orderList.getList()){
                if("1".equals(r.getStr("status"))){
                    r.set("isEdit", true);
                }else{
                    r.set("isEdit", false);
                }
            }
        }
        jhm.put("orderList", orderList);
        renderJson(jhm);
    }

    /**
     * 物流通过状态查询退货单信息
     * 如果没有传status，则查询所有
     */
    public void queryOrderListByLogistics(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(this.getRequest());
        Page<Record> orderList = null;
        String status = getPara("state");
        String orderId = getPara("orderCode");
        String store = getPara("store");

        String sql = " from return_order ro, dictionary d, store s where s.id=ro.store_id and d.value=ro.status and d.parent_id=500 ";
        List<Object> params = new ArrayList<>();
        if(status != null && status.length() > 0 && !"-1".equalsIgnoreCase(status)){
            sql += " and ro.status=? ";
            params.add(status);
        }else{
            sql += " and ro.status<>? ";
            params.add("5");
        }
        if(orderId != null && orderId.length() > 0 && !"null".equalsIgnoreCase(orderId)){
            sql += " and ro.order_number like ? ";
            params.add("%" + orderId + "%");
        }
        if(store != null && store.length() > 0 && !"null".equalsIgnoreCase(store) && !"-1".equals(store)){
            sql += " and s.id=? ";
            params.add(store);
        }
        sql += " order by ro.return_time desc";
        orderList = Db.paginate(pageNum, pageSize, "select ro.*, d.name dname, d.status_color status_color, s.name store_name ", sql, params.toArray());
        if(orderList != null && orderList.getList().size() > 0){
            for(Record r : orderList.getList()){
                if("1".equals(r.getStr("status"))){
                    r.set("isEdit", true);
                }else{
                    r.set("isEdit", false);
                }
            }
        }
        jhm.put("orderList", orderList);
        renderJson(jhm);
    }

    /**
     * 门店显示退货单详情，当status=1（未接收）可以修改原料数据
     */
    public void showDetailByOrderIdAndStore(){
        String orderId = getPara("orderId");
        boolean isRecive = false;
        boolean isFinish = false;
        boolean isClose = false;
        UserSessionUtil usu = new UserSessionUtil(this.getRequest());
        List<Record> materialOrderList = Db.find("select rom.*, ro.status rostatus, (select name from goods_unit gu where gu.id=rom.unit) unit_text from return_order_material rom, return_order ro where rom.return_order_id=ro.id and return_order_id=? and ro.store_id=? order by sort", orderId, usu.getUserBean().get("store_id"));
        if(materialOrderList != null && materialOrderList.size() > 0){
            for(Record r : materialOrderList){
                if("1".equals(r.getStr("rostatus"))){
                    isClose = true;
                }else if("2".equals(r.getStr("rostatus"))){
                }else if("3".equals(r.getStr("rostatus"))){
                }
                r.set("id", r.getStr("material_id"));
                r.set("number", r.getInt("return_num"));
                r.set("remark", r.getStr("reason"));
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("materialOrderList", materialOrderList).put("isRecive", isRecive).put("isFinish", isFinish).put("isClose", isClose);
        renderJson(jhm);
    }

    /**
     * 物流显示退货单详情，当status=2（已接收）可以修改原料数据
     */
    public void showDetailByOrderIdAndLogistics() throws ParseException {
        String orderId = getPara("orderId");
        //warehouse_stock  batch_code
        String today = DateTool.GetDate();
        String one_year_before = sdf.format(new Date(sdf.parse(today).getTime() - ONE_YEAR_TIME));
        List<Record> batchCodeMaterialList = Db.find("select * from warehouse_stock where create_time between ? and ?", one_year_before, today);
        Map<String, List<Record>> batchCodeMaterialMap = new HashMap<>();
        if(batchCodeMaterialList != null && batchCodeMaterialList.size() > 0){
            for(Record r : batchCodeMaterialList){
                List<Record> list = batchCodeMaterialMap.get(r.getStr("material_id"));
                if(list == null){
                    list = new ArrayList<>();
                    batchCodeMaterialMap.put(r.getStr("material_id"), list);
                }
                Record listR = new Record();
                listR.set("id", r.getStr("batch_code"));
                listR.set("name", r.getStr("batch_code"));
                list.add(listR);
            }
        }
        List<Record> materialOrderList = Db.find("select rom.*, s.name store_name, ro.status rostatus, ro.order_number order_number, (select name from goods_attribute ga where ga.id=rom.attribute_2) attribute2_text, (select name from goods_unit gu where gu.id=rom.unit) unit_text from return_order_material rom, return_order ro, store s where s.id=ro.store_id and rom.return_order_id=ro.id and return_order_id=? order by sort", orderId);
        boolean isRecive = false;
        boolean isFinish = false;
        boolean isClose = false;
        if(materialOrderList != null && materialOrderList.size() > 0){
            for(Record r : materialOrderList){
                if("1".equals(r.getStr("rostatus"))){
                    isRecive = true;
                }else if("2".equals(r.getStr("rostatus"))){
                    isFinish = true;
                    isClose = true;
                }else if("3".equals(r.getStr("rostatus"))){
                }
                r.set("id", r.getStr("material_id"));
                r.set("number", r.getInt("return_num"));
                r.set("remark", r.getStr("reason"));
                if(r.getStr("batch_code") != null){
                    r.set("batch_code_text", r.getStr("batch_code"));
                }else{
                    r.set("batch_code_text", "");
                }
                List<Record> list = batchCodeMaterialMap.get(r.getStr("material_id"));
                if(list == null){
                    List<Record> batchCodeMaterialByIdList = Db.find("select * from warehouse_stock where material_id=?", r.getStr("material_id"));
                    list = new ArrayList<>();
                    if(batchCodeMaterialByIdList != null && batchCodeMaterialByIdList.size() > 0){
                        for(Record mbiR : batchCodeMaterialByIdList){
                            Record listR = new Record();
                            listR.set("id", mbiR.getStr("batch_code"));
                            listR.set("name", mbiR.getStr("batch_code"));
                            list.add(listR);
                        }
                    }
                }
                r.set("batch_code", list);
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("materialOrderList", materialOrderList).put("isRecive", isRecive).put("isFinish", isFinish).put("isClose", isClose);
        renderJson(jhm);
    }

    /**
     * 获得原材料树
     */
    public void getMaterialTree(){
        UserSessionUtil usu = new UserSessionUtil(this.getRequest());
        JsonHashMap jhm=new JsonHashMap();
        List<Record> materialTypeList = Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from material_type order by sort");
        String sql = "select id," +
                "       code," +
                "       name," +
                "       CONCAT(name, '(', code, ')') as label," +
                "       pinyin," +
                "       wm_type," +
                "       (select name from wm_type where wm_type.id = wm_type) as wm_type_text," +
                "       attribute_1," +
                "       (select name" +
                "          from goods_attribute ga" +
                "         where ga.id = material.attribute_2) attribute_2," +
                "       type_1," +
                "       type_2," +
                "       unit," +
                "       (select name from goods_unit where goods_unit.id = material.unit) as unit_text," +
                "       (select number from store_stock ss where ss.material_id=material.id and ss.store_id=?) as stock_number" +
                "  from material" +
                " where status = 1" +
                " order by sort";
        List<Record> materialList = Db.find(sql, usu.getUserBean().get("store_id"));
        String orderId = getPara("orderId");
        if(!(orderId != null && orderId.length() > 0)){
            orderId = "error";
        }
        List<Record> storeOrderMaterialList = Db.find("select * from store_order_material where store_order_id=?", orderId);
        Map<String, Record> storeOrderMaterialMap = new HashMap<>();
        if(storeOrderMaterialList != null && storeOrderMaterialList.size() > 0){
            for(Record r : storeOrderMaterialList){
                storeOrderMaterialMap.put(r.getStr("material_id"), r);
            }
        }
        Map<String, Record> materialMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialMap.put(r.getStr("id"), r);
                //search_text: '原材料名称-编号-拼音头'
                r.set("search_text",r.getStr("name") + "-" + r.get("code") + "-" + r.get("pinyin"));
                r.set("number", 1);
                r.set("remark", "");
                Record storeOrderMaterial = storeOrderMaterialMap.get(r.getStr("id"));
                if(storeOrderMaterial != null){
                    r.set("isEdit", true);
                }else{
                    r.set("isEdit", true);
                }
            }
        }
        if(materialTypeList != null && materialTypeList.size() > 0){
            for(Record r : materialTypeList){
                r.set("search_text",r.getStr("name") + "-" + HanyuPinyinHelper.getFirstLettersLo(r.getStr("name")));
            }
        }
        List<Record> returnOrderMaterialList = Db.find("select * from return_order_material where return_order_id=? order by sort", orderId);
        if(returnOrderMaterialList != null && returnOrderMaterialList.size() > 0){
            for(Record r : returnOrderMaterialList){
                Record materialR = materialMap.get(r.getStr("material_id"));
                materialR.set("number", r.getInt("return_num"));
                materialR.set("remark", r.getStr("reason"));
            }
        }
        //构建树
        MaterialAndMaterialTypeTreeService service = MaterialAndMaterialTypeTreeService.getMe();
        //构建原材料分类数
        List materialTypeList2 = service.sort(materialTypeList);
        //将原材料挂载到原材料分类树下
        List resultList = service.addMaterial2MaterialType(materialTypeList2, materialList);
        jhm.putCode(1).put("tree", resultList).put("returnOrderMaterialList", returnOrderMaterialList);
        renderJson(jhm);
    }

    /**
     * 接收退货单
     */
    public void doReturn(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            returnGoodsService.doReturn(RequestTool.getJson(getRequest()));
            jhm.putMessage("接收成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage("接收失败！");
        }
        renderJson(jhm);
    }
    /**
     * 完成退货单
     */
    public void finishOrder(){
        returnGoodsService.finishOrder(RequestTool.getJson(getRequest()), new UserSessionUtil(getRequest()));
        JsonHashMap jhm = new JsonHashMap();
        jhm.putMessage("完成退货单！");
        renderJson(jhm);
    }

    /**
     * 取消退货单
     */
    public void cancleOrder(){
        String orderId = getPara("id");
        String type = getPara("type");
        Record order = Db.findById("return_order", orderId);
        if(order != null){
            if("s".equals(type)){
                order.set("status", 4);
            }else if("l".equals(type)){
                order.set("status", 5);
            }
            Db.update("return_order", order);
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.putMessage("取消成功！");
        renderJson(jhm);
    }

    /**
     * 创建退货单
     */
    public void createOrder(){
        JsonHashMap jhm = new JsonHashMap();
        JSONObject jsonObject = RequestTool.getJson(getRequest());
        JSONArray list = jsonObject.getJSONArray("list");
        String orderId = jsonObject.getString("orderId");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        if(list != null && list.size() > 0){
            if(orderId != null && orderId.length() > 0){
                try {
                    updateOrder(orderId, list, usu);
                    jhm.putMessage("修改成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    jhm.putCode(0).putMessage("修改失败！");
                }
            }else{
                try {
                    addOrder(list, usu, DateTool.GetDateTime());
                    jhm.putMessage("新增成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    jhm.putCode(0).putMessage("新增失败！");
                }
            }
        }else{
            jhm.putCode(0).putMessage("原材料列表不能为空！");
        }
        renderJson(jhm);
    }

    public void getStoreOrder(){
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(this.getRequest());
        List<Record> storeOrderList = Db.find("select * from store_order where status=? and store_id=?", "40", usu.getUserBean().get("store_id"));
        List<Record> returnList = new ArrayList<>();
        if(storeOrderList != null && storeOrderList.size() > 0){
            for(Record r : storeOrderList){
                Record returnR = new Record();
                returnR.set("id", r.getStr("id"));
                returnR.set("name", r.getStr("order_number"));
                returnList.add(returnR);
            }
            jhm.put("orderList", returnList);
        }else{
            jhm.putCode(0).putMessage("没有可以引单退货的订单！");
        }
        renderJson(jhm);
    }

    public void introductionOrder(){
        String orderId = getPara("orderId");
        List<Record> materialList =  Db.find("select * from store_order_material where store_order_id=?", orderId);
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                r.set("number", 0);
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("materialList", materialList);
        renderJson(jhm);
    }

    /**
     * 新建退货单
     * @param list 前台数据，原材料id和数量
     * @param usu 登录信息
     * @param returnTime 退货时间
     * @throws Exception
     */
    private void addOrder(JSONArray list, UserSessionUtil usu, String returnTime) throws Exception{
        List<Record> materialList = Db.find("select * from material where status=1");
        Map<String, Record> materialMap = new HashMap<>();
        for(Record r : materialList){
            materialMap.put(r.getStr("id"), r);
        }
        List<Record> saveList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            JSONObject obj = list.getJSONObject(i);
            if("0".equals(obj.getString("number"))){
                continue;
            }
            Record r = materialMap.get(obj.getString("id"));
            r.remove("creater_id","modifier_id","create_time","modify_time","status","desc","shelf_life","storage_condition");
            r.set("material_id", r.get("id"));
            r.set("return_num", obj.getString("number"));
            r.set("reason", obj.getString("remark"));
            r.set("id", UUIDTool.getUUID());
            saveList.add(r);
        }
        returnGoodsService.addOrder(saveList, usu, returnTime);
    }

    /**
     * 修改退货单
     * @param list 前台数据，原材料id和数量
     * @param usu 登录信息
     * @throws Exception
     */
    private void updateOrder(String orderId, JSONArray list, UserSessionUtil usu) throws Exception{
        List<Record> materialList = Db.find("select * from material where status=1");
        Map<String, Record> materialMap = new HashMap<>();
        for(Record r : materialList){
            materialMap.put(r.getStr("id"), r);
        }
        List<Record> returnOrderList = Db.find("select * from return_order_material where return_order_id=?", orderId);
        Map<String, Record> orderMap = new HashMap<>();
        if(returnOrderList != null && returnOrderList.size() > 0){
            for(Record r : returnOrderList){
                orderMap.put(r.getStr("material_id"), r);
            }
        }
        List<Record> saveList = new ArrayList<>();
        List<Record> updateList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            JSONObject obj = list.getJSONObject(i);
            if("0".equals(obj.getString("number"))){
                continue;
            }
            Record r = orderMap.get(obj.getString("id"));
            if(r != null){
                updateList.add(r);
            }else{
                r = materialMap.get(obj.getString("id"));
                r.remove("creater_id","modifier_id","create_time","modify_time","status","desc","shelf_life","storage_condition");
                r.set("material_id", r.get("id"));
                r.set("return_num", obj.getString("number"));
                r.set("reason", obj.getString("remark"));
                r.set("id", UUIDTool.getUUID());
                saveList.add(r);
            }
            r.set("return_num", obj.getString("number"));
            r.set("reason", obj.getString("remark"));
        }
        returnGoodsService.updateOrder(saveList, updateList, orderId, usu);
    }
}
