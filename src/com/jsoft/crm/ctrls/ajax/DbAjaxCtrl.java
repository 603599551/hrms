package com.jsoft.crm.ctrls.ajax;

import com.jfinal.KEY;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.mym.utils.RequestTool;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
此类的操作都不需要过滤是否登录
 */
public class DbAjaxCtrl extends Controller {
    /*
    dbObj表示数据库中的表名
     */
    public void save(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb = (String)paraMap.get("dbObj");
        String id=UUIDTool.getUUID();
        String time= DateTool.GetDateTime();

        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",KEY.CODE.DB_TB_NOT_EMPTY);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }

        try {

            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                r.set(en.getKey(),en.getValue());
            }
            r.remove("dbObj");
            r.set("id",id);
            r.set("create_time",time);
            r.set("modify_time",time);
            Db.save(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    public void updateById(){
        try {
            Map paraMap = RequestTool.getParameterMap(getRequest());
            String db_tb = (String)paraMap.get("dbObj");
            String id=(String)paraMap.get("id");
            String time= DateTool.GetDateTime();

            Record r = new Record();
            Set<Map.Entry<String,String>> set=paraMap.entrySet();
            for(Map.Entry<String,String> en:set){
                r.set(en.getKey(),en.getValue());
            }
            r.remove("dbObj");
//            r.set("create_time",time);
            r.set("modify_time",time);
            Db.update(db_tb, r);
            Map ret=r.getColumns();
            ret.put("id",id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    /*
    id:由前台传入id
    dbObj表示数据库中的表名
     */
    public void getById(){
        try{
            String id=getPara("id");
            String db_tb = getPara("dbObj");
            Record r=Db.findById(db_tb,id);
            renderJson(r.getColumns());
        }catch (Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",0);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    /*
    id:由前台传入id
    dbObj表示数据库中的表名
     */
    public void deleteById(){
        Map map=new HashMap();
        try{
            String id=getPara("id");
            String db_tb = getPara("dbObj");
            boolean b=Db.deleteById(db_tb,id);
            if(b){
                map.put("code",1);
            }else{
                map.put("code",0);
                map.put("msg","删除失败！");
            }
            renderJson(map);
        }catch (Exception e){
            e.printStackTrace();
            map.put("code",0);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
    /*
    记录日志
     */
    public void log(){
        try {
            Map paraMap = RequestTool.getParameterMap(getRequest());
            String id = UUIDTool.getUUID();
            String time = DateTool.GetDateTime();
            Map userSession = getSessionAttr(KEY.SESSION_ADMIN);

            Record r = new Record();
            Set<Map.Entry<String, String>> set = paraMap.entrySet();
            for (Map.Entry<String, String> en : set) {
                r.set(en.getKey(), en.getValue());
            }

            r.set("id", id);
            r.set("creator", userSession.get("id"));
            r.set("creator_name", userSession.get("username"));
            r.set("create_time", time);
            Db.save("log", r);
            Map ret = r.getColumns();
            ret.put("id", id);//执行save方法后，r中的id会被设置为null，所以重新填上
            renderJson(ret);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    dbObj：表名，必须
    fields 查询字段名，表示查询列名，为空则全查
    pageNum：页数，如果前台没传该参数，默认为1
    pageSize 每页显示记录数，如果前台没传该参数，默认为50条
    where 查询条件
    order：排序
     */
    public void filter(){
        try {
            Map paraMap = RequestTool.getParameterMap(getRequest());
            String db_tb = (String) paraMap.get("dbObj");
            int pageNum = NumberUtils.parseInt(paraMap.get("pageNum"), 1);
            int pageSize = NumberUtils.parseInt(paraMap.get("pageSize"), 15);
            String fields = (String) paraMap.get("fields");
            String where = (String) paraMap.get("where");
            String order = (String) paraMap.get("order");

            if (pageNum == 0) {
                pageNum = 1;
            }
            if (pageSize == 0) {
                pageSize = 15;
            }
            if(pageSize>50){
                pageSize = 50;
            }
        /*
        处理select部分
         */
            String select = "select *  ";
            if (fields != null && !"".equals(fields)) {
                select = "select " + fields;
            }
            select = select ;

            String whereEx = " from " + db_tb+" where 1=1  ";
            if (where == null || "".equals(where)) {

            } else {
                where=where.trim();
                //如果传进的where前面是and，则去掉
                if(where.startsWith("and")){
                    where=where.substring("and".length());
                }
                whereEx = whereEx + " and "+where;
            }
            if(order!=null && !"".equals(order)) {
                whereEx = whereEx + " " + order;
            }

            Page<Record> page = Db.paginate(pageNum, pageSize, select, whereEx);
            renderJson(page);
        }catch(Exception e){
            e.printStackTrace();
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg",e.toString());
            renderJson(map);
        }
    }
}
