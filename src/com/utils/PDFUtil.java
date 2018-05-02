package com.utils;

import com.itextpdf.text.pdf.BaseFont;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

public class PDFUtil {

    private static String font_black;
    private Configuration freemarkerCfg = null;
    private String templateBasePath;

    public PDFUtil(HttpServletRequest request, String relativePath){
        this.templateBasePath = request.getSession().getServletContext().getRealPath("") + "/" + relativePath;
        //this.templateBasePath = "C:\\Users\\szsw\\Desktop\\dist";
        this.font_black = request.getSession().getServletContext().getRealPath("") + "/font/simhei.ttf";
        freemarkerCfg =new Configuration();
        //freemarker的模板目录
        try {
            freemarkerCfg.setDirectoryForTemplateLoading(new File(templateBasePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public PDFUtil(String templateBasePath){
        this.templateBasePath = templateBasePath;
        this.font_black = templateBasePath + "/font/simhei.ttf";
        freemarkerCfg =new Configuration();
        //freemarker的模板目录
        try {
            freemarkerCfg.setDirectoryForTemplateLoading(new File(templateBasePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadDataByTemplate(Map<String, Object> data, String templateName){
        Writer out = new StringWriter();
        try {
            // 获取模板,并设置编码方式
            Template template = freemarkerCfg.getTemplate(templateName);
            template.setEncoding("gbk");
            // 合并数据模型与模板
            template.process(data, out); //将合并后的数据和模板写入到流中，这里使用的字符流
            out.flush();
            return out.toString();
//            return new String(out.toString().getBytes("GBK"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void createPdf(String content,String dest) throws Exception {
        ITextRenderer render = new ITextRenderer();
        ITextFontResolver fontResolver = render.getFontResolver();
        fontResolver.addFont(font_black, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 解析html生成pdf
        render.setDocumentFromString(content);
        //解决图片相对路径的问题 图片现在没有加进来，时间紧迫后期需要再研究
        //render.getSharedContext().setBaseURL("file://C:/Users/szsw/Desktop/dist/");
//        render.getSharedContext();
        render.layout();
        render.createPDF(new FileOutputStream(dest));
    }
}
