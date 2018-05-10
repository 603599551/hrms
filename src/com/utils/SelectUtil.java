package com.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class SelectUtil {
    /**
     * 什么也不判断
     */
    public static final int NONE=-1;
    /**
     * 不为null
     */
    public static final int NOT_NULL=100;
    /**
     * 不为空字符串
     */
    public static final int NOT_EMPTY_STRING=101;

    /**
     * 下划线
     */
    public static int WILDCARD_UNDERSCODE=200;
    /**
     * 星号
     */
    public static int WILDCARD_ASTERISK=201;
    /**
     * 不为null，而且不为空字符串
     */
    public static final int NOT_NULL_AND_NOT_EMPTY_STRING =102;

    private StringBuilder sql=new StringBuilder();
    private StringBuilder where=new StringBuilder();
    private List paraList;

    /**
     sql类型
     1：select
     2：insert
     3：update
     4：delete
     */
    int sqlType=1;
    LinkedHashMap columnValues=null;

    public SelectUtil() {
        paraList=new ArrayList();
    }

    public SelectUtil(String sql, List paraList) {
        this.sql.append(sql);
        if(paraList==null){
            throw new NullPointerException("paraList不能为空！");
        }
        this.paraList = paraList;
    }
    public SelectUtil(String sql){
        this.sql.append(sql);
        paraList=new ArrayList();
    }
    public static SelectUtil initSelectSQL(String sql, List paraList){
        SelectUtil instance=new SelectUtil(sql, paraList);
        return instance;
    }
    public static SelectUtil initSelectSQL(String sql){
        SelectUtil instance=new SelectUtil(sql);
        return instance;
    }
    /**
     * 拼装where条件
     * @param where 必须以and或者or开头
     * @return
     */
    public SelectUtil addWhere(String where){
        this.where.append(" "+where+" ");
        return this;
    }
    String order="";

    /**
     * 设置排序
     * @param order
     * @return
     */
    public SelectUtil order(String order){
        this.order=order;
        return this;
    }
    /**
     * 拼装where条件
     * @param where 必须以and或者or开头
     * @param obj 不为空时，where要有 ?
     * @return
     */
    public SelectUtil addWhere(String where, Object obj){

        return addWhere(where,NONE,obj);
    }

    /**
     * 拼装like sql语句
     * @param where
     * @param wildcard1 匹配类型。NONE表示无，WILDCARD_UNDERSCODE表示_，WILDCARD_ASTERISK表示*
     * @param keyword
     * @param wildcard2
     * @return
     */
    public SelectUtil like(String where, int wildcard1,String keyword,int wildcard2){
        if(keyword==null || "".equals(keyword)){
            return this;
        }else {
            String keyword2=keyword;
            if(wildcard1==WILDCARD_UNDERSCODE){
                keyword2="_"+keyword2;
            }
            if(wildcard1==WILDCARD_ASTERISK){
                keyword2="*"+keyword2;
            }
            if(wildcard2==WILDCARD_UNDERSCODE){
                keyword2=keyword2+"_";
            }
            if(wildcard2==WILDCARD_ASTERISK){
                keyword2=keyword2+"*";
            }
            return addWhere(where, keyword2);
        }
    }

    /**
     * 拼装where条件
     * @param where 必须以and或者or开头，
     * @param not
     * @param obj 不为空时，where要有 ?
     * @return
     */
    public SelectUtil addWhere(String where, int not, Object obj){
        if(not==NOT_NULL){
            if(obj!=null){
                this.where.append(" "+where+" ");
                paraList.add(obj);
            }
        }else if(not==NOT_EMPTY_STRING){
            String str=(String)obj;
            if(!"".equals(str)){
                this.where.append(" "+where+" ");
                paraList.add(str);
            }
        }else if(not== NOT_NULL_AND_NOT_EMPTY_STRING){
            String str=(String)obj;
            if(str!=null && !"".equals(str)){
                this.where.append(" "+where+" ");
                paraList.add(str);
            }
        }else if(not==NONE){
            this.where.append(" "+where+" ");
            paraList.add(obj);
        }else{
            throw new RuntimeException("错误的拼装条件！");
        }
        return this;
    }
//    public SQLUtil addWhere(String where,int not,String str){
//        if(not==NOT_NULL){
//            if(str!=null){
//                this.where.append(" "+where+" ");
//                paraList.add(str);
//            }
//        }else if(not==NOT_EMPTY_STRING){
//            if(!"".equals(str)){
//                this.where.append(" "+where+" ");
//                paraList.add(str);
//            }
//        }else if(not== NOT_NULL_AND_NOT_EMPTY_STRING){
//            if(str!=null && !"".equals(str)){
//                this.where.append(" "+where+" ");
//                paraList.add(str);
//            }
//        }else if(not==NONE){
//            this.where.append(" "+where+" ");
//            paraList.add(str);
//        }
//
//        return this;
//    }
    /**
     * 拼装sql
     * @param str 拼装的sql
     * @param objs 添加指定参数
     * @return
     */
    public SelectUtil append(String str, Object ... objs){
        sql.append(" "+str+" ");
        if(objs!=null && objs.length>0){
            for(Object obj:objs){
                paraList.add(obj);
            }
        }
        return this;
    }
    /**
     * 构建sql的where条件的in部分
     * 例子：sql.in("and jr_class in","1","2","3");
     * @param sqlStr 拼装sql，必须有in或者not in，不能有小括号(
     * @param array 添加指定参数
     * @return
     */
    public SelectUtil in(String sqlStr, Object ... array){
        where.append(" "+sqlStr+" ( ");
        if(array!=null && array.length>0) {
            for (int i = 0, length = array.length; i < length; i++) {
                where.append("?");
                paraList.add(array[i]);
                if (i <= (length - 2)) {
                    where.append(",");
                }
            }
        }else{
            throw new RuntimeException("必须传入参数！");
        }
        where.append(" ) ");
        return this;
    }
    public StringBuilder getSQL() {
        return getSelectSQL();
    }

    /**
     * 可以自动过滤掉第一个条件的and或者or
     * @return
     */
    private StringBuilder getSelectSQL(){
        String where0=where.toString().trim();
        if("".equals(where0)){//where条件为空
            sql.append(" ");
            sql.append(order);
            return sql;
        }else{//有where条件
            StringBuilder sql0=new StringBuilder(sql);
            sql0.append(" where ");
            if(where0.startsWith("and")){
                where0=where0.substring("and".length());
                sql0.append(where0);
            }else if(where0.startsWith("or")){
                where0=where0.substring("or".length());
                sql0.append(where0);
            }else{
                sql0.append(where0);
            }
            sql0.append(" ");
            sql0.append(order);
            return sql0;
        }
    }
    /**
     * 返回String类型的sql语句
     * @return
     */
    @Override
    public String toString() {
        return getSQL().toString();
    }

    public List getParameterList() {
        return paraList;
    }

    public SelectUtil setParameterList(List paraList) {
        this.paraList = paraList;
        return this;
    }
    public Object[] getParameters(){
        return paraList.toArray();
    }

    /**
     * 向paraList中添加参数
     * @param obj
     */
    public SelectUtil addParameter(Object obj){
        paraList.add(obj);
        return this;
    }

    public static void main(String[] args) {
        SelectUtil selectUtil=new SelectUtil("select * from store_order ");
        String orderCode="";
        selectUtil.like("and order_number like ?",WILDCARD_UNDERSCODE,orderCode,NONE);

        System.out.println(selectUtil.toString());
        System.out.println(Arrays.toString(selectUtil.getParameters()));
        /*
        String type="";
        String status="";
        String wx_type="";
        String key="";
        SelectUtil sqlUtil = new SelectUtil(" from material m ");
        if(StringUtils.isNotEmpty(type)){
            String[] typeArray=type.split(",");
            sqlUtil.in("and type_2 in ",  typeArray);
        }
        sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
        sqlUtil.addWhere(" and wm_type=?",SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wx_type);

        if(StringUtils.isNotEmpty(key)){
            String key2 = key + "%";
            sqlUtil.addWhere(" and (code like ? or name like ? or pinyin like ? )");
            sqlUtil.addParameter(key2);
            sqlUtil.addParameter(key2);
            sqlUtil.addParameter(key2);
        }
        sqlUtil.order(" order by status desc ,sort,id ");
        String select="select m.*,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text";
*/
//        String goodsId="11111";
//        String[] typeArray=new String[]{"1","2"};
//        String wm_type="3";
//        String key="mym";
//
//        String select=" select gm.id as gm_id,m.id, m.yield_rate/100 as yield_rate,m.name as name,m.code,m.purchase_price,m.balance_price,(select name from material_type where id=m.type_1) as type_1_text,(select name from material_type where id=m.type_2) as type_2_text,case m.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=m.wm_type) as wm_type_text,(select name from goods_unit where id=m.unit) as goods_unit_text,ifnull(gm.net_num,0) as net_num ,ifnull(gm.gross_num,0) as gross_num,m.purchase_price as price,gm.total_price,ifnull(gm.sort,100000) as gm_sort,m.sort as m_sort ";
//        SelectUtil sqlUtil = new SelectUtil(select);
//        sqlUtil.append(" from material m left join ( select * from goods_material where goods_id=?) gm on m.id=gm.material_id ");
//        sqlUtil.addParameter(goodsId);
//        sqlUtil.addWhere(" and m.status=1 ");
////            sqlUtil.addWhere(" and gm.goods_id=? ");
//        if(typeArray!=null && typeArray.length>0){
//            if(typeArray.length==1 && "".equals(typeArray[0])){//前台没有选择分类时，默认传进一个空字符串
//
//            }else {
//                sqlUtil.in("and m.type_2 in ", typeArray);
//            }
//        }
//        sqlUtil.addWhere(" and m.wm_type=?",SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING,wm_type);
//
//        if(org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
//            String key2 = key + "%";
//
//            sqlUtil.addWhere(" and (m.code like ? or m.name like ? or m.pinyin like ? )");
//            sqlUtil.addParameter(key2);
//            sqlUtil.addParameter(key2);
//            sqlUtil.addParameter(key2);
//        }
//
//        StringBuilder sql=sqlUtil.getSQL();
//        StringBuilder sql2=new StringBuilder("select * from (");
//        sql2.append(sql);
//        sql2.append(") as a");
//        sql2.append(" order by a.gm_sort,m_sort,a.a.id");
//
//
//
//        System.out.println(sql2.toString());
//        System.out.println(sqlUtil.getParameterList());

    }
}
