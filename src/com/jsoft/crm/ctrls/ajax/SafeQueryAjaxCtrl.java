package com.jsoft.crm.ctrls.ajax;

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

public class SafeQueryAjaxCtrl extends Controller{
    /*
    最多查询100条记录
     */
    public void index() {
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb = (String) paraMap.get("dbObj");
        int pageNum = NumberUtils.parseInt(paraMap.get("pageNum"), 1);
        int pageSize = NumberUtils.parseInt(paraMap.get("pageSize"), 100);
        String fields = (String) paraMap.get("fields");
        String where = (String) paraMap.get("where");
        String order = (String) paraMap.get("order");
        if (db_tb == null || "".equals(db_tb)) {
            Map map = new HashMap();
            map.put("code", -1);
            map.put("msg", "dbObj不能为空！");
            renderJson(map);
            return;
        }

        if (pageNum == 0) {
            pageNum = 1;
        }
        if (pageSize == 0) {
            pageSize = 100;
        }

        try {

        /*
        处理select部分
         */
            String select = "select *  ";
            if (fields != null && !"".equals(fields)) {
                select = "select " + fields;
            }
            select = select;

            String whereEx = " from " + db_tb + " where 1=1  ";
            if (where == null || "".equals(where)) {

            } else {
                where = where.trim();
                //如果传进的where前面是and，则去掉
                if (where.startsWith("and")) {
                    where = where.substring("and".length());
                }
                whereEx = whereEx + " and " + where;
            }
            if (order != null && !"".equals(order)) {
                whereEx = whereEx + " order by " + order;
            }

            Page<Record> page = Db.paginate(pageNum, pageSize, select, whereEx);
            renderJson(page.getList());
        } catch (Exception e) {
            e.printStackTrace();
            Map map = new HashMap();
            map.put("code", -1);
            map.put("msg", e.toString());
            renderJson(map);
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
    public void page(){
        Map paraMap = RequestTool.getParameterMap(getRequest());
        String db_tb = (String) paraMap.get("dbObj");
        int pageNum = NumberUtils.parseInt(paraMap.get("pageNum"), 1);
        int pageSize = NumberUtils.parseInt(paraMap.get("pageSize"), 15);
        String fields = (String) paraMap.get("fields");
        String where = (String) paraMap.get("where");
        String order = (String) paraMap.get("order");
        if(db_tb==null || "".equals(db_tb)){
            Map map=new HashMap();
            map.put("code",-1);
            map.put("msg","dbObj不能为空！");
            renderJson(map);
            return ;
        }

        if (pageNum == 0) {
            pageNum = 1;
        }
        if (pageSize == 0) {
            pageSize = 15;
        }
        if(pageSize>50){
            pageSize = 50;
        }

        try {

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
