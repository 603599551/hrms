package com.warehouse.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.RequestTool;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import com.warehouse.services.WarehouseManagerSrv;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import utils.bean.JsonHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 仓库管理
 */
public class WarehouseManagerCtrl extends BaseCtrl {
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        String uuid=UUIDTool.getUUID();

        try{
            JSONObject jsonObject=RequestTool.getJson(getRequest());
            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String desc=jsonObject.getString("desc");

            if(StringUtils.isEmpty(name)){
                jhm.putCode(0).putMessage("请输入名称！");
                return;
            }
            Record r=new Record();
            r.set("id", uuid);
            r.set("code", code);
            r.set("name", name);
            r.set("desc", desc);
            r.set("status", 1);
            r.set("creater_id", usu.getUserId());
            r.set("modifier_id", usu.getUserId());
            r.set("create_time", datetime);
            r.set("modify_time", datetime);

            boolean b=Db.save("warehouse",r);
            if(b){
                jhm.putCode(1).putMessage("添加成功！");
            }else{
                jhm.putCode(0).putMessage("添加失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void deleteById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            boolean b=Db.deleteById("warehouse",id);
            if(b){
                jhm.putCode(1).putMessage("删除成功！");
            }else{
                jhm.putCode(0).putMessage("删除失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void showById() {
        JsonHashMap jhm=new JsonHashMap();
        String id=getPara("id");
        try {
            Record r = Db.findById("warehouse", id);
            if (r != null) {
                jhm.putCode(1).put("data", r);
            } else {
                jhm.putCode(0).putMessage("查无此记录！");
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

        try{
            JSONObject jsonObject=RequestTool.getJson(getRequest());
            String id=jsonObject.getString("id");
            String name=jsonObject.getString("name");
            String desc=jsonObject.getString("desc");

            if(StringUtils.isEmpty(name)){
                jhm.putCode(0).putMessage("请输入名称！");
                return;
            }
            Record r=new Record();
            r.set("id", id);
            r.set("name", name);
            r.set("desc", desc);
            r.set("status", 1);
            r.set("modifier_id", usu.getUserId());
            r.set("modify_time", datetime);

            boolean b=Db.update("warehouse",r);
            if(b){
                jhm.putCode(1).putMessage("修改成功！");
            }else{
                jhm.putCode(0).putMessage("修改失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    @Override
    public void list() {
        String keyword=getPara("keyword");
        JsonHashMap jhm=new JsonHashMap();
        try{
            SelectUtil selectUtil=new SelectUtil("select id,code,name,`desc`,creater_id,modifier_id,create_time,modify_time,concat(status,'') as status,case status when 1 then '启用' when 0 then '停用' end as status_text from warehouse");
            selectUtil.addWhere("name=?",SelectUtil.NOT_NULL_AND_NOT_EMPTY_STRING,keyword);
            selectUtil.order("order by status desc,create_time");
            String sql=selectUtil.toString();
            List<Record> list=Db.find(sql,selectUtil.getParameterList().toArray());
            jhm.putCode(1).put("list",list);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    public void enabel(){
        String id=getPara("id");
        String status=getPara("status");
        JsonHashMap jhm=new JsonHashMap();
        if(StringUtils.isBlank(id)){
            jhm.putCode(0).putMessage("请输入ID！");
            return;
        }
        if(StringUtils.isBlank(status)){
            jhm.putCode(0).putMessage("请输入状态！");
            return;
        }
        try {
            int i=Db.update("update warehouse set status=? where id=?",status,id);
            if(i>0){
                jhm.putCode(1).putMessage("操作成功！");
            }else{
                jhm.putCode(0).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    @Override
    public void query() {
        super.query();
    }

    @Override
    public void index() {
        super.index();
    }

    /**
     * 导出原材料到excel
     */
    public void export(){
        //拼装生成excel的临时目录，及文件名
        String tempPath=getRequest().getServletContext().getRealPath("temp");
        File tempFile=new File(tempPath);
        if(!tempFile.exists()){
            tempFile.mkdirs();
        }
        String uuid=UUIDTool.getUUID();
        File tempXlsFile=new File(tempFile,uuid+".xls");

        /*
        读取模板文件
         */
        String excelTempPath=getRequest().getServletContext().getRealPath("excel_template");
        File excelTempFile=new File(excelTempPath);
        HSSFWorkbook workbook=null;
        try {
            FileInputStream fis = new FileInputStream(new File(excelTempFile,"初始化物流仓库库存模板.xls"));
            workbook = new HSSFWorkbook(fis);
        }catch (Exception e){
            e.printStackTrace();
        }
        //查询数据库
        String sql="select (select name from material_type where material_type.id=material.type_2)as type_2,(select name from wm_type where wm_type.id=material.wm_type) as wm_type,code,name,(select name from goods_unit where goods_unit.id=material.unit) as unit from material";
        List<Record> list=Db.find(sql);

        //获取第一个工作簿
        HSSFSheet sheet = workbook.getSheetAt(0);
        int rownum=2;
        for(Record r:list){
            String type_2=r.getStr("type_2");
            String wm_type=r.getStr("wm_type");
            String code=r.getStr("code");
            String name=r.getStr("name");
            String material=r.getStr("material");
            String unit=r.getStr("unit");


            //创建行,行号作为参数传递给createRow()方法,第一行从0开始计算
            HSSFRow row = sheet.createRow(rownum);
            //创建单元格,row已经确定了行号,列号作为参数传递给createCell(),第一列从0开始计算
            HSSFCell cell0 = row.createCell(0);
            //设置单元格的值,即C1的值(第一行,第三列)
            cell0.setCellValue(type_2);

            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(wm_type);

            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(code);

            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(name);

            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(unit);

            rownum++;
        }

        FileOutputStream fos=null;
        //输出到磁盘中
        try {

            fos = new FileOutputStream(tempXlsFile);
            workbook.write(fos);
        }catch (Exception e) {
            e.printStackTrace();

        }finally {
            try {
                fos.close();
            }catch (Exception e){

            }

        }
        renderText("导出成功！");
    }

    /**
     * 导入excel到数据库中
     */
    public void imp(){
        File file=new File("f:\\idea-workspace\\security_stock\\web\\upload_temp\\1.xls");
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        WarehouseManagerSrv srv = enhance(WarehouseManagerSrv.class);
        try {
            srv.doImp(file, "2", usu);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

    }
}
