package paiban.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.UUIDTool;
import paiban.service.VariableTimeGuideService;
import utils.ContentTransformationUtil;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VariableTimeGuideCtrl extends BaseCtrl {

    public static final String[] POSRS = {"", "", "waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    //public static final Integer[][] TURNOVER = new Integer[9][2];
//    static{
//        int h_start = 499;
//        int l_start = 0;
//        for(int i = 0; i < TURNOVER.length; i++){
//            Integer[] arr = TURNOVER[i];
//            arr[0] = l_start + i * 500;
//            arr[1] = h_start + i * 500;
//        }
//        TURNOVER[8][1] = 999999;
//    }

    private VariableTimeGuideService service = enhance(VariableTimeGuideService.class);

    public void add(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            UserSessionUtil usu = new UserSessionUtil(getRequest());
            JSONArray paramsArray = JSONArray.parseArray(this.getRequestObject());
            System.out.println(paramsArray);
            List<Record> saveList = new ArrayList<>();
            for(int i = 0; i < paramsArray.size(); i++){
                JSONArray params = paramsArray.getJSONArray(i);
                String[] moneyArr = params.getJSONObject(0).getString("input").split("-");
                String[] numberArr = params.getJSONObject(1).getString("input").split("-");
                Record record = new Record();
                record.set("id", UUIDTool.getUUID());
                record.set("store_id", usu.getUserBean().get("store_id"));
                record.set("l_money", new Integer(moneyArr[0]));
                record.set("h_money", new Integer(moneyArr[1]));
                record.set("l_number", new Integer(numberArr[0]));
                record.set("h_number", new Integer(numberArr[1]));
                Map<String, Integer> work = new HashMap<>();
                record.set("content", params.toString());
                for(int j = 2; j < params.size(); j++){
                    JSONObject obj = params.getJSONObject(j);
                    String input = obj.getString("input");
                    if(input != null && input.trim().length() > 0){
                        String[] inputArr = input.split("-");
                        int fir_num = new Integer(inputArr[0].trim());
                        int sed_num = new Integer(inputArr[inputArr.length - 1].trim());
                        work.put(POSRS[j], fir_num > sed_num ? fir_num : sed_num);
                    }else{
                        work.put(POSRS[j], 0);
                    }
                }
                String work_num = JSONObject.toJSONString(work);
                record.set("work_num", work_num);
                saveList.add(record);
            }
            service.add(saveList, usu.getUserBean().get("store_id").toString());
            jhm.putMessage("设置成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(0).putMessage("设置失败，请检查填入数据是否有非法符号！");
        }
        renderJson(jhm);
    }

    public void getVariableTimeGuide(){
        JsonHashMap jhm = new JsonHashMap();
        try{
            UserSessionUtil usu = new UserSessionUtil(getRequest());
            String select = "select * from h_variable_time_guide where store_id=? order by l_money";
            List<Record> vtgList = Db.find(select, usu.getUserBean().get("store_id"));
            List<Object> data = new ArrayList<>();
            if(vtgList != null && vtgList.size() > 0){
                for(Record r : vtgList){
                    data.add(JSONArray.parseArray(r.get("content")));
                }
            }
            jhm.put("data", data);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(0).putMessage("数据读取失败！");
        }
        //renderJson("{\"code\":1,\"data\":[[{\"input\":\"0-500\"},{\"input\":\"0-26\"},{\"input\":\"1-3\"},{\"input\":\"3\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"0-500\"},{\"input\":\"0-26\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}],[{\"input\":\"0-500\"},{\"input\":\"0-26\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"},{\"input\":\"\"}]]}");
        renderJson(jhm);
    }

    public void test(){
        String storeId = "234k5jl234j5lkj24l35j423l5j";
        String date = "2018-08-12";
        String d = "2018-08-19";
        String sql = "select * from h_staff_idle_time where store_id=? and date=?";
        List<Record> updateList = Db.find(sql, storeId, date);
        List<Record> saveList = new ArrayList<>();
        if(updateList != null && updateList.size() > 0){
            for(Record r : updateList){
                Record record = new Record();
                record.setColumns(r);
                record.set("id", UUIDTool.getUUID());
                record.set("date", d);
                record.set("app_content", ContentTransformationUtil.Pc2AppContentEvery15M4Xianshi(record.getStr("content")));
                saveList.add(record);

                r.set("app_content", ContentTransformationUtil.Pc2AppContentEvery15M4Xianshi(r.getStr("content")));
            }
        }
        Db.batchSave("h_staff_idle_time", saveList, saveList.size());
        Db.batchUpdate("h_staff_idle_time", updateList, updateList.size());
    }

}
