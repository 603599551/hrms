package com.store.order.controllers;

import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.store.order.services.StoreScrapManagerSrv;
import com.utils.Constants;
import com.utils.RequestTool;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;

/**
 * 门店订单管理
 */
public class StoreScrapManagerCtrl extends BaseCtrl implements Constants{

    private StoreScrapManagerSrv service = enhance(StoreScrapManagerSrv.class);

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
    public void createOrder(){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            Map paraMap=new HashMap();
            paraMap.put("jsonObject",jsonObject);

            String storeOrderUUID= UUIDTool.getUUID();
            service.addStoreScrapAndStoreScrapGoods(paraMap, storeOrderUUID, usu.getUserBean());

            jhm.putCode(1).put("id",storeOrderUUID);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void cancelOrder(){
        JsonHashMap jhm = new JsonHashMap();
        String orderId = getPara("orderId");
        try{
            service.cancelOrder(orderId);
            jhm.putMessage("取消成功！");
        } catch (Exception e){
            jhm.putMessage("取消失败！").putCode(0);
        }
        renderJson(jhm);
    }

    public void queryListByStore(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        String orderNumber = getPara("orderNumber");
        String scrapTime = getPara("scrapTime");
        String status = getPara("state");
        String sql = " from store_scrap where store_id=? ";
        List<Object> params = new ArrayList<>();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        params.add(usu.getUserBean().get("store_id"));
        if(orderNumber != null && orderNumber.length() > 0){
            sql += " and order_number=? ";
            params.add(orderNumber);
        }
        if(scrapTime != null && scrapTime.length() > 0){
            sql += " and scrap_time=? ";
            params.add(scrapTime);
        }
        if(status != null && status.length() > 0){
            sql += " and status=? ";
            params.add(status);
        }
        Page<Record> result = Db.paginate(pageNum, pageSize, "select * ", sql, params.toArray());
        if(result != null && result.getList().size() > 0){
            for(Record r : result.getList()){
                if("1".equals(r.getStr("status"))){
                    r.set("isEdit", true);
                }else{
                    r.set("isEdit", false);
                }
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("list", result);
        renderJson(jhm);
    }

    public void showDetailList(){
        String orderId = getPara("orderId");
        String sql = "select ssm.*, (select name from goods_unit gu where gu.id=ssm.unit) unit_text, (select name from goods_attribute ga where ga.id=ssm.attribute_2) attribute2_text from store_scrap_material ssm where ssm.store_scrap_id=?";
        List<Record> detailList = Db.find(sql, orderId);
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("list", detailList);
        renderJson(detailList);
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
        List<Record> storeOrderGoodsList = Db.find("select ssg.goods_id goods_id, ssg.number number from store_scrap ss, store_scrap_goods ssg where ss.id=ssg.store_scrap_id and ss.id=?", orderId);

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
                    double actual_order = new Double(String.format("%.2f", getDouble(r.getDouble("gmnet_num") * number)));
                    materialR.set("actual_order", actual_order);
                    if(stockR != null){
                        materialR.set("stock", stockR.getInt("number"));
                    }else{
                        materialR.set("stock", 0);
                    }
                    result.add(materialR);
                }
            }
        }
        //1
        //查询商品相关信息，缓存到内存，方便后面读取数据
        List<Record> materialList = Db.find("select m.*, gu.name unitname, gm.net_num net_num, gm.gross_num gross_num, gm.total_price total_price from material m, goods_unit gu, goods_material gm where m.id=gm.material_id and m.unit=gu.id");
        Map<String, Record> materialAllMap = new HashMap<>();
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                materialAllMap.put(r.getStr("id"), r);
            }
        }
        UserBean userBean = usu.getUserBean();
        String time = DateTool.GetDateTime();
        //3
        List<Record> saveList = new ArrayList<>();
        if(result != null && result.size() > 0){
            for(Record r : result){
                Record saveR = materialAllMap.get(r.getStr("id"));
                String id = UUIDTool.getUUID();
                saveR.set("id", id);
                r.set("stroe_order_material_id", id);
                saveR.set("store_scrap_id", orderId);
                saveR.set("store_id", usu.getUserBean().get("store_id"));
                saveR.set("material_id", r.getStr("id"));
                saveR.set("number", r.getStr("number"));
                saveR.set("creater_id", userBean.getId());
                saveR.set("create_time", time);
                saveR.set("modifier_id", userBean.getId());
                saveR.set("modify_time", time);
                saveR.set("scrap_time", time);

                saveR.remove("desc");
                saveR.remove("unitname");
                saveR.remove("storage_condition");
                saveR.remove("type");
                saveR.remove("city");
                saveR.remove("shelf_life");
                saveList.add(saveR);
            }
        }
        Db.batchSave("store_scrap_material", saveList, saveList.size());
        //3
        //前台需要非0的数据，如果是0这个数字，前台不能显示，所以将数字列转化成字符串传到前台

        JsonHashMap jhm=new JsonHashMap();
        jhm.putCode(1).put("materialList",result);
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
    public void addStoreScrapMaterial(){
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        JSONObject jsonObject = RequestTool.getJson(getRequest());
        try {
            service.addStoreScrapMaterial(jsonObject, usu);
            jhm.putCode(1).putMessage("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage("保存失败！");
        }
        renderJson(jhm);
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
}
