package com.ss.stock.controllers;

import com.bean.UserBean;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.stock.controllers.services.DailySummaryService;
import com.utils.FileUtil;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DailySummaryCtrl extends BaseCtrl {

    public static final String[] impDailySummaryColumns = {"code","name","price","sale_num","sale_price","refund_num","refund_price","manager_meal_num","manager_meal_price"};

    /**
     * 判断是否已经导入过了
     */
    public void hasDailySummary(){
        UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
        String impTime = this.getPara("impTime");
        List<Record> list = Db.find("select * from daily_summary where store_id=? and imp_time=?", userBean.get("store_id"), impTime);
        JsonHashMap jhm = new JsonHashMap();
        if(list != null && list.size() > 0){
            jhm.putCode(0).putError(impTime + "数据已经导入，再次导入将覆盖原数据，是否继续？");
        }
    }

    /**
     * 导入文件接口
     */
    public void importDailySummary(){
        JsonHashMap jhm = new JsonHashMap();
        try {
//TODO 通过前台获取导入数据的所属时间
            //String imp_time = this.getPara("impTime");
            String imp_time = DateTool.GetDate();
            UserBean userBean = new UserSessionUtil(getRequest()).getUserBean();
//TODO 通过文件导入的方式导入数据，暂时写死文件路径，方便调试
            File xls = uploadFile();
            //File xls = uploadFile(this.getRequest());

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
        }catch (IOException e){
            e.printStackTrace();
            jhm.putCode(0).putMessage(e.getMessage());
        }
        renderJson(jhm);
    }

    private File uploadFile(){
        File result = new File("C:\\Users\\szsw\\Desktop\\now\\1.html");
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
        DailySummaryService dailySummaryService = DailySummaryService.getMe();
        JsonHashMap jhm = new JsonHashMap();
        try {
            Map<String, Object> map = dailySummaryService.securityStockBudget(impTime);
            jhm.put("data", map);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putError(e.getMessage());
        }
        renderJson(jhm);
    }

}
