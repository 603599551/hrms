package com.common.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import easy.util.DateTool;

/**
 * 生成订单编号的公共类
 */
public class OrderNumberGenerator extends BaseCtrl{
    /**
     * 补零长度
     */
    static final int LENGTH=4;
    /**
     * 仓库出库订单类型
     */
    static final String TYPE_CK="CK";
    /**
     * 门店采购订单类型
     */
    static final String TYPE_CD="CD";
    /**
     * 门店退货单类型
     */
    static final String TYPE_TD = "TD";
    /**
     * 生成出库单号
     * @return
     */
    public synchronized String getOutWarehouseOrderNumber(){
        String reStr="";
        String date= DateTool.GetDate();
        Record r=Db.findFirst("select * from order_number where type=?",TYPE_CK);
        if(r!=null){//如果有记录就继续判断
            String dateInR=r.getStr("date");
            if(date.equals(dateInR)){//如果日期相同
                int number=r.getInt("number");
                number++;
                reStr=getNumber(TYPE_CK,date,LENGTH,number);
                Db.update("update order_number set number=? where type=?",number,TYPE_CK);
            }else{//如果数据库中的日期不是当前系统日期
                Db.update("update order_number set date=?,number=? where type=?",date,1,TYPE_CK);
                reStr=getNumber(TYPE_CK,date,LENGTH,1);
            }
        }else{//如果没有记录就添加记录
            Record saveR=new Record();
            saveR.set("date",date);
            saveR.set("type",TYPE_CK);
            saveR.set("number",1);
            saveR.set("remark","出库订单号");

            Db.save("order_number",saveR);

            reStr=getNumber(TYPE_CK,date,LENGTH,1);
        }
        return reStr.replace("-","");
    }

    /**
     * 生成门店订单号
     * @return
     */
    public synchronized String getStoreOrderNumber(){
        String reStr="";
        String date= DateTool.GetDate();
        Record r=Db.findFirst("select * from order_number where type=?",TYPE_CD);
        if(r!=null){//如果有记录就继续判断
            String dateInR=r.getStr("date");
            if(date.equals(dateInR)){//如果日期相同
                int number=r.getInt("number");
                number++;
                reStr=getNumber(TYPE_CD,date,LENGTH,number);
                Db.update("update order_number set number=? where type=?",number,TYPE_CD);
            }else{//如果数据库中的日期不是当前系统日期
                Db.update("update order_number set date=?,number=? where type=?",date,1,TYPE_CD);
                reStr=getNumber(TYPE_CD,date,LENGTH,1);
            }
        }else{//如果没有记录就添加记录
            Record saveR=new Record();
            saveR.set("date",date);
            saveR.set("type",TYPE_CD);
            saveR.set("number",1);
            saveR.set("remark","门店采购订单号");

            Db.save("order_number",saveR);

            reStr=getNumber(TYPE_CD,date,LENGTH,1);
        }
        return reStr.replace("-","");
    }

    /**
     * 生成门店订单号
     * @return
     */
    public synchronized String getReturnOrderNumber(){
        String reStr="";
        String date= DateTool.GetDate();
        Record r=Db.findFirst("select * from order_number where type=?",TYPE_TD);
        if(r!=null){//如果有记录就继续判断
            String dateInR=r.getStr("date");
            if(date.equals(dateInR)){//如果日期相同
                int number=r.getInt("number");
                number++;
                reStr=getNumber(TYPE_TD,date,LENGTH,number);
                Db.update("update order_number set number=? where type=?",number,TYPE_TD);
            }else{//如果数据库中的日期不是当前系统日期
                Db.update("update order_number set date=?,number=? where type=?",date,1,TYPE_TD);
                reStr=getNumber(TYPE_TD,date,LENGTH,1);
            }
        }else{//如果没有记录就添加记录
            Record saveR=new Record();
            saveR.set("date",date);
            saveR.set("type",TYPE_TD);
            saveR.set("number",1);
            saveR.set("remark","门店采购订单号");

            Db.save("order_number",saveR);

            reStr=getNumber(TYPE_TD,date,LENGTH,1);
        }
        return reStr.replace("-","");
    }

    private static String getNumber(String type,String date,int length,int number){
        return type+date+String.format("%0"+LENGTH+"d", number);
    }
    public static void main(String[] args) {
        for(int i=1;i<=15000;i++) {
            String s = String.format("%04d", i);
            System.out.println(s);
        }
    }
}
