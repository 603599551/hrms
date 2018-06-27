package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;

public class MoveOutCtrl extends BaseCtrl {

    /**
     * 名称	店长调出（借调出）店员
     描述	店长调出店员，要给调入门店发通知
     验证	无
     权限	店长可见
     URL	http://localhost:8081/mgr/moveOut/out
     请求方式	Post
     请求参数类型	JSON

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	array		否	调出员工id
     date	string		否	调出日期
     type	string		否	调动类型。参见数据字典
     to_store	string		否	调入门店id
     desc	string		是	说明

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "调出成功！"
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

    public void out(){
        renderJson("{\n" +
                "     \"code\": 1,\n" +
                "     \"message\": \"调出成功！\"\n" +
                "     }");
    }

}
