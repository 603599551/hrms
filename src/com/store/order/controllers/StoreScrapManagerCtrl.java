package com.store.order.controllers;

import com.alibaba.fastjson.JSONObject;
import com.bean.UserBean;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.store.order.services.MaterialAndMaterialTypeTreeService;
import com.store.order.services.StoreOrderManagerSrv;
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
        String orderId = getPara("id");
        try{
            service.cancelOrder(orderId);
            jhm.putMessage("取消成功！");
        } catch (Exception e){
            jhm.putMessage("取消失败！").putCode(0);
        }
        renderJson(jhm);
    }

    public void finishOrder(){
        JsonHashMap jhm = new JsonHashMap();
        String orderId = getPara("orderId");
        try{
            service.finishOrder(orderId, new UserSessionUtil(getRequest()));
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
        String sql = " from store_scrap ss, dictionary d where ss.status=d.value and d.parent_id=600 and store_id=? ";
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
        if(status != null && status.length() > 0 && !"-1".equals(status)){
            sql += " and status=? ";
            params.add(status);
        }else{
            sql += " and status<>? ";
            params.add("5");
        }
        Page<Record> result = Db.paginate(pageNum, pageSize, "select ss.*, d.name status_text, d.status_color status_color ", sql, params.toArray());
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
        jhm.put("data", result);
        renderJson(jhm);
    }

    public void showDetailList(){
        String orderId = getPara("id");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String sql = "select ssm.*, (select number from store_stock ss where ss.store_id=? and ss.material_id=ssm.material_id) stock, (select name from goods_unit gu where gu.id=ssm.unit) unit_text, (select name from goods_attribute ga where ga.id=ssm.attribute_2) attribute2_text from store_scrap_material ssm where ssm.store_scrap_id=?";
        List<Record> detailList = Db.find(sql, usu.getUserBean().get("store_id"), orderId);
        if(detailList != null && detailList.size() > 0){
            for(Record r : detailList){
                r.set("store_scrap_material_id", r.getStr("id"));
                r.set("id", r.getStr("material_id"));
                if(r.get("stock") == null){
                    r.set("stock", 0);
                }
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("list", detailList);
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

    public void getTree() {
        JsonHashMap jhm=new JsonHashMap();
        //查询原材料分类
        List<Record> materialTypeList = Db.find("select id,parent_id,code,name,sort,CONCAT(name,'(',code,')') as label from material_type order by sort");
        //查询原材料
        List<Record> materialList = Db.find("select id,code,name,CONCAT(name,'(',code,')') as label,pinyin,wm_type,(select name from wm_type where wm_type.id=wm_type) as wm_type_text ,attribute_1,attribute_2,type_1,type_2,unit,(select name from goods_unit where goods_unit.id=material.unit) as unit_text,0 as stock_number from material order by sort");
        String orderId = getPara("id");
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        List<Record> storeScrapMaterialList = Db.find("select ssm.*, (select number from store_stock ss where ss.store_id=? and ss.material_id=ssm.material_id) stock_num, (select name from goods_unit gu where gu.id=ssm.attribute_2) unit_text, (select name from goods_attribute ga where ga.id=ssm.attribute_2) attribute2_text from store_scrap_material ssm where store_scrap_id=?", usu.getUserBean().get("store_id"), orderId);
//        List<Record> storeScrapMaterialList = Db.find("select ssm.*, (select number from store_stock ss where ss.store_id=? and ss.material_id=ssm.material_id) stock_num from store_scrap_material ssm where store_scrap_id=?", usu.getUserBean().get("store_id"), orderId);
        Map<String, Record> storeScrapMaterialMap = new HashMap<>();
        if(storeScrapMaterialList != null && storeScrapMaterialList.size() > 0){
            for(Record r : storeScrapMaterialList){
                storeScrapMaterialMap.put(r.getStr("material_id"), r);
            }
        }
        if(materialList != null && materialList.size() > 0){
            for(Record r : materialList){
                r.set("search_text",r.getStr("name") + "-" + r.get("code") + "-" + r.get("pinyin"));
                Record ssmR = storeScrapMaterialMap.get(r.getStr("material_id"));
                if(ssmR != null){
                    if(ssmR.get("stock") != null){
                        r.set("stock", ssmR.getDouble("stock"));
                    }else{
                        r.set("stock", 0);
                    }
                    r.set("number", ssmR.get("number"));
                }else{
                    r.set("stock", 0);
                    r.set("number", 0);
                }
            }
        }
        if(materialTypeList != null && materialTypeList.size() > 0){
            for(Record r : materialTypeList){
                r.set("search_text",r.getStr("name") + "-" + r.get("code"));
            }
        }
        //构建树
        MaterialAndMaterialTypeTreeService service = MaterialAndMaterialTypeTreeService.getMe();
        //构建原材料分类数
        List materialTypeList2 = service.sort(materialTypeList);
        //将原材料挂载到原材料分类树下
        List resultList = service.addMaterial2MaterialType(materialTypeList2, materialList);
        jhm.putCode(1).put("tree", resultList);
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
