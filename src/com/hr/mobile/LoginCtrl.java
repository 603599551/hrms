package com.hr.mobile;

import com.bean.UserBean;
import com.common.controllers.BaseCtrl;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.sun.prism.impl.Disposer;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

public class LoginCtrl extends BaseCtrl{
    /**
     * 名称	登录
     描述	输入用户名、密码进行登录
     URL	http://localhost:8081/mobile/login
     请求方式	post
     请求参数类型	key=value

     请求参数：
     参数名	类型	最大长度	允许空	描述
     username	string		否	用户名
     password	string		否	密码

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "登录成功！",
     "user_info": {
     "id": "用户id",
     "username": "登录名",
     "name": "姓名",
     "pinyin": "拼音",
     "pic":"头像url"
     "gender": "性别字面值",
     "birthday": "生日",
     "phone": "电话号码",
     "address": "地址",
     "emp_num": "工号",
     "hiredate": "入职时间yyyy-MM-dd",
     "dept_id": "所在门店（部门）id",
     "dept_name": "所在门店（部门）名称",  *
     "job": "职位字面值",
     "kind": "岗位字面值，多个岗位由英文逗号分隔",
     "status": "在职状态字面值",
     "id_num": "身份证号",
     "work_type": "工作类型字面值",
     "level": "级别字面值",
     "hour_wage": "时薪",
     "month_wage": "月薪",
     "bank": "开户行",
     "bank_card_num": "银行卡号",
     "creater_id": "创建人姓名",
     "create_time": "创建时间",
     "modifier_id": "修改人姓名",
     "modify_time": "修改时间"
     "store_coordinates":"经纬度，由英文逗号分隔，经度在前" *
     }
     }
     失败	{
     "code": 0,
     "message": "请输入登录名！"
     }
     或者
     {
     "code": 0,
     "message": "登录失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */

    public void index(){
        JsonHashMap jhm = new JsonHashMap();
        try {
            String username = getPara("username");
            String password = getPara("password");
            String type = getPara("type");

            //对传入参数进行非空验证
            if(StringUtils.isEmpty("username")){
                jhm.putCode(0).putMessage("请输入用户名！");
                renderJson(jhm);
                return;
            }
            if(StringUtils.isEmpty("password")){
                jhm.putCode(0).putMessage("请输入密码！");
                renderJson(jhm);
                return;
            }
            if(StringUtils.isEmpty("type")){
                jhm.putCode(0).putMessage("请输入登陆端！");
                renderJson(jhm);
                return;
            }

            //对传入信息进行条件验证
            Record record = Db.findFirst("select *, (select name from h_store s where h_staff.dept_id = s.id) dept_name, (select coordinates from h_store s where h_staff.dept_id = s.id) store_coordinates, (select store_color from h_store s where s.id=h_staff.dept_id) store_color,(select city from h_store s where s.id=h_staff.dept_id) city, (select name from h_dictionary d where h_staff.job = d.value) job_name from h_staff where username = ? and password = ?", username, password);
            if(record !=null){
                //判断员工是否已离职
                if(StringUtils.equals("quit",record.getStr("status"))){
                    jhm.putCode(0).putMessage("离职员工不能登录！");
                    renderJson(jhm);
                    return;
                }

                //暂时只有餐厅经理，副经理，见习经理能够登陆店长端
                String sql = "SELECT count(*) AS c FROM h_staff s WHERE s.username = ? AND s.PASSWORD = ? AND (s.job = 'store_manager' OR s.job = 'assistant_manager' OR s.job = 'trainee_manager' ) ";
                Record r = Db.findFirst(sql, username, password);
                if(StringUtils.equals("1",type) && r.getInt("c") == 0){
                    jhm.putCode(0).putMessage("您没有登录权限！");
                    renderJson(jhm);
                    return;
                }
                if(StringUtils.equals("0",type) && r.getInt("c") != 0){
                    jhm.putCode(0).putMessage("您没有登录权限！");
                    renderJson(jhm);
                    return;
                }
                UserBean ub = new UserBean();
                ub.setId(record.get("id"));
                ub.setName(record.getStr("username"));
                ub.setRealName(record.getStr("name" ));
                ub.setDeptId(record.getStr("dept_id"));
                ub.setDeptName(record.getStr("dept_name"));
                ub.put("store_id", record.getStr("dept_id"));
                ub.put("store_color", record.getStr("store_color"));
                ub.put("city", record.getStr("city"));
                Object job=record.get("job");
                if(job==null) {
                    job = "";
                }else {
                    job = job + "";
                }
                ub.setJobId((String)job);
                ub.setJobName(record.getStr("job_name"));
                setSessionAttr(KEY.SESSION_USER,ub);
                setCookie("userId", record.get("id"), 60 * 60 * 24 * 3);
                if(record.getStr("kind") == null){
                    record.set("kind","");
                }
                //处理经纬度格式
                JSONObject  coordinates = JSONObject.fromObject(record.getStr("store_coordinates"));
                record.set("store_coordinates", coordinates);
                jhm.put("user_info",record);
                jhm.put("sessionId",getSession().getId());
                jhm.putCode(1).putMessage("登录成功！");
            }else{
                jhm.putCode(0).putMessage("用户名或密码错误！登录失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
