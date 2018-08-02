package paiban.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoreForecastTurnoverService extends BaseService {

    @Before(Tx.class)
    public void save(List<Record> saveList, String[] days, String store_id){
        String sql = "select * from h_store_forecast_turnover where store_id=? and scheduling_date in (?,?,?,?,?,?,?)";
        List<String> params = new ArrayList<>();
        params.add(store_id);
        params.addAll(Arrays.asList(days));
        List<Record> list = Db.find(sql, params.toArray());
        if(list != null && list.size() > 0){
            params = new ArrayList<>();
            String delete = "delete from h_store_forecast_turnover where id in (";
            for(Record r : list){
                delete += " ?,";
                params.add(r.get("id"));
            }
            delete = delete.substring(0, delete.length() - 1) + ")";
            Db.delete(delete, params.toArray());
        }
        Db.batchSave("h_store_forecast_turnover", saveList, saveList.size());
    }

}
