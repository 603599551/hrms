package com.hr.store.controllers;

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
		UserSessionUtil usu = new UserSessionUtil(getRequest());

		Record managerDept = Db.findFirst("select dept_id as dept from h_staff where id = ?",usu.getUserId());
		//页码和每页数据量
		String pageNumStr=getPara("pageNum");
		String pageSizeStr=getPara("pageSize");

		int pageNum= NumberUtils.parseInt(pageNumStr,1);
		int pageSize=NumberUtils.parseInt(pageSizeStr,10);

		//sql语句
		String select = "SELECT ( SELECT h. NAME FROM h_dictionary h WHERE h.parent_id = 700 AND h.VALUE = info.type ) AS type, info.date AS date, info.id AS id, ( SELECT s. NAME FROM h_store s WHERE s.id = info.to_dept ) AS in_store_name, ( SELECT s. NAME FROM h_store s WHERE s.id = info.from_dept ) AS out_store_name, staff. NAME AS name, staff.staff_id AS staff_id ";
    	StringBuilder sql = new StringBuilder(" FROM h_move_info info, h_move_staff staff, h_staff s WHERE info.to_dept = ? AND info.status = '3' AND staff.move_info_id = info.id AND s.id = staff.staff_id ");
		params.add(managerDept.getStr("dept"));

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

		//增加排序
		sql.append(" order by info.date desc");

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
				recordList.get(0).getStr("kind");
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

	/**
	 * 4.6 调入店员
	 * 名称 店长接收调入（借调入）店员
	 * 描述 店长调入或者拒绝调入店员，要给来源门店发确认接收的通知
	 		并要修改调出表的状态（status）
	 		并且将该条通知的状态设置为已办理
	   验证 此记录是否存在
	   权限 店长可见
	   URL http://localhost:8081/mgr/moveIn/in
	   请求方式 get
	   请求参数类型 key=value
	 参数名	类型	最大长度	允许空	描述
	 id		string		否	调出记录的id（注意是调出记录的id）
	 desc	string		是	说明
	 status	string		否	0：拒绝，拒绝时必须填写说明 1：同意

	 返回数据：
	 返回格式	JSON
	 成功	{
	 "code": 1,
	 "message": "调入成功！"
	 }
	 失败	{
	 "code": 0,
	 "message": "此记录不存在！"
	 }
	 或者
	 {
	 "code": 0,
	 "message": "调入失败！"
	 }
	 报错	{
	 "code": -1,
	 "message": "服务器发生异常！"
	 }
	 */
	public void in(){
		JsonHashMap jhm = new JsonHashMap();
		//调出记录的id
		String id = getPara("id");
		//说明
		String desc=getPara("desc");
		//同意或拒绝
		String status=getPara("status");

		try {
			//判断move_info表中的调出记录是否存在
			Record moveinfo=Db.findFirst("SELECT * FROM h_move_info WHERE id=?",id);
			if (moveinfo==null){
				jhm.putCode(0).putMessage("此记录不存在！");
				renderJson(jhm);
				return;
			}
			//修改move_info表记录的status,result
		    Db.update("UPDATE h_move_info SET status=? , result=? WHERE id=?",status,desc,id);

			//sender_id
			String senderId=Db.findFirst("SELECT id FROM h_staff WHERE dept_id=(SELECT to_dept FROM h_move_info WHERE id=?) AND job='store_manager'",id).getStr("id");
			//receiver_id
			String receiverId=Db.findFirst("SELECT id FROM h_staff WHERE dept_id=(SELECT from_dept FROM h_move_info WHERE id=?) AND job='store_manager'",id).getStr("id");
			//在notice表创建“给来源门店发确认接收的通知”，状态设置为已办理
			Record r2=new Record();
			r2.set("id", UUIDTool.getUUID());
			r2.set("title", "店长接收调入（借调入）店员");
			r2.set("content", desc);
			r2.set("sender_id", senderId);
			r2.set("receiver_id", receiverId);
			r2.set("create_time", DateTool.GetDateTime());
			r2.set("status", "2");
			r2.set("type", "movein_notice");
			r2.set("fid", id);
			Db.save("h_notice",r2);

			//在staff_log创建记录
			Record r3=Db.findFirst("SELECT * FROM h_move_staff WHERE move_info_id=?",id);
			if (r3==null){
				jhm.putCode(0).putMessage("此move_staff不存在！");
				renderJson(jhm);
				return;
			}
			Record r4=Db.findFirst("SELECT type,t.desc FROM h_move_info t WHERE id=?",id);
			String operateType=r4.getStr("type");
			String desc2=r4.getStr("desc");
			//move_infoID
			r3.set("creater_id",senderId);
			r3.set("create_time",DateTool.GetDateTime());
			r3.set("modifier_id",null);
			r3.set("modify_time",null);
			r3.set("fid",id);
			r3.set("operater_id",senderId);
			r3.set("operate_time",DateTool.GetDateTime());
			r3.set("operate_type",operateType);
			r3.set("desc",desc2);
			r3.remove("move_info_id");
			boolean flag=Db.save("h_staff_log",r3);

			String staffId=r3.getStr("staff_id");
			String pinyin=Db.findFirst("SELECT pinyin FROM h_staff WHERE id=?",staffId).getStr("pinyin");
			r3.set("pinyin",pinyin);

			//更改staff表的dept_id
			Db.update("UPDATE h_staff SET dept_id=(SELECT to_dept FROM h_move_info WHERE id=?)  WHERE id=?",id,staffId);

			if (flag){
				jhm.putCode(1).putMessage("调入成功！");
			}else {
				jhm.putCode(0).putMessage("调入失败！");
			}

		} catch (Exception e){
			e.printStackTrace();
			jhm.putCode(-1).putMessage("服务器发生异常");
		}

		renderJson(jhm);
	}

}
