package utils.jfinal;

import java.util.Iterator;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;

public class Map2Record {

	/**
	 * 
	 * @param map 源map，不能为null
	 * @param record 目标record，不能为null
	 * @return 返回传入的record
	 */
	public static Record execute(Map map ,Record record){
		Iterator it=map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en=(Map.Entry)it.next();
			record.set((String)en.getKey(), en.getValue());
		}
		return record;
	}
}
