package com.store.print;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.utils.PDFUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.io.*;
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
        List<Record> dataList = Db.find("select som.*, gu.name uname, (select name from goods_attribute where id=som.attribute_1) ganame from store_order_material som, goods_unit gu where som.unit=gu.id and store_order_id=?", orderId);
        String title = "<table class=\"order-list\"><thead style=\"display:table-header-group\"><tr><td colspan=\"5\"><table class=\"order-top\"><tr><th colspan=\"2\" align=\"center\">送货单</th></tr><tr><td width=\"60%\">送货单位：${send_company}</td><td>送货日期：${send_date}</td></tr><tr><td width=\"60%\">地址：${send_address}</td><td>餐厅名称：${store_name}</td></tr><tr><td width=\"60%\">客服电话：${phone}</td><td>单据号：${order_num}</td></tr></table></td></tr></thead></table>";
        for(String s : send_goods_one_page_arr){
            title = title.replace("${" + s + "}", onePageData.get(s));
        }
        System.out.println(title);
        if(dataList != null && dataList.size() > 0){
            int i = 1;
            for(; i < dataList.size(); i++){
                Record r = dataList.get(i);
                tableStr += "<tr><td>" + r.get("code") + "</td><td>" + r.get("ganame") + "</td><td>" + r.get("uname") + "</td><td>" + r.get("name") + "</td><td>" + r.get("send_num") + "</td></tr>";
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
        data.put("creater_name", "新曙光");
        String content = pdfUtil.loadDataByTemplate(data, "sendGoodsTemplate.html");
        try {
            String name = UUIDTool.getUUID();
            pdfUtil.createPdf(content, this.getRequest().getSession().getServletContext().getRealPath("") + "/pdf/" + name + ".pdf");
//            this.getResponse().sendRedirect(getRequest().getContextPath()  + "/pdf/a.pdf");
            createPrintDetails(orderId, dataRecord);
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
        String orderId = getPara("orderId");
        if(orderId == null || orderId.length() < 1){
            orderId = "01996bcb125a4987853d15103c781745";
        }
        Record dataRecord = Db.findFirst("SELECT so.arrive_date send_date, s.address send_address, s.name store_name, so.order_number order_num FROM store_order so, store s WHERE so.store_id=s.id and so.id=?", orderId);
        if(dataRecord == null){
            jhm.putCode(-1).putMessage("订单号有错误，请确认订单！");
            renderJson(jhm);
            return;
        }
        dataRecord.set("send_company", configMap.get("send_company"));
        dataRecord.set("phone", configMap.get("phone") + " 传真：" + configMap.get("fax"));
        Map<String, String> onePageData = new HashMap<>();
        for(String s : outgoing_goods_one_page_arr){
            onePageData.put(s, s);
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
        List<Record> dataList = Db.find("select som.*, gu.name uname, (select name from goods_attribute where id=som.attribute_1) ganame from store_order_material som, goods_unit gu where som.unit=gu.id and store_order_id=?", orderId);
        if(dataList != null && dataList.size() > 0){
            int i = 1;
            for(; i < dataList.size(); i++){
                Record r = dataList.get(i);
                tableStr += "<tr><td>" + r.get("code") + "</td><td>" + r.get("ganame") + "</td><td>" + r.get("uname") + "</td><td>" + r.get("name") + "</td><td>" + r.get("send_num") + "</td></tr>";
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
        data.put("creater_name", "新曙光");
        String content = pdfUtil.loadDataByTemplate(data, "outgoingGoodsTemplate.html");
        try {
            String name = UUIDTool.getUUID();
            pdfUtil.createPdf(content, this.getRequest().getSession().getServletContext().getRealPath("") + "/pdf/" + name + ".pdf");
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
        String orderId = getPara("id");
        List<Record> printDetailsList = Db.find("select * from print_details where order_id=? order by sort desc", orderId);
        int printTime = 0;
        if(printDetailsList != null && printDetailsList.size() > 0){
            printTime = printDetailsList.get(0).getInt("sort");
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("printDetail", printDetailsList).put("printTime", printTime);
        renderJson(jhm);
    }

    private void createPrintDetails(String orderId, Record dataRecord){
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
        printDetails.set("status", dataRecord.getStr("sostatus"));
        Db.save("print_details", printDetails);
    }

}
