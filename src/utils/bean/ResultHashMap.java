package utils.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultHashMap extends HashMap {

	private boolean result=false;
	private String message="";
	private String id="";
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean getResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
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
