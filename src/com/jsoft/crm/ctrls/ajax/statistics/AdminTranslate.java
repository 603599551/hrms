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

public class AdminTranslate extends Controller {

    /**
     * 全部市场人员的转换率
     */
    public void queryPer(){
        String year=getPara("year");
        if(year==null || "".equalsIgnoreCase(year)){
            year=DateTool.getDate(new Date(),"yyyy");
        }
        String[] yearMonthArray=new String[12];
        for(int i=1;i<=12;i++){
            yearMonthArray[i-1]=year+String.format("-%02d",i);
        }
//        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        List<Map> mapList=new ArrayList();

        try{
            List<Record> staffList = Db.find("select id,name from staff where dept=?", "0401709b7b5e47d8825bf91233e5aadc");
            for(Record staffR:staffList) {
                String id=staffR.get("id");
                String realName=staffR.get("name");
                List<Record> list = Db.find("select * from translate_staff where staff_id=? and month like ?", id, year + "%");
//                Integer[] inputArray = new Integer[12];
//                Arrays.fill(inputArray, 0);
//                Integer[] signArray = new Integer[12];
//                Arrays.fill(signArray, 0);
                Double[] perArray=new Double[12];
                Arrays.fill(perArray, 0.0);
                int i = 0;
                for (Record r : list) {
                    String yearMonthClm = r.getStr("month");
                    Object beforeInputNumObj = r.get("before_input_num");
                    Object monthInputNumObj = r.get("month_input_num");
                    Object monthSignNumObj = r.get("month_sign_num");
                    for (i = 0; i < 12; i++) {
                        if (yearMonthArray[i].equals(yearMonthClm)) {
                            int beforeInputNum = NumberUtils.parseInt(beforeInputNumObj, 0);
                            int monthInputNum = NumberUtils.parseInt(monthInputNumObj, 0);
                            int monthSignNum = NumberUtils.parseInt(monthSignNumObj, 0);
                            int input = beforeInputNum + monthInputNum;
                            int sign = monthSignNum;
                            if(input!=0){
                                perArray[i]=(double)sign/(double)input*100;
                            }
                            break;
                        }
                    }
                }
                jhm.putCode(1);
                Map map = new HashMap();
                map.put("name", realName+"的学员转换率");
                map.put("data", perArray);
                mapList.add(map);
            }
            jhm.put("series",mapList);
            jhm.put("remark","转换率的计算方式：当月报名学员的数量/学员数量*100%<br/>学员数量：当月新录入的学员数量+之前未报名的学生数量<br/>");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 统计各销售员入库学生数
     */
    public void queryInputTotalBySaleman(){
        String year=getPara("year");
        if(year==null || "".equalsIgnoreCase(year)){
            year=DateTool.getDate(new Date(),"yyyy");
        }
        JsonHashMap jhm=new JsonHashMap();
        List dataList=new ArrayList();
        int total=0;
        try{
            List<Record> staffList = Db.find("select id,name from staff where dept=?", "0401709b7b5e47d8825bf91233e5aadc");
            for(Record staffR:staffList) {
                String id=staffR.get("id");
                String realName=staffR.get("name");
                Record r = Db.findFirst("select ifnull(sum(month_input_num),0) as sum from translate_staff where staff_id=? and month like ?", id, year + "%");
                Object[] array=new Object[2];
                array[0]=realName;
                if(r!=null){
                    Object sumObj = r.get("sum");
                    int sum=NumberUtils.parseInt(sumObj,0);
                    array[1]=sumObj;
                    total=total+sum;
                }
                dataList.add(array);

            }
            if(!dataList.isEmpty()){
                Object[] array=(Object[])dataList.get(0);
                Map map=new HashMap();
                map.put("sliced",true);
                map.put("name",array[0]);
                map.put("y",array[1]);
                map.put("selected",true);
                dataList.set(0,map);
            }
            jhm.putCode(1);
            jhm.put("total",total);
            jhm.put("data",dataList);
            jhm.put("remark","说明：统计今年入库学员数，不包含上一年遗留的学员");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 统计各销售员的学生报名数
     */
    public void querySignTotalBySaleman(){
        String year=getPara("year");
        if(year==null || "".equalsIgnoreCase(year)){
            year=DateTool.getDate(new Date(),"yyyy");
        }
        JsonHashMap jhm=new JsonHashMap();
        List dataList=new ArrayList();
        int total=0;
        try{
            List<Record> staffList = Db.find("select id,name from staff where dept=?", "0401709b7b5e47d8825bf91233e5aadc");
            for(Record staffR:staffList) {
                String id=staffR.get("id");
                String realName=staffR.get("name");
                Record r = Db.findFirst("select ifnull(sum(month_sign_num),0) as sum from translate_staff where staff_id=? and month like ?", id, year + "%");
                Object[] array=new Object[2];
                array[0]=realName;
                if(r!=null){
                    Object sumObj = r.get("sum");
                    int sum=NumberUtils.parseInt(sumObj,0);
                    array[1]=sumObj;
                    total=total+sum;
                }
                dataList.add(array);

            }
            if(!dataList.isEmpty()){
                Object[] array=(Object[])dataList.get(0);
                Map map=new HashMap();
                map.put("sliced",true);
                map.put("name",array[0]);
                map.put("y",array[1]);
                map.put("selected",true);
                dataList.set(0,map);
            }
            jhm.putCode(1);
            jhm.put("total",total);
            jhm.put("data",dataList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
