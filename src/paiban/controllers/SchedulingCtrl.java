package paiban.controllers;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import paiban.service.SchedulingService;
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

    private SchedulingService service = enhance(SchedulingService.class);

    public void test(){
        service.paiban("234k5jl234j5lkj24l35j423l5j", "2018-08-06", new UserSessionUtil(getRequest()));
        //renderJson("{\"code\":1,\"data\":[{\"id\":\"1\",\"name\":\"张三\",\"color\":\"#FF5722\",\"work\":[{\"pos\":[0,1],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,0],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"2\",\"name\":\"李四\",\"color\":\"#448AFF\",\"work\":[{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,5],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,5],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#AFB42B\",\"work\":[{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,1],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[2,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,2],\"kind\":\"岗位\",\"salary\":7}]}],\"problemData\":[{\"pos\": [1, 2], \"maxNum\": 6},{\"pos\": [2, 2], \"maxNum\": 4},{\"pos\": [3,5], \"maxNum\": 5}]}");
    }

    public void save(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            JSONObject object = JSONObject.parseObject(this.getRequestObject());
//             service.update(object, new UserSessionUtil(getRequest()));
            jhm.putMessage("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putMessage("保存失败！");
        }
        renderJson(jhm);
    }

    public void getThead(){
        renderJson("{\"code\":1,\"data\":[{\"name\":\"前　　厅\",\"key\":0,\"children\":[{\"name\":\"服务员\",\"key\":0},{\"name\":\"传菜员\",\"key\":1},{\"name\":\"带位员\",\"key\":2},{\"name\":\"清理员\",\"key\":3},{\"name\":\"输单员\",\"key\":4},{\"name\":\"收银员\",\"key\":5}]},{\"name\":\"菜　　房\",\"key\":1,\"children\":[{\"name\":\"备料(熏酱)\",\"key\":6},{\"name\":\"备料(凉拌)\",\"key\":7},{\"name\":\"拌菜(熏酱)\",\"key\":8},{\"name\":\"拌菜(凉拌)\",\"key\":9}]},{\"name\":\"面　　房\",\"key\":2,\"children\":[{\"name\":\"煮面\",\"key\":10},{\"name\":\"摆面\",\"key\":11},{\"name\":\"备料\",\"key\":12},{\"name\":\"炒面/炸酱\",\"key\":13}]},{\"name\":\"\",\"key\":3,\"children\":[{\"name\":\"酒水/饮料\",\"key\":14}]}]}");
    }

    public void getStaff(){
        String x = getPara("x");
        int y = NumberUtils.parseInt(getPara("y"), 0);
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String sql = "select * from h_staff where dept_id=? and kind like ?";
        List<Record> staffList = Db.find(sql, usu.getUserBean().get("store_id"), "%" + posts[y] + "%");
        List<Record> data = new ArrayList<>();
        if(staffList != null && staffList.size() > 0){
            for(Record r : staffList){
                Record record = new Record();
                record.set("id", r.get("id"));
                record.set("name", r.get("name"));
                record.set("salary", r.get("hour_wage"));
                record.set("kind", r.get("kind"));
                data.add(record);
            }
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", data);
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":[{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#f60\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"4\",\"name\":\"大明\",\"color\":\"#060\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"5\",\"name\":\"明明\",\"color\":\"#f00\",\"salary\":9,\"kind\":\"岗位\"},{\"id\":\"6\",\"name\":\"小明明\",\"color\":\"#f6f\",\"salary\":9,\"kind\":\"岗位\"}]}");
    }

    public void createSchedulingTable() {
        JsonHashMap jhm = new JsonHashMap();
        String[] date = getParaValues("date");
        int day = NumberUtils.parseInt(getPara("day"), 0);
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String store_id = (String) usu.getUserBean().get("store_id");
        JSONArray data = service.getPaiban(store_id, nextDay(date[0], day));
        jhm.put("data", data);
        String sql = "select * from h_paiban_problem where store_id=? and date=?";
        List<Record> problemList = Db.find(sql, store_id, nextDay(date[0], day));
        if(problemList != null && problemList.size() > 0){
            JSONArray problemData = new JSONArray();
            for(Record r : problemList){
                problemData = JSONArray.parseArray(r.get("content"));
            }
            jhm.put("problemData", problemData);
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":[{\"id\":\"1\",\"name\":\"张三\",\"color\":\"#FF5722\",\"work\":[{\"pos\":[0,1],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,0],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"2\",\"name\":\"李四\",\"color\":\"#448AFF\",\"work\":[{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,5],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,5],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#AFB42B\",\"work\":[{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,1],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[2,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,2],\"kind\":\"岗位\",\"salary\":7}]}],\"problemData\":[{\"pos\": [1, 2], \"maxNum\": 6},{\"pos\": [2, 2], \"maxNum\": 4},{\"pos\": [3,5], \"maxNum\": 5}]}");
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
    public void createSchedulingTablesssss() {
        JsonHashMap jhm = new JsonHashMap();
        String[] date = getParaValues("date");
        String startDate = date[0];
        String endDate = date[1];
        int day = NumberUtils.parseInt(getPara("day"), 0);
        /**/
//        startDate = this.startDate;
//        endDate = this.endDate;
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        /**/
        /*
        String store_id =  usu.getBean().get("store_id");
         */

        String store_id = "234k5jl234j5lkj24l35j423l5j";

        List<Record> staffList = Db.find("select * from h_staff_paiban where date=? and store_id=?", day, store_id);
        if(staffList != null && staffList.size() > 0){

        }

        List<Record> storeForecastTurnoverList = Db.find("select * from h_store_forecast_turnover where scheduling_date BETWEEN ? and ? ORDER BY scheduling_date", startDate, endDate);
        List<Record> variableTimeGuideList = Db.find("select * from h_variable_time_guide where store_id=? order by h_money", store_id);
        List<Record> staffIdleTimeList = Db.find("select *, staff_id as name from h_staff_idle_time where store_id=? order by staff_id", store_id);
        if (staffIdleTimeList != null && staffIdleTimeList != null) {
            for (Record r : staffIdleTimeList) {
                JSONObject json = JSONObject.parseObject(r.getStr("content"));
                Iterator<Entry<String, Object>> it = json.entrySet().iterator();
                while (it.hasNext()) {
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

            List<Record> dataListResult = new ArrayList<>();
            List<Record> problemDataResult = new ArrayList<>();

            List<Record> saveList = new ArrayList<>();
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                Map<String, List<Record>> dataMap = getDataByDate(result, nextDay(startDate, dayIndex));
                List<Record> dataList = dataMap.get("dataList");

                if (dayIndex == day) {
                    dataListResult = dataList;
                    problemDataResult = dataMap.get("problemData");
                }
                String time = DateTool.GetDateTime();
                for (int i = 0; i < dataList.size(); i++) {
                    Record r = dataList.get(i);
                    Record record = new Record();
                    record.set("id", UUIDTool.getUUID());
                    record.set("staff_id", r.get("id"));
                    record.set("store_id", r.get("store_id"));
                    record.set("date", r.get("date"));
                    record.set("create_time", time);
                    record.set("modify_time", time);
                    record.set("creater_id", usu.getUserId());
                    record.set("modifier_id", usu.getUserId());
                    //String work = JSONObject.toJSONString(r.get("work"));
                    List<Record> recordList = r.get("work");
                    List<Map<String, Object>> workMapList = new ArrayList<>();
                    if(recordList != null && recordList.size() > 0){
                        for(Record rMap : recordList){
                            Map<String, Object> workMap = rMap.getColumns();
                            workMapList.add(workMap);
                        }
                    }
                    String work = net.sf.json.JSONArray.fromObject(workMapList).toString();
                    record.set("content", work);
                    saveList.add(record);
                }
            }


            if (saveList != null && saveList.size() > 0) {
                Db.batchSave("h_staff_paiban", saveList, saveList.size());
            }

            jhm.put("data", dataListResult);
            jhm.put("problemData", problemDataResult);
            //jhm.put("data", result.get(startDate));
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.getMessage());
        }

        renderJson(jhm);
        //renderJson("{\"code\":1,\"data\":[{\"id\":\"1\",\"name\":\"张三\",\"color\":\"#FF5722\",\"work\":[{\"pos\":[0,1],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,0],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"2\",\"name\":\"李四\",\"color\":\"#448AFF\",\"work\":[{\"pos\":[0,3],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[0,5],\"kind\":\"岗位\",\"salary\":6},{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[1,4],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[3,5],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,6],\"kind\":\"岗位\",\"salary\":7}]},{\"id\":\"3\",\"name\":\"王五\",\"color\":\"#AFB42B\",\"work\":[{\"pos\":[1,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[2,1],\"kind\":\"岗位\",\"salary\":4},{\"pos\":[2,2],\"kind\":\"岗位\",\"salary\":5},{\"pos\":[3,2],\"kind\":\"岗位\",\"salary\":7}]}],\"problemData\":[{\"pos\": [1, 2], \"maxNum\": 6},{\"pos\": [2, 2], \"maxNum\": 4},{\"pos\": [3,5], \"maxNum\": 5}]}");
    }

    private Map<String, List<Record>> getDataByDate(Map<String, Map<String, Map<String, Map<String, Object>>>> result, String date){
        Map<String, Record> dataMap = new HashMap<>();
        Map<String, Map<String, Map<String, Object>>> result_day = result.get(date);
        List<Record> problemData = new ArrayList<>();

        List<Record> colorList = Db.find("select * from h_store_color order by sort");
        int colorIndex = 0;

        for(int x = 0; x < 66; x++){
            Map<String, Map<String, Object>> result_day_time = result_day.get(x + "");
            if(result_day_time != null && result_day_time.size() > 0){
                for(int y = 0; y < posts.length; y++){
                    Map<String, Object> result_day_time_posts = result_day_time.get(posts[y]);
                    if(result_day_time_posts != null && result_day_time_posts.size() > 0){
                        Object obj = result_day_time_posts.get("empList");
                        if(obj != null){
                            List<Record> empList = (List<Record>) result_day_time_posts.get("empList");
                            String color = (String) result_day_time_posts.get("color");
                            int empNum = (int) result_day_time_posts.get("empNum");
                            if(empNum > 0){
                                Record pr = new Record();
                                pr.set("maxNum", empNum);
                                int[] posArr = {x, y};
                                pr.set("pos", posArr);
                                problemData.add(pr);
                            }
                            if(empList != null && empList.size() > 0){
                                for(Record r : empList){
                                    Record dataRecord = dataMap.get(r.getStr("id"));
                                    if(dataRecord == null){
                                        dataRecord = new Record();
                                        dataMap.put(r.getStr("id"), dataRecord);
                                        dataRecord.set("id", r.getStr("id"));
                                        dataRecord.set("name", r.getStr("name"));
                                        dataRecord.set("color", colorList.get(colorIndex++).get("color"));
                                        dataRecord.set("work", new ArrayList<Record>());
                                        dataRecord.set("store_id", r.get("store_id"));
                                        dataRecord.set("date", date);
                                    }
                                    List<Record> works = dataRecord.get("work");
                                    Record work = new Record();
                                    int[] pos = {x, y};
                                    work.set("pos", pos);
                                    work.set("kind", posts[y]);
                                    work.set("salary", r.get("salary"));
                                    works.add(work);
                                }
                            }
                        }
                    }
                }
            }
        }

        List<Record> dataList = new ArrayList<>();
        Iterator<Entry<String, Record>> dataMapIt = dataMap.entrySet().iterator();
        while(dataMapIt.hasNext()){
            Entry<String, Record> entry = dataMapIt.next();
            dataList.add(entry.getValue());
        }
        Map<String, List<Record>> resultMap = new HashMap<>();
        resultMap.put("dataList", dataList);
        resultMap.put("problemData", problemData);
        return resultMap;
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
                                //System.out.println();
                            }
                            List<Record> staffList = staffTimeKindMap.get(day).get(time).get(kind);
                            List<Record> staffAllowedList = new ArrayList<>();
                            for(int i = 0; i < empNum && i < staffList.size(); i++){
                                Record staff = staffList.get(i);
                                if(staffTimeIsAllowed(time, staff, oneEmpWorkTime)){
//                                if(staffTimeIsAllowedExclude(time, staff)){
                                    Record s = new Record();
                                    s.set("name", staff.get("name"));
                                    s.set("id", staff.get("id"));
                                    s.set("color", "#FF5722");
                                    s.set("salary", 15);
                                    s.set("store_id", staff.get("store_id"));
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
//                    if(new Integer(r.getStr("date")) != i + 1){
//                        continue;
//                    }
                    if(!date.equals(r.getStr("date"))){
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

}
