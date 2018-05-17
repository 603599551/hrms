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

import java.util.*;

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
        String sql="select a.id as tid,a.material_id,a.code,a.name,(select name from goods_attribute where id=a.attribute_2) attribute_2_text ,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,a.want_num,a.send_num,'' as search_text,0 as send_number from warehouse_out_order_material a left join material b on a.material_id=b.id where a.warehouse_out_order_id=?";
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
        if("10".equals(statusOfWarehouseOutOrderR)){//如果新建订单，出库数从库存中提取
            reList=buildWarehouseStockData(materialIdList,warehouseOutOrderMaterialList);
        }else{//保存、出库、完成时，出库数从warehouse_out_order_material_detail表中提取
            reList=buildWarehouseOutOrderMaterialDetail(id,materialIdList,warehouseOutOrderMaterialList);
        }


        return reList;
    }

    /**
     * 根据表warehouse_out_order_material_detail构建数据
     * @param id
     * @param materialIdList
     * @param warehouseOutOrderMaterialList
     */
    private List buildWarehouseOutOrderMaterialDetail(String id,List<String> materialIdList,List<Record> warehouseOutOrderMaterialList){
        SelectUtil selectSQL=new SelectUtil("select a.*,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,b.pinyin,c.number as warehouse_stock_num from warehouse_out_order_material_detail a left join material b on a.material_id=b.id  left join warehouse_stock c on a.material_id=c.material_id and a.batch_code=c.batch_code  ");
        selectSQL.in(" and a.material_id in ",materialIdList.toArray());
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

    /*
        查询库存
        注：入库时要录入批号，所以同一个原材料会有多条记录
     */
    private List buildWarehouseStockData(List<String> materialIdList,List<Record> warehouseOutOrderMaterialList){

        SelectUtil selectSQL=new SelectUtil("select a.*,(select name from goods_attribute where goods_attribute.id=a.attribute_2) as attribute_2_text,(select name from goods_unit where goods_unit.id=a.unit) as unit_text,b.pinyin from warehouse_stock a left join material b on a.material_id=b.id  ");
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
     * 构建子数据
     * 根据该订单原材料的id，获取对应库存原材料的库存数量、批号
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
        Record r=Db.findById("warehouse_out_order",id);
        if(r==null){
            jhm.putCode(0).putMessage("无此订单记录！");
            return jhm;
        }else{
            // 下面代码为debug模式，调试成功后，必须取消注释下面代码 author:mym
            String status=r.getStr("status");
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
        List<Record> recordList=new ArrayList();
        buildSaveData(id,array,recordList, warehouseOutOrderMaterialDetailBeanList);
        int numClear=clearWarehouseOutOrderMaterialDetail(id);
        int numSaveWarehouseOutOrderMaterialDetail=doSaveWarehouseOutOrderMaterialDetail(recordList);
        int num= doUpdateWarehouseOutOrderMaterial(warehouseOutOrderMaterialDetailBeanList,id);
        String datetime= DateTool.GetDateTime();
        Db.update("update warehouse_out_order set status=?,finish_time=? where id=?",20,datetime,id);
        jhm.putCode(1).putMessage("保存成功！");
        return jhm;
    }
    class WarehouseOutOrderMaterialDetailBean {
        int sendNum;
        String materialId;

    }
    /**
     * 构建保存的数据
     * 将传入的数据，重新构建，并放入到recordList，warehouseOutOrderMaterialMap
     * @param id
     * @param array
     * @param recordList
     * @param warehouseOutOrderMaterialDetailBeanList
     */
    private void buildSaveData(String id, JSONArray array, List<Record> recordList, List<WarehouseOutOrderMaterialDetailBean> warehouseOutOrderMaterialDetailBeanList){
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
                String batch_code = subJson.getString("batch_code");
                String warehouseOutOrderMaterialId = subJson.getString("warehouseOutOrderMaterialId");
                String code = subJson.getString("code");
                String name = subJson.getString("name");
//                String wantNum = subJson.getString("want_num");

                materialIdList.add(materialId);

                Record saveR = new Record();
                saveR.set("id", UUIDTool.getUUID());
                saveR.set("warehouse_out_order_id", id);
                saveR.set("warehouse_id", warehouseId);
                saveR.set("warehouse_out_order_material_id", warehouseOutOrderMaterialId);
                saveR.set("warehouse_stock_id", warehouseStockId);
                saveR.set("send_num", send_number);
                saveR.set("status", "1");
                saveR.set("sort", sort);
                saveR.set("batch_code", batch_code);
                saveR.set("material_id", materialId);
                saveR.set("code", code);
                saveR.set("name", name);
//                saveR.set("want_num", wantNum);
                recordList.add(saveR);
                sort++;

                WarehouseOutOrderMaterialDetailBean bean=new WarehouseOutOrderMaterialDetailBean();
                bean.materialId =materialId;
                bean.sendNum=NumberUtils.parseInt(send_number,0);
                warehouseOutOrderMaterialDetailBeanList.add(bean);
            }
        }

        /*
        填充warehouse_out_order_material_id数据
         */
        SelectUtil selectUtil=new SelectUtil("select id,warehouse_out_order_id,material_id from warehouse_out_order_material");
        selectUtil.in("and material_id in ",materialIdList.toArray());
        selectUtil.addWhere("and warehouse_out_order_id=?",id);
        String sql=selectUtil.toString();
        List<Record> warehouseOutOrderMaterialList=Db.find(sql,selectUtil.getParameters());

        for(Record r:recordList){
            String warehouseOutOrderIdOfR=r.getStr("warehouse_out_order_id");
            String materialIdOfR=r.getStr("material_id");
            for(Record warehouseOutOrderMaterialR:warehouseOutOrderMaterialList){
                String idOfWarehouseOutOrderMaterialR=warehouseOutOrderMaterialR.getStr("id");
                String wooiOfWarehouseOutOrderMaterialR=warehouseOutOrderMaterialR.getStr("warehouse_out_order_id");
                String materialIdOrderMaterialR=warehouseOutOrderMaterialR.getStr("material_id");
                if(warehouseOutOrderIdOfR.equals(wooiOfWarehouseOutOrderMaterialR) && materialIdOfR.equals(materialIdOrderMaterialR)){
                    r.set("warehouse_out_order_material_id",idOfWarehouseOutOrderMaterialR);
                }
            }
        }

    }
    /*
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
        Record r=Db.findById("warehouse_out_order",id);
        String storeOrderId=null;
        if(r==null){
            jhm.putCode(0).putMessage("无此订单记录！");
            return jhm;
        }else{
            // 下面代码为校验代码，为了debug调试，所以注释掉，调试成功后，必须取消注释下面代码 author:mym
            String status=r.getStr("status");
            storeOrderId=r.getStr("store_order_id");
            if("30".equals(status)){
                jhm.putCode(0).putMessage("此订单已经出库，不能再次出库！");
                return jhm;
            }
            if("40".equals(status)){
                jhm.putCode(0).putMessage("此订单已经完成，不能再次出库！");
                return jhm;
            }
        }
        String store_order_id=r.getStr("store_order_id");
        List<WarehouseOutOrderMaterialDetailBean> warehouseOutOrderMaterialDetailBeanList =new ArrayList<>();
        List<Record> recordList=new ArrayList();
        buildSaveData(id,array,recordList, warehouseOutOrderMaterialDetailBeanList);
        //保存Warehouse_Out_Order_Material_Detail前先清空
        int numClear=clearWarehouseOutOrderMaterialDetail(id);
        int numSaveWarehouseOutOrderMaterialDetail=doSaveWarehouseOutOrderMaterialDetail(recordList);
        int numUpdateWarehouseOutOrderMaterial= doUpdateWarehouseOutOrderMaterial(warehouseOutOrderMaterialDetailBeanList,id);
        int numUpdateStoreOrderMaterial=doUpdateStoreOrderMaterial(warehouseOutOrderMaterialDetailBeanList,storeOrderId);
        String datetime= DateTool.GetDateTime();
        Db.update("update warehouse_out_order set status=?,finish_time=? where id=?",30,datetime,id);
        Db.update("update store_order set status=?,out_time=? where id=?",40,datetime,store_order_id);
        jhm.putCode(1).putMessage("出库成功！");
        return jhm;
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
}
