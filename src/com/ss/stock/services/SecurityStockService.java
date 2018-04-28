package com.ss.stock.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityStockService implements Constants {

    private static SecurityStockService me = new SecurityStockService();

    public static SecurityStockService getMe(){
        return me;
    }

    public static Record getSecurityStock(String materialId){
        return getSecurityStockMap().get(materialId);
    }

    public static List<Record> getSecurityStockList(){
        List<Record> result = null;
        Date currentDate = new Date();
        String oneDayBeforeTime = sdf.format(new Date(currentDate.getTime() - ONE_DAY_TIME));
        String towDayBeforeTime = sdf.format(new Date(currentDate.getTime() - 2 * ONE_DAY_TIME));
        String threeDayBeforeTime = sdf.format(new Date(currentDate.getTime() - 3 * ONE_DAY_TIME));
        String sqlStoreOrder = "select * from store_order so where want_date in (?,?,?) order by want_date";
        List<Record> storeOrderList = Db.find(sqlStoreOrder, oneDayBeforeTime, towDayBeforeTime, threeDayBeforeTime);
        if(storeOrderList != null && storeOrderList.size() > 0){
            String sql = "select sum(som.want_num) sum_want_num, som.material_id from store_order_material som where som.store_order_id in (";
            int size = 0;
            String time = "";
            for(Record r : storeOrderList){
                sql += "'" + r.get("id") + "',";
                if(!time.equals(r.getStr("want_date"))){
                    size ++;
                    time = r.getStr("want_date");
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ") group by som.material_id ";
            result = Db.find(sql);
            for(Record r : result){
                double oneDayNum = 0;
                if(size > 0){
                    oneDayNum = r.getDouble("sum_want_num") / size;
                }
                r.set("oneDayNum", oneDayNum);
                r.set("security_stock", oneDayNum * 7);
            }
        }
        return result;
    }

    public static Map<String, Record> getSecurityStockMap(){
        Map<String, Record> result = new HashMap<>();
        List<Record> list = getSecurityStockList();
        if(list != null && list.size() > 0){
            for(Record r : list){
                result.put(r.getStr("material_id"), r);
            }
        }
        return result;
    }

}
