package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.goods.services.MaterialService;
import com.ss.services.MaterialTypeService;
import com.utils.*;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.NextInt;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;
import utils.jfinal.RecordUtils;

import java.util.ArrayList;
import java.util.List;

public class MaterialCtrl extends BaseCtrl {
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String yieldRateStr=jsonObject.getString("yield_rate");
            String purchasePriceStr=jsonObject.getString("purchase_price");
            String balancePriceStr=jsonObject.getString("balance_price");
            String wm_type=jsonObject.getString("wm_type");//库存类型
            String attribute_1=jsonObject.getString("attribute_1");
            String attribute_2=jsonObject.getString("attribute_2");
            String unit=jsonObject.getString("unit");
            String sortStr=jsonObject.getString("sort");
//            String type_1=jsonObject.getString("type_1");
            String type_2=jsonObject.getString("type");
            String desc=jsonObject.getString("desc");

            String unit_num = jsonObject.getString("unit_num");
            String unit_big = jsonObject.getString("unit_big");
            String box_attr_num = jsonObject.getString("box_attr_num");
            String box_attr = jsonObject.getString("box_attr");
            String out_unit = jsonObject.getString("out_unit");
            String storage_condition = jsonObject.getString("storage_condition");
            String security_time = jsonObject.getString("security_time");
            String order_type = jsonObject.getString("order_type");
            String model = jsonObject.getString("model");
            String size = jsonObject.getString("size");
            String brand = jsonObject.getString("brand");
            String shelf_life_num = jsonObject.getString("shelf_life_num");
            String shelf_life_unit = jsonObject.getString("shelf_life_unit");
            String out_price = jsonObject.getString("out_price");


