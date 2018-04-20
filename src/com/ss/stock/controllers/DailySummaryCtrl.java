package com.ss.stock.controllers;

import com.bean.UserBean;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.services.DailySummaryService;
import com.utils.FileUtil;
import com.utils.HanyuPinyinHelper;
import com.utils.UserSessionUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import utils.bean.JsonHashMap;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DailySummaryCtrl extends BaseCtrl {

    public static final String[] impDailySummaryColumns = {"code","name","price","sale_num","sale_price","refund_num","refund_price","manager_meal_num","manager_meal_price"};

    public static final String[] thead_title = {"title", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "last_year", "today", "unit"};
    /**
     * 判断是否已经导入过了
     */
    public void hasDailySummary(){
        UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
        String impTime = this.getPara("impTime");
        if(impTime != null && impTime.length() > 0){

        }else{
            impTime = DateTool.GetDate();
        }
        List<Record> list = Db.find("select * from daily_summary where store_id=? and imp_time=?", userBean.get("store_id"), impTime);
        JsonHashMap jhm = new JsonHashMap();
        if(list != null && list.size() > 0){
            jhm.putCode(0).putMessage(impTime + "数据已经导入，再次导入将覆盖原数据，是否继续？");
        }
        renderJson(jhm);
    }

    public void getDailySummary(){
        String imp_time = this.getPara("impTime");
        if(imp_time != null && imp_time.length() > 0){

        }else{
            imp_time = DateTool.GetDate();
        }
        UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
        List<Record> dailySummaryList = Db.find("select * from imp_daily_summary where sale_time=? and store_id=?", imp_time, userBean.get("store_id"));
        JsonHashMap jhm = new JsonHashMap();
        if(dailySummaryList != null && dailySummaryList.size() > 0){
            jhm.put("imp_data", dailySummaryList);
        }else{
            jhm.putCode(0).putMessage("没有导入的数据！");
        }
        renderJson(jhm);
    }

    /**
     * 导入文件接口
     */
    public void importDailySummary(){
        JsonHashMap jhm = new JsonHashMap();
        try {
//TODO 通过前台获取导入数据的所属时间
            String imp_time = this.getPara("impTime");
            if(imp_time != null && imp_time.length() > 0){

            }else{
                imp_time = DateTool.GetDate();
            }
            UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
//TODO 通过文件导入的方式导入数据，暂时写死文件路径，方便调试
//            File xls = uploadFile();
            File xls = uploadFile(this.getRequest());

            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xls), "UTF-8"));
            String s = "";
            while(true){
                s = br.readLine();
                if(s != null){
                    sb.append(s);
                }else{
                    break;
                }
            }
            br.close();
            Document doc = Jsoup.parse(sb.toString());
            Element tbody = doc.getElementsByTag("tbody").get(0);
            Elements trs = tbody.getElementsByAttributeValue("role", "row");
            List<Record> impDailySummaryList = new ArrayList<>();
            Record record;
            for(Element tr : trs){
                Elements tds = tr.getElementsByTag("td");
                record = new Record();
                for(int i = 0; i < impDailySummaryColumns.length; i++){
                    Element td = tds.get(i);
                    record.set(impDailySummaryColumns[i], td.html());
                }
                String time= DateTool.GetDateTime();
                record.set("id", UUIDTool.getUUID());
                record.set("create_time", time);
                record.set("modify_time", time);
                record.set("creater_id", userBean.getId());
                record.set("sale_time", imp_time);
                record.set("store_id", userBean.get("store_id"));
                impDailySummaryList.add(record);
            }
            DailySummaryService dailySummaryService = DailySummaryService.getMe();
            dailySummaryService.saveAllData(impDailySummaryList, userBean, imp_time);
            jhm.putMessage("导入数据成功！");
            jhm.put("imp_data", impDailySummaryList);
        }catch (IOException e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.getMessage());
        }
        renderJson(jhm);
    }

    private File uploadFile(){
        File result = new File("C:\\Users\\szsw\\Desktop\\1.html");
        return result;
    }
    private File uploadFile(HttpServletRequest request) throws IOException {
        String dir = UUIDTool.getUUID();
        String path = this.getRequest().getSession().getServletContext().getRealPath("WEB-INF/upload/" + dir);
        String name = FileUtil.upload(path, this.getRequest());
        File result = new File(path + "/" + name);
        return result;
    }

    /**
     * 安存预估
     * time是时间，暂时默认当前时间，从前台获取
     * 返回json：
     *  key                 value
     *  code                成功：1，失败：0
     *  data                失败：异常原因
     *                      成功：map thead和tbody
     */
    public void getSecurityStock(){
        String impTime = this.getPara("time");
        if(impTime != null && impTime.length() > 0){

        }else{
            impTime = DateTool.GetDate();
        }
        DailySummaryService dailySummaryService = DailySummaryService.getMe();
        JsonHashMap jhm = new JsonHashMap();
        try {
            Map<String, Object> map = dailySummaryService.securityStockBudget(impTime);
            List<String> theadList = (List<String>) map.get("thead");
            List<List<String>> tbodyList = (List<List<String>>) map.get("tbody");
            List<Record> theadResult = new ArrayList<>();
            for(int i = 0; i < theadList.size(); i++){
                Record thead = new Record();
                thead.set("key", thead_title[i]);
                thead.set("val", theadList.get(i));
                theadResult.add(thead);
            }
            List<Record> tbody = new ArrayList<>();
            for(List<String> list : tbodyList){
                Record r = new Record();
                for(int i = 0; i < list.size(); i++){
                    r.set(thead_title[i], list.get(i));
                }
                tbody.add(r);
            }
            jhm.put("thead", theadResult);
            jhm.put("tbody", tbody);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putError(e.getMessage());
        }
        String formula = "计算公式：（（最近十天数据总和 / 天数）+ 去年同期数据）/ 2";
        jhm.put("formula", formula);
        renderJson(jhm);
    }

    public void insertMaterial() throws Exception{
        HSSFWorkbook xls = new HSSFWorkbook(new FileInputStream(new File("C:\\Users\\szsw\\Desktop\\Book1.xls")));
        HSSFSheet sheet = xls.getSheetAt(0);
        String[] columns = {"id","code","name","pinyin","wm_type","unit","sort","creater_id","modifier_id","create_time","modify_time","status","type_1","type_2"};
        int rowNum = sheet.getLastRowNum();
        String time = DateTool.GetDateTime();
        String creater = "1";
        List<Record> recordList = new ArrayList<>();
        for(int i = 1; i < rowNum; i++){
            HSSFRow row = sheet.getRow(i);
            Record r = new Record();
            String id = UUIDTool.getUUID();
            String code = getCellFormatValue(row.getCell(1)).toString();
            String name = getCellFormatValue(row.getCell(2)).toString();
            String pinyin = HanyuPinyinHelper.getFirstLettersLo(name);
            String unit = getCellFormatValue(row.getCell(5)).toString();
            String type_2 = getCellFormatValue(row.getCell(6)).toString();
            String wm_type = getCellFormatValue(row.getCell(7)).toString();
            String sort = getCellFormatValue(row.getCell(8)).toString();
            String status = getCellFormatValue(row.getCell(9)).toString();
            String type_1 = getCellFormatValue(row.getCell(10)).toString();
            String[] values = {id,code,name,pinyin,wm_type,unit,sort,creater,creater,time,time,status,type_1,type_2};
            for(int j = 0; j < values.length; j++){
                r.set(columns[j], values[j]);
            }
            recordList.add(r);
        }
        Db.batchSave("material", recordList, recordList.size());
    }
    /**
     *
     * 根据Cell类型设置数据
     *
     * @param cell
     * @return
     * @author zengwendong
     */
    private static Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC
                {
                    DecimalFormat df = new DecimalFormat("0");
                    cellvalue = df.format(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式
                        // data格式是带时分秒的：2013-7-10 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();
                        // data格式是不带带时分秒的：2013-7-10
                        Date date = cell.getDateCellValue();
                        cellvalue = date;
                    } else {// 如果是纯数字

                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:// 默认的Cell值
                    cellvalue = "";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }
}
