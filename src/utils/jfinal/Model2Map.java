package utils.jfinal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class Model2Map {

//	public static Map execute(Model model){
//		Map reMap=new HashMap();
//		String[] nameArray=model._getAttrNames();
//		for(String name:nameArray){
//			reMap.put(name,model.get(name));
//		}
//		return reMap;
//	}
//	public static Map execute(Model model,Map map){
//		String[] nameArray=model._getAttrNames();
//		for(String name:nameArray){
//			map.put(name,model.get(name));
//		}
//		return map;
//	}
	public static Map execute(Record r,Map map){
		if(r!=null){
			String[] nameArray=r.getColumnNames();
			for(String name:nameArray){
				map.put(name,r.get(name));
			}
		}
		return map;
	}
//	public static List<Map> execute(List<Model> inputList,List<Map> outputList){
//		for(Model m:inputList){
//			Map map=new HashMap();
//            Model2Map.execute(m,map);
//            outputList.add(map);
//        }
//		return outputList;
//	}
	public static List<Map> execute2(List<Record> inputList,List<Map> outputList){
		for(Record r:inputList){
			Map map=new HashMap();
            Model2Map.execute(r,map);
            outputList.add(map);
        }
		return outputList;
	}
	
}
