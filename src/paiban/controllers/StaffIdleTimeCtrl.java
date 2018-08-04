package paiban.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import org.apache.commons.lang.StringUtils;
import paiban.service.StaffIdleTimeService;
import utils.bean.JsonHashMap;

import java.text.SimpleDateFormat;
import java.util.*;

import static utils.ContentTransformationUtil.*;

public class StaffIdleTimeCtrl extends BaseCtrl {

    /**

     6.9.	录入下周可上班时间（王泽）
     名称	录入员工下周闲时
     描述	通过id将员工闲时信息录入到数据库中
     验证	根据id验证员工是否存在
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/staffCtrl/saveTime
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	员工id
     times	string		否	员工的显示时间段

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": “添加成功！”
     }
     失败	{
     "code": 0,
     "message": "员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//其实失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     times=07:30:00-23:59:59,07:30:00-13:59:59  week=4  id=员工id
     */

    public void saveTime(){
//        renderJson("{\"code\":1,\"message\":\"添加成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");          //员工id
        String week = getPara("week");
        String times = getPara("times");
        UserSessionUtil usu = new UserSessionUtil(getRequest());

        //进行非空判断
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(week)){
            jhm.putCode(0).putMessage("请选择星期几！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(times)){
            jhm.putCode(0).putMessage("请选择时间段！");
            renderJson(jhm);
            return;
        }

        try {
            Map paraMap = new HashMap();
            paraMap.put("id", id);
            paraMap.put("week", week);
            paraMap.put("times", times);
            paraMap.put("usu", usu);

            StaffIdleTimeService srv = enhance(StaffIdleTimeService.class);
            jhm = srv.save(paraMap);
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 1.1.查看闲时
     名称	查看闲时
     描述	Pc端通过选择员工，根据日期查看该员工闲时
     验证
     权限	店长
     URL	http://localhost:8080/hrms/mgr/staffIdleTimeCtrl/getStaffIdleTimeById
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	string		否	员工id
     date	string		否	日期etc:2018-08-13

     返回格式	JSON
     成功	{
     "code": 1,
     "data\":[
     {\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"08:00-10:00\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},
     {\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"08:00-10:00\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},
     {\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},
     {\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"\",\"day7\":\"\"}
     ]
     }

     失败	{
     "code": 0,
     "message": "记录不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void getStaffIdleTimeById(){
//        renderJson("{\"code\":1,\"message\":\"\",\"data\":[{\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"08:00-10:00\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},{\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"08:00-10:00\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},{\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"08:00-10:00\",\"day7\":\"\"},{\"day1\":\"08:00-10:00\",\"day2\":\"08:00-10:00\",\"day3\":\"\",\"day4\":\"\",\"day5\":\"08:00-10:00\",\"day6\":\"\",\"day7\":\"\"}]}");
        JsonHashMap jhm = new JsonHashMap();
        String staffId = getPara("staff_id");
        String date = getPara("date");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        //进行非空验证
        if(StringUtils.isEmpty(staffId)){
            jhm.putCode(0).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }

        try {
            //day数组记录下周日期
            String []day = new String[7];
            for(int i = 0; i < day.length; i++){
                day[i] = this.nextDay(date,i);
            }

            String contentSearch = "select t.content as content, t.date as date from h_staff_idle_time t where t.staff_id = ? and t.date >= ? and t.date <= ? ";
            List <Record> recordList = Db.find(contentSearch, staffId, day[0], day[6]);

            int [] tempt = new int [7];                      //判断下一周哪一天已添加闲时
            String [] dayArray = new String[7];               //装下一周记录中的content
            List <Map> contentList = new  ArrayList ();            //装最终返回前台的数据
            int max = 0;

            //把数据库中content内容按照星期几放入按星期排序的dayList里
            for(int i = 0; i < dayArray.length; i++){
                tempt[i] = 0;
                for(int j = 0; j < recordList.size(); j++){
                    if(StringUtils.equals(recordList.get(j).getStr("date"),day[i])){
                        dayArray[i] = recordList.get(j).getStr("content");
                        tempt[i] = 1;
                    }
                }
                if(tempt[i] == 0){
                    dayArray[i] = "";
                }
            }

            //把content的json转化为时间段
            String[][] dayTime = new String [7][66];    //将一周分为7天66组15分钟，并根据前台传来数据将时间填进去二维数组记录下来

            //先将二维数组全部赋值为空字符串防止为空
            for(int i = 0; i < 7; i++){
                for(int j = 0; j < 66; j++){
                    dayTime[i][j] = "";
                }
            }

            for(int i = 0; i < dayArray.length; i++){
                if(tempt[i] ==0){
                    continue;
                }
                List <String>timeList = JsonTimeToStringTimeXianShi(dayArray[i]);
                for(int j = 0; j < timeList.size(); j++){
                    if(timeList.size() > max){
                        max = timeList.size();
                    }
                    dayTime[i][j] = timeList.get(j);
                }
            }

            //将时间段装入Map里并放入contentList传到前台
            for(int i = 0; i < max; i++ ){
                Map<String,String> dayMap = new HashMap();
                for(int j = 0; j < 7; j++){
                    String dayName = "day" + String.valueOf(j+1);
                    //装7天中每一天中一个连续时间段，没有则装的空字符串
                    if(tempt[j]==0){
                        dayMap.put(dayName, "");
                        continue;
                    }
                    for(int k = i; k < i+1; k++ ){
                        dayMap.put(dayName, dayTime[j][k]);
                    }
                }
                contentList.add(dayMap);
            }
            jhm.putCode(1);
            jhm.put("data", contentList);

        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }


    /**
     * 获取员工闲时
     */
    public void getStaffIdleTime(){
//        renderJson("{\"code\":1,\"message\":\"\",\"data\":{\"staff\":{\"id\":\"员工id\",\"gender\":\"男\",\"phone\":\"13888888888\",\"name\":\"鹿晗\"},\"list\":[[\"15:30:00-18:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"10:30:00-15:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"]]}}");
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        DateTool dateTool = new DateTool();

        //进行非空验证
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("id不能为空！");
            renderJson(jhm);
            return;
        }

        try {
            String nowDate = dateTool.GetDateTime();
            //获取下周星期几对应日期
            int nowDay = dateTool.getWeekDay(nowDate);
            int next = 9 - nowDay;
            List timeList = new ArrayList();
            String startDate = this.nextDay(nowDate, next);
            String endDate = this.nextDay(startDate, 6);
            String staffSearch = "select count(*) as c, s.id as id, case s.gender when '1' then '男' when '0' then '女' end as gender, s.phone as phone, s.name as name from h_staff s where s.id = ? ";
            Record staffR = Db.findFirst(staffSearch, id);
            if(staffR.getInt("c") > 0){
                String idleSearch = "select t.content as content  from h_staff_idle_time t where t.staff_id = ? and date >= ? and date <= ? order by date";
                List <Record> recordList = Db.find(idleSearch, id, startDate, endDate);
                for(int i = 0; i < recordList.size(); i++){
                    timeList.add(JsonTimeToStringTimeXianShi(recordList.get(i).getStr("content")));
                }
                staffR.remove("c");
                jhm.putCode(1);
                Record record = new Record();
                record.set("list", timeList);
                record.set("staff", staffR);
                record.set("week", "0");
                jhm.put("data",record);
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
