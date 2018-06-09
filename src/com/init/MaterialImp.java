package com.init;

import com.jfinal.kit.Prop;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;
import com.utils.HanyuPinyinHelper;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MaterialImp {
    String userId="1";//admin的id
    public MaterialImp(){
        config();
    }

    /**
     * 配置数据库
     */
    public void config(){
        //获取classpath路径下的config.txt配置文件
        Prop prop = new Prop("config.txt", "UTF-8");

        //读取配置文件
        String databaseURL=prop.get("jdbcUrl");
        String databaseUser=prop.get("username");
        String databasePassword=prop.get("password").trim();
        Integer initialPoolSize = prop.getInt("initialPoolSize");
        Integer minIdle = prop.getInt("minIdle");
        Integer maxActivee = prop.getInt("maxActivee");
        System.out.println("读取配置文件成功！");

        //调用druid连接池插件
        DruidPlugin druidPlugin = new DruidPlugin(databaseURL,databaseUser,databasePassword);
        druidPlugin.set(initialPoolSize,minIdle,maxActivee);
        druidPlugin.setFilters("stat,wall");
        druidPlugin.start();
        System.out.println("DruidPlugin加载成功！");


        //调用ActiveRecord插件
        ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
        activeRecordPlugin.start();
        System.out.println("数据库连接成功！");

    }
    public void execute(){

        /*
        先查询所有的分类
         */
        List<Record> materialTypeList=Db.find("select * from material_type");
        List<Record> goodsAttributeList=Db.find("select * from goods_attribute ");
        goodsAttributeSort=goodsAttributeList.size();
//        List<Record> list= Db.find("select * from material");
//        System.out.println();
        List<Object[]> list=readXls();
    }
    /*
    读取excel
     */
    private List<Object[]> readXls(){

        File file=new File("F:\\jr吉软国际\\m面对面\\面对面2.0\\货品明细分类--各部门负责（发软件公司）.xlsx");
        HSSFWorkbook workbook=null;
        try {
            FileInputStream fis = new FileInputStream(file);
            workbook = new HSSFWorkbook(fis);
        }catch (Exception e){
            e.printStackTrace();
        }
        //获取第三个工作簿
        HSSFSheet sheet = workbook.getSheetAt(2);
        int rowNum=sheet.getLastRowNum();
        List<Object[]> recordList=new ArrayList();
        /*
        从第三行开始读取
         */
        int sort=0;
        System.out.println("过滤excel的行号:"+0+"，记录为："+sheet.getRow(0).getCell(0).toString());
        for(int i=0;i<rowNum;i++){
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

            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name.getStringCellValue());

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
//            String typeCode=codeStr.substring(0,3);
//            Record mt2=getMaterialType(materialTypeList,typeCode);
//            String type2=mt2.getStr("id");
//            String parentId=mt2.getStr("parent_id");
//
//            /*
//            获取一级分类
//             */
//            Record mt1=getMaterialType2(materialTypeList,parentId);
//            String type1=mt1.getStr("id");
//
//            String attribute1Id="";
//            if(!"".equals(attribute_1.getStringCellValue())){
//                attribute1Id=getAttributeId(goodsAttributeList,attribute_1.getStringCellValue());
//            }
//            String attribute2Id="";
//            if(!"".equals(attribute_2.getStringCellValue())) {
//                attribute2Id = getAttributeId(goodsAttributeList, attribute_2.getStringCellValue());
//            }
//            Record r=new Record();
//            r.set("id", UUIDTool.getUUID());
//            r.set("code",code.getStringCellValue());
//            r.set("name",name.getStringCellValue());
//            r.set("pinyin",pinyin);
//            r.set("yield_rate",100);
//            r.set("wm_type","5");
//            r.set("unit","1");
//            r.set("sort",sort);
//            r.set("type_1",type1);
//            r.set("type_2",type2);
//            r.set("code",code.getStringCellValue());
//            r.set("attribute_2",attribute2Id);
//            r.set("attribute_1",attribute1Id);
//            r.set("creater_id",userId);
//            r.set("modifier_id",userId);
//            r.set("create_time", DateTool.GetDateTime());
//            r.set("modify_time", DateTool.GetDateTime());
//            r.set("status", 1);
//            r.set("shelf_life",shelf_life.getStringCellValue());
//            r.set("storage_condition",storage_condition.getStringCellValue());
//
//            recordList.add(r);
            sort++;
        }

        return recordList;
    }
    /**
     * 导入原材料
     */
    public void imp(List<Record> recordList){
        easy.util.JsonHashMap jhm=new easy.util.JsonHashMap();

        int sum=0;
        try {
            int[] numArray = Db.batchSave("material", recordList, 100);
            for (int i : numArray) {
                sum = sum + i;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        System.out.println("excel记录数："+rowNum+"；保存到数据库的记录数："+sum);
    }
    int goodsAttributeSort=1;
    private String getAttributeId(List<Record> goodsAttributeList,String attributeName){
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
        goods_attributeR.set("creater_id",userId);
        goods_attributeR.set("modifier_id",userId);
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

    public static void main(String[] args) {
        MaterialImp mi=new MaterialImp();
        mi.execute();
    }

    class MaterialBean extends Record{
//        String type2;
//        String wmType;//来源，自制、外采等
//        String code;//编码
//        String name;//名称
//        String pinyin;
//        String unit;//小单位
//        String unitBig;//大单位
//        int unitNum;//小单位转换大单位的数值
//        String boxAttr;//装箱单位
//        int boxAttrNum;//大单位转换装箱单位的数值
//        String outUnit;//提货单位
//        double outPrice;//餐厅价格
//        String shelfLifeNum;//保质期数
//        String shelfLifeUnit;//保质期单位
//        String storageCondition;//存储条件
//        String securityTime;//到货周期
//        String orderType;//订单类型
//        String model;//规格型号
//        String size;//尺寸大小
//        String brand;//品牌

        void setType2(String type2){
            set("type_2",type2);
        }
    }
}
