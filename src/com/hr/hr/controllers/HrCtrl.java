package com.hr.hr.controllers;

import com.common.controllers.BaseCtrl;

public class HrCtrl extends BaseCtrl {

    /**

     6.5.	Hr调动店长、店员
     名称	hr调动店长、店员
     描述	Hr直接调动店长、店员。不需要调出门店、调入门店确认，给调出门店调入门店发通知
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/hr/move
     请求方式	Post
     请求参数类型	JSON

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	array		否	调动员工id
     date	string		否	调动日期
     type	string		否	调动类别
     to_store	string		否	调入门店id
     desc	string		否	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "调动成功！"
     }
     失败	{
     "code": 0,
     "message": "请选择类别！"
     }
     或者
     {
     "code": 0,
     "message": "保存失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void move(){
        renderJson("{\"code\":1,\"message\":\"调动成功！\"}");
    }

}
