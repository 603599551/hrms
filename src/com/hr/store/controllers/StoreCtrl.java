package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.store.service.StoreService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class StoreCtrl extends BaseCtrl {

    //获取service的方法，这样获取才能将事务添加进去
    private StoreService service = enhance(StoreService.class);

    /**
     * @author wangze
     * @date 2018-06-27
     *  名称	添加门店
        描述	添加门店。
        验证	门店名称不能重复
        权限	Hr可见
        请求方式	post
        请求参数类型	json
     *
     *   参数名	    类型      最大长度	    允许空	描述
         city	    string		        否	    城市id
         name	    string		        否	    门店名称
         address	string		        否	    地址
         phone	    string		        是	    联系电话
         desc	    string		        是	    描述

     */
    public void add(){
        /*
        返回前台的json对象
            数据接口的返回值格式统一规定，为了开发方便封装了这个类。他的常用方法返回的都是这个对象本身，所以可以这样写：
            jhm.putCode(0).putMessage("所在城市不能为空！");
         */
        JsonHashMap jhm = new JsonHashMap();
        Record store = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //根据接口要求进行非空验证
        if(StringUtils.isEmpty(store.getStr("city"))){
            jhm.putCode(0).putMessage("所在城市不能为空！");
            //将jhm对象转化成json并响应给前台
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(store.getStr("name"))){
            jhm.putCode(0).putMessage("门店名称不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(store.getStr("address"))){
            jhm.putCode(0).putMessage("门店地址不能为空！");
            renderJson(jhm);
            return;
        }
        String sql = "select * from h_store where name=?";
        String name = store.getStr("name");
        //Db是jfinal中持久层操作的通用方法，包含多种CRUD方法
        //包括基础的CRUD、分页查询(paginate，返回值Page<Record> page，详见query方法)、批量新增（batchSave）、查询第一条记录(findFirst)、通过id查询（findByID）等操作
        List<Record> storeList = Db.find(sql, name);
        if(storeList != null && storeList.size() > 0){
            jhm.putCode(0);
            jhm.putMessage("门店名称重复！");
        }else{
            try{
                service.add(store, usu);
                jhm.putMessage("新增成功！");
            } catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     名称	停用门店
     描述	根据id停用门店。
     验证	根据id查询该门店是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/store/stop
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	    类型	    最大长度	允许空	描述
     id	        string		    否	    门店id
     status	    string		    否	    0表示停用，1表示启用

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "操作成功！"
     }
     失败	{
     "code": 0,
     "message": "门店不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     因为停用这个方法的业务逻辑很简单，只对一张表做一次的修改操作，不涉及到对数据库数据多次写的操作，所以有没必要创建service层代码处理业务逻辑
     是否添加service取决于对数据库写的次数，如果大于1需要添加service，查询无论多少次都不影响
     */
    public void stop(){
        JsonHashMap jhm = new JsonHashMap();
        //JSP中的String id = request.getParameter(id);等同于下面
        String id = getPara("id");
        String status = getPara("status");
        Record store = Db.findById("h_store", id);
        if(store == null){
            jhm.putCode(0).putMessage("门店不存在！");
            renderJson(jhm);
            return;
        }
        try{
            String sql = "update h_store set status=? where id=?";
            Db.update(sql, status, id);
            jhm.putMessage("操作成功！");
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     *   名称	修改后保存门店
         描述	修改后保存门店。
         验证	门店名称不能重复，根据id修改信息
         权限	Hr可见
         URL	http://localhost:8081/mgr/store/updateById
         请求方式	post
         请求参数类型	json

         请求参数：
         参数名	    类型	    最大长度	允许空	描述
         id	        string		    否	    门店id
         city	    string		    否	    城市id
         name	    string		    否	    门店名称
         address	string		    否	    地址
         phone	    string		    是	    联系电话
         desc	    string		    是	    描述

         返回数据：
         返回格式	JSON
         成功	{
         "code": 1,
         "message": "门店修改成功！"
         }
         失败	{
         "code": 0,
         "message": "门店名称重复！"
         }
         报错	{
         "code": -1,
         "message": "服务器发生异常！"
         }

     */
    public void updateById(){
        JsonHashMap jhm = new JsonHashMap();
        Record store = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //根据接口要求进行非空验证
        if(StringUtils.isEmpty(store.getStr("city"))){
            jhm.putCode(0).putMessage("所在城市不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(store.getStr("name"))){
            jhm.putCode(0).putMessage("门店名称不能为空！");
            renderJson(jhm);
            return;
        }
        if(StringUtils.isEmpty(store.getStr("address"))){
            jhm.putCode(0).putMessage("门店地址不能为空！");
            renderJson(jhm);
            return;
        }
        String sql = "select * from h_store where name=?";
        String name = store.getStr("name");
        List<Record> storeList = Db.find(sql, name);
        if(storeList != null && storeList.size() > 0){
            jhm.putCode(0);
            jhm.putMessage("门店名称重复！");
        }else{
            try{
                String time = DateTool.GetDateTime();
                store.set("modifier_id", usu.getUserId());
                store.set("modify_time", time);
                store.remove("city_text");
                Db.update("h_store", store);
                jhm.putMessage("门店修改成功！");
            } catch (Exception e){
                e.printStackTrace();
                jhm.putCode(-1).putMessage("服务器发生异常！");
            }
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     名称	显示所有门店
     描述	显示所有门店。
     验证	无
     权限	Hr可见
     URL	http://localhost:8081/mgr/store/list
     请求方式	get
     请求参数类型

     请求参数：
     参数名	类型	最大长度	允许空	描述
     name	string		允许	查询添加，按照姓名和拼音头模糊查询


     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "list": [{
     "address": "长大对面星城国际",
     "phone": "电话号码",
     "name": "店名",
     "id": "门店id",
     "status_text": "启用或停用",
     "store_color": "#b7a6d4",//颜色值
     "status": 1,//状态值
     "status_color": "success"//状态值（蓝：""绿：success灰：info黄：warning红：danger）
     }]
     }
     失败	{
     "code": 0,
     "message": "提示失败信息！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     */
    public void list(){
        JsonHashMap jhm = new JsonHashMap();
        String name = getPara("name");
        try{
            String sql = "select h_store.*, (select status_color from h_dictionary d where d.parent_id=1100 and d.value=h_store.status) status_color from h_store where 1=1 ";
            List<Object> params = new ArrayList<>();
            if(!StringUtils.isEmpty(name)){
                name = "%" + name + "%";
                sql += " and (name like ? or pinyin like ?) ";
                params.add(name);
                params.add(name);
            }
            sql += "order by sort ";
            List<Record> list = Db.find(sql, params.toArray());
            if(list != null && list.size() > 0){
                for(Record r : list){
                    //将字典值转化为文字
                    if(0 == r.getInt("status")){
                        r.set("status_text", "停用");
                    }else{
                        r.set("status_text", "启用");
                    }
                }
            }
            jhm.put("data", list);
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     * 分页查询
     * 分页常调用Db.paginate方法，参数分别是：
     * 页号，每页数量，要查询的项，条件和关联条件，传入参数
     *
     */
    public void query(){
        JsonHashMap jhm = new JsonHashMap();
        String name = getPara("name");

        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        //如果为空时赋给默认值
        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);
        try{
            //要查询出来的项
            String select = "select * ";
            String sql = " from h_store where 1=1 ";
            //参数集合
            List<Object> params = new ArrayList<>();
            if(!StringUtils.isEmpty(name)){
                name = "%" + name + "%";
                sql += " and (name like ? or pinyin like ?) ";
                params.add(name);
                params.add(name);
            }
            sql += "order by sort ";
            Page<Record> page = Db.paginate(pageNum, pageSize, select, sql, params.toArray());
            if(page != null && page.getList().size() > 0){
                for(Record r : page.getList()){
                    //将字典值转化为文字
                    if(0 == r.getInt("status")){
                        r.set("status_text", "停用");
                    }else{
                        r.set("status_text", "启用");
                    }
                }
            }
            jhm.put("data", page);
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     名称	查看门店
     描述	根据id查询门店信息
     验证	根据传入id判断门店是否存在
     权限	Hr可见
     URL	http://localhost:8081/mgr/store/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	    最大长度	允许空	描述
     id	    string		    否	    门店id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "address": "卫星路与亚泰大街交汇处",//地址
     "create_time": "2018-02-04 14:01:15",//创建时间
     "city": "changchun",//城市
     "modify_time": "2018-02-04 14:01:15",//最后一次修改时间
     "sort": 50,//排序
     "store_color": "#532971",//颜色
     "creater_id": "1",//创建人id
     "phone": "13904312345",//联系电话
     "name": "面对面（卫星路店）",//名称
     "modifier_id": "1",//最后一次修改人id
     "id": "e1866af6ec1a4342aed66b0a71f0a6ee",//id
     "desc": "",//描述
     "status": 1//状态
     }
     }
     失败	{
     "code": 0,
     "message": "门店不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     */
    public void showById(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        try{
            String sql = "select s.*, d.name city_text from h_store s, h_dictionary d where s.city=d.value and d.parent_id=(select id from h_dictionary where value='city') and s.id=?";
            Record store = Db.findFirst(sql, id);
            if(store != null){
                if(0 == store.getInt("status")){
                    store.set("status_text", "停用");
                }else{
                    store.set("status_text", "启用");
                }
                jhm.put("data", store);
            }else{
                jhm.putCode(0).putMessage("门店不存在！");
            }
        } catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * @author wangze
     * @date 2018-06-27
     * 获取门店字典值接口
     * 字典值格式都是name，value格式
     */
    public void getStoreDict(){
        String sql = "select name name, id value from h_store order by sort";
        List<Record> list = Db.find(sql);
        Record record = new Record();
        record.set("name", "请选择");
        record.set("value", "-1");
        if(list != null){
            list.add(0, record);
        }else{
            list = new ArrayList<>();
            list.add(record);
        }
        JsonHashMap jhm = new JsonHashMap();
        jhm.put("data", list);
        renderJson(jhm);
    }

}
