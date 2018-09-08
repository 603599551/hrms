package paiban.service;

import com.common.service.BaseService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;

import java.util.ArrayList;
import java.util.List;

public class AreaService extends BaseService {

    public void edit(String id, String[] workers, String date, UserSessionUtil usu){
        Record areaStaff = Db.findById("h_area", id);
        List<Record> areaStaffList = new ArrayList<>();
        String userId = usu.getUserId();
        String time = DateTool.GetDateTime();
        for(String w : workers){
            Record r = new Record();
            r.set("id", UUIDTool.getUUID());
            r.set("store_id", areaStaff.get("store_id"));
            r.set("staff_id", w);
            r.set("area_name", areaStaff.get("name"));
            r.set("area_id", areaStaff.get("id"));
            r.set("date", date);
            r.set("creater_id", userId);
            r.set("create_time", time);
            r.set("modifier_id", userId);
            r.set("modify_time", time);
            areaStaffList.add(r);
        }
        String delete = "delete from h_area_staff where area_id=? and date=?";
        Db.delete(delete, id, date);
        Db.batchSave("h_area_staff", areaStaffList, areaStaffList.size());
    }

}
