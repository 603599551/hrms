package com.hr.staff.controllers;

import com.common.controllers.BaseCtrl;
import com.common.service.OrderNumberGenerator;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.HanyuPinyinHelper;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class StaffCtrl extends BaseCtrl {
    /**
     * 6.1.	员工列表
     * 名称	员工列表
     * 描述	显示在职、借调入员工信息列表电话号码、拼音模糊查询。根据查询条件进行查询，查询条件及查询方式如下：
     * 1.	关键字：根据姓名、
     * 2.	性别：完全匹配查询
     * 3.	职位：完全匹配查询
     * 4.	岗位：完全匹配查询
     * 5.	工作类型：完全匹配查询
     * 验证	无
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/staff/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * keyword	string		是	关键字
     * gender	string		是	性别id
     * dept_id	string		是	门店（部门）的id。
     * job	string		否	职位id
     * kind	array		是	岗位id
     * type	string		否	工作类型id
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
     * "id": "员工id",
     * "store_id": "门店id",
     * "store_name": "门店名",
     * "name": "姓名",
     * "gender": "性别",
     * "phone": "电话号码",
     * "job": "职位名称",
     * "kind": "岗位名称",
     * "wage": "时薪/月薪",
     * "type": "工作类型名称",
     * "status_text": "在职状态"
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
    public void list() {
//        String result = "{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"员工id\",\"store_id\":\"门店id\",\"store_name\":\"长大店\",\"name\":\"鹿晗\",\"gender\":\"男\",\"phone\":\"13888888888\",\"job\":\"员工\",\"kind\":\"收银员/传菜员\",\"wage\":\"20/1500\",\"type\":\"兼职\",\"status_text\":\"在职\"}]}}";
//        renderJson(result);
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
        try {
            String select = "SELECT h_staff.id id, h_store.id store_id, h_store. NAME store_name, h_staff. NAME name, ( CASE h_staff.gender WHEN 1 THEN '男' ELSE '女' END ) gender, h_staff.phone phone, j.job job, REPLACE (k.kind, ',', '/') kind, CONCAT( h_staff.hour_wage, '/', h_staff.month_wage ) wage, w.type type, s.status_text status_text ";
            StringBuilder sql = new StringBuilder("FROM h_staff, h_store, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff.job AND h_dictionary.parent_id = 200 ) AS job FROM h_staff ) j, ( SELECT group_concat(h. NAME) kind, s.id id FROM h_staff s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind) and h.parent_id='3000' GROUP BY s.id ORDER BY s.id ASC ) k, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff.work_type AND h_dictionary.parent_id = 300 ) AS type FROM h_staff ) w, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff. STATUS AND h_dictionary.parent_id = 500 ) AS status_text, ( SELECT h_dictionary.sort FROM h_dictionary WHERE h_dictionary. VALUE = h_staff. STATUS AND h_dictionary.parent_id = 500 ) AS sort FROM h_staff\n ) s WHERE h_staff.dept_id = h_store.id AND h_staff.id = j.id AND h_staff.id = k.id AND h_staff.id = w.id AND h_staff.id = s.id  ");
            List<Object> params = new ArrayList<>();
            if (!StringUtils.isEmpty(keyword)) {
                keyword = "%" + keyword + "%";
                sql.append(" and (h_staff.name like ? or h_staff.pinyin like ? or h_staff.phone like ?)");
                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
            }
            if (!StringUtils.isEmpty(gender)&&!StringUtils.equals(gender,"-1")) {
                sql.append(" and h_staff.gender=? ");
                params.add(gender);
            }
            if (!StringUtils.isEmpty(deptId)&&!StringUtils.equals(deptId,"-1")) {
                sql.append(" and h_staff.dept_id=? ");
                params.add(deptId);
            }

            //kind为前台传来的岗位字符串数组
            //判断kind数组中是否包含-1，即判断用户是否选了"请选择"，用户选择请选择时查所有
            if (kind != null) {
                for (String k : kind) {
                    if(!StringUtils.equals(k,"-1")){
                        sql.append(" and find_in_set(?,h_staff.kind)");
                        params.add(k);
                    }
                }
            }
            if (!StringUtils.isEmpty(job)&&!StringUtils.equals(job,"-1")) {
                sql.append(" and h_staff.job=? ");
                params.add(job);
            }
            if (!StringUtils.isEmpty(type)&&!StringUtils.equals(type,"-1")) {
                sql.append(" and h_staff.work_type=?");
                params.add(type);
            }
            //按照在职状态，员工id排序
            sql.append(" order by s.sort,h_staff.id");
            Page<Record> page = Db.paginate(pageNum, pageSize, select, new String(sql), params.toArray());
            jhm.put("data", page);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 6.2.	录入员工
     * 名称	店员入职
     * 描述	输入员工信息保存。
     * 信息有：姓名、性别、生日、电话号、住址、工号、入职日期、门店、岗位、工种（多选）、在职状态、身份证号、工作类型、级别、时薪、月薪、开户行、银行卡
     * <p>
     * 验证	姓名不能相同、手机号不能相同
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/staff/add
     * 请求方式	Post
     * 请求参数类型	JSON
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * name	string		否	姓名
     * gender	string		否	性别
     * birthday	string		是	生日
     * phone	string		否	电话号码
     * address	string		是	住址
     * emp_num	string		是	工号
     * hiredate	string		否	入职时间
     * dept_id	string		否	所在门店（部门）的id
     * job	string		否	职位。保存数据字典id
     * 内容详见数据字典的职位
     * kind	string		是	岗位。保存数据字典id
     * 内容详见数据字典的岗位
     * status	string		否	在职状态。保存数据字典id。
     * 内容详见数据字典的在职状态
     * id_num	string		是	身份证号
     * work_type	string		否	工作类型。
     * 内容详见数据字典的工作类型
     * level	string		是	级别
     * 内容详见数据字典的级别
     * hour_wage	string		是	时薪。
     * 当工作类型是兼职时，必填此项
     * month_wage	string		是	月薪
     * 当工作类型是全职时，必填此项
     * bank	string		是	开户行
     * bank_card_num	string		是	银行卡号
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "保存成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "请输入姓名！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "保存失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void add() {
        //renderJson("{\"code\":1,\"message\":\"保存成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        Record staff = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String name = staff.getStr("name").replace(" ", "");
        //员工姓名不允许为空也不允许为空格
        if (StringUtils.isEmpty(name) || StringUtils.isBlank(name)) {
            jhm.putCode(0).putMessage("请输入姓名！");
            renderJson(jhm);
            return;
        }
        String gender = staff.getStr("gender");
        //员工性别不允许为空
        if (StringUtils.isEmpty(gender)) {
            jhm.putCode(0).putMessage("请输入性别！");
            renderJson(jhm);
            return;
        }
        //员工电话不允许为空
        String phone = staff.getStr("phone").replace(" ", "");
        if (StringUtils.isEmpty(phone)) {
            jhm.putCode(0).putMessage("请输入电话！");
            renderJson(jhm);
            return;
        }
        //员工入职时间不允许为空
        String hiredate = staff.getStr("hiredate");
        if (StringUtils.isEmpty(hiredate)) {
            jhm.putCode(0).putMessage("请输入入职时间！");
            renderJson(jhm);
            return;
        }
        //员工所在门店（部门）不允许为空
        String deptId = staff.getStr("dept_id");
        if (StringUtils.isEmpty(deptId) || StringUtils.equals(deptId, "-1")) {
            jhm.putCode(0).putMessage("请输入所在门店（部门）！");
            renderJson(jhm);
            return;
        }
        //员工职位不允许为空
        String job = staff.getStr("job");
        if (StringUtils.isEmpty(job) || StringUtils.equals(job, "-1")) {
            jhm.putCode(0).putMessage("请输入职位！");
            renderJson(jhm);
            return;
        }
        //员工在职状态不允许为空
        String status = staff.getStr("status");
        if (StringUtils.isEmpty(status) || StringUtils.equals(status, "-1")) {
            jhm.putCode(0).putMessage("请输入在职状态！");
            renderJson(jhm);
            return;
        }

        //员工工作类型不允许为空
        String workType = staff.getStr("work_type");
        if (StringUtils.isEmpty(workType) || StringUtils.equals(workType, "-1")) {
            jhm.putCode(0).putMessage("请输入工作类型！");
            renderJson(jhm);
            return;
        }
        String level = staff.getStr("level");

        if (StringUtils.equals(level, "-1")) {
            jhm.putCode(0).putMessage("请输入员工级别！");
            renderJson(jhm);
            return;
        }
        String monthWage = getPara("month_wage");
        String hourWage = getPara("hour_wage");
        if (StringUtils.equals(workType,"part_time")) {
            //当工作类型是兼职时，员工时薪不允许为空
            if (StringUtils.isEmpty(hourWage)) {
                jhm.putCode(0).putMessage("请输入员工时薪！");
                renderJson(jhm);
                return;
            }
        } else if (StringUtils.equals(workType,"full_time")) {
            //当工作类型是全职时，员工月薪不允许为空
            if (StringUtils.isEmpty(monthWage)) {
                jhm.putCode(0).putMessage("请输入员工月薪！");
                renderJson(jhm);
                return;
            }
        }
        //时薪、月薪为空时转换成0
        if (StringUtils.isEmpty(monthWage)) {
            monthWage = "0";
        } else if (monthWage.length() >=6 || monthWage.contains("-")) {
            jhm.putCode(0).putMessage("输入月薪过大或月薪为负数！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(hourWage)) {
            hourWage = "0";
        } else if (hourWage.length() >=6 || hourWage.contains("-")) {
            jhm.putCode(0).putMessage("输入时薪过大或时薪为负数！");
            renderJson(jhm);
            return;
        }
        //员工姓名不能重复
        try {
            String sqlName = "select count(*) c from h_staff where name=?";
            Record recordName = Db.findFirst(sqlName, name);
            if (recordName.getInt("c") != 0) {
                jhm.putCode(0).putMessage("员工姓名重复！");
            } else {
                //员工电话号码不能重复
                String sqlPhone = "select count(*) c from h_staff where phone=?";
                Record recordPhone = Db.findFirst(sqlPhone, phone);
                if (recordPhone.getInt("c") != 0) {
                    jhm.putCode(0).putMessage("员工电话号码重复！");
                } else {
                    String[] kindList = getParaValues("kind");
                    //从前台获取岗位的字符串数组，然后转成字符串存到数据库
                    StringBuilder kind = new StringBuilder();
                    if (kindList != null) {

                        for (int i = 0; i < kindList.length; i++) {
                            if (i == 0) {
                                kind.append(kindList[i]);
                            } else {
                                kind.append(",").append(kindList[i]);
                            }
                        }
                    }
                    String id = UUIDTool.getUUID();
                    String pinyin = HanyuPinyinHelper.getFirstLettersLo(name);
                    String createrId = usu.getUserId();
                    String modifierId = usu.getUserId();
                    String idNum = staff.getStr("id_num").replace(" ", "");
                    //username 为用户姓名全拼password为123456
                    String username=HanyuPinyinHelper.getPinyinString(name);
                    //自动生成员工工号
                    String empNum= OrderNumberGenerator.getOutWarehouseOrderNumber();
                    staff.set("id", id);
                    staff.set("pinyin", pinyin);
                    staff.set("username",username);
                    staff.set("password","123456");
                    staff.set("kind", new String(kind));
                    staff.set("phone", phone);
                    staff.set("emp_num", empNum);
                    staff.set("id_num", idNum);
                    staff.set("hour_wage", hourWage);
                    staff.set("month_wage", monthWage);
                    staff.set("creater_id", createrId);
                    staff.set("modifier_id", modifierId);
                    String time = DateTool.GetDateTime();
                    staff.set("create_time", time);
                    staff.set("modify_time", time);
                    boolean flag = Db.save("h_staff", staff);
                    if (flag) {
                        //添加成功
                        jhm.putCode(1).putMessage("保存成功！");
                    } else {
                        //添加失败
                        jhm.putCode(0).putMessage("保存失败！");
                    }
                }
            }
        } catch (Exception e) {
            jhm.putCode(-1).putMessage("服务器发生异常！");
            e.printStackTrace();
        }
        renderJson(jhm);
    }

    /**
     * 6.3.	查看员工信息
     * 名称	查询员工详细信息
     * 描述	根据员工id查询员工详细信息
     * 验证	根据id验证员工是否存在
     * 权限	Hr、店长可见
     * URL	http://localhost:8081/mgr/staff/showById
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
     * "data": {
     * "id": "员工id",
     * "name": "鹿晗",
     * "gender": "0",//0：女，1：男
     * "birthday": "1990-03-29",
     * "phone": "138888888",
     * "address": "北京王府井1号",
     * "emp_num": "123", //工号
     * "hiredate": "2018-06-29", //入职时间
     * "dept_id": "部门id",
     * "dept_text": "部门名称",
     * "job": "trainer",
     * "job_text": "职位",
     * "kind": ["passed","band"],
     * "kind_text": ["传菜员","带位员"],
     * "status": "on_loan", //在职状态
     * "status_text": "在职", //在职状态
     * "id_num": "身份证号",
     * "type": "full_time", //工作类型
     * "type_text": "全职", //工作类型
     * "level": "2",//级别
     * "level_text": "二星训练员",//级别
     * "hour_wage": "16", //时薪，返回为字符串
     * "month_wage": "3000", //月薪，返回为字符串
     * "bank": "工商银行", //开户行
     * "bank_card_num": "20023987413" //银行卡号
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
        //renderJson("{\"code\":1,\"data\":{\"id\":\"员工id\",\"name\":\"鹿晗\",\"gender\":\"1\",\"gender_text\":\"男\",\"birthday\":\"1990-03-29\",\"phone\":\"138888888\",\"address\":\"北京王府井1号\",\"emp_num\":\"123\",\"hiredate\":\"2018-06-29\",\"dept_id\":\"234k5jl234j5lkj24l35j423l5j\",\"dept_text\":\"长大店\",\"job\":\"staff\",\"kind\":[\"cashier\",\"passed\"],\"status\":\"on\",\"job_text\":\"员工\",\"kind_text\":\"收银员，传菜员\",\"status_text\":\"在职\",\"id_num\":\"身份证号\",\"work_type\":\"full_time\",\"level\":\"二星训练员\",\"work_type_text\":\"全职\",\"level_text\":\"二星训练员\",\"hour_wage\":\"16\",\"month_wage\":\"3000\",\"bank\":\"工商银行\",\"bank_card_num\":\"20023987413\"}}");
        JsonHashMap jhm = new JsonHashMap();
        //获取当前员工id
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("查看失败！");
            renderJson(jhm);
            return;
        }
        try {
            String sql = "SELECT h_staff.id id, h_staff. NAME name, h_staff.gender gender, ( CASE h_staff.gender WHEN 1 THEN '男' ELSE '女' END ) gender_text, h_staff.birthday birthday, h_staff.phone phone, h_staff.address address, h_staff.emp_num emp_num, h_staff.hiredate hiredate, h_staff.dept_id dept_id, h_store. NAME dept_text, h_staff.job job, j.job job_text, h_staff.kind kind, k.kind kind_text, h_staff. STATUS status, s.status_text status_text, h_staff.id_num id_num, h_staff.work_type work_type, w.type work_type_text, h_staff. LEVEL level, l. NAME level_text, h_staff.hour_wage hour_wage, h_staff.month_wage month_wage, h_staff.bank bank, h_staff.bank_card_num bank_card_num FROM h_staff, h_store, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff.job AND h_dictionary.parent_id = 200 ) AS job FROM h_staff ) j, ( SELECT group_concat(h. NAME) kind, s.id id FROM h_staff s LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, s.kind)and h.parent_id='3000' GROUP BY s.id ORDER BY s.id ASC ) k, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff.work_type AND h_dictionary.parent_id = 300 ) AS type FROM h_staff ) w, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff. STATUS AND h_dictionary.parent_id = 500 ) AS status_text, ( SELECT h_dictionary.sort FROM h_dictionary WHERE h_dictionary. VALUE = h_staff. STATUS AND h_dictionary.parent_id = 500 ) AS sort FROM h_staff ) s, ( SELECT h_staff.id id, ( SELECT h_dictionary. NAME FROM h_dictionary WHERE h_dictionary. VALUE = h_staff. LEVEL AND h_dictionary.parent_id = 400 ) AS NAME FROM h_staff ) l WHERE h_staff.dept_id = h_store.id AND h_staff.id = j.id AND h_staff.id = k.id AND h_staff.id = w.id AND h_staff.id = s.id AND h_staff.id = l.id AND h_staff.id =? ";
            Record record = Db.findFirst(sql, id);
            if (record != null) {
                //先判断岗位是否为空，不为空将字符串按逗号分隔转成数组
                //岗位为空时将岗位转成空数组发送到前台
                if (!StringUtils.isEmpty(record.getStr("kind"))) {
                    String[] kind = record.getStr("kind").split(",");
                    record.set("kind", kind);
                } else {
                    String[] kind = {null};
                    record.set("kind", kind);
                }
                jhm.put("data", record);
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

    /**
     * 6.4.	修改员工信息
     * 名称	修改后保存员工信息
     * 描述	修改后保存员工信息
     * 信息有：姓名、性别、生日、电话号、住址、工号、入职日期、门店、岗位、工种（多选）、在职状态、身份证号、工作类型、级别、时薪、月薪、开户行、银行卡
     * <p>
     * 验证	姓名不能相同、手机号不能相同
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/staff/updateById
     * 请求方式	Post
     * 请求参数类型	JSON
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * id	string		否	员工id
     * name	string		否	姓名
     * gender	string		否	性别
     * birthday	string		是	生日
     * phone	string		否	电话号码
     * address	string		是	住址
     * emp_num	string		是	工号
     * hiredate	string		否	入职时间
     * dept_id	string		否	所在门店（部门）的id
     * job	string		否	职位。保存数据字典id
     * 内容详见数据字典的职位
     * kind	string		是	岗位。保存数据字典id
     * 内容详见数据字典的岗位
     * status	string		否	在职状态。保存数据字典id。
     * 内容详见数据字典的在职状态
     * id_num	string		是	身份证号
     * type	string		否	工作类型。
     * 内容详见数据字典的工作类型
     * level	string		是	级别
     * 内容详见数据字典的级别
     * hour_wage	string		是	时薪。
     * 当工作类型是兼职时，必填此项
     * month_wage	string		是	月薪
     * 当工作类型是全职时，必填此项
     * bank	string		是	开户行
     * bank_card_num	string		是	银行卡号
     * <p>
     * 返回数据：
     * 返回格式	JSON
     * 成功	{
     * "code": 1,
     * "message": "修改成功！"
     * }
     * 失败	{
     * "code": 0,
     * "message": "请输入姓名！"
     * }
     * 或者
     * {
     * "code": 0,
     * "message": "保存失败！"
     * }
     * 报错	{
     * "code": -1,
     * "message": "服务器发生异常！"
     * }
     */
    public void updateById() {
        //renderJson("{\"code\":1,\"message\":\"修改成功！\"}");
        JsonHashMap jhm = new JsonHashMap();
        Record staff = this.getParaRecord();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        //员工id不允许为空
        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("获取员工id失败！");
            renderJson(jhm);
            return;
        }
        //员工姓名不允许为空或空格
        String name = staff.getStr("name").replace(" ", "");
        if (StringUtils.isEmpty(name) || StringUtils.isBlank(name)) {
            jhm.putCode(0).putMessage("请输入姓名！");
            renderJson(jhm);
            return;
        }
        //员工性别不允许为空
        String gender = staff.getStr("gender");
        if (StringUtils.isEmpty(gender)) {
            jhm.putCode(0).putMessage("请输入性别！");
            renderJson(jhm);
            return;
        }
        //员工电话不允许为空
        String phone = staff.getStr("phone").replace(" ", "");
        if (StringUtils.isEmpty(phone)) {
            jhm.putCode(0).putMessage("请输入电话！");
            renderJson(jhm);
            return;
        }
        //员工入职时间不允许为空
        String hiredate = staff.getStr("hiredate");
        if (StringUtils.isEmpty(hiredate)) {
            jhm.putCode(0).putMessage("请输入入职时间！");
            renderJson(jhm);
            return;
        }
        //员工所在门店（部门）不允许为空
        String deptId = staff.getStr("dept_id");
        if (StringUtils.isEmpty(deptId) || StringUtils.equals(deptId, "-1")) {
            jhm.putCode(0).putMessage("请输入所在门店（部门）！");
            renderJson(jhm);
            return;
        }
        //员工职位不允许为空
        String job = staff.getStr("job");
        if (StringUtils.isEmpty(job) || StringUtils.equals(job, "-1")) {
            jhm.putCode(0).putMessage("请输入职位！");
            renderJson(jhm);
            return;
        }
        //员工在职状态不允许为空
        String status = staff.getStr("status");
        if (StringUtils.isEmpty(status) || StringUtils.equals(status, "-1")) {
            jhm.putCode(0).putMessage("请输入在职状态！");
            renderJson(jhm);
            return;
        }
        //员工工作类型不允许为空
        String workType = staff.getStr("work_type");
        if (StringUtils.isEmpty(workType) || StringUtils.equals(workType, "-1")) {
            jhm.putCode(0).putMessage("请输入工作类型！");
            renderJson(jhm);
            return;
        }
        String level = getPara("level");
        if (StringUtils.equals(level, "-1")) {
            jhm.putCode(0).putMessage("请输入员工级别！");
            renderJson(jhm);
            return;
        }
        String monthWage = getPara("month_wage");
        String hourWage = getPara("hour_wage");
        if (StringUtils.equals("part_time", workType)) {
            //当工作类型是兼职时，员工时薪不允许为空
            if (StringUtils.isEmpty(hourWage)) {
                jhm.putCode(0).putMessage("请输入员工时薪！");
                renderJson(jhm);
                return;
            }
        } else if (StringUtils.equals("full_time", workType)) {
            //当工作类型是全职时，员工月薪不允许为空
            if (StringUtils.isEmpty(monthWage)) {
                jhm.putCode(0).putMessage("请输入员工月薪！");
                renderJson(jhm);
                return;
            }
        }
        //时薪、月薪为空时转换成0
        if (StringUtils.isEmpty(monthWage)) {
            monthWage = "0";
        } else if (monthWage.length() > 5 || monthWage.contains("-")) {
            jhm.putCode(0).putMessage("输入月薪过大或月薪为负数！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(hourWage)) {
            hourWage = "0";
        } else if (hourWage.length() > 5 || hourWage.contains("-")) {
            jhm.putCode(0).putMessage("输入时薪过大或时薪为负数！");
            renderJson(jhm);
            return;
        }
        //员工姓名不能重复
        try {
            String sqlName = "select count(*) c from h_staff where name=? and id !=?";
            Record recordName = Db.findFirst(sqlName, name, id);
            if (recordName.getInt("c") != 0) {
                jhm.putCode(0).putMessage("员工姓名重复！");
            } else {
                //员工电话号码不能重复
                String sqlPhone = "select count(*) c from h_staff where phone=? and id!=?";
                Record recordPhone = Db.findFirst(sqlPhone, phone, id);
                if (recordPhone.getInt("c") != 0) {
                    jhm.putCode(0).putMessage("员工电话号码重复！");
                } else {
                    String[] kindList = getParaValues("kind");
                    StringBuilder kind = new StringBuilder();
                    if (kindList != null) {
                        for (int i = 0; i < kindList.length; i++) {
                            if (i == 0) {
                                kind.append(kindList[i]);
                            } else {
                                kind.append(",").append(kindList[i]);
                            }
                        }
                    }
                    String pinyin = HanyuPinyinHelper.getFirstLettersLo(name);
                    String modifierId = usu.getUserId();
                    String idNum = staff.getStr("id_num").replace(" ", "");
                    String empNum = staff.getStr("emp_num").replace(" ", "");
                    staff.set("pinyin", pinyin);
                    staff.set("kind", new String(kind));
                    staff.set("phone", phone);
                    staff.set("emp_num", empNum);
                    staff.set("id_num", idNum);
                    staff.set("hour_wage", hourWage);
                    staff.set("month_wage", monthWage);
                    staff.set("modifier_id", modifierId);
                    String time = DateTool.GetDateTime();
                    staff.set("modify_time", time);
                    staff.remove("kind_text");
                    staff.remove("gender_text");
                    staff.remove("dept_text");
                    staff.remove("job_text");
                    staff.remove("level_text");
                    staff.remove("work_type_text");
                    staff.remove("status_text");
                    boolean flag = Db.update("h_staff", staff);
                    if (flag) {
                        //修改成功
                        jhm.putCode(1).putMessage("修改成功！");
                    } else {
                        //修改失败
                        jhm.putCode(0).putMessage("保存失败！");
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }

}
