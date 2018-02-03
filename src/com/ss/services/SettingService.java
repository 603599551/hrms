package com.ss.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;

public class SettingService {
    public static SettingService me=new SettingService();
    public int getGoodsNum(){
        int i=0;
        Object obj=Db.queryFirst("select value_int from setting where `key`=?","goods_num");
        i= NumberUtils.parseInt(obj,1);
        return i;
    }
    public void addGoodsNum(){
        int i=Db.update("update setting set value_int=value_int+1 where key=?","goods_num");
    }
}
