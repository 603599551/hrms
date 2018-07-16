package paiban.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.RequestTool;
import easy.util.DateTool;
import easy.util.JsonHashMap;
import easy.util.UUIDTool;

import java.util.Map;

/**
 * 门店预估金额Ctrl
 * date
 */
public class StoreForecastTurnoverCtrl extends BaseCtrl{

    public void add(){
//        Map<String, Object> params = this.getParaMaps();
//        Record r = new Record();
//        r.set("id", UUIDTool.getUUID());
//        r.set("store_id", "");
//        r.set("create_time", DateTool.GetDateTime());
//        r.set("creater_id", "1");
//        r.set("total_money", params.get("total_money"));
//        r.set("time_money", JSONObject.toJSONString(params));
//        Db.save("h_store_forecast_turnover", r);
        JSONObject json = RequestTool.getJson(getRequest());
        System.out.println(json);
        try {
            JSONArray arr = JSONArray.parseArray(this.getRequestObject());
            System.out.println(arr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("message", "录入成功！");
        renderJson(jhm
        );
    }

    public void getForecastTurnover(){
        renderJson("{\"code\":1,\"data\":[[{\"input\":\"12\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"200\"},{\"input\":\"300\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"500\"},{\"input\":\"\"},{\"input\":\"1100\"},{\"input\":\"\"},{\"input\":\"1800\"},{\"input\":\"\"},{\"input\":\"5000\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}]]}");
    }

}
