package com.store.print;

import com.ss.controllers.BaseCtrl;
import com.utils.PDFUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class PrintCtrl extends BaseCtrl {

    public void test() throws UnsupportedEncodingException {
        System.out.println(this.getRequest().getSession().getServletContext().getRealPath(""));
        PDFUtil pdfUtil = new PDFUtil(this.getRequest(), "template");
        Map<String,Object> data = new HashMap();
        //data.put("name",new String("鲁家宁啥地方啥地方".getBytes("UTF-8"), "GBK"));
        data.put("name","鲁家宁啥地方啥地方");
        String table = "";
        for(int i = 1; i < 100; i++){
            table += "<tr><td>小明" + i + "</td><td>" + i + "</td></tr>";
        }
        data.put("table", table);
        String content = pdfUtil.loadDataByTemplate(data, "template.html");
        try {
            pdfUtil.createPdf(content, this.getRequest().getSession().getServletContext().getRealPath("") + "/pdf/a.pdf");
            this.getResponse().sendRedirect(getRequest().getContextPath()  + "/pdf/a.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
