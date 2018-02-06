package com.ss.stock.controllers.services;

import com.bean.UserBean;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class DailySummaryService {

    private static DailySummaryService me = new DailySummaryService();
    private static String TIME_VISION = "";

    private static Map<String, Map<String, Object>> dataMap;
    private static Map<String, Record> storeMap;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private DailySummaryService(){
        super();
        dataMap = new HashMap<>();
        storeMap = new HashMap<>();
        init();
    }

    /**
     * 初始化方法
     * 完成三个初始化：1、生成时间版本号。2、查询商品相关数据。3、查询门店数据
     *
     * 1：初始化时获取当前时间存放在TIME_VISION中，用来判断下次访问时是否需要重新查询数据，减少查询量
     *        如果下次访问的时间和实际版本号相同，则表示这次操作和上次操作在同一天，不需要重新缓存数据
     * 2：查询三张表的数据：goods、material， goods_material
     *    将后期要用到的数据整理到static的dataMap中
     *
     *    dataMap结构：key：商品编号， value：商品数据map
     *    商品数据map结构：key：materialList， value：原材料List（关联material表查询出的数据）
     *                     key：data，         value：商品具体数据(goods表的数据)
     *
     * 3：将门店数据存放到storeMap中
     *    storeMap结构：key：门店名称， value：门店具体数据（store表的数据）
     *
     */
    private void init(){
        //初始化时获取当前时间存放在TIME_VISION中，用来判断下次访问时是否需要重新查询数据
        TIME_VISION = DateTool.GetDate();
        //编写sql语句
        StringBuffer sql = new StringBuffer();
        sql.append("select gt.id        gid,");
        sql.append("gt.code             gcode,");
        sql.append("gt.name             gname,");
        sql.append("gt.pinyin           gpinyin,");
        sql.append("gt.wn_type          gwn_type,");
        sql.append("gt.attr_1           gattr_1,");
        sql.append("gt.attr_2           gattr_2,");
        sql.append("gt.unit             gunit,");
        sql.append("gt.sort             gsort,");
        sql.append("gt.type_1           gtype_1,");
        sql.append("gt.type_2           gtype_2,");

        sql.append("m.id                mid,");
        sql.append("m.code              mcode,");
        sql.append("m.name              mname,");
        sql.append("m.pinyin            mpinyin,");
        sql.append("m.yield_rate        myield_rate,");
        sql.append("m.purchase_price    mpurchase_price,");
        sql.append("m.balance_price     mbalance_price,");
        sql.append("m.stock_type        mstock_type,");
        sql.append("m.attr_1            mattr_1,");
        sql.append("m.attr_2            mattr_2,");
        sql.append("m.unit              munit,");
        sql.append("m.sort              msort,");
        sql.append("m.type_1            mtype_1,");
        sql.append("m.type_2            mtype_2,");

        sql.append("gm.id               gmid,");
        sql.append("gm.net_num          gmnet_num,");
        sql.append("gm.gross_num        gmgross_num");

        sql.append("from goods gt,");
        sql.append("        (select max(modify_time) modify_time, code from goods GROUP BY code) a,");
        sql.append("        goods_material gm,");
        sql.append("        material m");
        sql.append("where gt.code = a.code");
        sql.append("and gt.modify_time = a.modify_time");
        sql.append("and gt.id = gm.goods_id");
        sql.append("and m.id = gm.material_id;");

        List<Record> list = Db.find(sql.toString());
        if(list != null && list.size() > 0){
            for(Record r : list){
                Map<String, Object> goodsCodeMap = dataMap.get(r.getStr("gcode"));
                if(goodsCodeMap == null){
                    goodsCodeMap = new HashMap<>();
                    dataMap.put(r.getStr("gcode"), goodsCodeMap);
                }
                List<Record> materialList = (List<Record>) goodsCodeMap.get("materialList");
                if(materialList == null){
                    materialList = new ArrayList<>();
                    goodsCodeMap.put("materialList", materialList);
                }
                materialList.add(r);
                goodsCodeMap.put(r.getStr("data"), r);
            }
        }

        List<Record> storeList = Db.find("select * from store");
        if(storeList != null && storeList.size() > 0){
            for(Record r : storeList){
                storeMap.put(r.getStr("name"), r);
            }
        }
    }

    /**
     * 获取DailySummaryService对象
     * 根据当前时间和时间版本号（TIME_VISION）是否相同判断是否需要初始化缓存数据
     * @return DailySummaryService对象
     */
    public static DailySummaryService getMe(){
        boolean initFlag = TIME_VISION.equals(DateTool.GetDate());
        if(!initFlag){
            me = new DailySummaryService();
        }
        return me;
    }

    /**
     * 验证daily_summary表数据，通过store_id和imp_time字段查询
     * 如果查到数据，表示已经导入过数据，重新导入的数据要覆盖掉曾经导入的数据
     * 如果没有查到数据，表示第一次导入，不需要删除数据
     *
     * 需要删除的数据：
     * daily_summary 主键删除
     * imp_daily_summary 关联daily_summary_id字段删除
     * sale_goods 关联daily_summary_id字段删除
     * sale_goods_material 关联daily_summary_id字段删除
     *
     * @param userBean
     * @param sale_time
     */
    @Before(Tx.class)
    public void deleteAllData(UserBean userBean, String sale_time){
        List<Record> dailySummaryList = Db.find("select * from daily_summary where store_id=? and imp_time=?", userBean.get("store_id"), sale_time);
        if(dailySummaryList != null && dailySummaryList.size() > 0){
            String daily_summary_id = dailySummaryList.get(0).getStr("id");
            //daily_summary 主键删除
            Db.deleteById("daily_summary", daily_summary_id);
            //imp_daily_summary 关联daily_summary_id字段删除
            Db.delete("delete from imp_daily_summary where daily_summary_id=?", daily_summary_id);
            //sale_goods 关联daily_summary_id字段删除
            Db.delete("delete from sale_goods where daily_summary_id=?", daily_summary_id);
            //sale_goods_material 关联daily_summary_id字段删除
            Db.delete("delete from sale_goods_material where daily_summary_id=?", daily_summary_id);
        }
    }

    /**
     * 保存需要所有安存相关数据，暂时包括：
     * sale_goods
     * sale_goods_material
     * imp_daily_summary
     * daily_summary
     *
     * @param impDailySummaryList xls导入的每日汇总数据
     * @param sale_time 前台页面填写的数据对应的时间
     * @param userBean 登录用户信息
     */
    @Before(Tx.class)
    public void saveAllData(List<Record> impDailySummaryList, UserBean userBean, String sale_time){
        //删除多余数据，具体删除规则详见deleteAllData方法注释
        deleteAllData(userBean, sale_time);
        String time= DateTool.GetDateTime();
        List<Record> saleGoodsList = new ArrayList<>();
        List<Record> saleGoodsMaterialList = new ArrayList<>();
        String daily_summary_id = UUIDTool.getUUID();
        //根据xls导入的数据整理要添加数据的四张表数据
        if(impDailySummaryList != null && impDailySummaryList.size() > 0){
            for(Record r : impDailySummaryList){
                Map<String, Object> goodsCodeMap = dataMap.get(r.getStr("code"));
                //从缓存中获取数据
                List<Record> goodsMaterialList = (List<Record>) goodsCodeMap.get("materialList");
                Record goodsCode = (Record) goodsCodeMap.get("data");
                //创建销售商品数据，详情见createSaleGoods注释
                Record saleGoods = createSaleGoods(goodsCode, userBean, time, sale_time, daily_summary_id);
                saleGoodsList.add(saleGoods);
                //创建销售商品原来数据，详情见createSaleGoodsMaterial注释
                createSaleGoodsMaterial(goodsMaterialList, saleGoodsMaterialList, userBean, saleGoods.getStr("id"), time, sale_time, daily_summary_id);
            }
            Record daily_summary = createDailySummary(saleGoodsMaterialList, daily_summary_id, userBean, time, sale_time);
            //保存所有需要保存的数据
            saveData(impDailySummaryList, saleGoodsList, saleGoodsMaterialList, daily_summary);
        }
    }

    /**
     * 保存四张表的数据
     * @param impDailySummaryList 导入的每日汇总数据
     * @param saleGoodsList 销售商品数据
     * @param saleGoodsMaterialList 销售商品原材料数据
     * @param daily_summary 每日汇总数据总表
     */
    private void saveData(List<Record> impDailySummaryList, List<Record> saleGoodsList, List<Record> saleGoodsMaterialList, Record daily_summary){
        //添加daily_summary表的主键数据，建立多对一的关联关系
        for(Record r : impDailySummaryList){
            r.set("daily_summary_id", daily_summary.getStr("id"));
        }
        //保存销售数据的总表数据
        Db.save("daily_summary", daily_summary);
        //保存导入的原始数据
        Db.batchSave("imp_daily_summary", impDailySummaryList, impDailySummaryList.size());
        //保存销售商品数据
        Db.batchSave("sale_goods", saleGoodsList, saleGoodsList.size());
        //保存销售商品原材料数据
        Db.batchSave("sale_goods_material", saleGoodsMaterialList, saleGoodsMaterialList.size());
    }

    /**
     * 创建每日汇总的总表数据
     * 每个门店导入一个销售数据就要在daily_summary表类中添加一条数据，这个表存放导入的基本信息和统计处理之后的数据
     * @param saleGoodsMaterialList 销售商品原料数据
     * @param daily_summary_id 总表的主键： 因为其他表也都用到这个id所以在方法外生成，以参数的形式传入
     * @param userBean 登录用户了数据
     * @param time 当前时间： 其他表也要用到时间，因为都在同一个业务逻辑中，所以时间在外部生成，以参数的形式传入
     * @param sale_time 销售时间： 前台用户选择的时间
     * @return 每日汇总总表对象
     */
    private Record createDailySummary(List<Record> saleGoodsMaterialList, String daily_summary_id, UserBean userBean, String time, String sale_time){
        Record daily_summary = new Record();
        daily_summary.set("id", daily_summary_id);
        daily_summary.set("store_id",userBean.get("store_id"));
        daily_summary.set("creater_id",userBean.get("id"));
        daily_summary.set("modifier_id",userBean.get("id"));
        daily_summary.set("create_time",time);
        daily_summary.set("modify_time",time);
        daily_summary.set("imp_time",sale_time);
        daily_summary.set("statistic", statisticDailySummary(saleGoodsMaterialList));
        return daily_summary;
    }

    /**
     * 统计每日数据
     * @param saleGoodsMaterialList 销售商品原材料数据
     * @return 统计好的数据：以json的格式返回字符串
     */
    private String statisticDailySummary(List<Record> saleGoodsMaterialList){
        //按照material_id分组整理数据
        Map<String, List<Record>> sgmMap = new HashMap<>();
        for(Record r : saleGoodsMaterialList){
            List<Record> sgmList = sgmMap.get(r.getStr("material_id"));
            if(sgmList != null){
                sgmList.add(r);
            }else{
                sgmList = new ArrayList<>();
                sgmMap.put(r.getStr("material_id"), sgmList);
                sgmList.add(r);
            }
        }
        //整理导入数据统计用量，存放到daily_summary表中，方便数据分析
        //整理的数据以json的形式存放在统计字段：statistic中
        Map<String, Record> dailySummaryMap = new HashMap<>();
        for(Entry<String, List<Record>> entry : sgmMap.entrySet()){
            String material_id = entry.getKey();
            List<Record> recordList = entry.getValue();
            double net_num = 0.0;
            double gross_num = 0.0;
            for(Record r : recordList){
                net_num += getDouble(r.getStr("net_num"));
                gross_num += getDouble(r.getStr("gross_num"));
            }
            Record sgmRecord = recordList.get(0);
            Record r = new Record();
            dailySummaryMap.put(material_id, r);
            r.set("material_id", material_id);
            r.set("code", sgmRecord.getStr("code"));
            r.set("name", sgmRecord.getStr("name"));
            r.set("yield_rate", sgmRecord.getStr("yield_rate"));
            r.set("purchase_price", sgmRecord.getStr("purchase_price"));
            r.set("balance_price", sgmRecord.getStr("balance_price"));
            r.set("stock_type", sgmRecord.getStr("stock_type"));
            r.set("unit", sgmRecord.getStr("nuit"));
            r.set("net_num", net_num);
            r.set("gross_num", gross_num);
        }
        return JSONObject.fromObject(dailySummaryMap).toString();
    }

    /**
     * 创建sale_goods表数据
     * @param goods 商品详细数据
     * @param userBean 登录用户数据
     * @param time 当前时间： 其他表也要用到时间，因为都在同一个业务逻辑中，所以时间在外部生成，以参数的形式传入
     * @param sale_time 销售时间： 前台用户选择的时间
     * @param daily_summary_id 总表的主键： 因为其他表也都用到这个id所以在方法外生成，以参数的形式传入
     * @return sale_goods表数据对象
     */
    private Record createSaleGoods(Record goods, UserBean userBean, String time, String sale_time, String daily_summary_id){
        String saleGoodsId = UUIDTool.getUUID();
        Record saleGoods = new Record();
        saleGoods.set("id",saleGoodsId);
        saleGoods.set("goods_id",goods.getStr("gid"));
        saleGoods.set("store_id",userBean.get("store_id"));
        saleGoods.set("code",goods.getStr("gcode"));
        saleGoods.set("name",goods.getStr("gname"));
        saleGoods.set("pinyin",goods.getStr("gpinyin"));
        saleGoods.set("price",goods.getStr("gprice"));
        saleGoods.set("stock_type",goods.getStr("gwn_type"));
        saleGoods.set("attr_1",goods.getStr("gattr_1"));
        saleGoods.set("attr_2",goods.getStr("gattr_2"));
        saleGoods.set("unit",goods.getStr("gunit"));
        saleGoods.set("sort",goods.getStr("gsort"));
        saleGoods.set("type_1",goods.getStr("gtype_1"));
        saleGoods.set("type_2",goods.getStr("gtype_2"));
        saleGoods.set("creater_id",userBean.getId());
        saleGoods.set("modifier_id",userBean.getId());
        saleGoods.set("create_time",time);
        saleGoods.set("modify_time",time);
        saleGoods.set("sale_time",sale_time);

        //添加daily_summary表的主键数据，建立多对一的关联关系
        saleGoods.set("daily_summary_id", daily_summary_id);
        return saleGoods;
    }

    /**
     * 创建商品销售原材料所有数据
     * 将数据存放在saleGoodsMaterialList中，然后通过批量新增添加到数据库中
     * @param goodsMaterialList 商品原材料数据
     * @param saleGoodsMaterialList 销售商品原材料总集合  因为一个商品对应多种原材料，所以用这个集合将多个商品的原材料保存起来，方便批量新增
     * @param userBean 登录用户数据
     * @param saleGoodsId 销售商品数据主键
     * @param time 当前时间： 其他表也要用到时间，因为都在同一个业务逻辑中，所以时间在外部生成，以参数的形式传入
     * @param sale_time 销售时间： 前台用户选择的时间
     * @param daily_summary_id 总表的主键： 因为其他表也都用到这个id所以在方法外生成，以参数的形式传入
     */
    private void createSaleGoodsMaterial(List<Record> goodsMaterialList, List<Record> saleGoodsMaterialList, UserBean userBean, String saleGoodsId, String time, String sale_time, String daily_summary_id){
        if(goodsMaterialList != null && goodsMaterialList.size() > 0){
            for(Record gcR : goodsMaterialList){
                Record saleGoodsMaterial = new Record();
                saleGoodsMaterial.set("id",UUIDTool.getUUID());
                saleGoodsMaterial.set("store_id",userBean.get("store_id"));
                saleGoodsMaterial.set("material_id",gcR.getStr("mid"));
                saleGoodsMaterial.set("sale_goods_id",saleGoodsId);
                saleGoodsMaterial.set("code",gcR.getStr("mcode"));
                saleGoodsMaterial.set("name",gcR.getStr("mname"));
                saleGoodsMaterial.set("pinyin",gcR.getStr("mpinyin"));
                saleGoodsMaterial.set("yield_rate",gcR.getStr("myield_rate"));
                saleGoodsMaterial.set("purchase_price",gcR.getStr("mpurchase_price"));
                saleGoodsMaterial.set("balance_price",gcR.getStr("mbalance_price"));
                saleGoodsMaterial.set("stock_type",gcR.getStr("mstock_type"));

                saleGoodsMaterial.set("attr_1",gcR.getStr("mattr_1"));
                saleGoodsMaterial.set("attr_2",gcR.getStr("mattr_2"));
                saleGoodsMaterial.set("unit",gcR.getStr("munit"));
                saleGoodsMaterial.set("sort",gcR.getStr("msort"));
                saleGoodsMaterial.set("type_1",gcR.getStr("mtype_1"));
                saleGoodsMaterial.set("type_2",gcR.getStr("mtype_2"));
                saleGoodsMaterial.set("net_num",gcR.getStr("gmnet_num"));
                saleGoodsMaterial.set("gross_num",gcR.getStr("gmgross_num"));

                //todo 暂时用毛料数量乘以采购价格
                saleGoodsMaterial.set("total_price",multiplyStr(gcR.getStr("gmgross_num"), gcR.getStr("mpurchase_price")));

                saleGoodsMaterial.set("creater_id",userBean.getId());
                saleGoodsMaterial.set("modifier_id",userBean.getId());
                saleGoodsMaterial.set("create_time",time);
                saleGoodsMaterial.set("modify_time",time);
                saleGoodsMaterial.set("sale_time",sale_time);

                //添加daily_summary表的主键数据，建立多对一的关联关系
                saleGoodsMaterial.set("daily_summary_id", daily_summary_id);

                saleGoodsMaterialList.add(saleGoodsMaterial);
            }
        }
    }

    /**
     * 返回double数据类型的数据
     * 如果参数为空，或者不能转换成double类型的数据返回0.0，其他正常返回
     * @param obj 形参
     * @return double数据
     */
    private double getDouble(Object obj){
        if(obj == null){
            return 0.0;
        }
        if(obj instanceof  Double){
            return (double)obj;
        }
        try{
            return new Double(obj.toString());
        }catch (Exception e){
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 两个double相乘
     * @param a
     * @param b
     * @return
     */
    private double multiplyStr(String a, String b){
        try{
            return getDouble(a) * getDouble(b);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 安存预算
     *
     * 1、查询daily_summary表中imp_time之前10天和去年同期的数据
     * 2、如果查询到数据，那么表示各个门店数据录入正常，如果没有查到数据表示没有任何用来推算的相关数据
     * 3、调用createMaterialStatistic方法整理数据，具体请参照createMaterialStatistic方法注释
     *
     *
     * @param imp_time 要预算的时间
     * @return
     */
    public Map<String, Object> securityStockBudget_bakkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk(String imp_time) throws Exception{
        Map<String, Object> result = new HashMap<>();
        String[] timeArr = getAllTime(imp_time);
        List<Record> dailySummaryList = Db.find("select * from daily_summary where imp_time in (?,?,?,?,?,?,?,?,?,?,?)", timeArr);
        if(dailySummaryList != null && dailySummaryList.size() > 0){
            //整理查询数据
//            Map<String, Object> map = createMaterialStatistic(dailySummaryList);
            Map<String, Object> map = new HashMap<>();
            //获取所有用到的原料id
            List<String> materialIdList = new ArrayList<>((Set<String>) map.get("materialIdSet"));
            //排序原料，保证顺序
            Collections.sort(materialIdList);
            Map<String, List<Map<String, Record>>> dataMap = (Map<String, List<Map<String, Record>>>) map.get("resultMap");
            //获取原料和名称的map，方法展示。key：原料id  value：原料名称
            Map<String, String> materialNameMap = (Map<String, String>) map.get("materialNameMap");
            //要返回的tbody数据，外层List表示所有行，内层List表示一行中的所有数据，对应了十天前和去年同期数据。
            List<List<String>> resultList = new ArrayList<>();
            for(int i = 0; i < materialIdList.size(); i++){
                List<String> list = new ArrayList<>();
                resultList.add(list);
            }
            /*
            有多少种原料就要有多少行数据，所以外层循环是按照原料种类循环
            有多少天就有多少列数据，所以内层循环按照时间循环
                预算需要前十天10列数据，去年同期1列数据，还要显示预算的1列数据，所以一共有10+1+1=12列数据
            预算公式：（前十天数据总和/天数 + 去年同期数据）/ 2
                因为系统刚开始用的时候没有历史数据，那么很有可能前10天的数据没有，或者只有几天的，那么天数只算有数据的天数
                去年同期数据也存在暂时没有数据的情况，如果去年同期没有数据，那么不会加去年同期除以2了，只算前十天的平均值

             */
            //外层循环，所有原料
            for(int i = 0; i < materialIdList.size(); i++){
                String materialId = materialIdList.get(i);
                //当前商品的前十天数据总和
                double before10Day = 0.0;
                //如果前十天数据没有，这个变量不会变化，这样保证没有数据的情况不会算到平均值中
                int before10DayNum = 0;
                //去年同期数据
                double lastYear = 0.0;
                //是否存在去年同期数据，如果不存在，那么没有必要把去年同期数据计算进来
                boolean lastYearFlag = false;
                //当前原料单位
                String unit = "";
                //内层循环，原料消耗时间
                for(int timeArrIndex = 0; timeArrIndex < timeArr.length; timeArrIndex++){
                    String time = timeArr[timeArrIndex];
                    List<Map<String, Record>> list = dataMap.get(time);
                    for(Map<String, Record> m : list){
                        String total = m.get(materialId).getStr("total_net_num");
                        //如果当前原料没有消耗，显示无用量
                        if(total == null || "0.0".equals(total)){
                            total = "无用量";
                        }else{
                            //当timeArrIndex==10表示去年同期数据
                            if(timeArrIndex == 10){
                                before10Day += getDouble(total);
                                before10DayNum++;
                            }else{
                                lastYear = getDouble(total);
                                lastYearFlag = true;
                            }
                        }
                        unit = m.get(materialId).getStr("unit");
                        resultList.get(i).add(total + unit);
                    }
                }
                resultList.get(i).add(0, materialNameMap.get(materialId));
                double budget = 0.0;
                budget = before10Day / before10DayNum + lastYear;
                if(lastYearFlag){
                    budget /= 2;
                }
                BigDecimal b = new BigDecimal(budget);
                budget = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                resultList.get(i).add(budget + unit);
            }
            List<String> titleList = new ArrayList<>();
            titleList.add("");
            for(int i = 0; i < timeArr.length - 1; i++){
                titleList.add(timeArr[i]);
            }
            titleList.add("去年同期");
            titleList.add("今日预估");
            result.put("titleList", titleList);
            result.put("data", resultList);
        }else{
            throw new Exception("没有历史数据！");
        }
        return result;
    }

    /**
     * 通过传入时间，退出前十天和去年同期日期
     * @param time 传入时间 yyyy-MM-dd
     * @return 推出的时间数组 yyyy-MM-dd，长度为11
     */
    private static String[] getAllTime(String time){
        String[] result = new String[11];
        try {
            Date date = df.parse(time);
            long dateLong = date.getTime();
            //一天的毫秒数
            long oneDayLong = 1000 * 60 * 60 * 24;
            //前十天的时间
            for(int i = 1; i <= 10; i++){
                long timeLong = dateLong - (i * oneDayLong);
                result[10 - i] = df.format(new Date(timeLong));
            }
            //去年同期时间
            String y = time.split("-")[0];
            String m = time.split("-")[1];
            String d = time.split("-")[2];
            //去除非闰年没有29日问题
            if("02".equals(m) && "29".equals(d)){
                d = "28";
            }
            result[10] = (new Integer(y) - 1) + "-" + m + "-" + d;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 整理统计表的数据
     * @return Map<时间, List<Map<原料id, 原料具体数据>>>
     *     statistic
     */
    //public Map<String, List<Map<String, Record>>> createMaterialStatistic(List<Record> dailySummaryList){
    public Map<String, Object> createMaterialStatistic_bakkkkkkkkkkkkkkkkkkkkkkkkkk(List<Record> dailySummaryList){
        Map<String, Object> result = new HashMap<>();
        //Map<时间, 所有原料集合List<Map<原料id, 原料具体数据>>>
        Map<String, List<Map<String, Record>>> resultMap = new HashMap<>();
        //Map<时间, 所有门店消耗List<每个门店数据>>
        Map<String, List<Record>> timeMap = new HashMap<>();
        //所有原材料id，去掉重复，所以用set
        Set<String> materialIdSet = new HashSet<>();
        //所有原材料id和名称数据，key：material_id  value：material_name
        Map<String, String> materialNameMap = new HashMap<>();
        //循环所有daily_summary表中数据
        for(Record r : dailySummaryList){
            //
            //获取每日统计数据，从daily_summary表的statistic字段中获取，json格式字符串，转化成对象
            //json格式：
            //          key                         value
            //          material_id                 原材料id
            //          name                        原材料名称
            //          unit                        原材料单位（克、罐等）
            //          net_num                     原料净量
            //
            //
            //
            //
            //
            //
            JSONObject jsonObject = JSONObject.fromObject(r.getStr("statistic"));
            //将json字符串转化成对象放到r中，key：json
            r.set("json", jsonObject);
            //添加所有原料id到materialIdSet中
            materialIdSet.addAll(jsonObject.keySet());
            /*
             * 因为有多个门店，第一步先按照时间分组，将十天和去年同期的数据整理一下，放到一个map中（timeMap）
             * key：时间
             * value：daily_summary表中对应时间（key值）的多家门店数据集合（List）
             */
            //获取对应当前这条统计记录的时间下集合
            List<Record> list = timeMap.get(r.getStr("imp_time"));
            //如果为空，表示还没有获取到这个时间的数据，那么创建一个集合，放到timeMap中
            if(list == null){
                list = new ArrayList<>();
                timeMap.put(r.getStr("imp_time"), list);
            }
            //将这条记录放到集合中
            list.add(r);
        }
        /*
        按照时间分组处理所有数据
         */
        //遍历timeMap， e.key：时间  e.value：这个时间对应的每家门店数据集合
        for(Entry<String, List<Record>> e : timeMap.entrySet()){
            //每家门店数据集合
            List<Record> list = e.getValue();
            //为了操作方便，将每个门店的json存放到一个集合中
            List<JSONObject> jsonObjectList = new ArrayList<>();
            for(Record r : list){
                jsonObjectList.add(r.get("json"));
            }
            //集合中的元素为key：material_id  value：
            List<Map<String, Record>> materialList = new ArrayList<>();
            //循环所有materialIdSet，取得
            for(String materialId : materialIdSet){
                //key：material_id
                //value：统计后的数据
                Map<String, Record> m = new HashMap<>();
                //material_id原来的总用量
                double total = 0.0;
                boolean flag = true;
                String name = "";
                String unit = "";
                //
                for(JSONObject json : jsonObjectList){
                    total += getDouble(json.getJSONObject(materialId).get("net_num"));
                    if(flag){
                        name = json.getString("name");
                        unit = json.getString("unit");
                        flag = false;
                    }
                }
                Record r = new Record();
                r.set("total_net_num", total);
                r.set("name", name);
                r.set("unit", unit);
                m.put(materialId, r);
                materialList.add(m);
                materialNameMap.put(materialId, name);
            }
            resultMap.put(e.getKey(), materialList);
        }
        result.put("materialdSet", materialIdSet);
        result.put("resultMap", resultMap);
        result.put("materialNameMap", materialNameMap);
        return result;
    }

    /**
     * 获得原材料id，不重复的数据
     * @return 所有用到的原材料ID
     */
    public Set<String> getMaterialIdNoRepeat(List<Record> dailySummaryList){
        //所有原材料id，去掉重复，所以用set
        Set<String> materialIdSet = new HashSet<>();
        //循环所有daily_summary表中数据
        for(Record r : dailySummaryList){
            //
            //获取每日统计数据，从daily_summary表的statistic字段中获取，json格式字符串，转化成对象
            //json格式：
            //          key                         value
            //          material_id                 原材料id
            //          name                        原材料名称
            //          unit                        原材料单位（克、罐等）
            //          net_num                     原料净量
            JSONObject jsonObject = JSONObject.fromObject(r.getStr("statistic"));
            //将json字符串转化成对象放到r中，key：json
            r.set("json", jsonObject);
            //添加所有原料id到materialIdSet中
            materialIdSet.addAll(jsonObject.keySet());
        }
        return materialIdSet;
    }

    /**
     * 先根据时间获得每一家门店的数据
     * 按照materialIdList中的分类统计每个分类中的总量
     * 返回值结构：
     * Map<material_id, Map<time, total>>
     * key：material_id
     * value：Map
     *      key：time
     *      value：Record r；r.get("total")表示这种商品所有门店的总用量
     * @param dailySummaryList daily_summaryList表中的所有数据
     * @param materialIdList 所有用到的原料id集合
     * @param timeArr 预估时间的前10天和去年同期时间
     */
    public Map<String, Map<String, Record>> getStatisticAtTime(List<Record> dailySummaryList, List<String> materialIdList, String[] timeArr){
        Map<String, Map<String, Record>> result = new HashMap<>();

        /*
        key：material_id
        value：List 每个门店material_id这个原来的数据，以集合形式都存放在List中，包括时间，时间从dailySummary对象中获取
         */
        Map<String, List<Record>> dailySummaryByMaterialIdMap = new HashMap<>();
        for(String material_id : materialIdList){
            dailySummaryByMaterialIdMap.put(material_id, new ArrayList<>());
        }
        for(Record r : dailySummaryList){
            //todo 注意类型转化问题
            JSONObject dailySummaryJson = r.get("json");
            for(String material_id : materialIdList){
                JSONObject obj = dailySummaryJson.getJSONObject(material_id);
                Record time_total = new Record();
                time_total.set("material_id", material_id);
                time_total.set("time", r.get("imp_time"));
                time_total.set("total", obj.get("total_net_num"));
                dailySummaryByMaterialIdMap.get(material_id).add(time_total);
            }
        }

        for(Entry<String, List<Record>> dailySummaryByMaterialIdEntry : dailySummaryByMaterialIdMap.entrySet()){
            String material_id = dailySummaryByMaterialIdEntry.getKey();
            List<Record> eveyStore_eveyTime_oneMaterialList = dailySummaryByMaterialIdEntry.getValue();

            //整理一下每天每个门店的当前material_id数据统计
            //key：time
            //value：List<Record> time时间每家门店的数据集合
            Map<String, List<Record>> oneTime_eveyStrore_map = new HashMap<>();
            for(String time : timeArr){
                oneTime_eveyStrore_map.put(time, new ArrayList<>());
            }
            for(Record r : eveyStore_eveyTime_oneMaterialList){
                oneTime_eveyStrore_map.get(r.getStr("time")).add(r);
            }
            //key：time
            //value：Record r；r.get("total")表示这种商品所有门店的总用量
            Map<String, Record> timeMap = new HashMap<>();
            for(String time : timeArr){
                Record time_total = new Record();
                List<Record> eveyStore_list = oneTime_eveyStrore_map.get(time);
                double total = 0.0;
                for(Record r : eveyStore_list){
                    total += getDouble(r.get("total"));
                }
                timeMap.put(time, time_total);
            }
            result.put(material_id, timeMap);
        }
        return result;
    }

    /**
     * 安存预算
     *
     * 1、查询daily_summary表中imp_time之前10天和去年同期的数据
     * ？？？2、如果查询到数据，那么表示各个门店数据录入正常，如果没有查到数据表示没有任何用来推算的相关数据
     *
     * @param imp_time 要预算的时间
     * @return
     */
    public Map<String, Object> securityStockBudget(String imp_time) throws Exception{
        Map<String, Object> result = new HashMap<>();
        String[] timeArr = getAllTime(imp_time);
        List<Record> dailySummaryList = Db.find("select * from daily_summary where imp_time in (?,?,?,?,?,?,?,?,?,?,?)", timeArr);
        if(dailySummaryList != null && dailySummaryList.size() > 0){
            //获取所有用到的原料id
            List<String> materialIdList = new ArrayList<>(getMaterialIdNoRepeat(dailySummaryList));
            //排序原料，保证顺序
            Collections.sort(materialIdList);
            //获得原材料基础数据
            List<Record> materialList = Db.find("select * from material");
            Map<String, Record> materialIdRecordMap = new HashMap<>();
            for(Record r : materialList){
                materialIdRecordMap.put(r.getStr("id"), r);
            }
            /*
             * Map<material_id, Map<time, total>>
             * key：material_id
             * value：Map
             *      key：time
             *      value：Record r；r.get("total")表示这种商品所有门店的总用量
             */
            Map<String, Map<String, Record>> materialId_time_record_map = getStatisticAtTime(dailySummaryList, materialIdList, timeArr);

            /*
             * table中数据
             * 数据格式：
             * List：某种原料的每天用量集合。长度和原来id集合materialIdList长度相同
             *      List：当前原料在当前时间的消耗量
             */
            List<List<String>> tbodyList = new ArrayList<>();
            int eveyMaterialMapSize = 0;
            for(String material_id : materialIdList){
                int size = 0;
                List<String> list = new ArrayList<>();
                Map<String, Record> eveyMaterial_map = materialId_time_record_map.get(material_id);
                for(String s : timeArr){
                    Record r = eveyMaterial_map.get(s);
                    if(r != null){
                        list.add(r.get("total"));
                        size ++;
                    }else{
                        list.add("无用量");
                    }
                }
                if(eveyMaterialMapSize < size){
                    eveyMaterialMapSize = size;
                }
                tbodyList.add(list);
            }
            //添加每行第一列和后两列，原料名称、预估数据、单位
            for(int i = 0; i < materialIdList.size(); i++){
                List<String> list = tbodyList.get(i);
                String name = materialIdRecordMap.get(materialIdList.get(i)).getStr("name");
                String unit = materialIdRecordMap.get(materialIdList.get(i)).getStr("unit");
                list.add(budgetToday(list, eveyMaterialMapSize));
                list.add(unit);
                list.add(0, name);
            }
            List<String> titleList = new ArrayList<>();
            titleList.add("");
            for(int i = 0; i < timeArr.length - 1; i++){
                titleList.add(timeArr[i]);
            }
            titleList.add("去年同期");
            titleList.add("今日预估");
            titleList.add("单位");
            result.put("thead", titleList);
            result.put("tbody", tbodyList);
        }else{
            throw new Exception("没有历史数据！");
        }
        return result;
    }

    /**
     * 获取预估原料用量
     * 预算公式：（前十天数据总和/天数 + 去年同期数据）/ 2
            如果这天消耗没有数据，那么totalList中存在的是“无用量”，在getDouble方法会返回0.0
            如果是无用量，这个天数没有计算到eveyMaterialMapSize中，那么加0.0不会影响平均值。
            如果eveyMaterialMapSize和totalList.size()不相等，说明系统数据不完全
                如果数据不完全，一定是最先没有去年同期数据，这样就不需要加去年同期再除以2了
     * @param totalList tbody中每列的值，每种商品这11天每天的销售总量
     * @param eveyMaterialMapSize 有数据的天数，因为存在开始没有数据的情况，所以用这个记录天数，用来求平均值
     * @return
     */
    public String budgetToday(List<String> totalList, int eveyMaterialMapSize){
        double total = 0.0;
        for(int i = 0; i < totalList.size() - 1; i++){
            total += getDouble(totalList.get(i));
        }
        total /= eveyMaterialMapSize;
        if(eveyMaterialMapSize == totalList.size()){
            total = total + getDouble(totalList.get(10)) / 2;
        }
        BigDecimal b = new BigDecimal(total);
        total = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return total + "";
    }

}
