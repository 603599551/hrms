package utils.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterHashMap extends HashMap{

	public String getString(Object key){
		return (String)get(key);
	}
	public int getInt(Object key){
		return (Integer)get(key);
	}
	public double getDouble(Object key){
		return (Double)get(key);
	}
	public float getFloat(Object key){
		return (Float)get(key);
	}
	public boolean getBoolean(Object key){
		return (Boolean)get(key);
	}
	public List getList(Object key){
		return (List)get(key);
	}
	public Map getMap(Object key){
		return (Map)get(key);
	}
}
