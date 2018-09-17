package paiban.service;

import com.alibaba.fastjson.JSON;
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

//@SuppressWarnings("all")
public class SchedulingService extends BaseService {

    private static final String[] timeArr = {"8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
    private static final String[] time4OneHourArr = new String[66];
    private static final String[] posts = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    private static final String[] posts_houchu = {"preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    private static final String[] posts_qianting = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier"};

    private static final Map<String, Integer> postsMap = new HashMap<>();
    static {
        for(int i = 0; i < 66; i++){
            time4OneHourArr[i] = i + "";
        }
        for(int i = 0; i < posts.length; i++){
            postsMap.put(posts[i], i);
        }
    }

    /**
     * 获取排班数据
     * @param storeId 门店id
     * @param date 日期
     * @return 排班数据
     */
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

    /**
     * 排班方法
     * @param storeId 门店id
     * @param beginDate 开始日期
     * @param usu session信息
     */
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

        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap;

        Map<String, List<Record>> datePaibListMap = new HashMap<>();
        Map<String, JSONArray> staffKindNumJsonMap = new HashMap<>();
        if((yjxsjeList != null && yjxsjeList.size() > 0)){
            for(Record r : yjxsjeList){
                String date = r.getStr("scheduling_date");
                String totalMoney = r.getStr("total_money");
                List<Record> paibanList = getPaibanHistory(storeId, totalMoney, beginDate);
                if(paibanList != null && paibanList.size() > 0){
                    datePaibListMap.put(date, paibanList);
                }
            }
            if(datePaibListMap != null && datePaibListMap.size() == 7){
                //1、修改员工闲时，将对应时间的闲时设置成0   ygxsDayTimeMap
                //2、修改创建json数据，将所需人数重新初始化
                for(String date : dateArr){
                    List<Record> paibanList = datePaibListMap.get(date);
                    JSONArray jsonArray = new JSONArray();
                    for(int index = 0; index < yjxsjeMap.get(date).size(); index++){
                        Record yjxsje = yjxsjeMap.get(date).get(index);
                        Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
                        JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
                        Map<String, Integer> kindMap = createNullMap(json);
                        for(Record r : paibanList){
                            JSONObject paibanContent = JSONObject.parseObject(r.getStr("content"));
                            JSONArray workArray = paibanContent.getJSONArray("work");
                            if(workArray != null && workArray.size() > 0){
                                for(int i = 0; i < workArray.size(); i++){
                                    JSONObject work = workArray.getJSONObject(i);
                                    int timeIndex = work.getJSONArray("pos").getIntValue(0);
                                    int kindIndex = work.getJSONArray("pos").getIntValue(1);
                                    if(index == timeIndex){
                                        kindMap.put(posts[kindIndex], kindMap.get(posts[kindIndex]) - 1);
                                        List<Record> staffList = ygxsDayTimeMap.get(date).get(index + "");
                                        if(staffList != null && staffList.size() > 0){
                                            for(int staffListIndex = 0; staffListIndex < staffList.size(); staffListIndex++){
                                                Record staffRecord = staffList.get(staffListIndex);
                                                if(staffRecord.getStr("id").equals(r.getStr("id"))){
                                                    staffList.remove(staffListIndex);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        jsonArray.add((JSONObject)JSON.toJSON(kindMap));
                    }
                    staffKindNumJsonMap.put(date, jsonArray);
                }
                pbDateTimeStaffMap = createMap_date_time_kind(dateArr, yjxsjeMap, kbgsznList, ygxsMap, ygxsDayTimeMap, staffKindNumJsonMap, true);
            }else{
                pbDateTimeStaffMap = createMap_date_time_kind(dateArr, yjxsjeMap, kbgsznList, ygxsMap, ygxsDayTimeMap, null, false);
            }
            save(pbDateTimeStaffMap, dateArr, usu);
        }
    }

    private Map<String, Integer> createNullMap(JSONObject json){
        Map<String, Integer> result = new HashMap<>();
        for(String s : posts){
            result.put(s, json.getIntValue(s));
        }
        return result;
    }

    private Map<String, Integer> createProblemNullMap(JSONObject json){
        Map<String, Integer> result = new HashMap<>();
        for(String s : posts){
            result.put(s, json.getIntValue(s));
            result.put(s + "_maxNum", json.getIntValue(s));
        }
        return result;
    }

    /**
     * 保存排班数据
     * @param pbDateTimeStaffMap 排班日期时间段员工数据
     * @param dateArr 日期数组
     * @param usu session数据
     */
    private void save(Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap, String[] dateArr, UserSessionUtil usu){
        List<Record> staffAllList = new ArrayList<>();
        StringBuilder ids = new StringBuilder("");
        for(String date : dateArr){
            ids = marshalingPaibanOneDay2Save(pbDateTimeStaffMap.get(date), date, ids, staffAllList);
        }
        saveStaffPaiban(staffAllList, dateArr, usu);
    }

    /**
     * 对指定日期排班排班数据整理，准备保存
     * @param timeKindStaff <时间段,岗位,员工List>>> 集合
     * @param date 日期
     * @param ids 避免重复添加员工，将员工的id拼接起来，用逗号分隔
     *            判断ids中是否包含当前循环中staff的id，不包含则加入集合，包含则不再次添加到集合中
     * @param staffAllList 排好班的员工集合
     * @return 处理过的员工ids
     */
    private StringBuilder  marshalingPaibanOneDay2Save(Map<String, Map<String, List<Record>>> timeKindStaff, String date, StringBuilder ids, List<Record> staffAllList){
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
        return ids;
    }

    /**
     * 保存h_staff_paiban表数据
     * @param staffAllList 所有员工数据
     * @param dateArr 日期数组
     * @param usu session信息
     */
    private void saveStaffPaiban(List<Record> staffAllList, String[] dateArr, UserSessionUtil usu){
        String storeId = (String) usu.getUserBean().get("store_id");
        List<Record> kindAreaList = Db.find("select has.*, ha.kind kind from h_area_staff has, h_area ha where has.area_id=ha.id and has.store_id=?", storeId);
        Map<String, Map<String, Record>> staffKindAreaMap = new HashMap<>();
        if(kindAreaList != null && kindAreaList.size() > 0){
            for(Record r : kindAreaList){
                Map<String, Record> kindAreaMap = staffKindAreaMap.computeIfAbsent(r.getStr("staff_id"), k -> new HashMap());
                kindAreaMap.put(r.getStr("kind"), r);
            }
        }
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
                        staffSave.set("app_area_content", ContentTransformationUtil.Pc2AppKindAreaContentEvery15M4Paiban(json, staffKindAreaMap.get(staff.get("staff_id"))));
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
            saveStaffClock(saveList, usu, dateArr, staffKindAreaMap);
            saveWorkTime(saveList, usu, dateArr);
        }
    }

    /**
     * 保存h_work_time和h_work_time_detail表数据
     * @param staffPaibanList
     * @param usu
     * @param dateArr
     */
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

    /**
     * 保存h_staff_clock表数据
     * @param staffPaibanList
     * @param usu
     * @param dateArr
     * @param staffKindAreaMap Map<staff_id,Map<kind,area>>
     */
    private void saveStaffClock(List<Record> staffPaibanList, UserSessionUtil usu, String[] dateArr, Map<String, Map<String, Record>> staffKindAreaMap){
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
                        Record areaRecord = ContentTransformationUtil.getAreaRecord(kind, staffKindAreaMap.get(r.get("staff_id")));
                        save.set("area_name", areaRecord.getStr("area_name"));
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
                    r.set("is_deal", "0");
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
     * @param dateArr 日期集合
     * @param yjxsjeMap 预计销售金额Map
     * @param kbgsznList 可变工时指南
     * @param ygxsMap 员工闲时
     * @param ygxsDayTimeMap 员工闲时日期时间Map
     * @return 排班后的数据
     */
    private Map<String, Map<String, Map<String, List<Record>>>> createMap_date_time_kind(String[] dateArr, Map<String, List<Record>> yjxsjeMap, List<Record> kbgsznList, Map<String, List<Record>> ygxsMap, Map<String, Map<String, List<Record>>> ygxsDayTimeMap, Map<String, JSONArray> staffKindNumJsonMap, boolean userJson){
        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap = new HashMap<>();
        //排班后人员不够的map
        List<Record> hxStaffList = new ArrayList<>();
        for(String date : dateArr){
            createMap_date_time_kind_oneDay(date, pbDateTimeStaffMap, yjxsjeMap, kbgsznList, ygxsMap, ygxsDayTimeMap, hxStaffList, staffKindNumJsonMap != null ? staffKindNumJsonMap.get(date) : null, userJson);
        }
        marshalingPaibanProblem(hxStaffList, dateArr);
        return pbDateTimeStaffMap;
    }

    /**
     * 整理h_paiban_problem数据
     * @param hxStaffList 排班后人员不够的list
     * @param dateArr 日期集合
     */
    private void marshalingPaibanProblem(List<Record> hxStaffList, String... dateArr){
        if(hxStaffList != null && hxStaffList.size() > 0){
            List<Record> problemList = new ArrayList<>();
            Map<String, List<Map<String, Object>>> dateProblemMap = new HashMap<>();
            String store_id = "";
            for(Record r : hxStaffList){
//                List<Map<String, Object>> problemData = dateProblemMap.get(r.getStr("date"));
//                if(problemData == null){
//                    problemData = new ArrayList<>();
//                    dateProblemMap.put(r.getStr("date"), problemData);
//                }
                List<Map<String, Object>> problemData = dateProblemMap.computeIfAbsent(r.getStr("date"), k -> new ArrayList<>());
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
    }

    /**
     * 排指定日期的班
     * @param date 指定日期
     * @param pbDateTimeStaffMap Map<String, Map<String, Map<String, List<Record>>>>
     * @param yjxsjeMap 预计销售金额Map
     * @param kbgsznList 可变工时指南
     * @param ygxsMap 员工闲时
     * @param ygxsDayTimeMap 员工闲时日期时间Map
     * @param hxStaffList 排班后人员不够的map
     * @param staffKindNumJson 每个时间段所需人数
     * @param userJson 是否使用json参数处理人数
     *
     */
    private void createMap_date_time_kind_oneDay(String date, Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap, Map<String, List<Record>> yjxsjeMap, List<Record> kbgsznList, Map<String, List<Record>> ygxsMap, Map<String, Map<String, List<Record>>> ygxsDayTimeMap, List<Record> hxStaffList, JSONArray staffKindNumJson, boolean userJson){
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
        for(int index = 0; index < yjxsjeList.size(); index++){
            Record yjxsje = yjxsjeList.get(index);
            String time = yjxsje.getStr("time");
            Map<String, List<Record>> postStaffMap = new HashMap<>();
            timePostsStaffMap.put(yjxsje.getStr("time"), postStaffMap);
            Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
            JSONObject json = userJson ? staffKindNumJson.getJSONObject(index) : JSONObject.parseObject(kbgszn.getStr("work_num"));
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
        if(toInt(ygxs.getStr("paiban_times")) < num && ygxs.getStr("kind") != null){
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
        List<Record> kindAreaList = Db.find("select has.*, ha.kind kind from h_area_staff has, h_area ha where has.area_id=ha.id and has.store_id=?", storeId);
        Map<String, Map<String, Record>> staffKindAreaMap = new HashMap<>();
        if(kindAreaList != null && kindAreaList.size() > 0){
            for(Record r : kindAreaList){
                Map<String, Record> kindAreaMap = staffKindAreaMap.computeIfAbsent(r.getStr("staff_id"), k -> new HashMap());
                kindAreaMap.put(r.getStr("kind"), r);
            }
        }
        List<Record> staffPaibanList = update_h_staff_paiban(object, usu);
        update_h_staff_clock(staffPaibanList, usu, date, staffKindAreaMap);
        update_h_work_time(staffPaibanList, usu, date);
        update_h_paiban_problem(object, staffPaibanList, usu);
    }

    /**
     * 修改h_staff_paiban表数据，删除历史数据，重新插入新数据
     * @param object
     * @param usu
     * @return
     */
    private List<Record> update_h_staff_paiban(JSONObject object, UserSessionUtil usu){
        List<Record> saveList = new ArrayList<>();
        String date = object.getString("date");
        String store_id = object.getString("dept");
        JSONArray workers = object.getJSONArray("workers");
        List<Record> kindAreaList = Db.find("select has.*, ha.kind kind from h_area_staff has, h_area ha where has.area_id=ha.id and has.store_id=?", store_id);
        Map<String, Map<String, Record>> staffKindAreaMap = new HashMap<>();
        if(kindAreaList != null && kindAreaList.size() > 0){
            for(Record r : kindAreaList){
                Map<String, Record> kindAreaMap = staffKindAreaMap.computeIfAbsent(r.getStr("staff_id"), k -> new HashMap());
                kindAreaMap.put(r.getStr("kind"), r);
            }
        }
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
                staff_paiban.set("app_area_content", ContentTransformationUtil.Pc2AppKindAreaContentEvery15M4Paiban(json, staffKindAreaMap.get(worker.get("id"))));
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

    /**
     * 修改h_staff_clock数据
     * @param staffPainbanList
     * @param usu
     * @param date
     */
    private void update_h_staff_clock(List<Record> staffPainbanList, UserSessionUtil usu, String date, Map<String, Map<String, Record>> staffKindAreaMap){
        String[] dateArr = new String[1];
        dateArr[0] = date;
        saveStaffClock(staffPainbanList, usu, dateArr, staffKindAreaMap);
    }

    /**
     * 修改h_work_time和h_work_time_detail数据
     * @param staffPainbanList
     * @param usu
     * @param date
     */
    private void update_h_work_time(List<Record> staffPainbanList, UserSessionUtil usu, String date){
        String[] dateArr = new String[1];
        dateArr[0] = date;
        saveWorkTime(staffPainbanList, usu, dateArr);
    }

    /**
     * 修改h_paiban_problem数据
     * @param staffPaibanList
     * @param usu
     */
    //TODO 未完成
    private void update_h_paiban_problem(JSONObject object, List<Record> staffPaibanList, UserSessionUtil usu){
        String date = object.getString("date");
        String storeId = object.getString("dept");
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

        List<Map<String, Integer>> kindMapList = new ArrayList<>();
        for(int index = 0; index < yjxsjeMap.get(date).size(); index++){
            Record yjxsje = yjxsjeMap.get(date).get(index);
            Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
            JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
            Map<String, Integer> kindMap = createProblemNullMap(json);
            for(Record r : staffPaibanList){
                JSONObject paibanContent = JSONObject.parseObject(r.getStr("content"));
                JSONArray workArray = paibanContent.getJSONArray("work");
                if(workArray != null && workArray.size() > 0){
                    for(int i = 0; i < workArray.size(); i++){
                        JSONObject work = workArray.getJSONObject(i);
                        int timeIndex = work.getJSONArray("pos").getIntValue(0);
                        int kindIndex = work.getJSONArray("pos").getIntValue(1);
                        if(index == timeIndex){
                            kindMap.put(posts[kindIndex], kindMap.get(posts[kindIndex]) - 1);
//                            kindMap.put(posts[kindIndex] + "_maxNum", kindMap.get(posts[kindIndex]));
                            List<Record> staffList = ygxsDayTimeMap.get(date).get(index + "");
                            if(staffList != null && staffList.size() > 0){
                                for(int staffListIndex = 0; staffListIndex < staffList.size(); staffListIndex++){
                                    Record staffRecord = staffList.get(staffListIndex);
                                    if(staffRecord.getStr("id").equals(r.getStr("id"))){
                                        staffList.remove(staffListIndex);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            kindMapList.add(kindMap);
        }
        List<Map<String, Object>> problemContentList = new ArrayList<>();
        for(int index = 0; index < yjxsjeMap.get(date).size(); index++){
            Map<String, Integer> kindMap = kindMapList.get(index);
            for(int i = 0; i < posts.length; i++){
                if(kindMap.get(posts[i]) != null && kindMap.get(posts[i]) != 0){
                    Map<String, Object> map = new HashMap<>();
                    map.put("maxNum", kindMap.get(posts[i] + "_maxNum"));
                    int[] pos = {index, i};
                    map.put("pos", pos);
                    problemContentList.add(map);
                }
            }
        }
        Record problem = new Record();
        problem.set("id", UUIDTool.getUUID());
        problem.set("date", date);
        problem.set("store_id", storeId);
        problem.set("content", JSONArray.toJSONString(problemContentList));
        String delete = "delete from h_paiban_problem where store_id=? and date=?";
        Db.delete(delete, storeId, date);
        Db.save("h_paiban_problem", problem);
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
        return null;
    }

    private List<Record> getPaibanHistory(String storeId, String totalMoney, String date){
        List<Record> result = null;
        String sft_sql = "select * from h_store_forecast_turnover where store_id=? and total_money=? and scheduling_date<?";
        List<Record> yu4Gu1Ying2YeList = Db.find(sft_sql, storeId, totalMoney, date);
        if(yu4Gu1Ying2YeList != null && yu4Gu1Ying2YeList.size() > 0){
            int index = random(0, yu4Gu1Ying2YeList.size());
            Record yu4Gu1Ying2Ye = yu4Gu1Ying2YeList.get(index);
            String paiban_sql = "select * from h_staff_paiban where store_id=? and date=? and staff_id in (select id from h_staff where dept_id=?)";
            result = Db.find(paiban_sql, storeId, yu4Gu1Ying2Ye.get("scheduling_date"), storeId);
        }else{
            sft_sql = "select * from h_store_forecast_turnover where store_id=? and total_money<=?";
            yu4Gu1Ying2YeList = Db.find(sft_sql, storeId, totalMoney);
            if(yu4Gu1Ying2YeList != null && yu4Gu1Ying2YeList.size() > 0){
                Record yu4Gu1Ying2Ye = yu4Gu1Ying2YeList.get(0);
                String paiban_sql = "select * from h_staff_paiban where store_id=? and date=? and staff_id in (select id from h_staff where dept_id=?)";
                result = Db.find(paiban_sql, storeId, yu4Gu1Ying2Ye.get("scheduling_date"), storeId);
            }
        }
        return result;
    }

    private static int random(int begin, int end){
        return (int)(Math.random() * (end - begin) + begin);
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
