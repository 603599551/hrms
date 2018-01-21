package com.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SQLUtil {
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

    public SQLUtil() {
        paraList=new ArrayList();
    }

    public SQLUtil(String sql, List paraList) {
        this.sql.append(sql);
        this.paraList = paraList;
    }
    public SQLUtil(String sql){
        this.sql.append(sql);
        paraList=new ArrayList();
    }

    /**
     * 拼装where条件
     * @param where 必须以and或者or开头
     * @return
     */
    public SQLUtil addWhere(String where){
        this.where.append(" "+where+" ");
        return this;
    }

    /**
     * 拼装where条件
     * @param where 必须以and或者or开头
     * @param obj 不为空时，where要有 ?
     * @return
     */
    public SQLUtil addWhere(String where,Object obj){

        return addWhere(where,NONE,obj);
    }

    /**
     * 拼装where条件
     * @param where 必须以and或者or开头，
     * @param not
     * @param obj 不为空时，where要有 ?
     * @return
     */
    public SQLUtil addWhere(String where,int not,Object obj){
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
    public SQLUtil append(String str,Object ... objs){
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
     * @param sqlStr 拼装sql，必须有in或者not in，不能有小括号(
     * @param array 添加指定参数
     * @return
     */
    public SQLUtil in(String sqlStr,Object ... array){
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
        if(sqlType==1){
            return getSelectSQL();
        }else if(sqlType==2){
            return getInsertSQL();
        }else if(sqlType==3){
            return getUpdateSQL();
        }else{
            return null;
        }
    }

    /**
     * 可以自动过滤掉第一个条件的and或者or
     * @return
     */
    public StringBuilder getSelectSQL(){
        String where0=where.toString().trim();
        if("".equals(where0)){//where条件为空
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

    public SQLUtil setParameterList(List paraList) {
        this.paraList = paraList;
        return this;
    }
    public Object[] getParameterArray(){
        return paraList.toArray();
    }

    /**
     * 向paraList中添加参数
     * @param obj
     */
    public SQLUtil addParameter(Object obj){
        paraList.add(obj);
        return this;
    }

    public SQLUtil initInsertSQL(String tableName){
        sqlType=2;
        sql.append("insert into "+tableName);
        columnValues=new LinkedHashMap();
        return this;
    }
    public SQLUtil addInsertColumn(String columnName,int not,Object obj){
        if(not==NOT_NULL){
            if(obj!=null){
                columnValues.put(columnName,obj);
            }
        }else if(not==NOT_EMPTY_STRING){
            String str=(String)obj;
            if(!"".equals(str)){
                columnValues.put(columnName,obj);
            }
        }else if(not== NOT_NULL_AND_NOT_EMPTY_STRING){
            String str=(String)obj;
            if(str!=null && !"".equals(str)){
                columnValues.put(columnName,obj);
            }
        }else if(not==NONE){
            columnValues.put(columnName,obj);
        }else{
            throw new RuntimeException("错误的拼装条件！");
        }
        return this;
    }
    public StringBuilder getInsertSQL(){
        if(columnValues==null || columnValues.isEmpty()){
            throw new RuntimeException("没有要插入的数据！");
        }
        StringBuilder sql0=new StringBuilder(sql);
        sql0.append(" ( ");
        StringBuilder sql1=new StringBuilder(" values ( ");
        Object[] array=columnValues.entrySet().toArray();
        for(int i=0,length=array.length;i<length;i++){
            Map.Entry<String,Object> en=(Map.Entry<String,Object>)array[i];
            sql0.append(en.getKey());
            sql1.append(" ? ");
            paraList.add(en.getValue());
            if(i<=(length-2)){
                sql0.append(" , ");
                sql1.append(" , ");
            }
        }
        sql0.append(" ) ");
        sql1.append(" ) ");
        sql0.append(sql1);
        return sql0;
    }

    public SQLUtil initUpdateSQL(String tableName){
        sqlType=3;
        sql.append("update "+tableName+" set ");
        columnValues=new LinkedHashMap();
        return this;
    }
    public SQLUtil addUpdateColumn(String columnName,int not,Object obj){
        if(not==NOT_NULL){
            if(obj!=null){
                columnValues.put(columnName,obj);
            }
        }else if(not==NOT_EMPTY_STRING){
            String str=(String)obj;
            if(!"".equals(str)){
                columnValues.put(columnName,obj);
            }
        }else if(not== NOT_NULL_AND_NOT_EMPTY_STRING){
            String str=(String)obj;
            if(str!=null && !"".equals(str)){
                columnValues.put(columnName,obj);
            }
        }else if(not==NONE){
            columnValues.put(columnName,obj);
        }else{
            throw new RuntimeException("错误的拼装条件！");
        }
        return this;
    }
    public StringBuilder getUpdateSQL(){
        if(columnValues==null || columnValues.isEmpty()){
            throw new RuntimeException("没有要插入的数据！");
        }
        StringBuilder sql0=new StringBuilder(sql);
        Object[] array=columnValues.entrySet().toArray();
        for(int i=0,length=array.length;i<length;i++){
            Map.Entry<String,Object> en=(Map.Entry<String,Object>)array[i];
            sql0.append(en.getKey()+" = ? ");
            paraList.add(en.getValue());
            if(i<=(length-2)){
                sql0.append(" , ");
            }
        }
        String where0=where.toString().trim();
        if("".equals(where0)){//where条件为空

        }else{//有where条件
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

        }
        return sql0;
    }
    public static void main(String[] args) {
        SQLUtil sql=new SQLUtil();
//        sql.append("select id,name from student");
//        String age=null;
//        sql.addWhere("and age=?",SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING,age)
//                .addWhere("and name like ? ","张%")
//                .in(" or status in",new String[]{"1","2","3"});
//        sql.in("and jr_class in","1","2","3");
//        sql.in("and school not in","1");
////        sql.in("id");
//        Object ageObj=null;
//        sql.addWhere("and ?<birthday",NOT_EMPTY_STRING,ageObj);
//        sql.addWhere("and name like ? ","张%")
//                .addWhere("or name ='马'");

        String name="哈哈";
        String age="18";
//        sql.initInsertSQL("student");
//        sql.addInsertColumn("name",NOT_NULL,name);
//        sql.addInsertColumn("age",NOT_EMPTY_STRING,age);

//        sql.append("delete from student ");
//        sql.addWhere("and age=?",NOT_EMPTY_STRING,age);
        String name2="马%";
        sql.initUpdateSQL("student");
        sql.addUpdateColumn("name",NOT_EMPTY_STRING,name);
        sql.addUpdateColumn("age",NOT_EMPTY_STRING,age);
        sql.addWhere("and name like ?",NOT_EMPTY_STRING,name2);
        System.out.println(sql.toString());
        System.out.println(sql.getParameterList());
    }
}
