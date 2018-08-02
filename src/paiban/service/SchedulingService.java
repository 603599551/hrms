package paiban.service;

import com.alibaba.fastjson.JSONObject;
import com.common.service.BaseService;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

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
    static {
        for(int i = 0; i < 66; i++){
            time4OneHourArr[i] = i + "";
        }
    }

    public void paiban(String storeId, String beginDate){
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
        String ygxsSql = "select sit.*, s.name from h_staff_idle_time sit, h_staff s where sit.staff_id=s.id and sit.store_id=? and sit.date between ? and ? order by date";
        List<Record> ygxsList = Db.find(ygxsSql, storeId, beginDate, endDate);
        Map<String, List<Record>> ygxsMap = ygxsToDateRecord(ygxsList);
        Map<String, Map<String, List<Record>>> ygxsDayTimeMap = getDayTimeYgxs(ygxsMap, dateArr);

        Map<String, Map<String, Map<String, List<Record>>>> pbDateTimeStaffMap = new HashMap<>();
        for(String date : dateArr){
            Map<String, Map<String, List<Record>>> timePostsStaffMap = new HashMap<>();
            pbDateTimeStaffMap.put(date, timePostsStaffMap);
            int houchuNum = 0;
            int qiantingNum = 0;
            int houchuIndex = 0;
            int qiantingIndex = 0;
            for(Record yjxsje : yjxsjeList){
                Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
                JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
                for(String h : posts_houchu){
                    houchuNum += toInt(json.get(h));
                }
                for(String q : posts_qianting){
                    qiantingNum += toInt(json.get(q));
                }
                houchuNum /= ygxsMap.get(date).size();
                qiantingNum /= ygxsMap.get(date).size();
            }
            for(Record yjxsje : yjxsjeList){
                Map<String, List<Record>> postStaffMap = new HashMap<>();
                timePostsStaffMap.put(yjxsje.getStr("time"), postStaffMap);
                Record kbgszn = getGSByKbgsznAndYjxsje(kbgsznList, yjxsje);
                JSONObject json = JSONObject.parseObject(kbgszn.getStr("work_num"));
                List<Record> list = ygxsDayTimeMap.get(date).get(yjxsje.getStr("time"));
                for(String post : posts){
                    List<Record> staffList = new ArrayList<>();
                    postStaffMap.put(post, staffList);
                    int num = toInt(json.getString(post));
                    for(int i = 0; i < num; i++){
                        Record r = list.get(i);

                    }
                }
            }
        }
    }

    private boolean staffTimeIsAllowed(Record ygxs, int num, String posts, String date, String time){
        if(toInt(ygxs.getStr("paiban_times")) < num){
            if(ygxs.getStr("kind").indexOf(posts) >= 0){
                ygxs.set("paiban_times", toInt(ygxs.getStr("paiban_times")) + 1);
            }

        }else{
        }
        return false;
    }

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