            if(org.apache.commons.lang.StringUtils.isEmpty(code)){
                code=buildCode(null)+"";
            }else{
                List<Record> list = Db.find("select * from material where code=? ", code );
                if(list != null && list .size() > 0){
                    jhm.putCode(-1).putMessage("编码不能重复，请重新填写！");
                    renderJson(jhm);
                    return;
                }
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(name)){
                jhm.putCode(-1).putMessage("请输入商品名称！");
                renderJson(jhm);
                return;
            }else{
                List<Record> list = Db.find("select * from material where name=? ", name );
                if(list != null && list .size() > 0){
                    jhm.putCode(-1).putMessage("名称不能重复，请重新填写！");
                    renderJson(jhm);
                    return;
                }
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(purchasePriceStr)){
                jhm.putCode(-1).putMessage("请输入采购价格！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(balancePriceStr)){
                jhm.putCode(-1).putMessage("请输入默认结算价格！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(wm_type)){
                jhm.putCode(-1).putMessage("请选择库存类型！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(unit)){
                jhm.putCode(-1).putMessage("请选择单位！");
                renderJson(jhm);
                return;
            }
//            if(org.apache.commons.lang.StringUtils.isEmpty(type_1)){
//                jhm.putCode(-1).putMessage("请选择分类！");
//                renderJson(jhm);
//                return;
//            }
            if(org.apache.commons.lang.StringUtils.isEmpty(type_2)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            String type_1=Db.queryFirst("select parent_id from material_type where id=?",type_2);
            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);

            double yieldRate= 0;
            try{
                yieldRate=NumberUtils.parseDouble(yieldRateStr,100);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的出成率！");
                renderJson(jhm);
                return;
            }
            double purchasePrice= 0;
            try{
                purchasePrice=NumberUtils.parseDouble(purchasePriceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的采购价格！");
                renderJson(jhm);
                return;
            }

            double balancePrice= 0;
            try{
                balancePrice=NumberUtils.parseDouble(balancePriceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的默认结算价格");
                renderJson(jhm);
                return;
            }

            int sort=0;
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)){
                int maxSort=DbUtil.queryMax("goods","sort");
                sort=NextInt.nextSortTen(maxSort);
            }else{
                sort=NumberUtils.parseInt(sortStr,1000);
            }
            Record r=new Record();
            r.set("id",uuid);
            r.set("code",code);
            r.set("name",name);
            r.set("pinyin",pinyin);
            r.set("yield_rate",yieldRate);
            r.set("purchase_price",purchasePrice);
            r.set("balance_price",balancePrice);
            r.set("wm_type",wm_type);
            r.set("attribute_1",attribute_1);
            r.set("attribute_2",attribute_2);
            r.set("unit",unit);
            r.set("sort",sort);
            r.set("type_1",type_1);
            r.set("type_2",type_2);
            r.set("creater_id",usu.getUserId());
            r.set("modifier_id",usu.getUserId());
            r.set("create_time",datetime);
            r.set("modify_time",datetime);
            r.set("status",1);
            r.set("desc",desc);

            r.set("unit_num",unit_num);
            r.set("unit_big",unit_big);
            r.set("box_attr_num",box_attr_num);
            r.set("box_attr",box_attr);
            r.set("out_unit",out_unit);
            r.set("storage_condition",storage_condition);
            r.set("security_time",security_time);
            r.set("order_type",order_type);
            r.set("model",model);
            r.set("size",size);
            r.set("brand",brand);
            r.set("shelf_life_num",shelf_life_num);
//            r.set("shelf_life_unit",shelf_life_unit);

            int num = 0;

            if(box_attr_num != null && box_attr_num.length() > 0){
                num = UnitConversion.outUnit2SmallUnit(1, unit, unit_big, new Integer(unit_num), box_attr, new Integer(box_attr_num), out_unit);
            }else{
                num = UnitConversion.outUnit2SmallUnit(1, unit, unit_big, new Integer(unit_num), box_attr, 0, out_unit);
                r.set("box_attr_num",null);
            }
            if(shelf_life_num == null || shelf_life_num.trim().length() == 0){
                r.set("shelf_life_num",null);
            }

            double outPrice = new Double(out_price);
            double price = new Double(String.format("%.7f", outPrice / num));
            r.set("purchase_price",price);
            r.set("balance_price",price);
            r.set("out_price",out_price);

            boolean b=Db.save("material",r);
            if(b){
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }

    /**
     * 停用商品
     * 将goods表的status字段改为2
     */
    public void stop() {
        String id=getPara("id");
        String state=getPara("state");
        JsonHashMap jhm=new JsonHashMap();
        if(org.apache.commons.lang.StringUtils.isEmpty(state)){
            jhm.putCode(-1).putMessage("请传状态！");
            renderJson(jhm);
            return;
        }
        try {
            int i = Db.update("update material set status=? where id=?", state, id);
            if (i == 1) {
                jhm.putCode(1).putMessage("操作成功！");
            } else {
                jhm.putCode(-1).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());

        }
        renderJson(jhm);
    }

    @Override
    public void showById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            Record r=Db.findById("material",id);
            RecordUtils.obj2str(r);
            if(r!=null){
                jhm.putCode(1).put("data",r);
            }else{
                jhm.putCode(-1).putMessage("操作失败！");
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
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String uuid=jsonObject.getString("id");
//            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String yieldRateStr=jsonObject.getString("yield_rate");
            String purchasePriceStr=jsonObject.getString("purchase_price");
            String balancePriceStr=jsonObject.getString("balance_price");
//            String wm_type=jsonObject.getString("wm_type");//库存类型
//            String attribute_1=jsonObject.getString("attribute_1");
//            String attribute_2=jsonObject.getString("attribute_2");
            String unit=jsonObject.getString("unit");
            String sortStr=jsonObject.getString("sort");
//            String type_1=jsonObject.getString("type_1");
            String type_2=jsonObject.getString("type");
            String desc=jsonObject.getString("desc");

            String unit_num = jsonObject.getString("unit_num");
            String unit_big = jsonObject.getString("unit_big");
            String box_attr_num = jsonObject.getString("box_attr_num");
            String box_attr = jsonObject.getString("box_attr");
            String out_unit = jsonObject.getString("out_unit");
            String storage_condition = jsonObject.getString("storage_condition");
            String security_time = jsonObject.getString("security_time");
            String order_type = jsonObject.getString("order_type");
            String model = jsonObject.getString("model");
            String size = jsonObject.getString("size");
            String brand = jsonObject.getString("brand");
            String shelf_life_num = jsonObject.getString("shelf_life_num");
            String shelf_life_unit = jsonObject.getString("shelf_life_unit");
            String out_price = jsonObject.getString("out_price");


//            if(org.apache.commons.lang.StringUtils.isEmpty(code)){
//                code= ""+SettingService.me.getGoodsNum();
//            }
            if(org.apache.commons.lang.StringUtils.isEmpty(uuid)){
                jhm.putCode(-1).putMessage("请输入商品id！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(name)){
                jhm.putCode(-1).putMessage("请输入商品名称！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(purchasePriceStr)){
                jhm.putCode(-1).putMessage("请输入采购价格！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(balancePriceStr)){
                jhm.putCode(-1).putMessage("请输入默认结算价格！");
                renderJson(jhm);
                return;
            }
//            if(org.apache.commons.lang.StringUtils.isEmpty(wm_type)){
//                jhm.putCode(-1).putMessage("请选择库存类型！");
//                renderJson(jhm);
//                return;
//            }
            if(org.apache.commons.lang.StringUtils.isEmpty(unit)){
                jhm.putCode(-1).putMessage("请选择单位！");
                renderJson(jhm);
                return;
            }
//            if(org.apache.commons.lang.StringUtils.isEmpty(type_1)){
//                jhm.putCode(-1).putMessage("请选择分类！");
//                renderJson(jhm);
//                return;
//            }
            if(org.apache.commons.lang.StringUtils.isEmpty(type_2)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            String type_1=Db.queryFirst("select parent_id from material_type where id=?",type_2);
            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);

            double yieldRate= 0;
            try{
                yieldRate=NumberUtils.parseDouble(yieldRateStr,100);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的出成率！");
                renderJson(jhm);
                return;
            }
            double purchasePrice= 0;
            try{
                purchasePrice=NumberUtils.parseDouble(purchasePriceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的采购价格！");
                renderJson(jhm);
                return;
            }

            double balancePrice= 0;
            try{
                balancePrice=NumberUtils.parseDouble(balancePriceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的默认结算价格");
                renderJson(jhm);
                return;
            }

            int sort=0;
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)){

            }else{
                try{
                    sort=NumberUtils.parseInt(sortStr,1);
                }catch (Exception e){
                    jhm.putCode(-1).putMessage("请输入正确的序号！");
                    renderJson(jhm);
                    return;
                }
            }
            Record r=new Record();
            r.set("id",uuid);
//            r.set("code",code);
            r.set("name",name);
            r.set("pinyin",pinyin);
            r.set("yield_rate",yieldRate);
            r.set("purchase_price",purchasePrice);
            r.set("balance_price",balancePrice);
//            r.set("wm_type",wm_type);
//            r.set("attribute_1",attribute_1);
//            r.set("attribute_2",attribute_2);
            r.set("unit",unit);
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)) {
//                r.set("sort", sort);
            }else{
                r.set("sort", sort);
            }
            r.set("type_1",type_1);
            r.set("type_2",type_2);
//            r.set("creater_id",usu.getUserId());
            r.set("modifier_id",usu.getUserId());
//            r.set("create_time",datetime);
            r.set("modify_time",datetime);
            r.set("status",1);
            r.set("desc",desc);

//            r.set("unit_num",unit_num);
//            r.set("unit_big",unit_big);
//            r.set("box_attr_num",box_attr_num);
//            r.set("box_attr",box_attr);
//            r.set("out_unit",out_unit);
//            r.set("storage_condition",storage_condition);
            r.set("security_time",security_time);
            r.set("order_type",order_type);
            r.set("model",model);
            r.set("size",size);
            r.set("brand",brand);
            r.set("shelf_life_num",shelf_life_num);

            int num = 0;

            if(box_attr_num != null && box_attr_num.length() > 0){
                num = UnitConversion.outUnit2SmallUnit(1, unit, unit_big, new Integer(unit_num), box_attr, new Integer(box_attr_num), out_unit);
            }else{
                num = UnitConversion.outUnit2SmallUnit(1, unit, unit_big, new Integer(unit_num), box_attr, 0, out_unit);
                r.set("box_attr_num",null);
            }
            double outPrice = new Double(out_price);
            double price = new Double(String.format("%.7f", outPrice / num));
            r.set("purchase_price",price);
            r.set("balance_price",price);
            r.set("out_price",out_price);
            if(shelf_life_num == null || shelf_life_num.trim().length() == 0){
                r.set("shelf_life_num",null);
            }


            boolean b=Db.update("material",r);
            if(b){
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void query() {
        String key=getPara("keyword");
        String type=getPara("materialTypeIds");
        String status=getPara("status");
        String wx_type=getPara("inventoryId");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum=NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        if("0".equals(wx_type)){
            wx_type="";
        }
        JsonHashMap jhm=new JsonHashMap();
        try {

            SQLUtil sqlUtil = new SQLUtil(" from material m ");
            if(StringUtils.isNotEmpty(type)){
                String[] typeArray=type.split(",");
                sqlUtil.in("and type_2 in ",  typeArray);
            }
            if(StringUtils.isNotEmpty(status)) {
                sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
            }else{
                sqlUtil.in("and status in", new Object[]{0,1});
            }
            sqlUtil.addWhere(" and wm_type=?",SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wx_type);

            StringBuilder sql=sqlUtil.getSelectSQL();
            List list=sqlUtil.getParameterList();

            if(org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
                String key2 = "%"+key + "%";
                if (list != null && !list.isEmpty()) {
                    sql.append(" and (code like ? or name like ? or pinyin like ? )");
                } else {
                    sql.append(" where (code like ? or name like ? or pinyin like ?)");

                }
                list.add(key2);
                list.add(key2);
                list.add(key2);
            }
            sql.append(" order by status desc ,sort,create_time desc,id ");
            String select="select m.*,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text";
            Page<Record> page = Db.paginate(pageNum, pageSize, select,sql.toString(),list.toArray() );
            if(page!=null){
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
     * 显示原材料类型（树形结构，用于原材料列表页面左侧的树）
     */
    public void showMaterialTypeTree(){
        JsonHashMap jhm=new JsonHashMap();
        try {
            List<Record> list = Db.find("select id,parent_id,code,name,CONCAT(name,'(',code,')') as label from material_type order by sort,id");
            if(list != null && list.size() > 0){
                for(Record r : list){
                    r.set("search_text",r.getStr("name") + "-" + r.get("code") + "-" + HanyuPinyinHelper.getFirstLettersLo(r.get("name")));
                }
            }
            List resultList=MaterialTypeService.getMe().sort(list);
            jhm.putCode(1).put("list",resultList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    MaterialService materialService=enhance(MaterialService.class);
    /**
     * 删除原材料，逻辑删除
     * 配方中的原材料，是真删除
     */
    public void deleteByIds(){
        String[] idArray=getParaValues("ids");
        JsonHashMap jhm=new JsonHashMap();
        try {
            if (idArray==null || idArray.length==0) {
                jhm.putCode(-1).putMessage("请选择要删除的原材料！");
                renderJson(jhm);
                return;
            }
            materialService.deleteByIds(idArray);
            jhm.putCode(1).putMessage("删除成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示原材料类别的树形结构（添加原材料表单页面使用）
     */
    public void showMaterialTypeTreeForm(){
        String type=getPara("type");
        JsonHashMap jhm = new JsonHashMap();
        List<Record> reList = new ArrayList();
        try {
            List<Record> list = Db.find("select id,parent_id,code,name,sort from material_type order by sort");
            for (Record r : list) {
                String parent_id = r.get("parent_id");
                if ("0".equals(parent_id)) {
                    r.set("name", "┗ " + r.get("name"));
                    reList.add(r);
                }
            }

            for (int i = 0; i < reList.size(); i++) {
                Record rootR = reList.get(i);
                String id = rootR.get("id");
                int x = 1;
                for (int j = 0; j < list.size(); j++) {
                    Record r = list.get(j);
                    String parent_id = r.getStr("parent_id");
                    if (id.equals(parent_id)) {
                        r.set("name", "　┣ " + r.get("name"));
                        reList.add(i + x, r);
                        x++;
                    }
                }
            }
            //将最后一个节点的开头符号改成┗
            Record r = reList.get(reList.size() - 1);
            String name = r.get("name");
            name = "　┗ " + name.substring(2, name.length());
            r.set("name", name);

            if("1".equals(type)) {

            }else{
                //插入第一个节点
                Record firstR = new Record();
                firstR.set("id", "0");
                firstR.set("name", "请选择商品分类");
                reList.add(0, firstR);

            }
            jhm.putCode(1).put("list",reList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    private int buildCode(String id){
        String key="material_code";
        int codeInt=0;

        Object codeObj=Db.queryFirst("select value_int from setting where `key`=?",key);
        codeInt= NumberUtils.parseInt(codeObj,1000)+1;

        if(org.apache.commons.lang.StringUtils.isEmpty(id)){
            String sql="select count(*) from material where code=? ";

            Object countObj = Db.queryFirst(sql, codeInt );
            int count=Integer.parseInt(countObj.toString());
            while(count>0){
                countObj = Db.queryFirst(sql, codeInt );
                count=Integer.parseInt(countObj.toString());
            }

        }else{
            String sql="select count(*) from material where id<>? and code=? ";

            Object countObj = Db.queryFirst(sql, id,codeInt );
            int count=Integer.parseInt(countObj.toString());
            while(count>0){
                countObj = Db.queryFirst(sql, id,codeInt );
                count=Integer.parseInt(countObj.toString());
            }
        }

        Db.update("update setting set value_int=? where `key`=?",codeInt,key);
        return codeInt;
    }





}
