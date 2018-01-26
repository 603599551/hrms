package com.ss.stock.controllers;

import com.bean.UserBean;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.sun.prism.impl.Disposer;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

public class ImportXlsCtrl extends BaseCtrl {

    public static final String[] dailySummaryColumns = {"code","name","price","sale_num","sale_price","refund_num","refund_price","manager_meal_num","manager_meal_price"};

    public void importDailySummary(){
//TODO 通过文件导入的方式导入数据，暂时写死文件路径，方便调试
        try {
            UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
            File xls = uploadFile("", this.getRequest());
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
            Record record;
            for(Element tr : trs){
                Elements tds = tr.getElementsByTag("td");
                record = new Record();
                for(int i = 0; i < dailySummaryColumns.length; i++){
                    Element td = tds.get(i);
                    record.set(dailySummaryColumns[i], td.html());
                }
                String time= DateTool.GetDateTime();
                record.set("id", UUIDTool.getUUID());
                record.set("create_time", time);
                record.set("modify_time", time);
                record.set("creater_id", userBean.getId());
//TODO 通过前台获取导入数据的所属时间
                record.set("sale_time", DateTool.GetDate());
                Db.save("imp_daily_summary", record);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private File uploadFile(String dir, HttpServletRequest request){
        File result = new File("C:\\Users\\szsw\\Desktop\\now\\1.html");
        return result;
    }

}
