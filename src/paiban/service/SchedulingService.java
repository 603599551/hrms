package paiban.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.service.BaseService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.ContentTransformationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulingService extends BaseService {

    private static final String[] timeArr = {"8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
    private static final String[] time4OneHourArr = new String[66];
    public static final String[] posts = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    public static final String[] posts_houchu = {"preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    public static final String[] posts_qianting = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier"};

    public static final Map<String, Integer> postsMap = new HashMap<>();
    static {
        for(int i = 0; i < 66; i++){
            time4OneHourArr[i] = i + "";
        }
        for(int i = 0; i < posts.length; i++){
            postsMap.put(posts[i], i);
        }
    }

    public JSONArray getPaiban(String storeId, String date){
        JSONArray result = new JSONArray();
        String sql = "select * from h_staff_paiban where store_id=? and date=?";
        List<Record> staffList = Db.find(sql, storeId, date);
        if(staffList != null && staffList.size() > 0){
            for(Record r : staffList){
                JSONObject jsonObject = JSONObject.parseObject(r.getStr("content"));
                if(jsonObject != null){
                    JSONArray work = jsonObject.getJSONArray("work");
                    if(work != null && work.size() > 0){
                        result.add(jsonObject);
                    }
                }
            }
        }
        return result;
    }

    public void paiban(String storeId, String beginDate, UserSessionUtil usu){
        String[] dateArr = new String[7];
        for(int i = 0; i < dateArr.length; i++){
            dateArr[i] = nextDay(beginDate, i);
        }
        String endDate = dateArr[6];
        //查询可变工时指南
        String kbgsznSql = "select * from h_variable_time_guide where store_id=? order by l_money";
        List<Record> kbgsznList = Db.find(kbgsznSql, storeId);
        //一周预计销售金额
        String yjxsjeSql = "select * from h_store_forecast_turnover where store_id=? and scheduling_date between ? and ? order by scheduling_date";
        List<Record> yjxsjeList = Db.find(yjxsjeSql, storeId, beginDate, endDate);
        Map<String, List<Record>> yjxsjeMap = yjxsjeToDateRecord(yjxsje(yjxsjeList));
        //员工闲时
        String ygxsSql = "select sit.*, s.name name, s.hour_wage salary from h_staff_idle_time sit, h_staff s where sit.staff_id=s.id and sit.store_id=? and sit.date between ? and ? order by date";
        List<Record> ygxsList = Db.find(ygxsSql, storeId, beginDate, endDate);
        Map<String, List<Record>> ygxsMap = ygxsToDateRecord(ygxsList);
        Map<String, Map<String, List<Record>>> ygxsDayTimeMap = getDayTimeYgxs(ygxsMap, dateArr);

        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap = createMap_date_time_kind(dateArr, yjxsjeMap, kbgsznList, ygxsMap, ygxsDayTimeMap);
        save(pbDateTimeStaffMap, dateArr, usu);
    }

    private void save(Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap, String[] dateArr, UserSessionUtil usu){
        List<Record> staffAllList = new ArrayList<>();
        StringBuffer ids = new StringBuffer("");
        for(String date : dateArr){
            Map<String, Map<String, List<Record>>> timeKindStaff = pbDateTimeStaffMap.get(date);
            for(String time : time4OneHourArr){
                Map<String, List<Record>> kindStaff = timeKindStaff.get(time);
                for(String kind : posts){
                    List<Record> staffList = kindStaff.get(kind);
                    if(staffList != null && staffList.size() > 0){
                        for(Record r : staffList){
                            List<Record> workPos = r.get("work_pos");
                            if(workPos == null){
                                workPos = new ArrayList<>();
                                r.set("work_pos", workPos);
                            }
                            Record record = new Record();
                            record.set("date", date);
                            record.set("time", time);
                            record.set("kind", kind);
                            workPos.add(record);
                            if(ids.indexOf(r.getStr("id")) < 0){
                                staffAllList.add(r);
                                ids.append(r.getStr("id"));
                            }
                        }
                    }
                }
            }
        }
        saveStaffPaiban(staffAllList, dateArr, usu);
    }

    private void saveStaffPaiban(List<Record> staffAllList, String[] dateArr, UserSessionUtil usu){
        String userId = usu.getUserId();
        String saveTime = DateTool.GetDateTime();
        if(staffAllList != null && staffAllList.size() > 0){
            for(Record staff : staffAllList){
                List<Record> workPos = staff.get("work_pos");
                if(workPos != null && workPos.size() > 0){
                    Map<String, List<Record>> map = new HashMap<>();
                    staff.set("work_pos_map", map);
                    for(Record r : workPos){
                        List<Record> posList = map.get(r.getStr("date"));
                        if(posList == null){
                            posList = new ArrayList<>();
                            map.put(r.getStr("date"), posList);
                        }
                        posList.add(r);
                    }
                }
            }
            List<Record> saveList = new ArrayList<>();
            List<Record> colorList = Db.find("select color from h_store_color order by sort");
            int colorIndex = 0;
            for(Record staff : staffAllList){
                Map<String, List<Record>> map = staff.get("work_pos_map");
                for(String date : dateArr){
                    List<Record> list = map.get(date);
                    List<Map<String, Object>> work = new ArrayList<>();
                    if(list != null && list.size() > 0){
                        for(Record r : list){
                            Map<String, Object> objMap = new HashMap<>();
                            int[] pos = {toInt(r.get("time")), postsMap.get(r.getStr("kind"))};
                            objMap.put("pos", pos);
                            objMap.put("kind", r.get("kind"));
                            objMap.put("salary", (staff.getDouble("salary") / 4) + "");
                            work.add(objMap);
                        }
                        Map<String, Object> staffJson = new HashMap<>();
                        staffJson.put("id", staff.get("staff_id"));
                        staffJson.put("name", staff.get("name"));
                        staffJson.put("color", colorList.get(colorIndex++ % colorList.size()).get("color"));
                        staffJson.put("work", work);
                        Record staffSave = new Record();
                        staffSave.set("id", UUIDTool.getUUID());
                        staffSave.set("staff_id", staff.get("staff_id"));
                        staffSave.set("store_id", staff.get("store_id"));
                        staffSave.set("date", date);
                        String json = JSONObject.toJSONString(staffJson);
                        staffSave.set("content", json);
                        staffSave.set("app_content", ContentTransformationUtil.Pc2AppContentEvery15M4Paiban(json));
                        staffSave.set("creater_id", userId);
                        staffSave.set("create_time", saveTime);
                        staffSave.set("modifier_id", userId);
                        staffSave.set("modify_time", saveTime);
                        saveList.add(staffSave);
                    }
                }
            }
            String deleteStaffPaiBanSql = "delete from h_staff_paiban where store_id=? and date between ? and ?";
            Db.delete(deleteStaffPaiBanSql, usu.getUserBean().get("store_id"), dateArr[0], dateArr[6]);
            Db.batchSave("h_staff_paiban", saveList, saveList.size());
            saveStaffClock(saveList, usu, dateArr);
            saveWorkTime(saveList, usu, dateArr);
        }
    }

    private void saveWorkTime(List<Record> staffPaibanList, UserSessionUtil usu, String[] dateArr){
        String userId = usu.getUserId();
        String time = DateTool.GetDateTime();
        if(staffPaibanList != null && staffPaibanList.size() > 0){
            List<Record> workTimeList = new ArrayList<>();
            List<Record> workTimeDetailList = new ArrayList<>();
            for(Record r : staffPaibanList){
                Record workTime = new Record();
                String workTimeId = UUIDTool.getUUID();
                workTime.set("id", workTimeId);
                workTime.set("staff_id", r.get("staff_id"));
                workTime.set("store_id", r.get("store_id"));
                workTime.set("date", r.get("date"));
                String pcContent = r.getStr("content");
                JSONObject pcContentObj = JSONObject.parseObject(pcContent);
                JSONArray workArr = pcContentObj.getJSONArray("work");
                workTime.set("number", workArr.size());
                workTime.set("real_number", 0);
                workTimeList.add(workTime);

                String timeArr = ContentTransformationUtil.PcToAppXianShi(ContentTransformationUtil.PcPaibanToPcXianShi(pcContent));
                JSONArray timeJsonArr = JSONArray.parseArray(timeArr);
                if(timeJsonArr != null && timeJsonArr.size() > 0){
                    for(int i = 0; i < timeJsonArr.size(); i++){
                        JSONObject timeObj = timeJsonArr.getJSONObject(i);
                        Record workTimeDetail = new Record();
                        workTimeDetail.set("id", UUIDTool.getUUID());
                        workTimeDetail.set("work_time_id", workTimeId);
                        workTimeDetail.set("staff_id", r.get("staff_id"));
                        workTimeDetail.set("store_id", r.get("store_id"));
                        workTimeDetail.set("date", r.get("date"));
                        workTimeDetail.set("start_time", timeObj.getString("start"));
                        workTimeDetail.set("end_time", timeObj.getString("end"));
                        workTimeDetail.set("status", 0);
                        workTimeDetail.set("creater_id", userId);
                        workTimeDetail.set("create_time", time);
                        workTimeDetail.set("modifier_id", userId);
                        workTimeDetail.set("modify_time", time);
                        workTimeDetailList.add(workTimeDetail);
                    }
                }
            }
            String delete_work_time = "delete from h_work_time where store_id=? and date>=? and date<=?";
            Db.delete(delete_work_time, usu.getUserBean().get("store_id"), dateArr[0], dateArr[dateArr.length - 1]);
            String delete_work_time_detail = "delete from h_work_time_detail where store_id=? and date>=? and date<=?";
            Db.delete(delete_work_time_detail, usu.getUserBean().get("store_id"), dateArr[0], dateArr[dateArr.length - 1]);
            if(workTimeDetailList != null && workTimeDetailList.size() > 0){
                for(Record r : workTimeDetailList){
                    String start_time = r.getStr("start_time");
                    String end_time = r.getStr("end_time");
                    start_time = start_time.substring(0, start_time.length() - 3);
                    end_time = end_time.substring(0, end_time.length() - 3);
                    r.set("start_time", start_time);
                    r.set("end_time", end_time);
                }
            }
            Db.batchSave("h_work_time", workTimeList, workTimeList.size());
            Db.batchSave("h_work_time_detail", workTimeDetailList, workTimeDetailList.size());
        }
    }

    private void saveStaffClock(List<Record> staffPaibanList, UserSessionUtil usu, String[] dateArr){
        if(staffPaibanList != null && staffPaibanList.size() > 0){
            String userId = usu.getUserId();
            String time = DateTool.GetDateTime();
            List<Record> saveList = new ArrayList<>();
            for(Record r : staffPaibanList){
                String appContent = r.getStr("app_content");
                String pcContent = r.getStr("content");
                if(appContent != null && appContent.trim().length() > 0){
                    JSONArray appContentArr = JSONArray.parseArray(ContentTransformationUtil.DispersedTime2ContinuousTime4String(appContent));
                    JSONObject pcContentObj = JSONObject.parseObject(pcContent);
                    String kind = pcContentObj.getJSONArray("work").getJSONObject(0).getString("kind");
                    String salary = pcContentObj.getJSONArray("work").getJSONObject(0).getString("salary");
                    for(int i = 0; i < appContentArr.size(); i++){
                        JSONObject obj = appContentArr.getJSONObject(i);
                        Record save = new Record();
                        save.set("id", UUIDTool.getUUID());
                        save.set("staff_id", r.get("staff_id"));
                        save.set("store_id", r.get("store_id"));
                        save.set("date", r.get("date"));
                        save.set("start_time", obj.getString("start"));
                        save.set("end_time", obj.getString("end"));
                        save.set("sign_in_time", "");
                        save.set("sign_back_time", "");
                        save.set("kind", kind);
                        save.set("slary", salary);
                        save.set("is_leave", 0);
                        save.set("creater_id", userId);
                        save.set("create_time", time);
                        save.set("modifier_id", userId);
                        save.set("modify_time", time);
                        saveList.add(save);
                    }
                }
            }
            String delete = "delete from h_staff_clock where store_id=? and date>=? and date<=?";
            Db.delete(delete, usu.getUserBean().get("store_id"), dateArr[0], dateArr[dateArr.length - 1]);
            if(saveList != null && saveList.size() > 0){
                for(Record r : saveList){
//                    String start_time = r.getStr("start_time");
//                    String end_time = r.getStr("end_time");
//                    start_time = start_time.substring(0, start_time.length() - 3);
//                    end_time = end_time.substring(0, end_time.length() - 3);
//                    r.set("start_time", start_time);
//                    r.set("end_time", end_time);PcToAppPaiban
                    r.set("status", "0");
                    r.set("is_late", "0");
                    r.set("is_leave_early", "0");
                    r.set("is_leave", "0");
                }
            }
            Db.batchSave("h_staff_clock", saveList, saveList.size());
        }
    }
/*
staff_clock里面 status,is_late,is_leave_early 应该有初始值是0
staff_paiban里面app_content存的是15分钟的时间段   没有秒
staff_idle_time里面app_content也是15分钟时间段  没有秒
 */
    /**
     * 排班成日期，时间段，岗位，员工集合的格式
     * @param dateArr
     * @param yjxsjeMap
     * @param kbgsznList
     * @param ygxsMap
     * @param ygxsDayTimeMap
     * @return
     */
    private Map<String, Map<String, Map<String, List<Record>>>> createMap_date_time_kind(String[] dateArr, Map<String, List<Record>> yjxsjeMap, List<Record> kbgsznList, Map<String, List<Record>> ygxsMap,Map<String, Map<String, List<Record>>> ygxsDayTimeMap){
        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap = new HashMap<>();
        //排班后人员不够的map
        List<Record> hxStaffList = new ArrayList<>();
        for(String date : dateArr){
            Map<String, Map<String, List<Record>>> timePostsStaffMap = new HashMap<>();
            pbDateTimeStaffMap.put(date, timePostsStaffMap);
            List<Record> yjxsjeList = yjxsjeMap.get(date);
            int houchuNum = 0;
            int qiantingNum = 0;
            for(Record yjxsje : yjxsjeList){
                Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
                JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
                for(String h : posts_houchu){
                    houchuNum += toInt(json.get(h));
                }
                for(String q : posts_qianting){
                    qiantingNum += toInt(json.get(q));
                }
            }
            int staffOneWeekNum = (houchuNum + qiantingNum) / ygxsMap.get(date).size();
            for(Record yjxsje : yjxsjeList){
                String time = yjxsje.getStr("time");
                Map<String, List<Record>> postStaffMap = new HashMap<>();
                timePostsStaffMap.put(yjxsje.getStr("time"), postStaffMap);
                Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
                JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
                List<Record> list = ygxsDayTimeMap.get(date).get(time);
                for(String post : posts){
                    List<Record> staffList = new ArrayList<>();
                    postStaffMap.put(post, staffList);
                    int num = toInt(json.getString(post));
                    for(int i = 0; i < list.size(); i++){
                        if(num == 0){
                            break;
                        }
                        Record r = list.get(i);
                        if(staffTimeIsAllowed(r, staffOneWeekNum, post, date, time)){
                            staffList.add(r);
                            num--;
                        }
                        if(i == list.size() - 1 && num > 0){
                            //"problemData\":[{\"pos\": [1, 2], \"maxNum\": 6},{\"pos\": [2, 2], \"maxNum\": 4},{\"pos\": [3,5], \"maxNum\": 5}]
                            Record hx = new Record();
                            hx.set("id", UUIDTool.getUUID());
                            hx.set("number", num);
                            hx.set("maxNum", toInt(json.getString(post)));
                            hx.set("date", date);
                            hx.set("time", time);
                            hx.set("kind", post);
                            hx.set("store_id", r.get("store_id"));
                            hxStaffList.add(hx);
                        }
                    }
                }
            }
        }
        if(hxStaffList != null && hxStaffList.size() > 0){
            List<Record> problemList = new ArrayList<>();
            Map<String, List<Map<String, Object>>> dateProblemMap = new HashMap<>();
            String store_id = "";
            for(Record r : hxStaffList){
                List<Map<String, Object>> problemData = dateProblemMap.get(r.getStr("date"));
                if(problemData == null){
                    problemData = new ArrayList<>();
                    dateProblemMap.put(r.getStr("date"), problemData);
                }
                store_id = r.getStr("store_id");
                Map<String, Object> content = new HashMap<>();
                content.put("maxNum", r.get("maxNum"));
                int x = 0;
                int y = 0;
                for(int i = 0; i < time4OneHourArr.length; i++){
                    if(time4OneHourArr[i].equals(r.getStr("time"))){
                        x = i;
                        break;
                    }
                }
                for(int i = 0; i < posts.length; i++){
                    if(posts[i].equals(r.getStr("kind"))){
                        y = i;
                        break;
                    }
                }
                int[] pos = {x, y};
                content.put("pos", pos);
                problemData.add(content);
            }
            for(String date : dateArr){
                Record problem = new Record();
                problem.set("id", UUIDTool.getUUID());
                problem.set("date", date);
                problem.set("store_id", store_id);
                problem.set("content", JSONArray.toJSONString(dateProblemMap.get(date)));
                problemList.add(problem);
            }
            String sql = "select * from h_paiban_problem where store_id=? and date>=? and date<=?";
            List<Record> proList = Db.find(sql, store_id, dateArr[0], dateArr[dateArr.length - 1]);
            if(proList != null && proList.size() > 0){
                String delete = "delete from h_paiban_problem where id in(";
                List<Object> paramsList = new ArrayList<>();
                for(Record r : proList){
                    delete += "?,";
                    paramsList.add(r.get("id"));
                }
                delete = delete.substring(0, delete.length() - 1) + ")";
                Db.delete(delete, paramsList.toArray());
            }
            Db.batchSave("h_paiban_problem", problemList, problemList.size());
        }
        return pbDateTimeStaffMap;
    }

    /**
     * 判断员工在date和time时间段内是否可以排班
     * @param ygxs
     * @param num
     * @param posts
     * @param date
     * @param time
     * @return
     */
    private boolean staffTimeIsAllowed(Record ygxs, int num, String posts, String date, String time){
        String key = date + " " + time + "isAllowed";
        if(ygxs.get(key) != null && !ygxs.getBoolean(key)){
            return false;
        }
        if(toInt(ygxs.getStr("paiban_times")) < num){
            if(ygxs.getStr("kind").indexOf(posts) >= 0){
                ygxs.set("paiban_times", toInt(ygxs.getStr("paiban_times")) + 1);
                ygxs.set(key, false);
                return true;
            }
        }
        return false;
    }

    /**
     * 整理员工闲时数据
     * 日期，时间段，员工集合
     * @param ygxsMap
     * @param dateArr
     * @return
     */
    private Map<String, Map<String, List<Record>>> getDayTimeYgxs(Map<String, List<Record>> ygxsMap, String[] dateArr){
        Map<String, Map<String, List<Record>>> result = new HashMap<>();
        for(String date : dateArr){
            Map<String, List<Record>> dateOneMap = new HashMap<>();
            result.put(date, dateOneMap);
            List<Record> ygxsList = ygxsMap.get(date);
            for(Record ygxs : ygxsList){
                JSONObject content = JSONObject.parseObject(ygxs.getStr("content"));
                for(String time : time4OneHourArr){
                    List<Record> timeOneList = dateOneMap.get(time);
                    if(timeOneList == null){
                        timeOneList = new ArrayList<>();
                        dateOneMap.put(time, timeOneList);
                    }
                    if(1 == content.getIntValue(time)){
                        timeOneList.add(ygxs);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取预计销售金额对应可变工时指南的员工配置
     * @param kbgsznList 可变工时指南集合
     * @param yjxsje 预计销售金额
     * @return 可变工时指南的员工配置
     */
    private Record getGSByKbgsznAndYjxsje(List<Record> kbgsznList, Record yjxsje){
        int money = toInt(yjxsje.get("money"));
        for(Record r : kbgsznList){
            int hMoney = toInt(r.get("h_money"));
            int lMoney = toInt(r.get("l_money"));
            if(money >= lMoney && money <= hMoney){
                return r;
            }
        }
        return null;
    }

    /**
     * 将员工闲时数据整理到map中
     * key：日期
     * value：List闲时集合
     * @param ygxsList
     * @return
     */
    private Map<String, List<Record>> ygxsToDateRecord(List<Record> ygxsList){
        Map<String, List<Record>> result = new HashMap<>();
        for(Record r : ygxsList){
            List<Record> list = result.get(r.getStr("date"));
            if(list == null){
                list = new ArrayList<>();
                result.put(r.getStr("date"), list);
            }
            list.add(r);
        }
        return result;
    }

    /**
     * 将预计销售金额整理到map中
     * key：日期
     * value：List预计销售金额集合
     * @param yjxsjeList
     * @return
     */
    private Map<String, List<Record>> yjxsjeToDateRecord(List<Record> yjxsjeList){
        Map<String, List<Record>> result = new HashMap<>();
        for(Record r : yjxsjeList){
            List<Record> list = result.get(r.getStr("scheduling_date"));
            if(list == null){
                list = new ArrayList<>();
                result.put(r.getStr("scheduling_date"), list);
            }
            list.add(r);
        }
        return result;
    }

    /**
     * 将预计销售金额数据修改成每十五分钟一段
     * @param yjxsjeList
     * @return
     */
    private List<Record> yjxsje(List<Record> yjxsjeList){
        List<Record> result = new ArrayList<>();
        for(Record r : yjxsjeList){
            JSONObject obj = JSONObject.parseObject(r.getStr("time_money"));
            int index = 0;
            for(String s : timeArr){
                int money = toInt(obj.getString(s));
                int length = 4;
                if("8".equals(s)){
                    length = 2;
                }
                for(int i = 0; i < length; i++){
                    Record record = new Record();
                    record.setColumns(r);
                    record.set("money", money);
                    record.set("time", index);
                    result.add(record);
                    index++;
                }
            }
        }
        return result;
    }



    /**
     * 1、删除 h_staff_paiban 表的历史数据
     * 2、h_staff_paiban表插入新数据
     * 3、删除 h_staff_clock 表历史数据
     * 4、插入数据
     * 5、删除 h_work_time 历史数据
     * 6、插入数据
     * 7、删除 h_work_time_detail 历史数据
     * 8、插入数据
     * 9、删除 h_paiban_problem 历史数据
     * 10、插入数据
     * @param object
     */
    public void update(JSONObject object, UserSessionUtil usu){
        String date = object.getString("date");
        String storeId = object.getString("dept");
        JSONArray workers = object.getJSONArray("workers");
//        Map<String, Integer> numberMap = new HashMap<>();
//
//        if(workers != null && workers.size() > 0){
//            for(int i = 0; i < workers.size(); i++){
//                JSONObject worker = workers.getJSONObject(i);
//                if(worker != null){
//                    JSONArray work = worker.getJSONArray("work");
//                    if(work != null && work.size() > 0){
//                        for(int index = 0; index < work.size(); index++){
//                            JSONObject obj = work.getJSONObject(index);
//                            String time = obj.getJSONArray("pos").getString(0) + "," + obj.getJSONArray("pos").getString(1);
//                            Integer number = numberMap.get(time);
//                            if(number == null){
//                                number = 0;
//                            }
//                            number ++;
//                            numberMap.put(time, number);
//                        }
//                    }
//                }
//            }
//        }
//        List<String> staffIds = new ArrayList<>();
//        System.out.println(date);
        List<Record> staffPaibanList = update_h_staff_paiban(object, usu);
        update_h_staff_clock(staffPaibanList, usu, date);
        update_h_work_time(staffPaibanList, usu, date);
    }

    private List<Record> update_h_staff_paiban(JSONObject object, UserSessionUtil usu){
        List<Record> saveList = new ArrayList<>();
        String date = object.getString("date");
        String store_id = object.getString("dept");
        JSONArray workers = object.getJSONArray("workers");
        String userId = usu.getUserId();
        String saveTime = DateTool.GetDateTime();
        String delete = "delete from h_staff_paiban where date=? and store_id=?";
        Db.delete(delete, date, store_id);
        if(workers != null && workers.size() > 0){
            for(int i = 0; i < workers.size(); i++){
                JSONObject worker = workers.getJSONObject(i);
                Record staff_paiban = new Record();
                staff_paiban.set("id", UUIDTool.getUUID());
                staff_paiban.set("staff_id", worker.get("id"));
                staff_paiban.set("store_id", store_id);
                staff_paiban.set("date", date);
                String json = worker.toJSONString();
                staff_paiban.set("content", json);
                staff_paiban.set("app_content", ContentTransformationUtil.Pc2AppContentEvery15M4Paiban(json));
                staff_paiban.set("creater_id", userId);
                staff_paiban.set("create_time", saveTime);
                staff_paiban.set("modifier_id", userId);
                staff_paiban.set("modify_time", saveTime);
                saveList.add(staff_paiban);
            }
        }
        if(saveList != null && saveList.size() > 0){
            Db.batchSave("h_staff_paiban", saveList, saveList.size());
        }
        return saveList;
    }

    private void update_h_staff_clock(List<Record> staffPainbanList, UserSessionUtil usu, String date){
        String[] dateArr = new String[1];
        dateArr[0] = date;
        saveStaffClock(staffPainbanList, usu, dateArr);
    }

    private void update_h_work_time(List<Record> staffPainbanList, UserSessionUtil usu, String date){
        String[] dateArr = new String[1];
        dateArr[0] = date;
        saveWorkTime(staffPainbanList, usu, dateArr);
    }

    private void update_h_paiban_problem(JSONObject object, UserSessionUtil usu){
        String date = object.getString("date");
        String storeId = object.getString("dept");
        JSONArray workers = object.getJSONArray("workers");
        String userId = usu.getUserId();
        String saveTime = DateTool.GetDateTime();
        String[] dateArr = new String[1];
        dateArr[0] = date;
        //查询可变工时指南
        String kbgsznSql = "select * from h_variable_time_guide where store_id=? order by l_money";
        List<Record> kbgsznList = Db.find(kbgsznSql, storeId);
        //一周预计销售金额
        String yjxsjeSql = "select * from h_store_forecast_turnover where store_id=? and scheduling_date between ? and ? order by scheduling_date";
        List<Record> yjxsjeList = Db.find(yjxsjeSql, storeId, date, date);
        Map<String, List<Record>> yjxsjeMap = yjxsjeToDateRecord(yjxsje(yjxsjeList));
        //员工闲时
        String ygxsSql = "select sit.*, s.name name, s.hour_wage salary from h_staff_idle_time sit, h_staff s where sit.staff_id=s.id and sit.store_id=? and sit.date between ? and ? order by date";
        List<Record> ygxsList = Db.find(ygxsSql, storeId, date, date);
        Map<String, List<Record>> ygxsMap = ygxsToDateRecord(ygxsList);
        Map<String, Map<String, List<Record>>> ygxsDayTimeMap = getDayTimeYgxs(ygxsMap, dateArr);

        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap = createMap_date_time_kind_update(dateArr, yjxsjeMap, kbgsznList, ygxsMap, ygxsDayTimeMap);

    }

    /**
     * 排班成日期，时间段，岗位，员工集合的格式
     * @param dateArr
     * @param yjxsjeMap
     * @param kbgsznList
     * @param ygxsMap
     * @param ygxsDayTimeMap
     * @return
     */
    private Map<String, Map<String, Map<String, List<Record>>>> createMap_date_time_kind_update(String[] dateArr, Map<String, List<Record>> yjxsjeMap, List<Record> kbgsznList, Map<String, List<Record>> ygxsMap,Map<String, Map<String, List<Record>>> ygxsDayTimeMap){


//        return pbDateTimeStaffMap;
        return null;
    }

    private static int toInt(Object obj){
        if(obj instanceof Integer){
            return (int)obj;
        }else{
            if(obj instanceof String){
                try{
                    int result = new Integer(obj.toString());
                    return result;
                } catch (Exception e){
                    e.printStackTrace();
                    return 0;
                }
            }else{
                return 0;
            }
        }
    }

}
