package com.hr.staff.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.staff.service.StaffSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class StaffNotOnJobCtrl extends BaseCtrl {


    /**
     * 6.7.	不在职员工列表
     * 名称	不在职员工列表
     * 描述	显示离职、调出、调出（借调）员工信息列表。根据查询条件进行查询，查询条件及查询方式如下：
     * 6.	关键字：根据姓名、电话号码、拼音模糊查询      //h_staff_log表中没有pinyin字段？？？
     * 7.	性别：完全匹配查询
     * 8.	职位：完全匹配查询
     * 9.	岗位：完全匹配查询
     * 10.	工作类型：完全匹配查询
     * 11.	不在职状态：完全匹配查询                   //不能根据不在职状态完全匹配查询？？？
     * 验证	无
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/staffNotOnJob/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * keyword	string		是	关键字
     * gender	string		是	性别id
     * dept_id	string		是	门店（部门）的id。
     * job	string		是	职位id
     * kind	array		是	岗位id
     * type	string		是	工作类型id
     * status	string		是	不在职状态id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "totalRow": 1,
     * "pageNumber": 1,
     * "firstPage": true,
     * "lastPage": true,
     * "totalPage": 1,
     * "pageSize": 10,
     * "list": [{
     * "id": "员工id",               //应该是h_staff_log表中的id字段吧？？？
     * "store_id": "门店id",
     * "store_name": "门店名",
     * "name": "姓名",
     * "gender": "性别",
     * "phone": "电话号码",
     * "job": "职位名称",
     * "kind": "岗位名称",
     * "wage": "时薪/月薪",
     * "type": "工作类型名称",
     * "status_text": "不在职状态"
     * }]
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "失败原因！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */

    private StaffSrv service = enhance(StaffSrv.class);

    public void list() {
        //renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"员工id\",\"store_id\":\"234k5jl234j5lkj24l35j423l5j\",\"store_name\":\"面对面（长大店）\",\"name\":\"鹿晗\",\"gender\":\"男\",\"phone\":\"13888888888\",\"job\":\"员工\",\"kind\":\"收银员/传菜员\",\"wage\":\"20/1500\",\"type\":\"全职\",\"status_text\":\"不在职状态\"}]}}");
        JsonHashMap jhm = new JsonHashMap();
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");
        //如果为空时赋给默认值
        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);

        String keyword = getPara("keyword").replace(" ", "");
        String gender = getPara("gender");
        String deptId = getPara("dept");
        String job = getPara("job");
        String[] kind = getParaValues("kind");
        String type = getPara("type");
        // String status =getPara("status");
        try {
            String select = "SELECT h_staff_log.id id, h_store.id store_id, h_store. NAME store_name, h_staff_log. NAME name, ( CASE h_staff_log.gender WHEN 1 THEN '男' ELSE '女' END ) gender, h_staff_log.phone phone, j.job job, REPLACE (k.kind, ',', '/') kind, CONCAT( h_staff_log.hour_wage, '/', h_staff_log.month_wage ) wage, w.type type, s.status_text status_text ";
            StringBuilder sql = new StringBuilder("FROM h_staff_log, h_store, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log.job AND h_dictionary.parent_id = 200 ) AS job FROM h_staff_log ) j, ( SELECT group_concat(h. NAME) kind, s.id id FROM h_staff_log s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind)and h.parent_id='3000' GROUP BY s.id ORDER BY s.id ASC ) k, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log.work_type AND h_dictionary.parent_id = 300 ) AS type FROM h_staff_log ) w, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log. STATUS AND h_dictionary.parent_id = 500 ) AS status_text, ( SELECT h_dictionary.sort FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log. STATUS AND h_dictionary.parent_id = 500 ) AS sort FROM h_staff_log ) s WHERE h_staff_log.dept_id = h_store.id AND h_staff_log.id = j.id AND h_staff_log.id = k.id AND h_staff_log.id = w.id AND h_staff_log.id = s.id and h_staff_log.status not in('on','on_load')");
            List<Object> params = new ArrayList<>();
            if (!StringUtils.isEmpty(keyword)) {
                keyword = "%" + keyword + "%";
                sql.append(" and (h_staff_log.name like ?or h_staff_log.pinyin like ? or h_staff_log.phone like ? ) ");
                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
            }
            //当选择“请选择”时，查所有
            if (!StringUtils.isEmpty(gender) && !StringUtils.equals(gender, "-1")) {
                sql.append(" and h_staff_log.gender=? ");
                params.add(gender);
            }
            if (!StringUtils.isEmpty(deptId) && !StringUtils.equals(deptId, "-1")) {
                sql.append(" and h_staff_log.dept_id=? ");
                params.add(deptId);
            }
            //kind为前台传来的岗位字符串数组
            if (kind != null) {
                for (String k : kind) {
                    if(!StringUtils.equals(k,"-1")){
                        sql.append(" and find_in_set(?,h_staff_log.kind)");
                        params.add(k);
                    }
                }
            }
            if (!StringUtils.isEmpty(job) && !StringUtils.equals(job, "-1")) {
                sql.append(" and h_staff_log.job=? ");
                params.add(job);
            }
            if (!StringUtils.isEmpty(type) && !StringUtils.equals(type, "-1")) {
                sql.append(" and h_staff_log.work_type=?");
                params.add(type);
            }
            //按照不在职状态，员工id排序
            sql.append(" order by s.sort,h_staff_log.staff_id");
            Page<Record> page = Db.paginate(pageNum, pageSize, select, new String(sql), params.toArray());
            jhm.put("data", page);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 6.8.	查看不在职员工信息
     * 名称	查询员工详细信息
     * 描述	根据员工id查询不在职员工详细信息
     * 验证	根据id验证员工是否存在
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/staffNotOnJob/showById
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	员工id                     //应该是h_staff_log表中的id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "data": {
     * "id": "员工id",                          //应该是h_staff_log表的id
     * "name": "鹿晗",
     * "gender": "0",//0：女，1：男
     * "birthday": "1990-03-29",
     * "phone": "138888888",
     * "address": "北京王府井1号",
     * "emp_num": "123", //工号
     * "hiredate": "2018-06-29", //入职时间
     * "dept_id": "部门id",
     * "job": "职位",
     * "kind": "岗位",
     * "status": "在职", //在职状态
     * "id_num": "身份证号",
     * "type": "全职", //工作类型
     * "level": "二星训练员",//级别
     * "hour_wage": "16", //时薪，返回为字符串
     * "month_wage": "3000", //月薪，返回为字符串
     * "bank": "工商银行", //开户行
     * "bank_card_num": "20023987413", //银行卡号
     * "desc": "不在职原因",
     * "modifier_id":"操作人"
     * }
     * }
     * 失败	{
     * "code": 0,
     * "message": "员工不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": ""//其实失败信息
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */

    public void showById() {
//        renderJson("{\"code\":1,\"data\":{\"id\":\"员工id\",\"name\":\"鹿晗\",\"gender\":\"1\",\"gender_text\":\"女\",\"birthday\":\"1990-03-29\",\"phone\":\"138888888\",\"address\":\"北京王府井1号\",\"emp_num\":\"123\",\"hiredate\":\"2018-06-29\",\"dept_text\":\"面对面（长大店）\",\"dept_id\":\"234k5jl234j5lkj24l35j423l5j\",\"job\":\"trainer\",\"job_text\":\"员工\",\"kind\":[\"passed\",\"band\"],\"kind_text\":\"传菜员/带位员\",\"status\":\"on_loan\",\"status_text\":\"在职\",\"id_num\":\"身份证号\",\"work_type\":\"full_time\",\"work_type_text\":\"全职\",\"level\":\"2\",\"level_text\":\"二星训练员\",\"hour_wage\":\"16\",\"month_wage\":\"3000\",\"bank\":\"工商银行\",\"bank_card_num\":\"20023987413\",\"desc\":\"不在职原因\",\"modifier_id\":\"操作人\"}}");
        JsonHashMap jhm = new JsonHashMap();
        //获取当前员工所在h_staff_id表的id
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("查看失败！");
            renderJson(jhm);
            return;
        }
        try {
            String sql = "SELECT h_staff_log.id id, h_staff_log. NAME name, h_staff_log.gender gender, ( CASE h_staff_log.gender WHEN 1 THEN '男' ELSE '女' END ) gender_text, h_staff_log.birthday birthday, h_staff_log.phone phone, h_staff_log.address address, h_staff_log.emp_num emp_num, h_staff_log.hiredate hiredate, h_store. NAME dept_text, h_staff_log.dept_id dept_id, h_staff_log.job job, j.job job_text, h_staff_log.kind kind, k.kind kind_text, h_staff_log. STATUS STATUS, s.status_text status_text, h_staff_log.id_num id_num, h_staff_log.work_type work_type, w.type work_type_text, h_staff_log. LEVEL LEVEL, l. NAME level_text, h_staff_log.hour_wage hour_wage, h_staff_log.month_wage month_wage, h_staff_log.bank bank, h_staff_log.bank_card_num bank_card_num, h_staff_log.DESC 'desc', h_staff_log.modifier_id modifier_id FROM h_staff_log, h_store, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log.job AND h_dictionary.parent_id = 200 ) AS job FROM h_staff_log ) j, ( SELECT group_concat(h. NAME) kind, s.id id FROM h_staff_log s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind) AND h.parent_id = '2000' GROUP BY s.id ORDER BY s.id ASC ) k, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log.work_type AND h_dictionary.parent_id = 300 ) AS type FROM h_staff_log ) w, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log. STATUS AND h_dictionary.parent_id = 500 ) AS status_text, ( SELECT h_dictionary.sort FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log. STATUS AND h_dictionary.parent_id = 500 ) AS sort FROM h_staff_log ) s, ( SELECT h_staff_log.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff_log. LEVEL AND h_dictionary.parent_id = 400 ) AS NAME FROM h_staff_log ) l WHERE h_staff_log.dept_id = h_store.id AND h_staff_log.id = j.id AND h_staff_log.id = k.id AND h_staff_log.id = w.id AND h_staff_log.id = s.id AND h_staff_log.id = l.id AND h_staff_log.id = ? ";
            Record record = Db.findFirst(sql, id);
            if (record != null) {
                if (!StringUtils.isEmpty((record.getStr("kind")))) {
                    String[] kind = record.getStr("kind").split(",");
                    record.set("kind", kind);
                } else {
                    String[] kind = {null};
                    record.set("kind", kind);
                }
                jhm.put("data", record);
            } else {
                jhm.putCode(0).putMessage("员工不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 6.10.	恢复在职
     * 名称	将离职员工恢复在职
     * 描述	通过id将离职员工恢复在职
     * 验证	根据id验证员工是否存在
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/staffNotOnJob/recovery
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	员工id
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "恢复成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "员工不存在！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": ""//其实失败信息
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void recovery() {

        //renderJson("{\"code\":1,\"message\":\"恢复成功！\"}");

        //执行事务，同步进行，在h_staff表中添加恢复员工记录,更新h_staff_log表中的记录status改为“on”
        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        Record record = this.getParaRecord();
        //员工id不允许为空
        String ids = getPara("ids");
        if (StringUtils.isEmpty(ids)) {
            jhm.putCode(0).putMessage("请选择离职员工！");
            renderJson(jhm);
            return;
        }
        //判断员工是否存在
        try {
            service.recovery(record, usu);
            jhm.putCode(1).putMessage("恢复成功!");

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
