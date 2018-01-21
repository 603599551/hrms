package utils.bean;

import java.util.HashMap;

public class MessageHashMap extends HashMap{

	private boolean result=false;;
	private String message="";
	
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
	public String get(String key) {
		// TODO Auto-generated method stub
		return (String)get(key);
	}
	public String put(String key, String value) {
		// TODO Auto-generated method stub
		return put(key, value);
	}
	
}
