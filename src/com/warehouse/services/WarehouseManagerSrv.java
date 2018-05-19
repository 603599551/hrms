package com.warehouse.services;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.JsonHashMap;
import easy.util.UUIDTool;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class WarehouseManagerSrv {
    @Before(Tx.class)
    public JsonHashMap doImp(File file, String warehouse_id, UserSessionUtil usu) throws Exception{
        JsonHashMap jhm=new JsonHashMap();
        /*
        读取excel
         */
        HSSFWorkbook workbook=null;
        try {
            FileInputStream fis = new FileInputStream(file);
            workbook = new HSSFWorkbook(fis);
        }catch (Exception e){
            throw e;
        }
        //获取第一个工作簿
        HSSFSheet sheet = workbook.getSheetAt(0);
        int rowNum=sheet.getLastRowNum();
        List<Record> recordList=new ArrayList();
        List<String> codeList=new ArrayList<>();
        /*
        从第三行开始读取
         */
        for(int i=2;i<rowNum;i++){
            HSSFRow row=sheet.getRow(i);
            HSSFCell cell0=row.getCell(0);
            HSSFCell cell1=row.getCell(1);
            HSSFCell code=row.getCell(2);
            HSSFCell cell3=row.getCell(3);
            HSSFCell cell4=row.getCell(4);
            HSSFCell batch_code=row.getCell(5);
            batch_code.setCellType(HSSFCell.CELL_TYPE_STRING);
            HSSFCell number=row.getCell(6);
            number.setCellType(HSSFCell.CELL_TYPE_STRING);

            Record r=new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("warehouse_id",warehouse_id);
            r.set("batch_code",batch_code.getStringCellValue());
            r.set("code",code.getStringCellValue());
            r.set("number",number.getStringCellValue());
            r.set("creater_id",usu.getUserId());
            r.set("create_time", DateTool.GetDateTime());
            r.set("type","material");

            codeList.add(code.getStringCellValue());
            recordList.add(r);
        }
        /*
        根据读取excel中原材料的code，查询原材料表，将空余的信息补充
         */
        SelectUtil selectUtil=new SelectUtil("select * from material");
        selectUtil.in(" and code in",codeList.toArray());
        List<Record> materialList= Db.find(selectUtil.toString(),selectUtil.getParameters());
        for(Record materialR:materialList){
            String idOfMaterialR=materialR.getStr("id");
            String nameOfMaterialR=materialR.getStr("name");
            String codeOfMaterialR=materialR.getStr("code");
            System.out.println();
            for(Record rOfList:recordList){
                String codeOfList=rOfList.getStr("code");
                if(codeOfMaterialR.equals(codeOfList)){
                    rOfList.set("material_id",idOfMaterialR);
                    rOfList.set("name",nameOfMaterialR);
                }
            }
        }
        int[] numArray=Db.batchSave("warehouse_stock",recordList,100);
        int sum=0;
        for(int i :numArray){
            sum=sum+i;
        }
        System.out.println("excel记录数："+rowNum+"；保存到数据库的记录数："+sum);
        return jhm;
    }
}
