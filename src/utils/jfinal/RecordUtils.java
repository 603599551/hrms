package utils.jfinal;

import com.jfinal.plugin.activerecord.Record;

import java.util.Iterator;
import java.util.Map;

public class RecordUtils {
    /**
     * 将任意类型的值转换为String
     * @param r
     */
    public static void obj2str(Record r){
        Map<String,Object> map=r.getColumns();
        Iterator<Map.Entry<String,Object>> it=map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Object> en=it.next();
            Object value=en.getValue();
            String valueStr="";
            if(value!=null){
                valueStr=String.valueOf(value);
            }
            en.setValue(valueStr);
        }
    }
}
