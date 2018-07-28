package com.hr.mobile.leave.services;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.Map;

public class LeaveSrv {
    /*
    增加事务
     */
    @Before(Tx.class)
    public JsonHashMap apply(Map paraMap) {
        UserSessionUtil usu = (UserSessionUtil) paraMap.get("usu");
        String date = (String) paraMap.get("date");
        String time = (String) paraMap.get("time");

        String userId = usu.getUserId();

        JsonHashMap jhm = new JsonHashMap();

        String[] timeArray = time.split("-");
        String datetime = DateTool.GetDateTime();//yyyy-MM-dd HH:mm:ss
        String sql = "select count(*) as c from h_staff_leave where staff_id=? and store_id=? and date=? and leave_start_time=? and leave_end_time=? and status='0'";
//        try {
        Record countR = Db.findFirst(sql, userId, usu.getUserBean().getDeptId(), date, timeArray[0], timeArray[1]);
        Object cObj = countR.get("c");
        int c = NumberUtils.parseInt(cObj, 0);
        if (c > 0) {
            jhm.putCode(0).putMessage("请不要重复提交数据！");
            return jhm;
        }

        Record r = new Record();
        r.set("id", UUIDTool.getUUID());
        r.set("staff_id", userId);
        r.set("store_id", usu.getUserBean().getDeptId());
        r.set("date", date);
        r.set("leave_start_time", timeArray[0]);
        r.set("leave_end_time", timeArray[1]);
        r.set("status", 0);
        r.set("creater_id", userId);
        r.set("create_time", datetime);
        r.set("modifier_id", userId);
        r.set("modify_time", datetime);

        Record noticeR = new Record();
        noticeR.set("id", "1");

        boolean flag = Db.save("h_staff_leave", r);
        Db.save("h_notice", noticeR);


        if (flag) {
            jhm.putCode(1).putMessage("提交成功！");
        } else {
            jhm.putCode(0).putMessage("提交失败！");
        }
//        }catch (Exception e){
//            jhm.putCode(-1).putMessage("服务器发生错误！");
//            /*
//            必须抛出异常，否则事务不启用
//             */
//            throw e;
//        }

        return jhm;
    }
}
