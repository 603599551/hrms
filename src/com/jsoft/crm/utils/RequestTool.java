package com.jsoft.crm.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mym
 * 将request对象中所有参数，全部获取到，并封装到map中
 */
public class RequestTool {
	
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unchecked")
	public static Map getParameterMap(HttpServletRequest request){
		Map<String,Object> returnMap = new HashMap(); 
		Enumeration<String> names=request.getParameterNames();
		while(names.hasMoreElements()){
			String name=names.nextElement();
			String value=request.getParameter(name);
			String[] values=request.getParameterValues(name);
			if(values.length==1){
				returnMap.put(name, value);
			}else{
				returnMap.put(name, values);
			}
		}
		return returnMap;
	}
	
	public static JSONObject getJson(HttpServletRequest request){
		JSONObject paraJsonObject=null;
		Map paraMap=request.getParameterMap();
		Iterator it=paraMap.keySet().iterator();
		if(it.hasNext()){
			Object obj=it.next();
			paraJsonObject= JSONObject.parseObject(obj.toString());
		}
		return paraJsonObject;
	}
}
