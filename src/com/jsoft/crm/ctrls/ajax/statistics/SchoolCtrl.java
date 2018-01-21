package com.jsoft.crm.ctrls.ajax.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import utils.DateTool;
import utils.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.*;

/**
 * 按照学校、专业统计
 */
public class SchoolCtrl extends Controller {

    public void school(){
        Map paraMap=getParaMap();
//        System.out.println(paraMap);

        String year=DateTool.GetDate().substring(0,4);
        String para="";
        JSONObject paraObj=null;
        String startTime=year+"-01-01 00:00:00";
        String endTime=year+"-12-31 23:59:59";
        JsonHashMap jhm=new JsonHashMap();
        String sql="select (select name from dictionary where dictionary.id=s.school) as school_name,count(id) as count from student s where ?<=create_time and create_time<=? group by school order by count desc";
        try {
            if(paraMap!=null && !paraMap.isEmpty()){
                Iterator<Map.Entry> it=paraMap.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry en=it.next();
                    para=(String)en.getKey();
                    paraObj=JSONObject.parseObject(para);
                    break;
                }
                JSONArray timeArray=paraObj.getJSONArray("time");
                if(timeArray!=null && !timeArray.isEmpty()) {
                    startTime = timeArray.getString(0)+" 00:00:00";
                    endTime = timeArray.getString(1)+" 23:59:59";
                }
            }


            List<Record> list = Db.find(sql,startTime,endTime);
            int studentTotal=0;
            List reList = new ArrayList();
            for (Record r : list) {
                Object[] array = new Object[2];
                String shoolName = r.get("school_name");
                Object countObj = r.get("count");
                int count = NumberUtils.parseInt(countObj, 0);
                array[0] = shoolName;
                array[1] = count;
                studentTotal=studentTotal+count;
                reList.add(array);
            }
            if(reList!=null && !reList.isEmpty()){
                Object[] array=(Object[])reList.get(0);
                Map map=new HashMap();
                map.put("name",array[0]);
                map.put("y",array[1]);
                map.put("sliced",true);
                map.put("selected",true);
                reList.set(0,map);
            }

//            Map reMap = new HashMap();
//            reMap.put("data", reList);
            jhm.putCode(1);
            jhm.put("data",reList);
            jhm.put("total",studentTotal);
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }

    public void speciality(){
        Map paraMap=getParaMap();
//        System.out.println(paraMap);

        String year=DateTool.GetDate().substring(0,4);
        String para="";
        JSONObject paraObj=null;
        String startTime=year+"-01-01 00:00:00";
        String endTime=year+"-12-31 23:59:59";
        JsonHashMap jhm=new JsonHashMap();
        String sql="select (select name from dictionary where parent_id='25' and dictionary.id=s.speciality) as speciality_name,count(id) as count from student s where ?<=create_time and create_time<=? group by speciality order by count desc";
        try {
            if(paraMap!=null && !paraMap.isEmpty()){
                Iterator<Map.Entry> it=paraMap.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry en=it.next();
                    para=(String)en.getKey();
                    paraObj=JSONObject.parseObject(para);
                    break;
                }
                JSONArray timeArray=paraObj.getJSONArray("time");
                if(timeArray!=null && !timeArray.isEmpty()) {
                    startTime = timeArray.getString(0)+" 00:00:00";
                    endTime = timeArray.getString(1)+" 23:59:59";
                }
            }
            /*
            专业为空的，自动修改为其他
             */
            Db.update("update student set speciality='10',speciality_name='其他' where speciality is null or speciality='' ");
            List<Record> list = Db.find(sql,startTime,endTime);
            List reList = new ArrayList();
            int studentTotal=0;
            for (Record r : list) {
                Object[] array = new Object[2];
                String shoolName = r.get("speciality_name");
                Object countObj = r.get("count");
                int count = NumberUtils.parseInt(countObj, 0);
                array[0] = shoolName;
                array[1] = count;
                studentTotal=studentTotal+count;
                reList.add(array);
            }
            if(reList!=null && !reList.isEmpty()){
                Object[] array=(Object[])reList.get(0);
                Map map=new HashMap();
                map.put("name",array[0]);
                map.put("y",array[1]);
                map.put("sliced",true);
                map.put("selected",true);
                reList.set(0,map);
            }

//            Map reMap = new HashMap();
//            reMap.put("data", reList);
            jhm.putCode(1);
            jhm.put("data",reList);
            jhm.put("total",studentTotal);
            renderJson(jhm);
        }catch(Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
            renderJson(jhm);
        }
    }
}
