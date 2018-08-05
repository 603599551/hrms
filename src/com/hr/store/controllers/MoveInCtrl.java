package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class MoveInCtrl extends BaseCtrl {

    /**
    7.1.	调入员工列表
名称	查询调入员工列表
描述	查询调入员工的列表信息
验证	无
权限	店长可见
URL	http://localhost:8081/mgr/moveIn/list
请求方式	get
请求参数类型	key=value

请求参数列表：
参数名	类型	最大长度	允许空	描述
out_store_id	string		是	调出门店
In_store_id	string		是	调入门店
keyword	string		是	姓名、拼音
start_date	string		是	开始日期
end_date	string		是	结束日期
type	string		是	调出类型。接口获取

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
            "staff_id": "员工id",
			"out_store_name": "调出门店名称",
			"in_store_name": "调入门店名称",
	 		"out_store_color":"#fa7a19",
	 		"in_store_color":"#7e7e74",
			"name": "姓名",
			"date": "2018-06-23",//调出日期
			"type": "调出",//数据字典的字面值
			"id": ""//调出记录id
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

		//页码和每页数据量
		String pageNumStr=getPara("pageNum");
		String pageSizeStr=getPara("pageSize");

		int pageNum= NumberUtils.parseInt(pageNumStr,1);
		int pageSize=NumberUtils.parseInt(pageSizeStr,10);

		//sql语句
		String select = "SELECT ( SELECT h. NAME FROM h_dictionary h WHERE h.parent_id = 700 AND h.VALUE = info.type ) AS type, info.date AS date, info.id AS id, ( SELECT s. NAME FROM h_store s WHERE s.id = info.to_dept ) AS in_store_name, ( SELECT s. NAME FROM h_store s WHERE s.id = info.from_dept ) AS out_store_name, staff. NAME AS name, staff.staff_id AS staff_id ";
    	StringBuilder sql = new StringBuilder(" FROM h_move_info info, h_move_staff staff, h_staff s WHERE staff.move_info_id = info.id AND s.id = staff.staff_id");

		String outStoreId = getPara("out_store_id");
		String inStoreId = getPara("in_store_id");
		String keyWord = getPara("keyword");
		String startDate = getPara("start_date");
		String endDate = getPara("end_date");
		String status = getPara("type");

		if(!(StringUtils.isEmpty(outStoreId) || outStoreId.equals("-1") )){
			sql.append( " and info.from_dept =  ? ");
			params.add(outStoreId);
		}
		if(!(StringUtils.isEmpty(inStoreId) || inStoreId.equals("-1") )){
			sql.append( " and info.to_dept = ? ");
			params.add(inStoreId);
		}
		if(!(StringUtils.isEmpty(status) || status.equals("-1"))){
			sql.append( " and info.status = ? ");
			params.add(status);
		}
		if(!StringUtils.isEmpty(startDate) ){
			sql.append( " and info.date >= ? ");
			params.add(startDate);
		}
		if(!StringUtils.isEmpty(endDate)){
			sql.append( " and info.date <= ? ");
			params.add(endDate);
		}
		if(!(StringUtils.isEmpty(keyWord))){
			keyWord  = "%" + keyWord + "%";
			sql.append(" and (staff.name like ? or staff.phone like ? or s.pinyin like ?)");
			params.add(keyWord);
			params.add(keyWord);
			params.add(keyWord);
		}

		try{
			Page<Record> page = Db.paginate(pageNum,pageSize,select,sql.toString(),params.toArray());
			//加入颜色
			for(int i = 0 ; i < page.getList().size() ; ++i){
				page.getList().get(i).set("out_store_color","#b7a6d4");
				page.getList().get(i).set("in_store_color","#fa7a19");
			}
			jhm.put("data",page);
		} catch (Exception e){
			e.printStackTrace();
			jhm.putCode(-1).putMessage("服务器发生异常！");
		}
		renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"staff_id\":\"员工id\",\"out_store_name\":\"面对面（长大店）\",\"in_store_name\":\"面对面（红旗街店）\",\"out_store_color\":\"#b7a6d4\",\"in_store_color\":\"#fa7a19\",\"name\":\"马云\",\"date\":\"2018-06-23\",\"type\":\"调出\",\"id\":\"id\"}]}}");
    }
    /**
7.2.	查看调入信息
名称	查询调入详细信息
描述	根据调入记录的id查询详细信息
验证	无
权限	店长可见
URL	http://localhost:8081/mgr/moveIn/showById
请求方式	get
请求参数类型	key=value

请求参数列表：
参数名	类型	最大长度	允许空	描述
id	string		是	调入记录的id

返回数据：
返回格式	JSON
成功	{
	"code": 1,
	"data": {
            "out_store_name": "长大店",//来源门店名称
"staffList":[
{
   "name": "鹿晗",
   "gender": "男",
   "phone": "1370000",
   "job": "员工",//职位
   "kind": "服务员、传菜员",
   "money": "16",
   "work_type": "全职"//工作类型
}
],
			"date": "2018-06-23",//调出日期
			"type": "调入",//数据字典的字面值
			"id": "",//调出记录id
	  "desc":"说明"
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
     {"code":1,"data":{"out_store_name":"长大店","staffList":[{"name":"鹿晗","gender":"男","phone":"1370000","job":"员工","kind":["服务员","传菜员"],"money":"16","work_type":"全职"}],"date":"2018-06-23","type":"调入","id":""}}

     */
    public void showById(){
    	JsonHashMap jhm = new JsonHashMap();
		String id = getPara("id");

		//先查询h_move_info得到部分数据
		String selectInfo = "SELECT (SELECT s.name FROM h_store s WHERE s.id = info.from_dept ) as out_store_name  , info.desc, info.date as date , (SELECT d.name FROM h_dictionary d WHERE d.parent_id = 700 AND d.value = info.type) as type , info.id as id FROM h_move_info info where id = ?";
		try {
			Record record = Db.findFirst(selectInfo,id);
			if(record != null){
				//再查h_move_staff表得到员工信息
				String selectStaff = " SELECT (SELECT d.name FROM h_dictionary d WHERE d.parent_id = 200 AND d.id = staff.job) as job, staff. NAME AS name, staff.hour_wage AS money, staff.phone AS phone, ( CASE staff.gender WHEN '1' THEN '男' ELSE '女' END ) AS gender, ( SELECT d. NAME FROM h_dictionary d WHERE d. VALUE = staff.work_type AND d.parent_id = 300 ) AS work_type, ( SELECT group_concat(h. NAME) kind FROM h_move_staff s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind) WHERE s.id = staff.id GROUP BY s.id ORDER BY s.id ASC ) AS kind FROM h_move_info info, h_move_staff staff WHERE info.id = staff.move_info_id AND staff.move_info_id = ? ";
				List<Record> recordList = Db.find(selectStaff,id);
				//按要求格式丢进去
				recordList.get(0).getStr("kind").replace(",","、");
				record.set("staffList",recordList);
				jhm.put("data",record);
			} else {
				jhm.putCode(0).putMessage("该记录不存在！");
			}
		} catch (Exception e){
			e.printStackTrace();
			jhm.putCode(-1).putMessage("服务器发生异常");
		}

    	renderJson(jhm);
//        renderJson("{\"code\":1,\"data\":{\"out_store_name\":\"面对面（长大店）\",\"staffList\":[{\"name\":\"鹿晗\",\"gender\":\"男\",\"phone\":\"13888888888\",\"job\":\"员工\",\"kind\":\"收银员/传菜员\",\"money\":\"20\",\"work_type\":\"全职\"}],\"date\":\"2018-06-23\",\"type\":\"调入\",\"id\":\"id\",\"desc\":\"最近腰腿没有劲，打算去锻炼锻炼\"}}");
    }

}
