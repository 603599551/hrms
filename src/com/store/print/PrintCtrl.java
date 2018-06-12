package com.store.print;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.PDFUtil;
import com.utils.UnitConversion;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintCtrl extends BaseCtrl {

    private static final String[] send_goods_one_page_arr = {"send_company","send_date","send_address","store_name","phone","order_num"};
    private static final String[] outgoing_goods_one_page_arr = {"warehouse_name","date","order_num","store_name"};

    private static Map<String, String> configMap = new HashMap<>();
    static{
        List<Record> configList = Db.find("select * from config");
        if(configList != null && configList.size() > 0){
            for(Record r : configList){
                configMap.put(r.getStr("k"), r.getStr("v"));
            }
        }
    }

    public void printSendGoodsOrder() throws UnsupportedEncodingException {
        JsonHashMap jhm = new JsonHashMap();
        String orderId = getPara("id");
        Record dataRecord = Db.findFirst("SELECT so.arrive_date send_date, s.address send_address, s.name store_name, so.order_number order_num, so.status sostatus FROM store_order so, store s WHERE so.store_id=s.id and so.id=?", orderId);
        if(dataRecord == null){
            jhm.putCode(-1).putMessage("订单号有错误，请确认订单！");
            renderJson(jhm);
            return;
        }
        if(!"40".equals(dataRecord.getStr("sostatus"))){
            jhm.putCode(-1).putMessage("只有已出库的订单可以打印送货单！");
            renderJson(jhm);
            return;
        }
        dataRecord.set("send_company", configMap.get("send_company"));
        dataRecord.set("phone", configMap.get("phone") + " 传真：" + configMap.get("fax"));
        Map<String, String> onePageData = new HashMap<>();
        for(String s : send_goods_one_page_arr){
            onePageData.put(s, dataRecord.getStr(s));
        }
        File onePageTemp = new File(this.getRequest().getSession().getServletContext().getRealPath("") + "/template/sendGoodsOnePage.template");
        BufferedReader br = null;
        String onePageTempStr = "";
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(onePageTemp), "utf-8"));
            String str = br.readLine();
            while(str != null){
                onePageTempStr += str;
                str = br.readLine();
            }
            for(String s : send_goods_one_page_arr){
                onePageTempStr = onePageTempStr.replace("${" + s + "}", onePageData.get(s));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PDFUtil pdfUtil = new PDFUtil(this.getRequest(), "template");
        Map<String,Object> data = new HashMap();
        String table = "";
        String tableStr = "";
        List<Record> dataList = Db.find("select so.order_number order_number, som.*, (select name from goods_unit gu where gu.id=som.unit) uname, (select name from goods_attribute where id=som.attribute_1) ganame from store_order_material som, store_order so where so.id=som.store_order_id and store_order_id=?", orderId);
        String title = "<table class=\"order-list\"><thead style=\"display:table-header-group\"><tr><td colspan=\"5\"><table class=\"order-top\"><tr><th colspan=\"2\" align=\"center\">送货单</th></tr><tr><td width=\"60%\">送货单位：${send_company}</td><td>送货日期：${send_date}</td></tr><tr><td width=\"60%\">地址：${send_address}</td><td>餐厅名称：${store_name}</td></tr><tr><td width=\"60%\">客服电话：${phone}</td><td>单据号：${order_num}</td></tr></table></td></tr></thead></table>";
        for(String s : send_goods_one_page_arr){
            title = title.replace("${" + s + "}", onePageData.get(s));
        }
        if(dataList != null && dataList.size() > 0){
            dataRecord.set("order_number", dataList.get(0).get("order_number"));
            int i = 1;
            for(int j = 0; j < dataList.size(); j++,i++){
                Record r = dataList.get(j);
                tableStr += "<tr><td>" + r.get("code") + "</td><td>" + UnitConversion.getAttrByOutUnit(r) + "</td><td>" + r.get("out_unit") + "</td><td>" + r.get("name") + "</td><td>" + r.get("real_send_num") + "</td></tr>";
                if(i % 31 == 0){
                    table += onePageTempStr.replace("${table}", tableStr);
                    table += "<div class='pageNext'></div>";
                    tableStr = "";
                }
            }
            if(tableStr != null && tableStr.length() > 0){
                table += onePageTempStr.replace("${table}", tableStr);
            }
            data.put("firstPage", "");
            data.put("secondPage", "");
            data.put("thirdPage", "");
            if(i % 31 < 31 && i % 31 >= 27){
                data.put("firstPage", "<div class='pageNext'></div>" + title);
            }else if(i % 31 < 27 && i % 31 >= 22){
                data.put("secondPage", "<div class='pageNext'></div>" + title);
            }else if(i % 31 < 22 && i % 31 >= 18){
                data.put("thirdPage", "<div class='pageNext'></div>" + title);
            }
        }
        data.put("table", table);
        data.put("creater_name", new UserSessionUtil(getRequest()).getRealName());
        String content = pdfUtil.loadDataByTemplate(data, "sendGoodsTemplate.html");
        String path=this.getRequest().getSession().getServletContext().getRealPath("pdf");
        File pathFile=new File(path);
        if(!pathFile.exists()){
            boolean b=pathFile.mkdirs();
            if(!b){
                System.out.println("创建目录【"+pathFile.getAbsolutePath()+"】失败！");
            }
        }
        try {
            String name = UUIDTool.getUUID();
            File file=new File(pathFile,name + ".pdf");
            pdfUtil.createPdf(content,  file.getAbsolutePath() );
//            this.getResponse().sendRedirect(getRequest().getContextPath()  + "/pdf/a.pdf");
            createPrintDetails(orderId, dataRecord, "send_goods");
//            this.getRequest().getRequestDispatcher(getRequest().getContextPath()  + "/pdf/a.pdf");
            Record result = new Record();
            result.set("pageUrl", getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest() .getServerPort()  + "/pdf/" + name + ".pdf");
            result.set("title", "送货单");
            jhm.put("data", result);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.getMessage());
        }
        renderJson(jhm);
    }

    public void printOutgoingGoodsOrder() throws UnsupportedEncodingException {
        JsonHashMap jhm = new JsonHashMap();
        String orderId = getPara("id");
        Record dataRecord = Db.findFirst("SELECT so.out_time date, w.name warehouse_name, s.name store_name, so.order_number order_num, so.status sostatus FROM warehouse_out_order so, store s, warehouse w WHERE so.store_id = s.id and so.warehouse_id=w.id and so.id=?", orderId);
        if(dataRecord == null){
            jhm.putCode(-1).putMessage("订单号有错误，请确认订单！");
            renderJson(jhm);
            return;
        }
        Map<String, String> onePageData = new HashMap<>();
        for(String s : outgoing_goods_one_page_arr){
            onePageData.put(s, dataRecord.getStr(s));
        }
        File onePageTemp = new File(this.getRequest().getSession().getServletContext().getRealPath("") + "/template/outgoingGoodsOnePage.template");
        BufferedReader br = null;
        String onePageTempStr = "";
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(onePageTemp), "utf-8"));
            String str = br.readLine();
            while(str != null){
                onePageTempStr += str;
                str = br.readLine();
            }
            for(String s : outgoing_goods_one_page_arr){
                onePageTempStr = onePageTempStr.replace("${" + s + "}", onePageData.get(s));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PDFUtil pdfUtil = new PDFUtil(this.getRequest(), "template");
        Map<String,Object> data = new HashMap();
        String table = "";
        String tableStr = "";
//        String title = "<table class=\"order-list\"><thead style=\"display:table-header-group\"><tr><td colspan=\"5\"><table class=\"order-top\"><tr><th colspan=\"2\" align=\"center\">出库单</th></tr><tr><td width=\"60%\">出库单号：${order_num}</td><td>仓库：${warehouse_name}</td></tr><tr><td width=\"60%\">出库日期：${date}</td><td>门店：${store_name}</td></tr></table></td></tr></thead></table>";
//        for(String s : outgoing_goods_one_page_arr){
//            title = title.replace("${" + s + "}", onePageData.get(s));
//        }
        String warehouse_out_order_sql = "SELECT " +
                " so.order_number order_number, " +
                " som.*, " +
                " ( " +
                "  SELECT " +
                "   NAME " +
                "  FROM " +
                "   goods_attribute " +
                "  WHERE " +
                "   id = som.attribute_1 " +
                " ) ganame " +
                "FROM " +
                " warehouse_out_order_material_detail som, " +
                " warehouse_out_order so " +
                "WHERE " +
                " so.id = som.warehouse_out_order_id " +
                "AND warehouse_out_order_id =?";
        List<Record> dataList = Db.find(warehouse_out_order_sql, orderId);
        if(dataList != null && dataList.size() > 0){
            dataRecord.set("order_number", dataList.get(0).get("order_number"));
            int i = 1;
            for(; i < dataList.size(); i++){
                Record r = dataList.get(i);
                tableStr += "<tr><td>" + r.get("code") + "</td><td>" + r.get("name") + "</td><td>" + r.get("box_attr") + "</td><td>" + r.get("out_unit") + "</td><td>" + r.get("box_attr_num") + "</td></tr>";
                if(i % 31 == 0){
                    table += onePageTempStr.replace("${table}", tableStr);
                    table += "<div class='pageNext'></div>";
                    tableStr = "";
                }
            }
            if(tableStr != null && tableStr.length() > 0){
                table += onePageTempStr.replace("${table}", tableStr);
            }
        }
        data.put("table", table);
        data.put("creater_name", new UserSessionUtil(getRequest()).getRealName());
        String content = pdfUtil.loadDataByTemplate(data, "outgoingGoodsTemplate.html");
        try {
            String name = UUIDTool.getUUID();
            pdfUtil.createPdf(content, this.getRequest().getSession().getServletContext().getRealPath("") + "/pdf/" + name + ".pdf");
            createPrintDetails(orderId, dataRecord, "outgoing_goods");
//            this.getResponse().sendRedirect(getRequest().getContextPath()  + "/pdf/" + name + ".pdf");
//            this.getRequest().getRequestDispatcher(getRequest().getContextPath()  + "/pdf/a.pdf");
            Record result = new Record();
            result.set("pageUrl", getRequest().getScheme()+"://"+getRequest().getServerName()+":"+getRequest() .getServerPort()  + "/pdf/" + name + ".pdf");
            result.set("title", "出库单");
            jhm.put("data", result);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putMessage(e.getMessage()).putCode(-1);
        }
        renderJson(jhm);
    }

    public void getPrintDetail(){
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        String orderId = getPara("orderNumber");
        String type = getPara("type");
//        String date = getPara("date");
        String sql = " from print_details where 1=1 ";
        List<Object> params = new ArrayList<>();
        if(orderId != null && orderId.length() > 0){
            sql += " and order_number=? ";
            params.add(orderId);
        }
        if(type != null && type.length() > 0){
            sql += " and type=? ";
            params.add(type);
        }
//        if(date != null && date.length() > 0){
//            sql += " and print_date=? ";
//            params.add(date);
//        }
        sql += " order by sort desc";
        Page<Record> orderList = Db.paginate(pageNum, pageSize, "select * ", sql, params.toArray());
        int printTime = 0;
        if(orderList != null && orderList.getList().size() > 0){
            printTime = orderList.getList().get(0).getInt("sort");
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", orderList).put("printTime", printTime);
        renderJson(jhm);
    }

    private void createPrintDetails(String orderId, Record dataRecord, String type){
        List<Record> printDetailsList = Db.find("select * from print_details where order_id=? order by sort", orderId);
        int sort = 1;
        if(printDetailsList != null && printDetailsList.size() > 0){
            sort += printDetailsList.get(printDetailsList.size() - 1).getInt("sort");
        }
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        Record printDetails = new Record();
        printDetails.set("id", UUIDTool.getUUID());
        printDetails.set("order_id", orderId);
        printDetails.set("print_date", DateTool.GetDateTime());
        printDetails.set("sort", sort);
        printDetails.set("creater_id", usu.getUserId());
        printDetails.set("creater_name", usu.getRealName());
        printDetails.set("type", type);
        printDetails.set("status", dataRecord.getStr("sostatus"));
        printDetails.set("order_number", dataRecord.getStr("order_number"));
        Db.save("print_details", printDetails);
    }

}
