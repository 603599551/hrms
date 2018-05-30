package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.goods.services.MaterialService;
import com.ss.services.MaterialTypeService;
import com.utils.HanyuPinyinHelper;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import utils.NextInt;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;
import utils.jfinal.RecordUtils;

import java.io.File;
import java.io.FileInputStream;
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

    /**
     * 导入原材料
     */
    public void imp(){
        easy.util.JsonHashMap jhm=new easy.util.JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        /*
        先查询所有的分类
         */
        List<Record> materialTypeList=Db.find("select * from material_type");
        List<Record> goodsAttributeList=Db.find("select * from goods_attribute ");
        goodsAttributeSort=goodsAttributeList.size();
        /*
        读取excel
         */
        File file=new File("F:\\idea-workspace\\security_stock\\文档\\面对面BOM\\面对面货品资料表--用于导入.xls");
        HSSFWorkbook workbook=null;
        try {
            FileInputStream fis = new FileInputStream(file);
            workbook = new HSSFWorkbook(fis);
        }catch (Exception e){
            e.printStackTrace();
        }
        //获取第一个工作簿
        HSSFSheet sheet = workbook.getSheetAt(0);
        int rowNum=sheet.getLastRowNum();
        List<Record> recordList=new ArrayList();
        /*
        从第三行开始读取
         */
        int sort=0;
        System.out.println("过滤excel的行号:"+0+"，记录为："+sheet.getRow(0).getCell(0).toString());
        for(int i=1;i<rowNum;i++){
            HSSFRow row=sheet.getRow(i);
            HSSFCell cell0=row.getCell(0);
            HSSFCell code=row.getCell(1);
            code.setCellType(HSSFCell.CELL_TYPE_STRING);
            HSSFCell name=row.getCell(2);
            HSSFCell attribute_2=row.getCell(3);
            HSSFCell attribute_1=row.getCell(4);
            HSSFCell shelf_life=row.getCell(5);
            shelf_life.setCellType(HSSFCell.CELL_TYPE_STRING);
            HSSFCell storage_condition=row.getCell(6);
            if(storage_condition==null){
                System.out.println("过滤excel的行号:"+i+"，记录为："+row.getCell(0).getStringCellValue());
                continue;
            }
            storage_condition.setCellType(HSSFCell.CELL_TYPE_STRING);

            String pinyin=HanyuPinyinHelper.getFirstLettersLo(name.getStringCellValue());

            if(code==null ){
                System.out.println("过滤excel的行号:"+i+"，记录为："+row.getCell(0).getStringCellValue());
                continue;
            }

            /*
            获取二级分类
             */
            String codeStr=code.getStringCellValue();
            if(codeStr==null || "".equals(codeStr) || codeStr.length()<=3){
                System.out.println("过滤excel的行号:"+i+"，记录为："+row.getCell(0).getStringCellValue());
                continue;
            }
            String typeCode=codeStr.substring(0,3);
            Record mt2=getMaterialType(materialTypeList,typeCode);
            String type2=mt2.getStr("id");
            String parentId=mt2.getStr("parent_id");

            /*
            获取一级分类
             */
            Record mt1=getMaterialType2(materialTypeList,parentId);
            String type1=mt1.getStr("id");

            String attribute1Id="";
            if(!"".equals(attribute_1.getStringCellValue())){
                attribute1Id=getAttributeId(goodsAttributeList,attribute_1.getStringCellValue(),usu);
            }
            String attribute2Id="";
            if(!"".equals(attribute_2.getStringCellValue())) {
                attribute2Id = getAttributeId(goodsAttributeList, attribute_2.getStringCellValue(), usu);
            }
            Record r=new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("code",code.getStringCellValue());
            r.set("name",name.getStringCellValue());
            r.set("pinyin",pinyin);
            r.set("yield_rate",100);
            r.set("wm_type","5");
            r.set("unit","1");
            r.set("sort",sort);
            r.set("type_1",type1);
            r.set("type_2",type2);
            r.set("code",code.getStringCellValue());
            r.set("attribute_2",attribute2Id);
            r.set("attribute_1",attribute1Id);
            r.set("creater_id",usu.getUserId());
            r.set("modifier_id",usu.getUserId());
            r.set("create_time", DateTool.GetDateTime());
            r.set("modify_time", DateTool.GetDateTime());
            r.set("status", 1);
            r.set("shelf_life",shelf_life.getStringCellValue());
            r.set("storage_condition",storage_condition.getStringCellValue());

            recordList.add(r);
            sort++;
        }
        int sum=0;
        try {
            int[] numArray = Db.batchSave("material", recordList, 100);
            for (int i : numArray) {
                sum = sum + i;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("excel记录数："+rowNum+"；保存到数据库的记录数："+sum);
    }
    int goodsAttributeSort=1;
    private String getAttributeId(List<Record> goodsAttributeList,String attributeName,UserSessionUtil usu){
        int max=1;
        /*
            如果有就返回id
             */
        for(Record r:goodsAttributeList){
            String id=r.getStr("id");
            String name=r.getStr("name");

            if(name.equals(attributeName)) {
                return id;
            }
        }
        /*
        没有就保存，并返回id
         */
        String datetime=DateTool.GetDateTime();
        String uuid=UUIDTool.getUUID();
        Record goods_attributeR=new Record();
        goods_attributeR.set("id",uuid);
        goods_attributeR.set("parent_id","0");
        goods_attributeR.set("name",attributeName);
        goods_attributeR.set("sort",goodsAttributeSort);
        goods_attributeR.set("creater_id",usu.getUserId());
        goods_attributeR.set("modifier_id",usu.getUserId());
        goods_attributeR.set("create_time",datetime);
        goods_attributeR.set("modify_time",datetime);
        Db.save("goods_attribute",goods_attributeR);
        goodsAttributeSort++;
        return uuid;
    }
    private Record getMaterialType(List<Record> materialTypeList,String code){
        if(code==null || "".equals(code)){
            throw new NullPointerException("必须输入code！");
        }
        for(Record r:materialTypeList){
            String codeDb=r.getStr("code");
            if(code.equals(codeDb)){
                return r;
            }
        }
        return null;
    }
    private Record getMaterialType2(List<Record> materialTypeList,String parentId){
        if(parentId==null || "".equals(parentId)){
            throw new NullPointerException("必须输入parentId！");
        }
        for(Record r:materialTypeList){
            String id=r.getStr("id");
            if(parentId.equals(id)){
                return r;
            }
        }
        return null;
    }
}
