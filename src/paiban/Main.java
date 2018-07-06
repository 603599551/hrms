package paiban;

import com.jfinal.plugin.activerecord.Record;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static final String[] columns = {"id", "staff_id", "create_time", "creater_id", "modify_time", "modifier_id", "content"};

    public static final String[] posts = {"waiter", "passed", "band", "cleanup", "inputorder", "cashier", "preparation_xj", "preparation_lb", "pickle_xj", "pickle_lb", "Noodle", "pendulum", "preparation", "fried_noodles", "drink"};
    public static final int[] p0 = {1,0,0,0,0,1,1,1,0,0,1,0,1,0,0};
    public static final int[] p1 = {1,1,0,0,0,1,1,1,0,0,1,0,1,0,0};
    public static final int[] p2 = {2,1,0,0,0,1,1,1,0,0,1,0,1,0,0};
    public static final int[] p3 = {2,1,0,0,1,1,1,1,0,0,1,1,1,0,0};
    public static final int[] p4 = {2,2,1,1,1,1,1,1,0,1,1,1,1,1,0};
    public static final int[] p5 = {3,3,1,1,1,1,1,1,0,1,1,1,1,1,0};
    public static final int[] p6 = {4,3,1,1,1,1,1,1,0,1,1,1,1,1,0};
    public static final int[] p7 = {5,4,1,1,1,1,1,1,1,1,2,2,1,1,1};
    public static final int[] p8 = {6,4,1,1,2,2,1,1,1,2,2,2,1,1,1};
    public static Map<Integer, int[]> map = new HashMap<>();
    static{
        map.put(0, p0);
        map.put(1, p1);
        map.put(2, p2);
        map.put(3, p3);
        map.put(4, p4);
        map.put(5, p5);
        map.put(6, p6);
        map.put(7, p7);
        map.put(8, p8);
    }

    public static void main(String[] args){
        String sql = "update h_staff_idle_time set kind= ";
        for(int i = 1; i <= 20; i++){
            System.out.println(sql + "'" + getKinds() + "'" + "where id=" + i + ";");
        }
//        createStaffIdleTime();
    }

    public static String getKinds(){
        int length = random(posts.length);
        String s = "";
        for(int i = 0; i < length; i++){
            s += posts[random(posts.length)] + ",";
        }
        return s.substring(0, s.length() - 1);
    }

    public static void createh_variable_time_guide(){
        for(int i = 0; i < 9; i++){
            int[] p = map.get(i);
            Map<String, Integer> r = new HashMap<>();
            for(int j = 0; j < posts.length; j++){
                r.put(posts[j], p[j]);
            }
            System.out.println(JSONObject.fromObject(r));
        }
    }
    /*
{"inputorder":0,"Noodle":1,"fried_noodles":0,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":0,"cashier":1,"pickle_lb":0,"waiter":1,"passed":0,"band":0,"pickle_xj":0,"pendulum":0}
{"inputorder":0,"Noodle":1,"fried_noodles":0,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":0,"cashier":1,"pickle_lb":0,"waiter":1,"passed":1,"band":0,"pickle_xj":0,"pendulum":0}
{"inputorder":0,"Noodle":1,"fried_noodles":0,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":0,"cashier":1,"pickle_lb":0,"waiter":2,"passed":1,"band":0,"pickle_xj":0,"pendulum":0}
{"inputorder":1,"Noodle":1,"fried_noodles":0,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":0,"cashier":1,"pickle_lb":0,"waiter":2,"passed":1,"band":0,"pickle_xj":0,"pendulum":1}
{"inputorder":1,"Noodle":1,"fried_noodles":1,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":1,"cashier":1,"pickle_lb":1,"waiter":2,"passed":2,"band":1,"pickle_xj":0,"pendulum":1}
{"inputorder":1,"Noodle":1,"fried_noodles":1,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":1,"cashier":1,"pickle_lb":1,"waiter":3,"passed":3,"band":1,"pickle_xj":0,"pendulum":1}
{"inputorder":1,"Noodle":1,"fried_noodles":1,"preparation_lb":1,"preparation_xj":1,"drink":0,"preparation":1,"cleanup":1,"cashier":1,"pickle_lb":1,"waiter":4,"passed":3,"band":1,"pickle_xj":0,"pendulum":1}
{"inputorder":1,"Noodle":2,"fried_noodles":1,"preparation_lb":1,"preparation_xj":1,"drink":1,"preparation":1,"cleanup":1,"cashier":1,"pickle_lb":1,"waiter":5,"passed":4,"band":1,"pickle_xj":1,"pendulum":2}
{"inputorder":2,"Noodle":2,"fried_noodles":1,"preparation_lb":1,"preparation_xj":1,"drink":1,"preparation":1,"cleanup":1,"cashier":2,"pickle_lb":2,"waiter":6,"passed":4,"band":1,"pickle_xj":1,"pendulum":2}

     */

    public static void createStaffIdleTime(){
        List<Record> list = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            Record r = new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("content", content());
            String time = DateTool.GetDateTime();
            r.set("create_time", time);
            r.set("modify_time", time);
            r.set("creater_id", 1);
            r.set("modifier_id", 1);
            r.set("staff_id", "张" + i);
            list.add(r);
        }
        String sql = "insert into h_staff_Idle_time(id, staff_id, create_time, creater_id, modify_time, modifier_id, content) values (";
        String all = "";
        for(Record r : list){
            all += sql;
            for(String s : columns){
                all += "'" + r.get(s) + "',";
            }
            all = all.substring(0, all.length() - 1) + ");\n";
        }
        System.out.println(all);
    }

    public static String content(){
        Map<String, Integer> staff = new HashMap<>();
        for(int i = 0; i < 66; i++){
            staff.put(i + "", random());
        }
        return JSONObject.fromObject(staff).toString();
    }

    public static int random(){
        int i = (int) (Math.random() * 100);
        if(i % 2 == 0){
            return 0;
        }else{
            return 1;
        }
    }

    public static int random(int end){
        int i = (int) (Math.random() * end);
        return i;
    }

}
