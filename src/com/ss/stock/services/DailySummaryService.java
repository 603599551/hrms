package com.ss.stock.services;

import com.bean.UserBean;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.UUIDTool;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class DailySummaryService {

    private static DailySummaryService me;
    private static String TIME_VISION = "";

    private static Map<String, Map<String, Object>> dataMap;
    public static Map<String, Map<String, Object>> dataGoodsIdMap;
    private static Map<String, Record> storeMap;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    private DailySummaryService(){
        super();
        dataMap = new HashMap<>();
        storeMap = new HashMap<>();
        dataGoodsIdMap = new HashMap<>();
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
        sql.append("gt.wm_type          gwm_type,");
        sql.append("gt.attribute_1      gattr_1,");
        sql.append("gt.attribute_2      gattr_2,");
//        sql.append("ggu.name            gunit,");
        sql.append("(select name from goods_unit where goods_unit.id=gt.unit)            gunit,");
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
        sql.append("m.wm_type           mstock_type,");
        sql.append("m.attribute_1       mattr_1,");
        sql.append("m.attribute_2       mattr_2,");
        sql.append("(select number from goods_attribute where goods_attribute.id=m.attribute_1)            mattr_1_number,");
        sql.append("(select number from goods_attribute where goods_attribute.id=m.attribute_2)            mattr_2_number,");
        sql.append("(select name from goods_unit where goods_unit.id=m.unit)            munit,");
        sql.append("m.sort              msort,");
        sql.append("m.type_1            mtype_1,");
        sql.append("m.type_2            mtype_2,");

        sql.append("gm.id               gmid,");
        sql.append("gm.net_num          gmnet_num,");
        sql.append("gm.gross_num        gmgross_num");

        sql.append(" from goods gt,");
        sql.append("        (select max(modify_time) modify_time, code from goods GROUP BY code) a,");
        sql.append("        goods_material gm,");
        sql.append("        material m ");
//        sql.append("        goods_unit ggu, ");
//        sql.append("        goods_unit mgu");
        sql.append(" where gt.code = a.code");
        sql.append(" and gt.modify_time = a.modify_time");
//        sql.append(" and gt.unit = ggu.id");
//        sql.append(" and m.unit = mgu.id");
        sql.append(" and gt.id=gm.goods_id");
        sql.append(" and m.id = gm.material_id;");

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
                goodsCodeMap.put("data", r);
            }
        }

        List<Record> storeList = Db.find("select * from store");
        if(storeList != null && storeList.size() > 0){
            for(Record r : storeList){
                storeMap.put(r.getStr("name"), r);
            }
        }

        Iterator<Entry<String, Map<String, Object>>> dataMapIt = dataMap.entrySet().iterator();
        while(dataMapIt.hasNext()){
            Entry<String, Map<String, Object>> entry = dataMapIt.next();
            Record goods = (Record) entry.getValue().get("data");
            dataGoodsIdMap.put(goods.getStr("gid"), entry.getValue());
        }
    }

    /**
     * 获取DailySummaryService对象
     * 根据当前时间和时间版本号（TIME_VISION）是否相同判断是否需要初始化缓存数据
     * @return DailySummaryService对象
     */
    public static DailySummaryService getMe(){
        boolean initFlag = TIME_VISION.equals(DateTool.GetDate());
//        if(!initFlag){
            me = new DailySummaryService();
//        }
//        if(me == null){
//            me = new DailySummaryService();
//        }
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
            //添加事务管理
            boolean success = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    try {
                        //daily_summary 主键删除
                        Db.deleteById("daily_summary", daily_summary_id);
                        //imp_daily_summary 关联daily_summary_id字段删除
                        Db.delete("delete from imp_daily_summary where daily_summary_id=?", daily_summary_id);
                        //sale_goods 关联daily_summary_id字段删除
                        Db.delete("delete from sale_goods where daily_summary_id=?", daily_summary_id);
                        //sale_goods_material 关联daily_summary_id字段删除
                        Db.delete("delete from sale_goods_material where daily_summary_id=?", daily_summary_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
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
                Record saleGoods = createSaleGoods(goodsCode, userBean, time, sale_time, daily_summary_id, r.getStr("sale_num"));
                saleGoodsList.add(saleGoods);
                //创建销售商品原来数据，详情见createSaleGoodsMaterial注释
                createSaleGoodsMaterial(goodsMaterialList, saleGoodsMaterialList, userBean, saleGoods.getStr("id"), time, sale_time, daily_summary_id, r.getStr("sale_num"));
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
    @Before(Tx.class)
    private void saveData(List<Record> impDailySummaryList, List<Record> saleGoodsList, List<Record> saleGoodsMaterialList, Record daily_summary){
        //添加daily_summary表的主键数据，建立多对一的关联关系
        for(Record r : impDailySummaryList){
            r.set("daily_summary_id", daily_summary.getStr("id"));
        }
        //添加事务管理
        boolean success = Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                try {
                    //保存销售数据的总表数据
                    Db.save("daily_summary", daily_summary);
                    //保存导入的原始数据
                    Db.batchSave("imp_daily_summary", impDailySummaryList, impDailySummaryList.size());
                    //保存销售商品数据
                    Db.batchSave("sale_goods", saleGoodsList, saleGoodsList.size());
                    //保存销售商品原材料数据
                    Db.batchSave("sale_goods_material", saleGoodsMaterialList, saleGoodsMaterialList.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
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
        Map<String, Map<String, Object>> dailySummaryMap = new HashMap<>();
        for(Entry<String, List<Record>> entry : sgmMap.entrySet()){
            String material_id = entry.getKey();
            List<Record> recordList = entry.getValue();
            double net_num = 0.0;
            double gross_num = 0.0;
            for(Record r : recordList){
                net_num += getDouble(r.getStr("net_num")) * getDouble(r.getStr("sale_num"));
                gross_num += getDouble(r.getStr("gross_num")) * getDouble(r.getStr("sale_num"));
            }
            Record sgmRecord = recordList.get(0);
            Map<String, Object> r = new HashMap<>();
            dailySummaryMap.put(material_id, r);
            r.put("material_id", material_id);
            r.put("code", sgmRecord.getStr("code") != null ? sgmRecord.getStr("code") : "");
            r.put("name", sgmRecord.getStr("name") != null ? sgmRecord.getStr("name") : "");
            r.put("yield_rate", sgmRecord.getStr("yield_rate") != null ? sgmRecord.getStr("yield_rate") : "");
            r.put("purchase_price", sgmRecord.getStr("purchase_price") != null ? sgmRecord.getStr("purchase_price") : "");
            r.put("balance_price", sgmRecord.getStr("balance_price") != null ? sgmRecord.getStr("balance_price") : "");
            r.put("stock_type", sgmRecord.getStr("stock_type") != null ? sgmRecord.getStr("stock_type") : "");
            r.put("unit", sgmRecord.getStr("nuit") != null ? sgmRecord.getStr("nuit") : "");
            r.put("net_num", net_num);
            r.put("gross_num", gross_num);
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
    private Record createSaleGoods(Record goods, UserBean userBean, String time, String sale_time, String daily_summary_id, String sale_num){
        String saleGoodsId = UUIDTool.getUUID();
        Record saleGoods = new Record();
        saleGoods.set("id",saleGoodsId);
        saleGoods.set("goods_id",goods.getStr("gid"));
        saleGoods.set("store_id",userBean.get("store_id"));
        saleGoods.set("code",goods.getStr("gcode"));
        saleGoods.set("name",goods.getStr("gname"));
        saleGoods.set("pinyin",goods.getStr("gpinyin"));
        saleGoods.set("price",goods.getStr("gprice"));
        saleGoods.set("stock_type",goods.getStr("gwm_type"));
        saleGoods.set("attribute_1",goods.getStr("gattr_1"));
        saleGoods.set("attribute_2",goods.getStr("gattr_2"));
        saleGoods.set("unit",goods.getStr("gunit"));
        saleGoods.set("sort",goods.getStr("gsort"));
        saleGoods.set("type_1",goods.getStr("gtype_1"));
        saleGoods.set("type_2",goods.getStr("gtype_2"));
        saleGoods.set("creater_id",userBean.getId());
        saleGoods.set("modifier_id",userBean.getId());
        saleGoods.set("create_time",time);
        saleGoods.set("modify_time",time);
        saleGoods.set("sale_time",sale_time);
        saleGoods.set("sale_num",sale_num);

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
    private void createSaleGoodsMaterial(List<Record> goodsMaterialList, List<Record> saleGoodsMaterialList, UserBean userBean, String saleGoodsId, String time, String sale_time, String daily_summary_id, String sale_num){
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

                saleGoodsMaterial.set("attribute_1",gcR.getStr("mattr_1"));
                saleGoodsMaterial.set("attribute_2",gcR.getStr("mattr_2"));
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
                saleGoodsMaterial.set("sale_num",sale_num);

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
//            JSONObject jsonObject = JSONObject.fromObject(r.getStr("statistic").replaceAll(":null", ":\"\""));
            JSONObject jsonObject = JSONObject.fromObject(r.getStr("statistic").replaceAll(":null", ":\"\""));
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
                if(obj != null && !"null".equalsIgnoreCase(obj + "") && obj.get("net_num") != null){
                    time_total.set("total", obj.get("net_num"));
                }else{
                    time_total.set("total", 0.0);
                }
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
                time_total.set("total", total);
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
        List<Record> dailySummaryGroupByImpTimeList = Db.find("select * from daily_summary where imp_time in (?,?,?,?,?,?,?,?,?,?,?) group by imp_time", timeArr);
        List<Record> lastYearDailySummaryGroupByImpTimeList = Db.find("select * from daily_summary where imp_time=? group by imp_time", timeArr[timeArr.length - 1]);

        if(dailySummaryList != null && dailySummaryList.size() > 0){
            //获取所有用到的原料id
            List<String> materialIdList = new ArrayList<>(getMaterialIdNoRepeat(dailySummaryList));
            //排序原料，保证顺序
            Collections.sort(materialIdList);
            //获得原材料基础数据
            List<Record> materialList = Db.find("select m.id id, m.name name, gu.name unit from material m, goods_unit gu where m.unit=gu.id");
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
            int eveyMaterialMapSize = dailySummaryGroupByImpTimeList.size() - lastYearDailySummaryGroupByImpTimeList.size();
            for(String material_id : materialIdList){
                List<String> list = new ArrayList<>();
                Map<String, Record> eveyMaterial_map = materialId_time_record_map.get(material_id);
                for(String s : timeArr){
                    Record r = eveyMaterial_map.get(s);
                    if(r != null){
                        list.add(r.get("total"));
                    }else{
                        list.add("无用量");
                    }
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
            titleList.add("原料");
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
        if(eveyMaterialMapSize == totalList.size() - 1){
            total = (total + getDouble(totalList.get(10))) / 2;
        }
        BigDecimal b = new BigDecimal(total);
        total = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return total + "";
    }
}
