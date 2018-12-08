package paiban.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import paiban.service.SchedulingService;
import paiban.service.StoreForecastTurnoverService;
import utils.bean.JsonHashMap;

import java.util.*;

/**
 * 门店预估金额Ctrl
 * date
 */
public class StoreForecastTurnoverCtrl extends BaseCtrl{

    private StoreForecastTurnoverService service = enhance(StoreForecastTurnoverService.class);

    public void add(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            UserSessionUtil usu = new UserSessionUtil(getRequest());
            String store_id = usu.getUserBean().get("store_id") + "";
            String time = DateTool.GetDateTime();
            String create_id = usu.getUserId();
            JSONObject json = JSONObject.parseObject(this.getRequestObject());
            String date = json.getString("date");
            JSONArray timeAndMoney = json.getJSONArray("list");
//            String year = sdf_year.format(new Date());
            String[] dayArr = new String[7];
            for(int i = 0; i < dayArr.length; i++){
//                dayArr[i] = nextDay(year + "-" + date, i);
                dayArr[i] = nextDay(date, i);
            }
            List<Map<String, String>> dataList = new ArrayList<>();
            //{"10":"200","11":"400","12":"1000","13":"1500","14":"1500","15":"500","16":"300","17":"500","18":"800","19":"800","20":"300","21":"200","22":"0","9":"200","total_money":"8200"}
            for(int i = 0; i < 7; i++){
                Map<String, String> map = new HashMap<>();
                dataList.add(map);
            }
            List<Record> saveList = new ArrayList<>();
            for(int j = 0; j < dataList.size(); j++){
                Map<String, String> map = dataList.get(j);
                int totalMoney = 0;
                for(int i = 0; i < timeAndMoney.size(); i++){
                    JSONArray timeDay = timeAndMoney.getJSONArray(i);
                    JSONObject obj = timeDay.getJSONObject(j);
                    String input = obj.getString("input");
                    if(input == null || input.trim().length() <= 0){
                        input = "0";
                    }
                    totalMoney += new Integer(input);
                    map.put(i + 9 + "", input);
                }
                map.put("total_money", totalMoney + "");
                Record r = new Record();
                r.set("id", UUIDTool.getUUID());
                r.set("store_id", store_id);
                r.set("create_time", time);
                r.set("creater_id", create_id);
                r.set("total_money", totalMoney);
                r.set("time_money", JSONObject.toJSONString(map));
                r.set("scheduling_date", dayArr[j]);
                saveList.add(r);
            }
            service.save(saveList, dayArr, store_id, usu);
            jhm.put("message", "录入成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).put("message", "录入失败，请检查数据格式！");
        }
        renderJson(jhm);
    }

    public void getForecastTurnover(){
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String store_id = usu.getUserBean().get("store_id") + "";
        String date = getPara("date");
//        date = "2018-08-06";
        String select = "select * from h_store_forecast_turnover where store_id=? and scheduling_date>=? and scheduling_date<=?";
        String dateEnd = nextDay(date, 6);
        List<Record> list = Db.find(select, store_id, date, dateEnd);
        if(list == null || list.size() == 0){
            String startDate = nextDay(date, -7);
            String endDate =nextDay(date, -1);
            list = Db.find(select, store_id, startDate, endDate);
        }
        List<List<Map<String, String>>> dataList = new ArrayList<>();
        JSONArray jsonArr = new JSONArray();
        if(list != null && list.size() > 0){
            for(Record r : list){
                JSONObject obj = JSONObject.parseObject(r.getStr("time_money"));
                jsonArr.add(obj);
            }
            for(int i = 0; i < 14; i++){
                List<Map<String, String>> oneList = new ArrayList<>();
                for(int day = 0; day < jsonArr.size(); day++){
                    JSONObject obj = jsonArr.getJSONObject(day);
                    Map<String, String> map = new HashMap<>();
                    map.put("input", obj.getString(9 + i + ""));
                    oneList.add(map);
                }
                dataList.add(oneList);
            }
            jhm.put("data", dataList);
        }else{
            jhm.putCode(2);
        }
//        renderJson("{\"code\":1,\"data\":[[{\"input\":\"12\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"200\"},{\"input\":\"300\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"500\"},{\"input\":\"\"},{\"input\":\"1100\"},{\"input\":\"\"},{\"input\":\"1800\"},{\"input\":\"\"},{\"input\":\"5000\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}]]}");
        renderJson(jhm);
    }

}
