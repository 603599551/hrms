package paiban.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.ContentTransformationUtil;
import utils.bean.JsonHashMap;

import java.util.Map;


import static utils.ContentTransformationUtil.PcToAppXianShi;
import static utils.ContentTransformationUtil.StringTimeToJsonTimeXianShi;

public class StaffIdleTimeService extends BaseService {
    @Before(Tx.class)
    public JsonHashMap save(Map paraMap) {
        JsonHashMap jhm = new JsonHashMap();
        String id = (String) paraMap.get("id");
        String week = (String) paraMap.get("week");
        String times = (String) paraMap.get("times");
        UserSessionUtil usu = (UserSessionUtil) paraMap.get("usu");

        DateTool dateTool = new DateTool();

        try {
            String searchStaff = "select count(*) as c, s.dept_id as store_id, s.kind as kind from h_staff s where s.id = ? ";
            Record record = Db.findFirst(searchStaff, id);
            if (record.getInt("c") > 0) {
                String nowDate = dateTool.GetDateTime();
                //获取下周星期几对应日期
                int nowDay = dateTool.getWeekDay(nowDate);
                int next = 9 - nowDay + Integer.parseInt(week);
                String thatDate = this.nextDay(nowDate, next);

                String idleSearch = "select count(*) as c, t.id as idleId from h_staff_idle_time t where t.staff_id = ? and t.date = ? ";
                Record idleR = Db.findFirst(idleSearch, id, thatDate);

                //如果数据库已有数据，则删除原数据
                if(idleR.getInt("c") != 0){
                    boolean flagDelete = Db.deleteById("h_staff_idle_time",idleR.getStr("idleId"));
                    if(!flagDelete){
                        jhm.putCode(0).putMessage("原数据删除失败！");
                        return jhm;
                    }
                }

                String content = StringTimeToJsonTimeXianShi(times);
                record.remove("c");
                record.set("date", thatDate);
                String date = DateTool.GetDateTime();
                record.set("id", UUIDTool.getUUID());
                record.set("staff_id", id);
                record.set("create_time", date);
                record.set("modify_time", date);
                String userId = usu.getUserId();
                record.set("creater_id", userId);
                record.set("modifier_id", userId);
                record.set("content", content);
                record.set("app_content", ContentTransformationUtil.Pc2AppContentEvery15M4Xianshi(content));

                boolean flag = Db.save("h_staff_idle_time", record);
                if (flag) {
                    jhm.putCode(1).putMessage("提交完成！");
                } else {
                    jhm.putCode(0).putMessage("提交失败！");
                }
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        return jhm;
    }
}
