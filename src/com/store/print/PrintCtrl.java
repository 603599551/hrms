package com.store.print;

import com.ss.controllers.BaseCtrl;
import com.utils.PDFUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PrintCtrl extends BaseCtrl {

    private static final String[] send_goods_one_page_arr = {"send_company","send_date","send_address","store_name","phone","order_num"};

    public void printSendGoodsOrder() throws UnsupportedEncodingException {
        System.out.println(this.getRequest().getSession().getServletContext().getRealPath(""));
        Map<String, String> onePageData = new HashMap<>();
        for(String s : send_goods_one_page_arr){
            onePageData.put(s, s);
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
        for(int i = 1; i < 110; i++){
            tableStr += "<tr><td>小明" + i + "</td><td>" + i + "</td><td>" + i + "</td><td>" + i + "</td><td>" + i + "</td></tr>";
            if(i % 31 == 0){
                table += onePageTempStr.replace("${table}", tableStr);
                table += "<div class='pageNext'></div>";
                tableStr = "";
            }
        }
        if(tableStr != null && tableStr.length() > 0){
            table += onePageTempStr.replace("${table}", tableStr);
        }
        data.put("table", table);
        data.put("creater_name", "新曙光");
        String content = pdfUtil.loadDataByTemplate(data, "sendGoodsTemplate.html");
        try {
            pdfUtil.createPdf(content, this.getRequest().getSession().getServletContext().getRealPath("") + "/pdf/a.pdf");
            this.getResponse().sendRedirect(getRequest().getContextPath()  + "/pdf/a.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
