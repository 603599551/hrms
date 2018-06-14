package com.store.order.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.store.order.services.StoreOrderManagerSrv;
import com.utils.Constants;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 门店订单管理
 */
public class StoreOrderManagerCtrl extends BaseCtrl implements Constants{

    public void createOrder(){
        JSONObject jsonObject = RequestTool.getJson(getRequest());
        String type = jsonObject.getString("orderType");
        if(type != null && type.length() > 0){
            createOrderType(type);
        }else{
            createOrderType("day");
        }
    }

    /**
     * 商品转原材料
     * 参数：
     *      arriveDate：到货时间
     *      wantDate：要货时间
     *      list：商品数据 id：商品id number：商品数量
     * 向store_order表插入一条数据
     * 向store_order_goods表中插入多条记录，每条记录对应着用户选择的商品和数量
     * 返回值：
     *      arriveDate
     *      wantDate
     *      id：是store_order表的主键
     */
    private void createOrderType(String type){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String arriveDate=jsonObject.getString("arriveDate");
            String wantDate=jsonObject.getString("wantDate");
            Map paraMap=new HashMap();
            paraMap.put("jsonObject",jsonObject);

            StoreOrderManagerSrv service = enhance(StoreOrderManagerSrv.class);
            String storeOrderUUID= UUIDTool.getUUID();
            service.goodsToMaterialTypes(paraMap, storeOrderUUID, usu.getUserBean(), type);

            JSONArray jsonArr = jsonObject.getJSONArray("list");
            String[] idArr = {};
            String[] numberArr = {};
            if(jsonArr != null && jsonArr.size() > 0){
                idArr = new String[jsonArr.size()];
                numberArr = new String[jsonArr.size()];
                for(int i = 0; i < jsonArr.size(); i++){
                    JSONObject obj = jsonArr.getJSONObject(i);
                    idArr[i] = obj.getString("id");
                    numberArr[i] = obj.getString("number");
                }
            }
            jhm.putCode(1).put("arriveDate",arriveDate).put("wantDate",wantDate).put("id",storeOrderUUID);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 将商品拆分成原材料，并计算数量
     * 参数：
     *      id：store_order表的主键。用来查询准备订货的商品相关信息
     *
     * 1、根据商品配方将商品数据拆分成原材料，将所有原材料放置到一个map（materialMAP）中，原材料id是key，具体数据是value。
     *      这样做的目的是为了方便数据整理，因为很多商品有相同的原材料，要将所有原材料的数据统计到一起
     * 2、根据当前订单时间查询昨天和前天的订单，昨天订单和前天订单情况相同，以昨天订单为例，举例说明
     *      昨天订单中有后天的到货数据和后天的预计消耗数据，这两个数据用计算到用量和库存中
     *      但是昨天的订单中可能存在今天没有用到的原材料，因为今天的订单是主要的，没有必要显示昨天的多余数据，所以今天订单中
     *      没有的原材料数据将不显示到今天订单中。
     *      //TODO 因为原材料也可以单独定货，所以涉及到其他原材料时还要做同步数据，这个问题暂时没有考虑，后期需要再做一个接口搞定这个问题
     * 3、这个方法还要保存原材料数据到store_order_material表
     *
     * @throws ParseException
     */
    public void goodsToMaterial() throws ParseException {
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String orderId = getPara("id");
        //1
        List<Record> storeOrderGoodsList = Db.find("select sog.*, so.want_date sowant_date, so.arrive_date sarrive_date from store_order_goods sog, store_order so where so.id=sog.store_order_id and store_order_id=?", orderId);
        String wantDateStr = null;
        String arriveDateStr = null;
        if(storeOrderGoodsList != null && storeOrderGoodsList.size() > 0){
            wantDateStr = storeOrderGoodsList.get(0).getStr("sowant_date");
            arriveDateStr = storeOrderGoodsList.get(0).getStr("sarrive_date");
        }else{
            Record stroeOrder = Db.findById("store_order", orderId);
            wantDateStr = stroeOrder.getStr("want_date");
            arriveDateStr = stroeOrder.getStr("arrive_date");
        }
        List<String> idArr = new ArrayList<>();
        List<Integer> numberArr = new ArrayList<>();
        if(storeOrderGoodsList != null && storeOrderGoodsList.size() > 0){
            for(int i = 0; i < storeOrderGoodsList.size(); i++){
                idArr.add(storeOrderGoodsList.get(i).getStr("goods_id"));
                numberArr.add(storeOrderGoodsList.get(i).getInt("number"));
            }
        }

        List<Record> stockList = Db.find("select * from store_stock where store_id=?", usu.getUserBean().get("store_id"));
        Map<String, Record> stockMap = new HashMap<>();
        if(stockList != null && stockList.size() > 0){
            for(Record r : stockList){
                stockMap.put(r.getStr("material_id"), r);
            }
        }
        DailySummaryService dailySummaryService = DailySummaryService.getMe();
        Map<String, Record> materialMap = new HashMap<>();
        List<Record> result = new ArrayList<>();

        for(int i = 0; i < idArr.size(); i++){
            String goodsId = idArr.get(i);
            int number = numberArr.get(i);

            Map goodsIdMap=dailySummaryService.dataGoodsIdMap.get(goodsId);
            if(goodsIdMap==null){
                continue;
            }
            List<Record> goodsMaterialList = (List<Record>) goodsIdMap.get("materialList");
            for(Record r : goodsMaterialList){
                Record materialR = materialMap.get(r.getStr("mid"));
                Record stockR = stockMap.get(r.getStr("mid"));
                if(materialR != null){
                    //TODO 暂时用r净料数量计算
                    double actual_order = new Double(String.format("%.2f", getDouble(materialR.getDouble("actual_order") + r.getDouble("gmnet_num") * number)));
                    materialR.set("actual_order", actual_order);
                }else{
                    materialR = new Record();
                    materialMap.put(r.getStr("mid"), materialR);
                    materialR.set("id", r.getStr("mid"));
                    materialR.set("name", r.getStr("mname"));
                    materialR.set("code", r.getStr("mcode"));
                    materialR.set("unit_text", r.getStr("munit"));
                    materialR.set("unit", r.getStr("m_unit"));
//                    materialR.set("unit_text", r.getStr("m_unit"));
                    double actual_order = new Double(String.format("%.2f", getDouble(r.getDouble("gmnet_num") * number)));
                    materialR.set("actual_order", actual_order);
                    if(stockR != null){
                        materialR.set("stock", stockR.getInt("number"));
                    }else{
                        materialR.set("stock", 0);
                    }
                    materialR.set("nextOneNum", 0);
                    materialR.set("nextOneGetNum", 0);
                    materialR.set("nextTwoNum", 0);
                    materialR.set("nextTwoGetNum", 0);
                    result.add(materialR);
                }
                double actualOrder = getDouble(materialR.get("nextOneGetNum")) + getDouble(materialR.get("nextTwoGetNum")) + getDouble(materialR.get("stock")) - getDouble(materialR.get("nextOneNum")) - getDouble(materialR.get("nextTwoNum"));
                //actualOrder = Math.ceil(actualOrder);
                actualOrder = Math.ceil(getDouble(materialR.get("actual_order")) - actualOrder);
                materialR.set("number", actualOrder);
            }
        }
        //1
        //查询商品相关信息，缓存到内存，方便后面读取数据
        List<Record> materialList = Db.find("select m.*, (select name from goods_unit where goods_unit.id=m.unit) unitname from material m");
        Map<String, Record> materialAllMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialAllMap.put(r.getStr("id"), r);
            }
        }
        //3
        List<Record> saveList = new ArrayList<>();
        if(result != null && result.size() > 0){
            for(Record r : result){
                Record saveR = materialAllMap.get(r.getStr("id"));
                String id = UUIDTool.getUUID();
                saveR.set("id", id);
                r.set("stroe_order_material_id", id);
                saveR.set("store_order_id", orderId);
                saveR.set("store_id", usu.getUserBean().get("store_id"));
                saveR.set("material_id", r.getStr("id"));
                saveR.set("use_num", r.getStr("number"));
                saveR.set("send_num", 0);
                saveR.set("status", 10);
                //saveR.set("type", "day");
                //saveR.set("city", usu.getUserBean().get("city"));
                saveR.set("want_num", r.getStr("number"));
                saveR.set("next1_order_num", r.getStr("nextOneNum"));
                saveR.set("next2_order_num", r.getStr("nextTwoNum"));

                saveR.remove("creater_id");
                saveR.remove("modifier_id");
                saveR.remove("create_time");
                saveR.remove("modify_time");
                saveR.remove("desc");
                saveR.remove("unitname");
                saveR.remove("storage_condition");
                saveR.remove("type");
                saveR.remove("city");
                saveR.remove("shelf_life");
                saveList.add(saveR);
            }
        }
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("store_order_material", saveList, saveList.size());
        }
        //3
        //2
        Date wantDate = sdf.parse(wantDateStr);
        String paramDate = sdf.format(new Date(wantDate.getTime() + ONE_DAY_TIME));
        List<Record> nextOneOrderList = Db.find("select som.* from store_order so, store_order_material som where so.id=som.store_order_id and so.arrive_date=? and so.store_id=?", paramDate, usu.getUserId());
        paramDate = sdf.format(new Date(wantDate.getTime() + ONE_DAY_TIME * 2));
        List<Record> nextTwoOrderList = Db.find("select som.* from store_order so, store_order_material som where so.id=som.store_order_id and so.arrive_date=? and so.store_id=?", paramDate, usu.getUserId());

        //整理昨天和前天的数据
        addNextNum(1, nextOneOrderList, materialMap, materialAllMap, result);
        addNextNum(2, nextTwoOrderList, materialMap, materialAllMap, result);
        //2
        //前台需要非0的数据，如果是0这个数字，前台不能显示，所以将数字列转化成字符串传到前台
        for(Record r : result){
            r.set("actual_order", r.getStr("actual_order"));
            r.set("stock", r.getStr("stock"));
            r.set("nextOneNum", r.getStr("nextOneNum"));
            r.set("nextOneGetNum", r.getStr("nextOneGetNum"));
            r.set("nextTwoNum", r.getStr("nextTwoNum"));
            r.set("nextTwoGetNum", r.getStr("nextTwoGetNum"));
        }
        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1).put("materialList",result).put("arriveDate", arriveDateStr).put("wantDate", wantDateStr);
        renderJson(jhm);
    }

    /**
     * 添加订单原材料门店修改过的数据
     *      订单原材料已经存放到数据库中，但是门店修改后还要修改数量，其实这个方法是一个修改方法
     * 参数：
     *       stroe_order_material_id
     *       number：want_num和send_num字段数据。send_num数据还需要物流再次修改，但是要将门店数据提交，所以这里也添加
     *       nextOneNum：next1_order_num字段数据，方便以后查询
     *       nextTwoNum：next2_order_num字段数据，方便以后查询
     *
     */
    public void addStoreOrderMaterial(){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JSONObject jsonObject = RequestTool.getJson(getRequest());
        StoreOrderManagerSrv service = enhance(StoreOrderManagerSrv.class);
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        String materialIs0Message = "";
        if(jsonArray != null && jsonArray.size() > 0){
            List<String> materialIdList = new ArrayList<>();
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                if(new Double(obj.getString("number")) <= 0){
                    materialIdList.add(obj.getString("id"));
                }
            }
            if(materialIdList.size() == jsonArray.size()){
                jhm.putCode(0).putMessage("所有原材料订货数量为0，此订单无存在意义，不能保存！");
                renderJson(jhm);
                return;
            }
            List<Record> materialList = Db.find("select * from material");
            Map<String, Record> materialMap = new HashMap<>();
            if(materialList != null && materialList.size() > 0){
                for(Record r : materialList){
                    materialMap.put(r.getStr("id"), r);
                }
            }
            if(materialIdList.size() != 0){
                for(String id : materialIdList){
                    materialIs0Message += materialMap.get(id).getStr("name") + "、";
                }
                materialIs0Message = materialIs0Message.substring(0, materialIs0Message.length() - 1) + "订货数量为0，不会添加到订单中。";
            }
        }
        try {
            service.addStoreOrderMaterial(jsonObject, usu);
            jhm.putCode(1).putMessage("保存成功！" + materialIs0Message);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage("保存失败！");
        }
        renderJson(jhm);
    }

    /**
     * 整理历史数据
     * @param day 明天数据：1，后天数据：2。逻辑基本相同，以明天数据为例
     * @param list 数据库中查询到的明天数据
     * @param materialMap 返回的原材料MAP
     * @param materialAllMap 所有配方处理后的MAP
     * @param result 返回前台的集合
     */
    private void addNextNum(int day, List<Record> list, Map<String, Record> materialMap, Map<String, Record> materialAllMap, List<Record> result){
        if(list != null && list.size() > 0){
            for(Record r : list){
                Record materialR = materialMap.get(r.getStr("material_id"));
                if(materialR != null){
                    if(1 == day){
                        materialR.set("nextOneNum", r.getStr("use_num"));
                        materialR.set("nextOneGetNum", r.getStr("send_num"));
                    }else if(2 == day){
                        materialR.set("nextTwoNum", r.getStr("use_num"));
                        materialR.set("nextTwoGetNum", r.getStr("send_num"));
                    }
                }else{
                    materialR = new Record();
                    Record materialRecord = materialAllMap.get(r.get("material_id"));
                    materialMap.put(materialRecord.getStr("material_id"), materialR);
                    materialR.set("id", materialRecord.getStr("material_id"));
                    materialR.set("name", materialRecord.getStr("name"));
                    materialR.set("code", materialRecord.getStr("code"));
                    materialR.set("unit_text", materialRecord.getStr("unitname"));
                    materialR.set("actual_order", 0);
                    materialR.set("stock", 0);
                    materialR.set("nextOneNum", 0);
                    materialR.set("nextOneGetNum", 0);
                    materialR.set("nextTwoNum", 0);
                    materialR.set("nextTwoGetNum", 0);
                    if(1 == day){
                        materialR.set("nextOneNum", r.getStr("use_num"));
                        materialR.set("nextOneGetNum", r.getStr("send_num"));
                    }else if(2 == day){
                        materialR.set("nextTwoNum", r.getStr("use_num"));
                        materialR.set("nextTwoGetNum", r.getStr("send_num"));
                    }
                    //TODO 不显示当前订单中没有的原材料数据
                    //result.add(materialR);
                }
                /*
                预计订货量计算。
                计算公式：总需求量 - 剩余总量
                            剩余总量 = 明天到货量 + 后天到货量 + 库存量 - 明天预计消耗量 - 后天预计消耗量
                            总需求量从前台传入
                 */
                //int actualOrder = getInt(materialR.get("nextOneGetNum")) + getInt(materialR.get("nextTwoGetNum")) + getInt(materialR.get("stock")) - getInt(materialR.get("nextOneNum")) - getInt(materialR.get("nextTwoNum"));
                //actualOrder = getInt(materialR.get("actual_order")) - actualOrder;
                double actualOrder = getDouble(materialR.get("nextOneGetNum")) + getDouble(materialR.get("nextTwoGetNum")) + getDouble(materialR.get("stock")) - getDouble(materialR.get("nextOneNum")) - getDouble(materialR.get("nextTwoNum"));
                actualOrder = Math.ceil(getDouble(materialR.get("actual_order")) - actualOrder);
                materialR.set("number", actualOrder);
            }
        }
    }

    /**
     * 将obj转化成int类型
     *      如果为空返回0
     *      如果是double类型，将double转化成int
     * @param obj
     * @return
     */
    private double getDouble(Object obj){
        if(obj != null && obj.toString().trim().length() > 0 && !"null".equalsIgnoreCase(obj.toString())){
            if(obj instanceof Double){
                double result = new Double(obj.toString());
                return result;
            }else if(obj instanceof Integer){
                return new Double(obj.toString());
            }
        }
        return 0;
    }

    /**
     * 将obj转化成int类型
     *      如果为空返回0
     *      如果是double类型，将double转化成int
     * @param obj
     * @return
     */
    private int getInts(Object obj){
        if(obj != null && obj.toString().trim().length() > 0 && !"null".equalsIgnoreCase(obj.toString())){
            if(obj instanceof Double){
                double result = new Double(obj.toString());
                return (int)result;
            }else if(obj instanceof Integer){
                return new Integer(obj.toString());
            }
        }
        return 0;
    }

}
