package paiban.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import utils.bean.JsonHashMap;

import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

public class SchedulingCtrl extends BaseCtrl {

    /**
     * 测试数据
     */
    private static final String startDate = "2018-07-02";
    private static final String endDate = "2018-07-08";

    /**
     * 真实应用常量
     */
    private static final String[] timeArr = {"9","10","11","12","13","14","15","16","17","18","19","20","21","22"};
    private static final String[] time4OneHourArr = new String[66];
//    public static final String[] posts = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    public static final String[] posts = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    static {
        for(int i = 0; i < 66; i++){
            time4OneHourArr[i] = i + "";
        }
    }

    public void save(){
        try {
            JSONArray array = JSONArray.parseArray(this.getRequestObject());
            System.out.println(array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.putMessage("保存成功！");
        renderJson(jhm);
    }

    public void getThead(){
        renderJson("{\"code\":1,\"data\":[{\"name\":\"前　　厅\",\"key\":0,\"children\":[{\"name\":\"服务员\",\"key\":0},{\"name\":\"传菜员\",\"key\":1},{\"name\":\"带位员\",\"key\":2},{\"name\":\"清理员\",\"key\":3},{\"name\":\"输单员\",\"key\":4},{\"name\":\"收银员\",\"key\":5}]},{\"name\":\"菜　　房\",\"key\":1,\"children\":[{\"name\":\"备料(熏酱)\",\"key\":6},{\"name\":\"备料(凉拌)\",\"key\":7},{\"name\":\"拌菜(熏酱)\",\"key\":8},{\"name\":\"拌菜(凉拌)\",\"key\":9}]},{\"name\":\"面　　房\",\"key\":2,\"children\":[{\"name\":\"煮面\",\"key\":10},{\"name\":\"摆面\",\"key\":11},{\"name\":\"备料\",\"key\":12},{\"name\":\"炒面/炸酱\",\"key\":13}]},{\"name\":\"\",\"key\":3,\"children\":[{\"name\":\"酒水/饮料\",\"key\":14}]}]}");
    }

    public void getStaff(){
        String x = getPara("x");
        String y = getPara("y");
        System.out.println("x = " + x + "y = " + y);
        renderJson("{\"code\":1,\"data\":[{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#f60\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"4\",\"name\":\"大明\",\"color\":\"#060\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"5\",\"name\":\"明明\",\"color\":\"#f00\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"6\",\"name\":\"小明明\",\"color\":\"#f6f\",\"salary\":9,\"kind\":\"岗位\"}]}");
    }

    /**
     * 1、查出当天每个时段所需岗位的人数*
     * 2、查出每个员工的闲时和胜任岗位*
     * 3、根据总工时计算出每个员工的平均工时*
     * 4、将所有员工的工时全部排到排班表中
     * 5、根据每个时间段所需人数将多余员工从这个工时中移除
     *      贪心算法，尽可能的将一根员工的闲时全部占用，再排第二个员工的
     * 返回的Map
     *  Map<日期, Map<时间段, Map<岗位, Map<color:单元格颜色（红色代码人员不足，需要手动排班），emp:List<Record（name:人名）>>>>>>
     *
     */
    public void createSchedulingTable(){
        JsonHashMap jhm = new JsonHashMap();
        String startDate = getPara("startDate");
        String endDate = getPara("endDate");
        /**/
        startDate = this.startDate;
        endDate = this.endDate;
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        /**/
        /*
        String store_id =  usu.getBean().get("store_id");
         */
        /*
        String store_id = "234k5jl234j5lkj24l35j423l5j";
        List<Record> storeForecastTurnoverList = Db.find("select * from h_store_forecast_turnover where scheduling_date BETWEEN ? and ? ORDER BY scheduling_date", startDate, endDate);
        List<Record> variableTimeGuideList = Db.find("select * from h_variable_time_guide where store_id=? order by h_money", store_id);
        List<Record> staffIdleTimeList = Db.find("select *, staff_id as name from h_staff_idle_time where store_id=? order by staff_id", store_id);
        if(staffIdleTimeList != null && staffIdleTimeList != null){
            for(Record r : staffIdleTimeList){
                JSONObject json = JSONObject.parseObject(r.getStr("content"));
                Iterator<Entry<String, Object>> it = json.entrySet().iterator();
                while(it.hasNext()){
                    Entry<String, Object> entry = it.next();
                    r.set(entry.getKey(), entry.getValue());
                }
            }
        }
        try {
            Map<String, Map<String, Map<String, Integer>>> oneDayOneTimeOneKindEmpNum = getOneDayOneTimeOneKindEmpNum(storeForecastTurnoverList, variableTimeGuideList);
            int totalWorkTime = getTotalWorkTime(oneDayOneTimeOneKindEmpNum);
            int oneEmpWorkTime = totalWorkTime / staffIdleTimeList.size();
            Map<String, Map<String, Map<String, Map<String, Object>>>> result = createSchedulingTable(oneDayOneTimeOneKindEmpNum, staffIdleTimeList, oneEmpWorkTime, startDate);
            jhm.put("data", result.get(startDate));
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.getMessage());
        }
        */
//        renderJson(jhm);
        renderJson("{\"code\":1,\"data\":[{\"id\":\"1\",\"name\":\"张三\",\"color\":\"#FF5722\",\"work\":[{\"pos\":[0,1],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,0],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"2\",\"name\":\"李四\",\"color\":\"#448AFF\",\"work\":[{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,5],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,5],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#AFB42B\",\"work\":[{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,1],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[2,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,2],\"kind\":\"岗位\",\"salary\":7}]}],\"problemData\":[{\"pos\": [1, 2], \"maxNum\": 6},{\"pos\": [2, 2], \"maxNum\": 4},{\"pos\": [3,5], \"maxNum\": 5}]}");
    }

    /**
     * 返回的Map
     *  Map<日期, Map<时间段, Map<岗位, Map<color:单元格颜色（红色代码人员不足，需要手动排班），empList:List<Record（name:人名）>>>>>>
     * @param oneDayOneTimeOneKindEmpNum
     * @param staffIdleTimeList
     * @param oneEmpWorkTime
     * @param startDate
     */
    private Map<String, Map<String, Map<String, Map<String, Object>>>> createSchedulingTable(Map<String, Map<String, Map<String, Integer>>> oneDayOneTimeOneKindEmpNum, List<Record> staffIdleTimeList, int oneEmpWorkTime, String startDate){
        Map<String, Map<String, Map<String, List<Record>>>> staffTimeKindMap = getStaffTimeKind(staffIdleTimeList, startDate);
        Map<String, Map<String, Map<String, Map<String, Object>>>> result = new HashMap<>();
        Iterator<Entry<String, Map<String, Map<String, Integer>>>> oneDayOneTimeOneKindEmpNumIt = oneDayOneTimeOneKindEmpNum.entrySet().iterator();
        while(oneDayOneTimeOneKindEmpNumIt.hasNext()){
            Entry<String, Map<String, Map<String, Integer>>> oneDayOneTimeOneKindEmpNumEntry = oneDayOneTimeOneKindEmpNumIt.next();
            if(oneDayOneTimeOneKindEmpNumEntry.getValue() != null){
                String day = oneDayOneTimeOneKindEmpNumEntry.getKey();
                Map<String, Map<String, Map<String, Object>>> oneDayOneTimeOneKindEmpNumMap = new HashMap<>();
                result.put(oneDayOneTimeOneKindEmpNumEntry.getKey(), oneDayOneTimeOneKindEmpNumMap);
                Iterator<Entry<String, Map<String, Integer>>> oneTimeOneKindEmpNumIt = oneDayOneTimeOneKindEmpNumEntry.getValue().entrySet().iterator();
                while(oneTimeOneKindEmpNumIt.hasNext()){
                    Entry<String, Map<String, Integer>> oneTimeOneKindEmpNumEntry = oneTimeOneKindEmpNumIt.next();
                    if(oneTimeOneKindEmpNumEntry.getValue() != null){
                        String time = oneTimeOneKindEmpNumEntry.getKey();
                        Map<String, Map<String, Object>> oneTimeOneKindEmpNumMap = new HashMap<>();
                        oneDayOneTimeOneKindEmpNumMap.put(oneTimeOneKindEmpNumEntry.getKey(), oneTimeOneKindEmpNumMap);
                        Map<String, Integer> oneKindEmpNumMap = oneTimeOneKindEmpNumEntry.getValue();
                        for(String kind : posts){
                            Map<String, Object> empNumMap = new HashMap<>();
                            oneTimeOneKindEmpNumMap.put(kind, empNumMap);
                            int empNum = oneKindEmpNumMap.get(kind);
                            if("19".equals(time)){
                                System.out.println();
                            }
                            List<Record> staffList = staffTimeKindMap.get(day).get(time).get(kind);
                            List<Record> staffAllowedList = new ArrayList<>();
                            for(int i = 0; i < empNum && i < staffList.size(); i++){
                                Record staff = staffList.get(i);
                                if(staffTimeIsAllowed(time, staff, oneEmpWorkTime)){
//                                if(staffTimeIsAllowedExclude(time, staff)){
                                    Record s = new Record();
                                    s.set("name", staff.get("name"));
                                    staffAllowedList.add(s);
                                    staff.set(time, 2);
                                    staff.set("workTimes", staff.getInt("workTimes") != null ? staff.getInt("workTimes") + 1 : 1);
                                }
                                if(staffAllowedList.size() == empNum){
                                    break;
                                }
                            }
                            empNumMap.put("empList", staffAllowedList);
                            empNumMap.put("empNum", empNum);
                            if(staffAllowedList.size() == empNum){
                                empNumMap.put("color", "green");
                            }else{
                                for(int i = 0; i < empNum - staffList.size()&& i < staffList.size(); i++){
                                    Record staff = staffList.get(i);
                                    if(staffTimeIsAllowedExclude(time, staff)){
                                        Record s = new Record();
                                        s.set("name", staff.get("name"));
                                        staffAllowedList.add(s);
                                        staff.set(time, 2);
                                        staff.set("workTimes", staff.getInt("workTimes") != null ? staff.getInt("workTimes") + 1 : 1);
                                    }
                                    if(staffAllowedList.size() == empNum){
                                        break;
                                    }
                                }
                                if(staffAllowedList.size() == empNum){
                                    empNumMap.put("color", "green");
                                }else{
                                    empNumMap.put("color", "red");
                                    empNumMap.put("needNum", empNum - staffAllowedList.size());
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 员工当前时间段是否可以排班
     * @return
     */
    private boolean staffTimeIsAllowed(String time, Record staff, int oneEmpWorkTime){
        boolean result = false;
        int i = staff.getInt(time);
        if(1 == i){
            if(staff.get("workTimes") == null || oneEmpWorkTime >= staff.getInt("workTimes")){
                result =  true;
            }else{
                result = false;
            }
        }
//        result = true;
        return result;
    }

    /**
     * 员工当前时间段是否可以排班
     * @return
     */
    private boolean staffTimeIsAllowedExclude(String time, Record staff){
        boolean result = false;
        int i = staff.getInt(time);
        if(1 == i){
            result =  true;
        }
//        result = true;
        return result;
    }

    /**
     * Map<String, Map<String, Record>>
     *     时间段       岗位      员工
     * @param staffIdleTimeList
     */
    private Map<String, Map<String, Map<String, List<Record>>>> getStaffTimeKind(List<Record> staffIdleTimeList, String startDate){
        Map<String, Map<String, Map<String, List<Record>>>> result = new HashMap<>();
        for(int i = 0; i < 7; i++){
            String date = nextDay(startDate, i);
            Map<String, Map<String, List<Record>>> secMap = new HashMap<>();
            result.put(date, secMap);
            for(String s : time4OneHourArr){
                Map<String, List<Record>> map = new HashMap<>();
                secMap.put(s, map);
                List<Record> list = new ArrayList<>();
                for(Record r : staffIdleTimeList){
                    if(r.getInt("date") != i + 1){
                        continue;
                    }
                    if(1 == r.getInt(s)){
                        list.add(r);
                    }
                }
                for(String p : posts){
                    List<Record> resultList = new ArrayList<>();
                    for(Record r : list){
                        if(r.getStr("kind").indexOf(p) >= 0){
                            resultList.add(r);
                        }
                    }
                    map.put(p, resultList);
                }
            }
        }
        return result;
    }

    private int getTotalWorkTime(Map<String, Map<String, Map<String, Integer>>> oneDayOneTimeOneKindEmpNum){
        int result = 0;
        Iterator<Entry<String, Map<String, Map<String, Integer>>>> oneDayOneTimeOneKindEmpNumIt = oneDayOneTimeOneKindEmpNum.entrySet().iterator();
        while(oneDayOneTimeOneKindEmpNumIt.hasNext()){
            Map<String, Map<String, Integer>> oneTimeOneKindEmpNum = oneDayOneTimeOneKindEmpNumIt.next().getValue();
            Iterator<Entry<String, Map<String, Integer>>> oneTimeOneKindEmpNumIt = oneTimeOneKindEmpNum.entrySet().iterator();
            while(oneTimeOneKindEmpNumIt.hasNext()){
                Map<String, Integer> oneKindEmpNum = oneTimeOneKindEmpNumIt.next().getValue();
                if(oneKindEmpNum != null){
                    Iterator<Entry<String, Integer>> oneKindEmpNumIt = oneKindEmpNum.entrySet().iterator();
                    while(oneKindEmpNumIt.hasNext()){
                        result += oneKindEmpNumIt.next().getValue();
                    }
                }
            }
        }
        return result;
    }

    private Map<String, Map<String, Map<String, Integer>>> getOneDayOneTimeOneKindEmpNum(List<Record> storeForecastTurnoverList, List<Record> variableTimeGuideList) throws Exception{
        if(storeForecastTurnoverList == null || storeForecastTurnoverList.size() < 7){
            throw new Exception("没有录入本周每日预计销售金额！");
        }
        if(variableTimeGuideList == null || variableTimeGuideList.size() == 0){
            throw new Exception("没有初始化可变工时！");
        }

        /**
         * Map<日期, Map<时间段, Map<岗位, 人数>>>
         */
        Map<String, Map<String, Map<String, Integer>>> oneDayOneTimeOneKindEmpNumMap = new HashMap<>();
        for(Record r : storeForecastTurnoverList){
            Map<String, Map<String, Integer>> oneTimeOneKindEmpNumMap = oneDayOneTimeOneKindEmpNumMap.get(r.getStr("scheduling_date"));
            JSONObject timeMoneyJson = JSONObject.parseObject(r.getStr("time_money"));
            oneTimeOneKindEmpNumMap = moneyToOneTimeOneKindEmpNumMap(variableTimeGuideList, timeMoneyJson);
            oneDayOneTimeOneKindEmpNumMap.put(r.getStr("scheduling_date"), oneTimeOneKindEmpNumMap);
        }
        return oneDayOneTimeOneKindEmpNumMap;
    }

    private Map<String, Map<String, Integer>> moneyToOneTimeOneKindEmpNumMap(List<Record> variableTimeGuideList, JSONObject timeMoneyJson){
        //Map<时间段, Map<岗位, 人数>>
        Map<String, Map<String, Integer>> oneTimeOneKindEmpNumMap = new HashMap<>();
        int time4OneHourIndex = 0;
        oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], createh_variable_time_guide());
        oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], createh_variable_time_guide());
        for(int i = 0; i < timeArr.length; i++){
            double money = timeMoneyJson.getDouble(timeArr[i]);
            Record oneKindEmpNum = null;
            for(Record r : variableTimeGuideList){
                if(money <= r.getDouble("h_money")){
                    oneKindEmpNum = r;
                    break;
                }
            }
            Map<String, Integer> oneKindEmpNumMap = new HashMap<>();
            JSONObject json = JSONObject.parseObject(oneKindEmpNum.getStr("work_num"));
            for(String s : posts){
                oneKindEmpNumMap.put(s, json.getIntValue(s));
            }
            oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], oneKindEmpNumMap);
            oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], oneKindEmpNumMap);
            oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], oneKindEmpNumMap);
            oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex++], oneKindEmpNumMap);
        }
        if(time4OneHourIndex < time4OneHourArr.length){
            for(;time4OneHourIndex < time4OneHourArr.length; time4OneHourIndex++){
                oneTimeOneKindEmpNumMap.put(time4OneHourArr[time4OneHourIndex], createh_variable_time_guide());
            }
        }
        return oneTimeOneKindEmpNumMap;
    }
    public Map<String, Integer> createh_variable_time_guide(){
        int[] p = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Map<String, Integer> r = new HashMap<>();
        for(int j = 0; j < posts.length; j++){
            r.put(posts[j], p[j]);
        }
        return r;
    }

    private String nextDay(String date, int nextDay){
        String result = "";
        try {
            Date today = sdf_ymd.parse(date);
            today = new Date(today.getTime() + nextDay * ONE_DAY_TIME);
            result = sdf_ymd.format(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

}
