package com.logistics.order.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.ss.stock.services.SecurityStockService;
import com.utils.SelectUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutWarehouseOrderSrv {

    /**
     * 出库
     * @param id
     * @return
     *
     *
            返回list中的数据有：
            id: '原材料id-批号',
            tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
            label: '原材料名称(批号)',
            search_text: '原材料名称-批号-拼音头', // 搜索条件
            material_id: '原材料id',
            name: '原材料名称',
            code: '编号',
            attribute_2: '规格',
            unit_text: '单位',
            want_num: '订货数量',
            security_stock: '安存数量',
            batch_code: '批号',
            send_number: '出货数量',
            warehouseStockNumber: '库存数量',
            isEdit: true, // 三级树中加就行
            warehouse_stock_id:''//库存（warehouse_stock）id。子数组第一个元素的warehouse_stock_id，赋给外部数组

     */
    public List<Record> showDetailsById(String id){
        Record warehouseOutOrderR=Db.findById("warehouse_out_order",id);
        String statusOfWarehouseOutOrderR=warehouseOutOrderR.getStr("status");

        /*
        查询该订单的出库原材料
         */
        String sql="select a.id as tid,a.material_id,a.code,a.name,(select name from goods_attribute where id=a.attribute_2) attribute_2_text ,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,a.want_num,a.send_num,'' as search_text,0 as send_number from warehouse_out_order_material a left join material b on a.material_id=b.id where a.warehouse_out_order_id=? order by b.sort";
        List<Record> warehouseOutOrderMaterialList=Db.find(sql,id);

        //获取安存
        Map securityStock=SecurityStockService.getSecurityStockMap();
        List<String> materialIdList=new ArrayList();

        /*
        获取当前出库订单中原材料的安存数量，并放入到该条记录中
         */
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
            warehouseOutOrderMaterialR.set("isEdit",true);
            String materialId=warehouseOutOrderMaterialR.get("material_id");
            Record securityStockR=(Record)securityStock.get(materialId);
            if(securityStockR!=null) {
                int security_stock = securityStockR.getInt("security_stock");
                warehouseOutOrderMaterialR.set("security_stock",security_stock);//安存数量
            }else{
                warehouseOutOrderMaterialR.set("security_stock",0);//安存数量

            }
            materialIdList.add(materialId);
        }
        List reList=null;
        if("10".equals(statusOfWarehouseOutOrderR)){
            reList=buildWarehouseStockData(materialIdList,warehouseOutOrderMaterialList);
        }else{//保存、出库、完成时，出库数从warehouse_out_order_material_detail表中提取
            reList=buildWarehouseOutOrderMaterialDetail(id,materialIdList,warehouseOutOrderMaterialList);
        }


        return reList;
    }

    /**
     * 根据表warehouse_out_order_material_detail构建数据
     *
     * 保存、出库、完成时，出库数从warehouse_out_order_material_detail表中提取
     *
     * @param id
     * @param materialIdList
     * @param warehouseOutOrderMaterialList
     */
    private List buildWarehouseOutOrderMaterialDetail(String id,List<String> materialIdList,List<Record> warehouseOutOrderMaterialList){
        SelectUtil selectSQL=new SelectUtil("select a.*,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,b.pinyin,c.number as warehouse_stock_num from warehouse_out_order_material_detail a left join material b on a.material_id=b.id  left join warehouse_stock c on a.material_id=c.material_id and a.batch_code=c.batch_code  ");
        selectSQL.in(" and a.material_id in ",materialIdList.toArray());
        selectSQL.addWhere(" and a.warehouse_out_order_id=?",id);
        selectSQL.addWhere("and c.number>0");
        selectSQL.order(" order by a.material_id,a.batch_code ");//按照批号排序
        List<Record> dataList=Db.find(selectSQL.toString(),selectSQL.getParameters());

        List reList=new ArrayList();
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
            String warehouseOutOrderMaterialId=warehouseOutOrderMaterialR.get("id");
            String materialId=warehouseOutOrderMaterialR.get("material_id");
            String name=warehouseOutOrderMaterialR.get("name");
            int wantNum=warehouseOutOrderMaterialR.get("want_num");

            /*
            根据该订单原材料的id，获取对应出库的明细（批号、数量）
             */
            List<Map> warehouseStockMapList= getWarehouseOutOrderMaterialDetailData(warehouseOutOrderMaterialR,dataList);
//            warehouseOutOrderMaterialR.set("warehouseStockInfo",warehouseStockMapList);
//            if(warehouseStockMapList!=null && !warehouseStockMapList.isEmpty()){
//                Map map=(Map)warehouseStockMapList.get(0);
//                String batchCode=(String)map.get("batch_code");
//                String warehouseStockId=(String)map.get("warehouse_stock_id");
//
//                warehouseOutOrderMaterialR.set("id",materialId+"-"+batchCode);
//                warehouseOutOrderMaterialR.set("label",name+"("+batchCode+")");
//                warehouseOutOrderMaterialR.set("batch_code",batchCode);
//                warehouseOutOrderMaterialR.set("warehouse_stock_id",warehouseStockId);
//            }
            Map elementMap=new HashMap();
            elementMap.put("warehouseStockInfo",warehouseStockMapList);
            if(warehouseStockMapList!=null && !warehouseStockMapList.isEmpty()){
                Map map=(Map)warehouseStockMapList.get(0);
                String batchCode=(String)map.get("batch_code");
                String warehouseStockId=(String)map.get("warehouse_stock_id");

                elementMap.putAll(map);

                elementMap.put("id",materialId+"-"+batchCode);
                elementMap.put("label",name+"("+batchCode+")");
                elementMap.put("batch_code",batchCode);
                elementMap.put("warehouse_stock_id",warehouseStockId);
            }
            reList.add(elementMap);

        }
        return reList;
    }

    /**
     * 新建订单，构建数据
     * 传入出库原材料集合，查询这些原材料的库存数量
     * 注：入库时要录入批号，所以同一个原材料会有多条记录
     */
    private List buildWarehouseStockData(List<String> materialIdList,List<Record> warehouseOutOrderMaterialList){

        SelectUtil selectSQL=new SelectUtil("select a.*,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,b.pinyin from warehouse_stock a left join material b on a.material_id=b.id  ");
        selectSQL.addWhere("and a.number>0");
        selectSQL.in(" and a.material_id in ",materialIdList.toArray());
        selectSQL.order(" order by a.material_id,a.batch_code ");//按照批号排序
        List warehouseStockList=Db.find(selectSQL.toString(),selectSQL.getParameters());

        List reList=new ArrayList();
        for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
            String warehouseOutOrderMaterialId=warehouseOutOrderMaterialR.get("id");
            String materialId=warehouseOutOrderMaterialR.get("material_id");
            String name=warehouseOutOrderMaterialR.get("name");
            int wantNum=warehouseOutOrderMaterialR.get("want_num");

            /*
            根据该订单原材料的id，获取对应库存原材料的库存数量、批号
             */
            List<Map> warehouseStockMapList=getWarehouseStock(warehouseOutOrderMaterialR,warehouseStockList);
//            warehouseOutOrderMaterialR.set("warehouseStockInfo",warehouseStockMapList);
//            if(warehouseStockMapList!=null && !warehouseStockMapList.isEmpty()){
//                Map map=(Map)warehouseStockMapList.get(0);
//                String batchCode=(String)map.get("batch_code");
//                String warehouseStockId=(String)map.get("warehouse_stock_id");
//
//                warehouseOutOrderMaterialR.set("id",materialId+"-"+batchCode);
//                warehouseOutOrderMaterialR.set("label",name+"("+batchCode+")");
//                warehouseOutOrderMaterialR.set("batch_code",batchCode);
//                warehouseOutOrderMaterialR.set("warehouse_stock_id",warehouseStockId);
//            }

            Map elementMap=new HashMap();
            elementMap.put("warehouseStockInfo",warehouseStockMapList);
            if(warehouseStockMapList!=null && !warehouseStockMapList.isEmpty()){
                Map map=(Map)warehouseStockMapList.get(0);
                String batchCode=(String)map.get("batch_code");
                String warehouseStockId=(String)map.get("warehouse_stock_id");

                elementMap.putAll(map);

                elementMap.put("id",materialId+"-"+batchCode);
                elementMap.put("label",name+"("+batchCode+")");
                elementMap.put("batch_code",batchCode);
                elementMap.put("warehouse_stock_id",warehouseStockId);
            }
            reList.add(elementMap);

        }

        return reList;
    }

    /**
     * 构建数据
     * 传入出库原材料数据、库存集合，根据该原材料的id，从库存集合中找到对应库存原材料的库存数量、批号
     * 注：入库时要录入批号，所以同一个原材料会有多条记录
     *
     *  返回list中的数据有：
             id: '原材料id-批号',
             tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
             label: '原材料名称(批号)',
             search_text: '原材料名称-批号-拼音头', // 搜索条件
             material_id: '原材料id',
             name: '原材料名称',
             code: '编号',
             attribute_2: '规格',
             unit_text: '单位',
             want_num: '订货数量',
             security_stock: '安存数量',
             batch_code: '批号',
             send_number: '出货数量',
             warehouseStockNumber: '库存数量',
             isEdit: true, // 三级树中加就行
             want_num:0//订购的数量，有外部传入
             warehouse_stock_id:''//库存（warehouse_stock）id

     *
     *
     * @return
     */
    private List<Map> getWarehouseStock(Record record,List<Record> warehouseStockList){
        String warehouseOutOrderMaterialId=record.getStr("id");
        String materialId=record.getStr("material_id");
        int wantNum=record.getInt("want_num");
        String tid=record.getStr("tid");//warehouse_out_order_material表的id
        String attribute2TextOfRecord=record.getStr("attribute_2_text");//规格

        List<Map> reList=new ArrayList<>();
        int sum=0;
        int lastSum=0;
        for(Record r:warehouseStockList){
            String id=r.getStr("id");
            String name=r.getStr("name");
            String materialIdDb=r.getStr("material_id");
            String pinyin=r.getStr("pinyin");
            String unitText=r.getStr("unit_text");
            String code=r.getStr("code");
//            String attribute2Text=r.getStr("attribute_2_text");
            String warehouseId=r.getStr("warehouse_id");

            if(!materialId.equals(materialIdDb)){
                continue;
            }
            int number=r.get("number");//库存数量
            String batch_code=r.get("batch_code");

            sum=sum+number;


            Map map=new HashMap();
            map.put("id",materialIdDb+"-"+batch_code);
            map.put("warehouse_stock_id",id);
            map.put("name",name);
            map.put("tid",tid);
            map.put("label",name+"("+batch_code+")");
            map.put("search_text",name+"-"+batch_code+"-"+pinyin);
//            map.put("warehouseOutOrderMaterialId",warehouseOutOrderMaterialId);
            map.put("material_id",materialId);
//            map.put("warehouseStockId",id);
            map.put("warehouseStockNumber",number);
            map.put("batch_code",batch_code);
            map.put("isEdit",true);
            map.put("send_number",0);
            map.put("security_stock",0);
//            map.put("want_num",wantNum);
            map.put("unit_text",unitText);
            map.put("attribute_2_text",attribute2TextOfRecord);
            map.put("code",code);
            map.put("warehouse_id",warehouseId);
            map.put("want_num",wantNum);
            if(sum>=wantNum){//如果该原材料的库存数大于需求数
                int num=0;
                if(reList.isEmpty()){
                    map.put("send_number", wantNum);
                }else {
                    map.put("send_number", wantNum - lastSum);
                }
                reList.add(map);
                break;
            }else{//如果该原材料的库存数小于需求数，把当前原材料的信息放入到list中
                map.put("send_number",number);
                reList.add(map);
            }
            lastSum=lastSum+number;
        }
        return reList;
    }
    /**
     *   id: '原材料id-批号',
     tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
     label: '原材料名称(批号)',
     search_text: '原材料名称-批号-拼音头', // 搜索条件
     material_id: '原材料id',
     name: '原材料名称',
     code: '编号',
     attribute_2: '规格',
     unit_text: '单位',
     want_num: '订货数量',
     security_stock: '安存数量',
     batch_code: '批号',
     send_number: '出货数量',
     warehouseStockNumber: '库存数量',
     isEdit: true, // 三级树中加就行
     want_num:0//订购的数量，有外部传入
     warehouse_stock_id:''//库存（warehouse_stock）id
     */
    private List<Map> getWarehouseOutOrderMaterialDetailData(Record warehouseOutOrderMaterialR, List<Record> dataList){
        String warehouseOutOrderMaterialId=warehouseOutOrderMaterialR.getStr("id");
        String materialId=warehouseOutOrderMaterialR.getStr("material_id");
        int wantNum=warehouseOutOrderMaterialR.getInt("want_num");
        String tid=warehouseOutOrderMaterialR.getStr("tid");//warehouse_out_order_material表的id
        String attribute2TextOfRecord=warehouseOutOrderMaterialR.getStr("attribute_2_text");//规格

        List<Map> reList=new ArrayList<>();
//        int sum=0;
//        int lastSum=0;
        for(Record r:dataList){
            String id=r.getStr("id");
            String name=r.getStr("name");
            String materialIdDb=r.getStr("material_id");
            String pinyin=r.getStr("pinyin");
            String unitText=r.getStr("unit_text");
            String code=r.getStr("code");
//            String attribute2Text=r.getStr("attribute_2_text");
            String warehouseId=r.getStr("warehouse_id");

            if(!materialId.equals(materialIdDb)){
                continue;
            }
//            int number=r.get("number");
            int number=0;
            String batch_code=r.get("batch_code");
            Integer send_num=r.get("send_num");
            Integer warehouseStockNum=r.get("warehouse_stock_num");//库存
            if(send_num==null)send_num=0;
            if(warehouseStockNum==null)warehouseStockNum=0;
//            sum=sum+number;


            Map map=new HashMap();
            map.put("id",materialIdDb+"-"+batch_code);
            map.put("warehouse_stock_id",id);
            map.put("name",name);
            map.put("tid",tid);
            map.put("label",name+"("+batch_code+")");
            map.put("search_text",name+"-"+batch_code+"-"+pinyin);
//            map.put("warehouseOutOrderMaterialId",warehouseOutOrderMaterialId);
            map.put("material_id",materialId);
//            map.put("warehouseStockId",id);
            map.put("warehouseStockNumber",warehouseStockNum);
            map.put("batch_code",batch_code);
            map.put("isEdit",true);
            map.put("send_number",0);
            map.put("security_stock",0);
            map.put("want_num",wantNum);
            map.put("unit_text",unitText);
            map.put("attribute_2_text",attribute2TextOfRecord);
            map.put("code",code);
            map.put("warehouse_id",warehouseId);
            map.put("send_number",send_num);
            reList.add(map);
//            if(sum>=wantNum){//如果该原材料的库存数大于需求数
//                int num=0;
//                if(reList.isEmpty()){
//                    map.put("send_number", wantNum);
//                }else {
//                    map.put("send_number", wantNum - lastSum);
//                }
//                reList.add(map);
//                break;
//            }else{//如果该原材料的库存数小于需求数，把当前原材料的信息放入到list中
//                map.put("send_number",number);
//                reList.add(map);
//            }
//            lastSum=lastSum+number;
        }

        return reList;
    }
    /**
     * 保存出库订单
     * 如果提交的数据中，如果原订单就有的原材料，那么将此数据保存到warehouse_out_order_material_detail表中，并将数量更新warehouse_out_order_material表
     * 如果原订单没有的原材料，那么是从树上点击添加的原材料，那么将此数据保存到warehouse_out_order_material_detail表中，store_order_material表、并将数量更新warehouse_out_order_material表，
     *
     * @param id 订单编号。warehouse_out_order表的主键id
     * @param array 提交的数据
     *              array是json数组，json有以下元素
                    返回list中的数据有：
                    id: '原材料id-批号',
                    tid: '有值', // warehouse_out_order_material表的id。table 中用的，分类树给空串就行
                    label: '原材料名称(批号)',
                    search_text: '原材料名称-批号-拼音头', // 搜索条件
                    material_id: '原材料id',
                    name: '原材料名称',
                    code: '编号',
                    attribute_2: '规格',
                    unit_text: '单位',
                    want_num: '订货数量',
                    security_stock: '安存数量',
                    batch_code: '批号',
                    send_number: '出货数量',
                    warehouseStockNumber: '库存数量',
                    isEdit: true, // 三级树中加就行
                    warehouse_stock_id:''//库存（warehouse_stock）id
     * @return
     */
    public JsonHashMap save(String id, JSONArray array){
        JsonHashMap jhm=new JsonHashMap();
        Record warehouseOutOrderR=Db.findById("warehouse_out_order",id);
        if(warehouseOutOrderR==null){
            jhm.putCode(0).putMessage("无此订单记录！");
            return jhm;
        }else{
            // 下面代码为debug模式，调试成功后，必须取消注释下面代码 author:mym
            String status=warehouseOutOrderR.getStr("status");
            if("30".equals(status)){
                jhm.putCode(0).putMessage("此订单已经出库，不能再次修改！");
                return jhm;
            }
            if("40".equals(status)){
                jhm.putCode(0).putMessage("此订单已经完成，不能再次修改！");
                return jhm;
            }
        }
//        Map<String,Integer> warehouseOutOrderMaterialMap=new HashMap();
        List<WarehouseOutOrderMaterialDetailBean> warehouseOutOrderMaterialDetailBeanList =new ArrayList<>();
        List<WarehouseOutOrderMaterialDetailBean> newWarehouseOutOrderMaterialDetailBeanList =new ArrayList<>();
        List<Record> recordList=new ArrayList();
        List<Record> newRecordList=new ArrayList();
        buildSaveData(warehouseOutOrderR,array,recordList,newRecordList, warehouseOutOrderMaterialDetailBeanList,newWarehouseOutOrderMaterialDetailBeanList);
        int numClear=clearWarehouseOutOrderMaterialDetail(id);
        int numSaveWarehouseOutOrderMaterialDetail=doSaveWarehouseOutOrderMaterialDetail(recordList);
        int num= doUpdateWarehouseOutOrderMaterial(warehouseOutOrderMaterialDetailBeanList,id);

        /*
        从树中添加的原材料，保存到warehouse_out_order_material表、warehouse_out_order_material_detail表、store_order_material表中
        保存记录前，先清空
         */
        doSaveNewRecord(warehouseOutOrderR,newRecordList,null,false);

        String datetime= DateTool.GetDateTime();
        Db.update("update warehouse_out_order set status=?,finish_time=? where id=?",20,datetime,id);
        jhm.putCode(1).putMessage("保存成功！");
        return jhm;
    }
    class WarehouseOutOrderMaterialDetailBean {
        int sendNum;
        String materialId;
        /**
         * 批号，出库时，从库存表warehouse_stock减去数量时会用到
         */
        String batchCode;

    }
    /**
     * 构建保存的数据
     * 将传入的数据，重新构建，并放入到recordList，warehouseOutOrderMaterialMap
     * @param warehouseOutOrderR
     * @param array 前台页面传递的数据
     * @param recordList 表格上本来就有的原材料，封装在该list中。该list的元素是要保持到warehouse_out_order_material_detail表的
     * @param newRecordList 从树上选择添加的原材料，封装在该list中。该list的元素是要保持到warehouse_out_order_material_detail表的
     * @param warehouseOutOrderMaterialDetailBeanList 表格上本来就有的原材料，封装在该list中，用于后续更新warehouse_Out_Order_Material表的数量
     * @param newWarehouseOutOrderMaterialDetailBeanList 从树上选择添加的原材料，封装在该list中，该list的元素是要保持到warehouse_out_order_material表、store_order_material表
     */
    private void buildSaveData(Record warehouseOutOrderR, JSONArray array, List<Record> recordList,List<Record> newRecordList, List<WarehouseOutOrderMaterialDetailBean> warehouseOutOrderMaterialDetailBeanList,List<WarehouseOutOrderMaterialDetailBean> newWarehouseOutOrderMaterialDetailBeanList){
        String id=warehouseOutOrderR.getStr("id");
        String storeId=warehouseOutOrderR.getStr("store_id");
        String storeOrderId=warehouseOutOrderR.getStr("store_order_id");

        List<Record> allList=new ArrayList<>();
        List<WarehouseOutOrderMaterialDetailBean> allBeanList=new ArrayList<>();

        int sort=1;
        List<String> materialIdList=new ArrayList();
        for(Object obj:array){
            JSONObject json=(JSONObject)obj;
            JSONArray subArray=json.getJSONArray("warehouseStockInfo");
            for(Object subObj:subArray) {
                JSONObject subJson=(JSONObject)subObj;
                String warehouseStockId = subJson.getString("warehouse_stock_id");
                String materialId = subJson.getString("material_id");
                String warehouseId = subJson.getString("warehouse_id");

//            String warehouseStockNumber=subJson.getString("warehouseStockNumber");
                String send_number = subJson.getString("send_number");
                int sendNumberInt=NumberUtils.parseInt(send_number,0);
                String batchCode = subJson.getString("batch_code");
                String warehouseOutOrderMaterialId = subJson.getString("warehouseOutOrderMaterialId");
                String code = subJson.getString("code");
                String name = subJson.getString("name");
//                String wantNum = subJson.getString("want_num");

                materialIdList.add(materialId);

                Record warehouseOutOrderMaterialDetailR = new Record();
                warehouseOutOrderMaterialDetailR.set("id", UUIDTool.getUUID());
                warehouseOutOrderMaterialDetailR.set("warehouse_out_order_id", id);
                warehouseOutOrderMaterialDetailR.set("warehouse_out_order_material_id", warehouseOutOrderMaterialId);
                warehouseOutOrderMaterialDetailR.set("warehouse_stock_id", warehouseStockId);
                warehouseOutOrderMaterialDetailR.set("warehouse_id", warehouseId);
                warehouseOutOrderMaterialDetailR.set("store_id", storeId);
                warehouseOutOrderMaterialDetailR.set("material_id", materialId);
                warehouseOutOrderMaterialDetailR.set("batch_code", batchCode);
                warehouseOutOrderMaterialDetailR.set("code", code);
                warehouseOutOrderMaterialDetailR.set("name", name);
                warehouseOutOrderMaterialDetailR.set("send_num", sendNumberInt);
                warehouseOutOrderMaterialDetailR.set("sort", sort);
                warehouseOutOrderMaterialDetailR.set("status", "1");

                sort++;

                WarehouseOutOrderMaterialDetailBean bean=new WarehouseOutOrderMaterialDetailBean();
                bean.materialId =materialId;
                bean.sendNum=NumberUtils.parseInt(send_number,0);
                bean.batchCode=batchCode;

                allList.add(warehouseOutOrderMaterialDetailR);
                allBeanList.add(bean);
            }
        }

        /*
        判断传入的原材料是否在原订单中
        填充warehouse_out_order_material_id数据
         */
        SelectUtil selectUtil=new SelectUtil("select id,material_id from store_order_material");
        selectUtil.in("and material_id in ",materialIdList.toArray());
        selectUtil.addWhere("and store_order_id=?",storeOrderId);
        selectUtil.addWhere("and status<>?",20);
        String sql=selectUtil.toString();
        List<Record> warehouseOutOrderMaterialList=Db.find(sql,selectUtil.getParameters());

        for(int i=0,length=allList.size();i<length;i++){
            Record r=allList.get(i);
            String materialIdOfR=r.getStr("material_id");
            boolean ieEqual=false;
            for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
                String idOfWarehouseOutOrderMaterialR=warehouseOutOrderMaterialR.getStr("id");
//                String wooiOfWarehouseOutOrderMaterialR=warehouseOutOrderMaterialR.getStr("warehouse_out_order_id");
                String materialIdOrderMaterialR=warehouseOutOrderMaterialR.getStr("material_id");
                if( materialIdOfR.equals(materialIdOrderMaterialR)){//如果相同，说明与原订单的原材料相同，那么存放到已存在的集合中
                    r.set("warehouse_out_order_material_id",idOfWarehouseOutOrderMaterialR);
                    recordList.add(r);
                    warehouseOutOrderMaterialDetailBeanList.add(allBeanList.get(i));
                    ieEqual=true;
                    break;
                }
            }
            if(!ieEqual) {
                //如果都没匹配上，说明与原订单的原材料不相同，那么存放到新纪录集合中
                r.set("status", "20");
                newRecordList.add(r);
                newWarehouseOutOrderMaterialDetailBeanList.add(allBeanList.get(i));
            }
        }

    }
    /**
        更新warehouse_out_order_material表的send_num（发送数量字段）
        同一个原材料，不同批号的数量，累加，并执行保存
         */
    private int doUpdateWarehouseOutOrderMaterial(List<WarehouseOutOrderMaterialDetailBean> list,String warehouseOutOrderId){

        Object[][] array=new Object[list.size()][3];
        int i=0;
        String lastMaterialId="";
        for(WarehouseOutOrderMaterialDetailBean bean:list){
            if(lastMaterialId.equals(bean.materialId)) {//如果原材料id相同，认为是同一个原材料，那么sendNum相加
                Object[] lastArray=array[i-1];
                int sendNum=(Integer)lastArray[0];
                lastArray[0]=sendNum+bean.sendNum;
            }else{
                array[i][0] = bean.sendNum;
                array[i][1] = warehouseOutOrderId;
                array[i][2] = bean.materialId;
                i++;
            }
            lastMaterialId=bean.materialId;
        }
        int[] numArray=Db.batch("update warehouse_out_order_material set send_num=? where warehouse_out_order_id=? and material_id=?",array,100);
        int sum=0;
        if(numArray!=null){
            for(int num:numArray){
                sum=sum+num;
            }
        }
        return sum;
    }

    /**
     * 更新store_order_material表real_send_num字段（物流真实的发货数量）
     * @param list
     * @return
     */
    private int doUpdateStoreOrderMaterial(List<WarehouseOutOrderMaterialDetailBean> list,String storeOrderId){
        Object[][] array=new Object[list.size()][3];
        int i=0;
        String lastMaterialId="";
        for(WarehouseOutOrderMaterialDetailBean bean:list){
            if(lastMaterialId.equals(bean.materialId)) {//如果原材料id相同，认为是同一个原材料，那么sendNum相加
                Object[] lastArray=array[i-1];
                int sendNum=(Integer)lastArray[0];
                lastArray[0]=sendNum+bean.sendNum;
            }else{
                array[i][0] = bean.sendNum;
                array[i][1] = storeOrderId;
                array[i][2] = bean.materialId;
                i++;
            }
            lastMaterialId=bean.materialId;
        }
        int[] numArray=Db.batch("update store_order_material set real_send_num=? where store_order_id=? and material_id=? ",array,100);
        int sum=0;
        if(numArray!=null){
            for(int num:numArray){
                sum=sum+num;
            }
        }
        return sum;
    }
    /**
     * 保存并出库
     * 将数据保存到warehouse_out_order_material表
     * 将数据保存到warehouse_out_order_material_detail表
     * 将数据保存到store_order_material表
     *
     * 更新warehouse_out_order、store_order状态
     *
     * @param id 订单编号
     * @param array 提交的数据
     * @return
     */
    @Before(Tx.class)
    public JsonHashMap out(String id, JSONArray array){
        JsonHashMap jhm=new JsonHashMap();
        Record warehouseOutOrderR=Db.findById("warehouse_out_order",id);
        String storeOrderId=null;
        if(warehouseOutOrderR==null){
            jhm.putCode(0).putMessage("无此订单记录！");
            return jhm;
        }else{
            // 下面代码为校验代码，为了debug调试，所以注释掉，调试成功后，必须取消注释下面代码 author:mym
            String status=warehouseOutOrderR.getStr("status");
            storeOrderId=warehouseOutOrderR.getStr("store_order_id");
            if("30".equals(status)){
                jhm.putCode(0).putMessage("此订单已经出库，不能再次出库！");
                return jhm;
            }
            if("40".equals(status)){
                jhm.putCode(0).putMessage("此订单已经完成，不能再次出库！");
                return jhm;
            }
        }
        String store_order_id=warehouseOutOrderR.getStr("store_order_id");
        /*
        表格上本来就有的原材料，封装在该list中，用于后续更新warehouse_Out_Order_Material表的数量
         */
        List<WarehouseOutOrderMaterialDetailBean> warehouseOutOrderMaterialDetailBeanList =new ArrayList<>();
        /*
        从树上选择添加的原材料，封装在该list中，该list的元素是要保持到warehouse_out_order_material表、store_order_material表
         */
        List<WarehouseOutOrderMaterialDetailBean> newWarehouseOutOrderMaterialDetailBeanList =new ArrayList<>();
        /*
        表格上本来就有的原材料，封装在该list中。该list的元素是要保持到warehouse_out_order_material_detail表的
         */
        List<Record> recordList=new ArrayList();
        /*
        从树上选择添加的原材料，封装在该list中。该list的元素是要保持到warehouse_out_order_material_detail表的
         */
        List<Record> newRecordList=new ArrayList();
        buildSaveData(warehouseOutOrderR,array,recordList, newRecordList,warehouseOutOrderMaterialDetailBeanList,newWarehouseOutOrderMaterialDetailBeanList);
        //保存Warehouse_Out_Order_Material_Detail前先清空
        int numClear=clearWarehouseOutOrderMaterialDetail(id);
        int numSaveWarehouseOutOrderMaterialDetail=doSaveWarehouseOutOrderMaterialDetail(recordList);
        int numUpdateWarehouseOutOrderMaterial= doUpdateWarehouseOutOrderMaterial(warehouseOutOrderMaterialDetailBeanList,id);

        /*
        从树中添加的原材料，保存到warehouse_out_order_material表、warehouse_out_order_material_detail表中
         */
        doSaveNewRecord(warehouseOutOrderR,newRecordList,null,true);

        /*
        更新store_order_material表real_send_num字段（物流真实的发货数量）
         */
        int numUpdateStoreOrderMaterial=doUpdateStoreOrderMaterial(warehouseOutOrderMaterialDetailBeanList,storeOrderId);

        /*
        从库存中减去数量
         */
        List<WarehouseOutOrderMaterialDetailBean> allWarehouseOutOrderMaterialDetailBeanList=new ArrayList<>();
        allWarehouseOutOrderMaterialDetailBeanList.addAll(warehouseOutOrderMaterialDetailBeanList);
        allWarehouseOutOrderMaterialDetailBeanList.addAll(newWarehouseOutOrderMaterialDetailBeanList);
        sub(allWarehouseOutOrderMaterialDetailBeanList);

        String datetime= DateTool.GetDateTime();
        Db.update("update warehouse_out_order set status=?,finish_time=? where id=?",30,datetime,id);
        Db.update("update store_order set status=?,out_time=? where id=?",40,datetime,store_order_id);
        jhm.putCode(1).putMessage("出库成功！");
        return jhm;
    }

    /**
     * 从库存中减去出库的数量
     * @param list
     */
    private void sub(List<WarehouseOutOrderMaterialDetailBean> list){
        Object[][] array=new Object[list.size()][3];
        int i=0;
        for(WarehouseOutOrderMaterialDetailBean bean:list){
            array[i][0]=bean.sendNum;
            array[i][1]=bean.materialId;
            array[i][2]=bean.batchCode;

            i++;
        }
        Db.batch("update warehouse_stock set number=number-? where material_id=? and batch_code=?",array,30);
    }
    /**
     * 从树上选择添加的原材料，添加到store_order_material表、store_order_material_detail、store_order_material表
     * @param newRecordList 该list里的元素是封装warehouse_out_order_material_detail的数据
     * @param newWarehouseOutOrderMaterialDetailBeanList
     */
    private void doSaveNewRecord(Record warehouseOutOrderR,List<Record> newRecordList,List<WarehouseOutOrderMaterialDetailBean> newWarehouseOutOrderMaterialDetailBeanList,boolean isSaveStoreOrderMaterial){
        String warehouseOutOrderId=warehouseOutOrderR.getStr("id");
        String storeId=warehouseOutOrderR.getStr("store_id");
        String storeOrderId=warehouseOutOrderR.getStr("store_order_id");
        /*
           封装warehouse_out_order_material表的保存数据
         */
        List<Record> warehouseOutOrderMaterialList=new ArrayList<>();
        List<Record> storeOrderMaterialList=new ArrayList<>();
        String lastMaterialId="";
        String lastId="";
        for(Record r:newRecordList){
            String warehouseId=r.getStr("warehouse_id");
            String materialId=r.getStr("material_id");
            String code=r.getStr("code");
            String name=r.getStr("name");
            int sendNum=r.getInt("send_num");
            String unit=r.getStr("unit");

            if(!materialId.equals(lastMaterialId)){//如果不相同，说明是不同的原材料，准备封装warehouse_out_order_material数据
                Record warehouseOutOrderMaterial=new Record();
                String uuid= UUIDTool.getUUID();
                /*
                构建出库订单明细表（原材料）
                 */
                Record warehouseOutOrderMaterialR=new Record();
                warehouseOutOrderMaterialR.set("id",uuid);
                warehouseOutOrderMaterialR.set("warehouse_out_order_id",warehouseOutOrderId);
                warehouseOutOrderMaterialR.set("warehouse_id",warehouseId);
                warehouseOutOrderMaterialR.set("store_id",storeId);
                warehouseOutOrderMaterialR.set("material_id",materialId);
                warehouseOutOrderMaterialR.set("code",code);
                warehouseOutOrderMaterialR.set("name",name);
                warehouseOutOrderMaterialR.set("send_num",sendNum);
                warehouseOutOrderMaterialR.set("status","20");//1表示门店订货
                warehouseOutOrderMaterialR.set("unit",unit);
                warehouseOutOrderMaterialR.set("want_num",0);

                warehouseOutOrderMaterialList.add(warehouseOutOrderMaterialR);

                Record storeOrderMaterialR = new Record();
                String idOfstoreOrderMaterial = UUIDTool.getUUID();
                storeOrderMaterialR.set("id", idOfstoreOrderMaterial);
                storeOrderMaterialR.set("store_order_id", storeOrderId);
                storeOrderMaterialR.set("store_id", storeId);
                storeOrderMaterialR.set("material_id", materialId);
                storeOrderMaterialR.set("code", code);
                storeOrderMaterialR.set("name", name);
                storeOrderMaterialR.set("use_num", 0);
                storeOrderMaterialR.set("send_num", sendNum);
                storeOrderMaterialR.set("real_send_num", sendNum);
                storeOrderMaterialR.set("status", "20");//物流主动给门店发的货物
                storeOrderMaterialR.set("want_num", 0);

                storeOrderMaterialList.add(storeOrderMaterialR);

                /*
                给warehouse_out_order_material_detail表数据回填warehouse_out_order_material表的id
                 */
                r.set("warehouse_out_order_material_id",uuid);
                lastId=uuid;
            }else{//如果相同，说明该原材料与上一个原材料是同一个，那么发送数量相加
                Record warehouseOutOrderMaterialR=warehouseOutOrderMaterialList.get(warehouseOutOrderMaterialList.size()-1);
                int sendNumInList=warehouseOutOrderMaterialR.getInt("send_num");
                warehouseOutOrderMaterialR.set("send_num",sendNumInList+sendNum);
                /*
                给warehouse_out_order_material_detail表数据回填warehouse_out_order_material表的id
                 */
                r.set("warehouse_out_order_material_id",lastId);
            }


            lastMaterialId=materialId;
        }
        Db.batchSave("warehouse_out_order_material_detail",newRecordList,30);
        Db.batchSave("warehouse_out_order_material",warehouseOutOrderMaterialList,30);
        if(isSaveStoreOrderMaterial) {
            Db.batchSave("store_order_material", storeOrderMaterialList, 30);
        }
    }
    /**
     * 根据出库单id删除warehouse_out_order_material_detail表记录
     * 用户保存、出库操作
     * @param warehouseOutOrderId
     * @return
     */
    private int clearWarehouseOutOrderMaterialDetail(String warehouseOutOrderId){
        return Db.update("delete from warehouse_out_order_material_detail where warehouse_out_order_id=?",warehouseOutOrderId);
    }
    /**
     * 批量保存
     * @param recordList
     * @return
     */
    private int doSaveWarehouseOutOrderMaterialDetail(List<Record> recordList){
        int sum=0;
        int[] numArray = Db.batchSave("warehouse_out_order_material_detail", recordList, 100);
        if (numArray != null) {
            for (int num : numArray) {
                sum = sum + num;
            }
        }
        return sum;
    }

    /**
     * 点击树添加
     * @param recordList
     * @return
     */
    private int doSaveNewWarehouseOutOrderMaterialDetail(List<Record> recordList){
        int sum=0;
        int[] numArray = Db.batchSave("warehouse_out_order_material_detail", recordList, 100);
        if (numArray != null) {
            for (int num : numArray) {
                sum = sum + num;
            }
        }
        return sum;
    }
}
