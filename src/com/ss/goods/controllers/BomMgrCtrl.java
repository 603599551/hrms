package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.Config;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.ss.controllers.BaseCtrl;
import com.ss.services.GoodsService;
import com.ss.services.MaterialTypeService;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.SelectUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;
import utils.jfinal.RecordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BomMgrCtrl extends BaseCtrl {

    /**
     * 显示原材料类型（树形结构，用于原材料列表页面左侧的树）
     */
    public void showGoodsTypeTree(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select id,parent_id,code,name,CONCAT(name,'(',code,')') as label from goods_type order by sort,id");
            List resultList= GoodsService.getMe().sort(list);
            jhm.putCode(1).put("list",resultList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示商品列表
     * 仅显示启用的商品
     */
    public void queryGoodsList() {
        Map map=getParaMap();
        String key=getPara("keyword");
        String wmtype=getPara("inventoryId");//库存类型
        String bomStatus=getPara("bomStatus");//配方编辑状态
        String goodsTypeIds=getPara("goodsTypeIds");//商品类别，多个id由英文逗号分割
        String status=getPara("stateId");//商品启用、停用状态。0：停用，1：启用
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        //当type为0时，表示选择全部，所以将type设置为空字符串
        if("0".equals(wmtype)){
            wmtype="";
        }
        //当status为-1时，表示配置状态未选择
        if("-1".equals(bomStatus)){
            bomStatus="";
        }
        JsonHashMap jhm=new JsonHashMap();
        try {
            SQLUtil sqlUtil = new SQLUtil(" from goods g");
            sqlUtil.addWhere("and bom_status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, bomStatus);

            if(goodsTypeIds!=null && !"".equals(goodsTypeIds)){
                String[] array=goodsTypeIds.split(",");
                sqlUtil.in(" and type_2 in ",array);
            }
            sqlUtil.addWhere("and wm_type=? ", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, wmtype);
            sqlUtil.addWhere("and status=? ", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);

            StringBuilder sql=sqlUtil.getSelectSQL();
            List paraList=sqlUtil.getParameterList();

            if(org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
                String key2 = key + "%";
                if (paraList != null && !paraList.isEmpty()) {
                    sql.append(" and (code like ? or name like ? )");
                } else {
                    sql.append(" where (code like ? or name like ? )");

                }
                paraList.add(key2);
                paraList.add(key2);
            }
//            if(org.apache.commons.lang.StringUtils.isNotEmpty(wmtype)){
//
//                //sqlUtil.addWhere("and type_2=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, type);
//                if (paraList != null && !paraList.isEmpty()) {
//                    sql.append(" and wm_type=? ");
//                } else {
//                    sql.append(" where wm_type=?");
//
//                }
//                paraList.add(wmtype);
//            }
            sql.append(" order by status asc,sort,modify_time,id");
            String select="select g.*,ifnull(bom_time,'-') as bom_time_text,case g.bom_status when 0 then '未配置' when 1 then '已配置' end as bom_status_text,(select name from goods_type gt where gt.id=g.type_1) as type_1_text,(select name from goods_type gt where gt.id=g.type_2) as type_2_text,case g.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=g.wm_type) as wm_type_text,(select name from goods_unit where id=g.unit) as goods_unit_text";
            Page<Record> page = Db.paginate(pageNum, pageSize, select,sql.toString(),paraList.toArray() );
            if(page!=null){
                List<Record> list=page.getList();
                if(list!=null && !list.isEmpty()){
                    for(Record r:list){
                        RecordUtils.obj2str(r);
                    }
                }
                jhm.putCode(1).put("data",page);
            }else{
                jhm.putCode(-1).putMessage("请重试！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 查询原料列表
     * 为了让前端实现是联动，所以此接口查询的字段与queryBomByGoodsId()接口的字段一致
     * 实现了左外查询，
     * 为了让选中的原材料排在上面，让关联不上的gm.sort（即返回为null）设置为10000
     *
     *
     *
     *
     select *
     from (

     select gm.id as gm_id,m.id, m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,
     #(select name from material_type where id=m.type_2) as type_2_text,
     case m.status when 1 then '启用' when 0 then '停用' end as status_text,
     (select name from wm_type where id=m.wm_type) as wm_type_text,
     (select name from goods_unit where id=m.unit) as goods_unit_text,
     ifnull(gm.net_num,0) as net_num ,ifnull(gm.gross_num,0) as gross_num,
     m.purchase_price as price,
     gm.total_price,
     ifnull(gm.sort,100000) as gm_sort,m.sort as m_sort

     from material m , ( select * from goods_material where goods_id='e2d6f28dfd0343ae97599500b10a8430') gm where m.id=gm.material_id and   m.status=1
     ) as a
     order by a.gm_sort,m_sort,a.a.id


     */
    public void queryMaterialList() {
        String key=getPara("keyword");
        String[] typeArray=getParaValues("materialTypeIds");//原材料分类
//        String status=getPara("status");
        String wm_type=getPara("inventoryId");//库存类型
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");
        String goodsId=getPara("goodsId");
        String checkedIds=getPara("checkedIds");
        int pageNum=NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        if("0".equals(wm_type)){
            wm_type="";
        }
        JsonHashMap jhm=new JsonHashMap();
        try {

            /*
             * 查询数据库中的配方
             */
            List<Record> dbList=null;
            {
                SelectUtil sqlUtil = new SelectUtil(" from (select gm.id as gm_id,m.id, m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text,ifnull(gm.net_num,0) as net_num ,ifnull(gm.gross_num,0) as gross_num,m.purchase_price as price,gm.total_price,ifnull(gm.sort,100000) as gm_sort,m.sort as m_sort ");
                sqlUtil.append(" from  material m , goods_material gm");
                sqlUtil.addWhere(" and gm.goods_id =? ", goodsId);
                sqlUtil.addWhere(" and m.id=gm.material_id and m.status=1 ");
                sqlUtil.addWhere(" ) as a");
                sqlUtil.order(" order by a.gm_sort,m_sort,a.a.id");

                StringBuilder sql = sqlUtil.getSQL();
                List paraList = sqlUtil.getParameterList();
                if (Config.devMode) {
                    System.out.println(sql);
                    System.out.println(paraList);
                }
                dbList=Db.find("select * " + sql.toString(), paraList.toArray());

            }

            /*
            查询选中的商品，并且该商品不在上面的配方中
             */
            List selectList=null;
            {
                List<String> selectIdList=new ArrayList();
                if (StringUtils.isNotEmpty(checkedIds)) {
                    String[] array = checkedIds.split(",");
                    if(dbList!=null && !dbList.isEmpty()){
                        for(String id:array) {
                            for (Record r : dbList) {
                                String materialId = r.get("id");
                                if (materialId.equals(id)){

                                }else{
                                    selectIdList.add(id);
                                }
                            }
                        }
                    }else{
                        selectIdList.addAll(Arrays.asList(array));
                    }
                }
                if(!selectIdList.isEmpty()) {
                    SelectUtil sqlUtil = new SelectUtil(" from (select '',m.id, m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text,0 as net_num ,0 as gross_num,m.purchase_price as price,0 as total_price,100000 as gm_sort,m.sort as m_sort ");
                    sqlUtil.append(" from  material m ");
                    sqlUtil.addWhere(" m.status=1 ");
                    if (StringUtils.isNotEmpty(checkedIds)) {
                        String[] array = checkedIds.split(",");
                        sqlUtil.in(" and id in ", selectIdList.toArray());//)
                    }
                    sqlUtil.addWhere(" and m.status=1 ");
                    sqlUtil.addWhere(" ) as a");
                    sqlUtil.order(" order by a.gm_sort,m_sort,a.a.id");

                    StringBuilder sql = sqlUtil.getSQL();
                    List paraList = sqlUtil.getParameterList();
                    if (Config.devMode) {
                        System.out.println(sql);
                        System.out.println(paraList);
                    }
                    selectList = Db.find("select * " + sql.toString(), paraList.toArray());

                }
            }
            /*
            将数据库中的配方，选中的的原材料整合到一起
             */
            List<Record> mList=new ArrayList();
            if(dbList!=null && !dbList.isEmpty()) {
                mList.addAll(dbList);
            }
            if(selectList!=null && !selectList.isEmpty()) {
                mList.addAll(selectList);
            }
            List mList2=new ArrayList();
            if (StringUtils.isNotEmpty(checkedIds)) {
                String[] array = checkedIds.split(",");
                if(mList!=null && !mList.isEmpty()){
                    for(String id:array) {
                        for (Record r : mList) {
                            String materialId = r.get("id");
                            if (materialId.equals(id)){
                                mList2.add(r);
                                break;
                            }
                        }
                    }
                }
            }
            /*
            查询其他数据（不是配方的原材料，也不是选中的原材料）
             */
            {
                SelectUtil sqlUtil = new SelectUtil(" from (select '',m.id, m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text,0 as net_num ,0 as gross_num,m.purchase_price as price,0 as total_price,100000 as gm_sort,m.sort as m_sort ");
                sqlUtil.append(" from material  m ");
                sqlUtil.addWhere(" m.status=1 ");
                if (StringUtils.isNotEmpty(checkedIds)) {
                    String[] array = checkedIds.split(",");
                    sqlUtil.in(" and id not in ", array);//)
                }
                if (typeArray != null && typeArray.length > 0) {
                    if (typeArray.length == 1 && "".equals(typeArray[0])) {//前台没有选择分类时，默认传进一个空字符串

                    } else {
                        sqlUtil.in("and m.type_2 in ", typeArray);
                    }
                }
                sqlUtil.addWhere(" and m.wm_type=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, wm_type);
                if (org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
                    String key2 = key + "%";
                    sqlUtil.addWhere(" and (m.code like ? or m.name like ? or m.pinyin like ? )");
                    sqlUtil.addParameter(key2);
                    sqlUtil.addParameter(key2);
                    sqlUtil.addParameter(key2);
                }
                sqlUtil.addWhere(" ) as a");
                sqlUtil.order(" order by a.gm_sort,m_sort,a.a.id");

                StringBuilder sql = sqlUtil.getSQL();
                List paraList = sqlUtil.getParameterList();
                if (Config.devMode) {
                    System.out.println(sql);
                    System.out.println(paraList);
                }



                Page<Record> page = Db.paginate(pageNum, pageSize, "select * ", sql.toString(), paraList.toArray());
                page.getList().addAll(0,mList2);
                if(page!=null){
                    jhm.putCode(1).put("data",page);
                }else{
                    jhm.putCode(-1).putMessage("请重试！");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     *  根据商品id获取该商品的配方清单
     */
    public void queryBomByGoodsId(){
        String goodsId=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        String sql="select gm.goods_id,gm.net_num,gm.gross_num,gm.total_price,gm.sort,gm.id as gm_id,gm.material_id as id,m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,(select name from wm_type where id=m.wm_type) wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text,(select name from material_type where id=m.type_2) as type_2_text from goods_material gm ,material m where gm.material_id=m.id and gm.goods_id=?  order by gm.sort ";
        try{
            List<Record> list=Db.find(sql,goodsId);
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    @Before(Tx.class)
    public void save(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            JSONObject json = RequestTool.getJson(getRequest());
            String goodsId=json.getString("id");
            Object totalPriceObj=json.get("totalPrice");

            //先清空原数据
            Db.delete("delete from goods_material where goods_id=? ",goodsId);

            JSONArray list=json.getJSONArray("list");
            if(list!=null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject jsonObj = list.getJSONObject(i);
                    String mid = jsonObj.getString("id");
                    Object net_numObj = jsonObj.get("net_num");
                    Object gross_numObj = jsonObj.get("gross_num");
                    Object total_priceObj = jsonObj.get("total_price");

                    double net_num = NumberUtils.parseDouble(net_numObj, 0);
                    double gross_num = NumberUtils.parseDouble(gross_numObj, 0);
                    double total_price = NumberUtils.parseDouble(total_priceObj, 0);

                    Record r = new Record();
                    r.set("id", UUIDTool.getUUID());
                    r.set("goods_id", goodsId);
                    r.set("material_id", mid);
                    r.set("net_num", net_num);
                    r.set("gross_num", gross_num);
                    r.set("total_price", total_price);
                    r.set("sort", i + 1);
                    Db.save("goods_material", r);
                }
                Db.update("update goods set bom_status=? , bom_time=? ,total_bom_price=? where id=?", 1, DateTool.GetDateTime(), totalPriceObj, goodsId);
            }else{
                Db.update("update goods set bom_status=? , bom_time=? ,total_bom_price=? where id=?",0, null,totalPriceObj,goodsId);
            }
            jhm.putCode(1).putMessage("保存成功！");
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void updateById(){

    }
}
