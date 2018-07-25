package com.hr.hr.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class PerformanceCtrl extends BaseCtrl {

    /**
     14.1.	绩效列表
     名称	绩效列表
     描述	根据查询条件，查询绩效考核，查询条件及查询方式如下：
     1.门店id：完全匹配
     2.开始日期：时间段
     3.结束日期：时间段
     4.关键字：根据姓名、拼音模糊查询
     验证	无
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/performance/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     store_id	string		是	门店id
     start_date	string		是	开始日期
     end_date	string		是	结束日期
     keyword	string		否	关键字

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "totalRow": 1,
     "pageNumber": 1,
     "firstPage": true,
     "lastPage": true,
     "totalPage": 1,
     "pageSize": 10,
     "list": [{
     "id": "奖罚记录id",
     "staff_id": "员工id",
     "store_id": "门店id",
     "store_name": "门店名",
     "name": "姓名",
     "type": "奖励或者惩罚",
     "date": "奖罚日期",
     "money": "金额"
     }]
     }
     }
     失败	{
     "code": 0,
     "message": "失败原因！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void list(){
        JsonHashMap jhm = new JsonHashMap();
        List<Object> params = new ArrayList<>();
        
        StringBuilder select = new StringBuilder(" SELECT p.id,p.staff_id,p.store_id,(SELECT store.name FROM h_store store WHERE store.id = p.store_id) store_name , staff.name  name , (SELECT d.name FROM h_dictionary d WHERE d.value = p.type AND  d.parent_id = 800 ) type ,p.date,p.money ");
        StringBuilder sql = new StringBuilder(" FROM h_performance p,h_staff staff where p.staff_id = staff.id  ");


        //页码和每页数据量
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum= NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        //判断所给数据是否为空
        String storeId = getPara("store_id");
        if(!(StringUtils.isEmpty(storeId) || storeId.equals("-1") )){
            sql.append( " and p.store_id = ?");
            params.add(storeId);
        }
        String startDate = getPara("start_date");
        if(!StringUtils.isEmpty(startDate)){
            sql.append( " and p.date >= ? ");
            params.add(startDate);
        }
        String endDate = getPara("end_date");
        if(!StringUtils.isEmpty(endDate)){
            sql.append(" and p.date <= ? ");
            params.add(endDate);
        }

        //模糊查询拼音和名字
        String keyWord = getPara("keyword");
        if(!StringUtils.isEmpty(keyWord)) {
            keyWord = "%" + keyWord + "%";
            sql.append(" and (staff.name like ? OR staff.pinyin like ?)");
            params.add(keyWord);
            params.add(keyWord);
        }

        try {
            Page<Record> page = Db.paginate(pageNum,pageSize,select.toString(),sql.toString(),params.toArray());
            jhm.put("data",page);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
    /**
     14.2.	添加奖罚
     姓名、原因（备选项）、金额
     名称	添加奖罚
     描述	添加奖罚。
     验证
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/add
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     staff_id	array		否	员工id
     date	string		否	日期
     type	string		否	奖或者罚，参加数据字典
     money	string		否	金额
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "添加成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择奖罚的员工！"
     }
     或者
     {
     "code": 0,
     "message": "请选择日期！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/


    //还没有测试
    public void add(){
        JsonHashMap jhm = new JsonHashMap();

        //获取staff_id
        String staffs[] = getParaValues("staff_id");

        if(!(staffs.length > 0 && staffs != null)){
            jhm.putCode(0).putMessage("请选择添加员工！");
            renderJson(jhm);
            return;
        }

        String staffId = staffs[0];

        //前台传的是String还是array
//        String staffId = getPara("staff_id");

        Record staffRecord = Db.findFirst("select s.dept_id from h_staff s where id = ?",staffId);//Db.findById("h_staff",staffId);
        String deptId = staffRecord.getStr("dept_id");

        //为空判断
        if(staffRecord == null){
            jhm.putCode(0).putMessage("找不到该员工！");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("请选择日期！");
            renderJson(jhm);
            return;
        }
        String type = getPara("type");
        if(StringUtils.isEmpty(type)){
            jhm.putCode(0).putMessage("请选择类型！");
            renderJson(jhm);
            return;
        }
        String money = getPara("money");
        if(StringUtils.isEmpty(money)){
            jhm.putCode(0).putMessage("请添加奖惩金额！");
            renderJson(jhm);
            return;
        }
        String desc = getPara("desc");
        if(StringUtils.isEmpty(desc)){
            jhm.putCode(0).putMessage("请添加奖惩说明！");
            renderJson(jhm);
            return;
        }

        Record record = new Record();

        //当前登录人时间即创建时间
        String createrTime = DateTool.GetDateTime();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String createId = usu.getUserId();

        //按格式放入数据
        record.set("id",UUIDTool.getUUID());
        record.set("staff_id",staffId);
        record.set("store_id",deptId);
        record.set("type",type);
        record.set("date",date);
        record.set("money",money);
        record.set("desc",desc);
        record.set("creater_id",createId);
        record.set("create_time",createrTime);
        record.set("modifier_id",createId);
        record.set("modify_time",createrTime);


        try{
            boolean flag = Db.save("h_performance",record);
            if(flag){
                jhm.putCode(1).putMessage("添加成功！");
            }else{
                jhm.putCode(0).putMessage("添加失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }
    /**
     14.3.	查看惩罚
     名称	查看惩罚
     描述	根据id查询惩罚信息
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/showById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "data": {
     "id": "id",//记录id
     "names": "鹿晗、吴亦凡",//员工姓名
     "date": "2018-02-04",//时间
     "type": "1",
     "type_text": "奖励",//参见奖罚类别
     "money": "200",//奖罚金额
     "desc": "说明"
     }
     }
     失败	{
     "code": 0,
     "message": "记录不存在！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void showById(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");

        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("请选择员工！");
            renderJson(jhm);
            return;
        }


        String sql = "SELECT p.id,(SELECT staff.name FROM h_staff staff WHERE staff.id = p.staff_id) names,p.date,p.type,(SELECT d.name FROM h_dictionary d WHERE p.type = d.value and d.parent_id = 800) type_text , p.money , p.desc FROM h_performance p where id = ? ";
        try{
            Record record = Db.findFirst(sql,id);
            if(record == null){
                jhm.putCode(0).putMessage("记录不存在！");
            }else{
                jhm.put("data",record);
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }
    /**
     14.4.	修改奖罚
     名称	修改后保存奖罚
     描述	根据id修改奖罚记录
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/updateById
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id
     date	string		否	日期
     type	string		否	奖或者罚，参加数据字典
     money	string		否	金额
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "修改成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择奖罚的员工！"
     }
     或者
     {
     "code": 0,
     "message": "请选择日期！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
*/
    public void updateById(){
        JsonHashMap jhm = new JsonHashMap();

        String id = getPara("id");
        if(StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择奖罚信息！");
            renderJson(jhm);
            return;
        }

        Record record = new Record();

        //为空判断
        String date = getPara("date");
        if(StringUtils.isEmpty(date)){
            jhm.putCode(0).putMessage("请输入奖惩日期！");
            renderJson(jhm);
            return;
        }
        String type = getPara("type");
        if(StringUtils.isEmpty(type) || type.equals("-1")){
            jhm.putCode(0).putMessage("请输入奖惩类型！");
            renderJson(jhm);
            return;
        }
        String money = getPara("money");
        if(StringUtils.isEmpty(money)){
            jhm.putCode(0).putMessage("请输入奖惩金额！");
            renderJson(jhm);
            return;
        }
        String desc = getPara("desc");
        if(StringUtils.isEmpty(desc)){
            jhm.putCode(0).putMessage("请输入奖惩说明！");
            renderJson(jhm);
            return;
        }

        String modifyTime = DateTool.GetDateTime();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String modifierId = usu.getUserId();

        //加入需要修改的信息，其中加入id为依据判断修改哪一个，没有的数据不会发生修改
        record.set("id",id);
        record.set("type",type);
        record.set("date",date);
        record.set("money",money);
        record.set("desc",desc);
        record.set("modifier_id",modifierId);
        record.set("modify_time",modifyTime);

        try{
            boolean flag = Db.update("h_performance",record);
            if(flag){
                jhm.putCode(1).putMessage("修改成功！");
            }else {
                jhm.putCode(0).putMessage("修改失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }
    /**
     14.5.	删除奖罚
     名称	删除奖罚
     描述	根据id删除奖罚记录
     验证	根据传入id判断记录是否存在
     权限	店长、Hr可见
     URL	http://localhost:8081/mgr/performance/deleteById
     请求方式	get
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     id	string		否	记录id

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "删除成功！"
     }
     失败	{
     "code": 0,
     "message": "失败的信息"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     */
    public void deleteById(){
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("id");
        if(StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("所选员工不能为空！");
            renderJson(jhm);
            return;
        }

        try{
            Record record = Db.findFirst("select count(*) c from h_performance where id = ?",id);

            //是否被删了已经
            if(record.getLong("c") == 0 ){
                jhm.putCode(0).putMessage("找不到该记录！");
            }else {
                boolean flag = Db.deleteById("h_performance",id);
                if(flag){
                    jhm.putCode(1).putMessage("删除成功！");
                }else{
                    jhm.putCode(0).putMessage("删除失败!");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }

}
