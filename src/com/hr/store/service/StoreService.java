package com.hr.store.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.HanyuPinyinHelper;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.jfinal.DbUtil;

public class StoreService extends BaseService{

    /**
     * 新增门店
     * @param store 前台获取的数据
     * @param usu 当前登录信息
     * @throws Exception
     */
    @Before(Tx.class)//添加事务管理
    public void add(Record store, UserSessionUtil usu) throws Exception{
        //添加数据库里其他字段需要的数据
        store.set("id", UUIDTool.getUUID());//获取主键（UUID）的通用方法
        store.set("status", 1);//根据业务了解门店的status=1时表示启用的门店，新增门店默认启用状态
        store.set("creater_id", usu.getUserId());
        store.set("modifier_id", usu.getUserId());
        String time = DateTool.GetDateTime();//获取时间的通用方法，yyyy-MM-dd HH:mm:ss   这个类中也有其他格式的获取方法
        store.set("create_time", time);
        store.set("modify_time", time);
        //添加名称的拼音头
        store.set("pinyin", HanyuPinyinHelper.getFirstLettersLo(store.getStr("name")));

        //为了客户体验，每个门店都有自己的颜色，从h_store_color表中查找，状态为0的表示已经用过的颜色了，所有查询第一条status!=0的数据
        Record storeColor = Db.findFirst("select * from h_store_color where status<> 0 order by sort");
        storeColor.set("status", 0);
        //因为已经选用了这个颜色，所有要将这个颜色的status修改成0
        Db.update("h_store_color", storeColor);

        //获取下一个排序数字
        int sort = nextSort(DbUtil.queryMax("h_store","sort"));

        store.set("store_color", storeColor.getStr("color"));
        store.set("sort", sort);

        //保存数据到数据库
        Db.save("h_store", store);
    }

}
