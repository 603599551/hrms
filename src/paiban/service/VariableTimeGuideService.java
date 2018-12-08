package paiban.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.List;

public class VariableTimeGuideService extends BaseService {

    @Before(Tx.class)
    public void add(List<Record> saveList, String store_id){
        String delete = "delete from h_variable_time_guide where store_id=?";
        Db.delete(delete, store_id);
        Db.batchSave("h_variable_time_guide", saveList, saveList.size());
    }

}
