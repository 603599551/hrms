package com.jsoft.crm.ctrls.ajax.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.Config;
import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jsoft.crm.bean.UserBean;
import com.jsoft.crm.timer.MyTranslateTimer;
import com.jsoft.crm.utils.UserSessionUtil;
import utils.DateTool;
import utils.NumberUtils;
import utils.bean.JsonHashMap;

import java.util.*;

/**
 * 我的转换率统计
 */
public class MyTranslateCtrl extends Controller {
    private String[] getMonthArray(){
        int length=12;
        String[] reArray=new String[length];
        String year=DateTool.GetDate().substring(0,4);
        for(int i=0;i<length;i++){
            reArray[i]=year+String.format("-%02d",(i+1));
        }

        return reArray;
    }

    /**
     * 显示当前登陆人1-12月每个月入库学员数、报名学员数
     */
    public void index(){
        String year=getPara("year");
        if(year==null || "".equalsIgnoreCase(year)){
            year=DateTool.getDate(new Date(),"yyyy");
        }
        String[] yearMonthArray=new String[12];
        for(int i=1;i<=12;i++){
            yearMonthArray[i-1]=year+String.format("-%02d",i);
        }
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select * from translate_staff where staff_id=? and month like ?",usu.getUserId(),year+"%");
            Integer[] inputArray=new Integer[12];
            Arrays.fill(inputArray,0);
            Integer[] signArray=new Integer[12];
            int i=0;
            int inputTotal=0;
            int signTotal=0;
            for(Record r:list) {
                String yearMonthClm = r.getStr("month");
                Object beforeInputNumObj = r.get("before_input_num");
                Object monthInputNumObj = r.get("month_input_num");
                Object monthSignNumObj = r.get("month_sign_num");
                for (i = 0; i < 12; i++) {
//                Record r=list.get(i);

                    if (yearMonthArray[i].equals(yearMonthClm)) {
                        int beforeInputNum = NumberUtils.parseInt(beforeInputNumObj, 0);
                        int monthInputNum = NumberUtils.parseInt(monthInputNumObj, 0);
                        int monthSignNum = NumberUtils.parseInt(monthSignNumObj, 0);
                        inputArray[i] = beforeInputNum + monthInputNum;
                        signArray[i] = monthSignNum;
                        inputTotal=inputTotal+inputArray[i];
                        signTotal=signTotal+signArray[i];

                        break;
                    }
                }
            }
            jhm.putCode(1);
            Map[] mapArray=new HashMap[2];
            mapArray[0]=new HashMap();
            mapArray[0].put("name","学员数量");
            mapArray[0].put("data",inputArray);

            mapArray[1]=new HashMap();
            mapArray[1].put("name","当月报名数量");
            mapArray[1].put("data",signArray);

            jhm.put("inputTotal",inputTotal);
            jhm.put("signTotal",signTotal);
            jhm.put("series",mapArray);
            jhm.put("remark","学员数量：当月新录入的学员数量+之前未报名的学生数量<br/>");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    /**
     * 显示当前登陆人1-12月的转化率
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
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        JsonHashMap jhm=new JsonHashMap();
        try{
            List<Record> list=Db.find("select * from translate_staff where staff_id=? and month like ?",usu.getUserId(),year+"%");
            Double[] perArray=new Double[12];
            Arrays.fill(perArray,0.0);
            int i=0;
            int inputTotal=0;
            int signTotal=0;
            for(Record r:list) {
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
                        int sign= monthSignNum;
                        inputTotal=inputTotal+input;
                        signTotal=signTotal+sign;

                        if(input!=0){
                            double per=(double)sign/(double)input*100;
                            perArray[i]=NumberUtils.getMoney(per);
                        }
                        break;
                    }
                }
            }
            jhm.putCode(1);
            Map[] mapArray=new HashMap[1];
            mapArray[0]=new HashMap();
            mapArray[0].put("name","转化率");
            mapArray[0].put("data",perArray);

            double perTotal=0;
            if(inputTotal!=0){
                perTotal=(double)signTotal/(double)inputTotal*100;
                perTotal=NumberUtils.getMoney(perTotal);
            }
            jhm.put("perTotal",perTotal);
            jhm.put("series",mapArray);
            jhm.put("remark","转换率的计算方式：当月报名学员的数量/学员数量*100%<br/>学员数量：当月新录入的学员数量+之前未报名的学生数量<br/>");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void build(){
        MyTranslateTimer mtt=new MyTranslateTimer();
        JsonHashMap jhm=new JsonHashMap();
        try {
            mtt.exe();
            jhm.putCode(1);
            jhm.putMessage("执行成功！");
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1);
            jhm.putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
