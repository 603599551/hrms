package com.hr.question.controllers;

import com.common.controllers.BaseCtrl;

public class ExamCtrl extends BaseCtrl {

    /**
     17.1.	查看员工考核
     名称	查看员工考核
     描述	查看员工考核
     根据员工姓名模糊查询考题
     根据考试日期完全匹配查询
     验证	无
     权限	Hr可见
     URL	http://localhost:8081/mgr/exam/list
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     name	string		是	员工姓名
     date	string		是	日期

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
     "id": "记录id",
     "name": "考生姓名",
     "datetime": "2018-6-26 13:52",//考核日期
     "hiredate": "2018-6-26",//入职日期
     "kind": "传菜员/收银员",//岗位名称
     "examiner": "马云",//考官
     "result_color": "success",//数据字典的颜色，success绿色（通过），warming红色（未通过）
     "result_text": "通过",//数据字典，参见考核结果状态
     "result": "1",//数据字典，参见考核结果状态

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
        renderJson("{\"code\":1,\"data\":{\"totalRow\":1,\"pageNumber\":1,\"firstPage\":true,\"lastPage\":true,\"totalPage\":1,\"pageSize\":10,\"list\":[{\"id\":\"记录id\",\"name\":\"考生姓名\",\"datetime\":\"2018-6-26 13:52\",\"hiredate\":\"2018-6-26\",\"kind\":\"传菜员/收银员\",\"examiner\":\"马云\",\"result_color\": \"warming\",\"result_text\":\"通过\",\"result\":\"1\"}]}}");
    }

}
