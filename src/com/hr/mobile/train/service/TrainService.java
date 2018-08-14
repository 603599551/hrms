package com.hr.mobile.train.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.Map;

public class TrainService extends BaseService {
    /*
    增加事务
     */
    @Before(Tx.class)
    public JsonHashMap finish(Map paraMap){
        JsonHashMap jhm = new JsonHashMap();
        String staff_id =(String)paraMap.get("staff_id");
        String type_id = (String)paraMap.get("type_id");
        String date = DateTool.GetDateTime();

        try {
            String search = "select count(*) as c from h_staff where id = ? ";
            Record countR = Db.findFirst(search, staff_id);
            if(countR.getInt("c") != 0){
                String repeatSearch = "select count(*) as c from h_staff_train where staff_id = ? and type_2 = ? ";
                Record repeatR = Db.findFirst(repeatSearch, staff_id, type_id);
                if(repeatR.getInt("c") != 0){
                    jhm.putCode(1).putMessage("培训已完成！");
                } else {
                    String searchType = "select t.parent_id as type_1 from h_train_type t where t.id = ? ";
                    Record record = Db.findFirst(searchType, type_id);
                    record.set("id", UUIDTool.getUUID());
                    record.set("type_2", type_id);
                    record.set("create_time", date);
                    record.set("status", "1");
                    record.set("staff_id", staff_id);
                    String sql = "SELECT c.count as allCount, s.count as staffCount from (SELECT count(*)as count from h_train_type  where parent_id= ? )c,(SELECT count(*) as count from h_staff_train where type_1 = ? AND staff_id = ? )s";
                    Record countNumber = Db.findFirst(sql, record.getStr("type_1"), record.getStr("type_1"), staff_id);
                    if(countNumber.getInt("allCount")==(countNumber.getInt("staffCount")+1)){
                        countNumber.set("type_1",record.getStr("type_1"));
                        countNumber.set("id", UUIDTool.getUUID());
                        countNumber.set("staff_id", staff_id);
                        countNumber.set("status", "1");
                        countNumber.set("create_time", date);
                        countNumber.remove("allCount");
                        countNumber.remove("staffCount");
                        boolean flag1 = Db.save("h_staff_train", countNumber);
                        if(!flag1){
                            jhm.putCode(0).putMessage("一级分类进度更新失败！");
                        }
                    }
                    boolean flag2 = Db.save("h_staff_train", record);
                    if(flag2){
                        jhm.putCode(1).putMessage("培训完成！");
                    } else {
                        jhm.putCode(0).putMessage("培训完成失败！");
                    }
                }
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e){
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        return jhm;
    }
}
