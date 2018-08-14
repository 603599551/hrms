package com.common.service;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.DateTool;

/**
 * 生成员工工号的公共类
 */
public class StaffNumberGenerator extends BaseCtrl {
    /**
     * 补零长度
     */
    static final int LENGTH=4;

    /**
     * 员工工号
     */
    static final String TYPE_GH="GH";

    /**
     * 生成员工工号
     * @return
     */
    public static synchronized String getStaffOrderNumber(){
        String reStr="";
        String date= DateTool.GetDate();

        try {
            Record r=Db.findFirst("select * from h_staff_number where type=?",TYPE_GH);
            if(r!=null){//如果有记录就继续判断
                String dateInR=r.getStr("date");
                if(date.equals(dateInR)){//如果日期相同
                    int number=r.getInt("number");
                    number++;
                    reStr=getNumber(TYPE_GH,date,LENGTH,number);
                    Db.update("update h_staff_number set number=? where type=?",number,TYPE_GH);
                }else{//如果数据库中的日期不是当前系统日期
                    Db.update("update h_staff_number set date=?,number=? where type=?",date,1,TYPE_GH);
                    reStr=getNumber(TYPE_GH,date,LENGTH,1);
                }
            }else{//如果没有记录就添加记录
                Record saveR=new Record();
                saveR.set("date",date);
                saveR.set("type",TYPE_GH);
                saveR.set("number",1);
                saveR.set("remark","员工工号");
                Db.save("h_staff_number",saveR);
                reStr=getNumber(TYPE_GH,date,LENGTH,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reStr.replace("-","");
    }
    private static String getNumber(String type,String date,int length,int number){
        return type+date+String.format("%0"+LENGTH+"d", number);
    }

}
