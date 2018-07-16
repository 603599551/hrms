package paiban.controllers;

import com.common.controllers.BaseCtrl;

public class StaffIdleTimeCtrl extends BaseCtrl {

    /**

     6.9.	录入下周可上班时间（王泽）
     名称	录入员工下周闲时
     描述	通过id将员工闲时信息录入到数据库中
     验证	根据id验证员工是否存在
     权限	Hr、店长可见
     URL	http://localhost:8081/mgr/staffCtrl/saveTime
     请求方式	post
     请求参数类型	key=value

     请求参数列表：
     参数名	类型	最大长度	允许空	描述
     id	string		否	员工id
     times	string		否	员工的显示时间段

     返回数据：
     返回格式	JSON
     成功	{
     "code": 1,
     "message": “添加成功！”
     }
     失败	{
     "code": 0,
     "message": "员工不存在！"
     }
     或者
     {
     "code": 0,
     "message": ""//其实失败信息
     }
     报错	{
     "code": -1,
     "message": "服务器发生异常！"
     }

     times=07:30:00-23:59:59,07:30:00-13:59:59  week=4  id=员工id
     */

    public void saveTime(){
        renderJson("{\"code\":1,\"message\":\"添加成功！\"}");
    }

    /**
     * 获取员工闲时
     */
    public void getStaffIdleTime(){
        renderJson("{\"code\":1,\"message\":\"\",\"data\":{\"week\":\"0\",\"staff\":{\"id\":\"员工id\",\"gender\":\"男\",\"phone\":\"13888888888\",\"name\":\"鹿晗\"},\"list\":[[\"09:30:00-18:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"10:30:00-15:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"],[\"07:30:00-08:30:00\",\"07:30:00-08:30:00\"]]}}");
    }
}
