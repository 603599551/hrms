package com.hr.question.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.NumberUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class ExamCtrl extends BaseCtrl {

    /**
     * 17.1.	查看员工考核
     * 名称	查看员工考核
     * 描述	查看员工考核
     * 根据员工姓名模糊查询考题
     * 根据考试日期完全匹配查询
     * 验证	无
     * 权限	Hr可见
     * URL	http://localhost:8081/mgr/exam/list
     * 请求方式	get
     * 请求参数类型	key=value
     * <p>
     * 请求参数列表：
     * 参数名	类型	最大长度	允许空	描述
     * name	string		是	员工姓名
     * date	string		是	日期
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
     * "id": "记录id",
     * "name": "考生姓名",
     * "datetime": "2018-6-26 13:52",//考核日期
     * "hiredate": "2018-6-26",//入职日期
     * "kind": "传菜员/收银员",//岗位名称
     * "examiner": "马云",//考官
     * "result_color": "success",//数据字典的颜色，success绿色（通过），warming红色（未通过）
     * "result_text": "通过",//数据字典，参见考核结果状态
     * "result": "1",//数据字典，参见考核结果状态
     * <p>
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
//        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"记录id\",\"name\":\"考生姓名\",\"datetime\":\"2018-6-26 13:52\",\"hiredate\":\"2018-6-26\",\"kind\":\"传菜员/收银员\",\"examiner\":\"马云\",\"result_color\": \"warming\",\"result_text\":\"通过\",\"result\":\"1\"}]}}");

        JsonHashMap jhm = new JsonHashMap();
        UserSessionUtil usu = new UserSessionUtil(getRequest());
        String pageNumStr = getPara("pageNum");
        String pageSizeStr = getPara("pageSize");
        //如果为空时赋给默认值
        int pageNum = NumberUtils.parseInt(pageNumStr, 1);
        int pageSize = NumberUtils.parseInt(pageSizeStr, 10);
        String name = getPara("name");
        String date = getPara("date");

        try {
            String select = "select e.id id, hs.name name,e.create_time datetime,e.hiredate hiredate,(SELECT group_concat(h. NAME) kind FROM h_exam ee LEFT JOIN h_dictionary h ON find_in_set(h. VALUE, ee.kind_id)where h.parent_id = '3000' and ee.id=e.id GROUP BY ee.id ORDER BY ee.id ASC ) kind,(select s.name from h_staff s where s.id=e.examiner_id) examiner,(case result when '1' then 'success' else 'warnning' end)result_color,(case result when'1'then'通过'else'未通过' end)result_text,result result ";
            StringBuilder sql = new StringBuilder("from h_exam e,h_staff hs  where hs.id=e.staff_id ");
            List<Object> params = new ArrayList<>();
            //获取当前登录人的id和门店id
            String id=usu.getUserId();
            String storeId=usu.getUserBean().getDeptId();
            //当前登录人不是admin
            if(!StringUtils.equals(id,"1")){
                sql.append(" and hs.dept_id=? ");
                params.add(storeId);
            }
            if (!StringUtils.isEmpty(name)) {
                name = "%" + name + "%";
                sql.append(" and hs.name like ? ");
                params.add(name);
            }
            if (!StringUtils.isEmpty(date)) {
                date=date+"%";
                sql.append(" and e.create_time like ? ");
                params.add(date);
            }
            sql.append(" order by e.create_time,e.id");
            Page<Record> page = Db.paginate(pageNum, pageSize, select, new String(sql), params.toArray());
            jhm.put("data", page);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);
    }
}
