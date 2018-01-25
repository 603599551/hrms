package utils.jfinal;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DbUtil {

    /**
     * 查询门店最大的sort
     * @param table
     * @param column
     * @param where
     * @return
     */
    public static int queryMax(String table, String column, String where){
        if(table==null || "".equals(table)){
            throw new NullPointerException("table不能为空！");
        }
        if(column==null || "".equals(column)){
            throw new NullPointerException("column不能为空！");
        }
        if(where==null){
            where="";
        }
        String sql=String.format("select max(%s) as max from %s %s",column,table,where);
        int sort=1;
        Record r= Db.findFirst(sql);
        if(r!=null){
            Object obj=r.get("max");
            if(obj==null){
                sort=1;
            }else{
                sort=Integer.parseInt(obj.toString());
            }
        }
        return sort;
    }

    /**
     * 查询总数
     * @param from
     * @return
     */
    public static int queryCount(String from){
        if(from==null || "".equals(from)){
            throw new NullPointerException("table不能为空！");
        }
//        if(column==null || "".equals(column)){
//            throw new NullPointerException("column不能为空！");
//        }
        String sql=String.format("select count(*) as count ",from);
        int count=1;
        Record r= Db.findFirst(sql);
        if(r!=null){
            Object obj=r.get("count");
            if(obj==null){
                count=1;
            }else{
                count=Integer.parseInt(obj.toString());
            }
        }
        return count;
    }
    public static int queryMax(String table, String column){
        return queryMax(table,column,null);
    }
    public static void main(String[] args) {
        queryMax("student","sort");
    }
}
