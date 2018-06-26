package com.hr.store.controllers;

import com.common.controllers.BaseCtrl;

public class StoreMgrCtrl extends BaseCtrl {

    /**
     6.6.	辞退
     名称	辞退店长、员工
     描述	辞退店长、员工。将【在职状态】从“在职”改为“离职”
     验证	辞退员工是否存在
     权限	店长、hr可见
     URL	http://localhost:8081/mgr/storeMgr/fire
     请求方式	get
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     staff_id	string		否	辞退员工id
     date	string		否	日期
     desc	string		否	原因

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": "辞退成功！"
     }
     失败	{
     "code": 0,
     "message": "此员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": "辞退失败！"
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }
     */
    public void fire(){
        renderJson("{\"code\":1,\"message\":\"辞退成功！\"}");
    }

}
