package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BeanTool {
	
	private static Map propertiesMap = new HashMap();
	
	public static boolean isNull(Object obj) {
		if(obj==null) return true;
		if(obj.getClass().equals(String.class)){
			String str = (String)obj;
			if(str!=null&&!"".equals(str.trim())){
				return false;
			}else {
				return true;
			}
		}else if(obj.getClass().equals(ArrayList.class)){
			List ls = (List)obj;
			if (ls!=null&&!ls.isEmpty()) {
				return false;
			}else {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param srcMap
	 * @param keyDescFile
	 * @return
	 */
	public static Map conversionMapkey(Map srcMap,String keyDescFile){
		Map resultMap = new HashMap();
		resultMap.putAll(srcMap);
		Properties pro = (Properties)propertiesMap.get(keyDescFile);
		if(pro==null){
			
			InputStream is = null;
			pro = new Properties();
			File confFile = new File(keyDescFile);
			if(confFile.exists()){
				try {
					is = new FileInputStream(keyDescFile);
					pro.load(is);
					propertiesMap.put(keyDescFile, pro);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}finally{
					if(is!=null){
						try {
							is.close();
							is = null;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		Iterator keyIterator = pro.keySet().iterator();
		while (keyIterator.hasNext()) {
			String keySrc = (String) keyIterator.next();
			String keyDest = pro.getProperty(keySrc);
			if(!keySrc.equals(keyDest)){
				Object value = resultMap.get(keySrc);
				resultMap.put("keyDest", value);
				resultMap.remove(keySrc);
			}
		}
		return resultMap;
	}
	
	public static Map buildSvrMapParameter(Object obj,String[] keys ) throws Exception{
		if (keys==null||keys.length<1) {
			return null;
		}
		Map map = new HashMap();
		for (int i = 0; i < keys.length; i++) {
			String keyString = keys[i];
			map.put(keyString, obj.getClass().getDeclaredField(keyString).get(obj));
		} 
		return map;
	}
	
	public static void main(String[] args) {
		String sdf = " ";
		System.out.println("sdf = '' "+ BeanTool.isNull(sdf));
		String aa = null;
		System.out.println("aa = null "+ BeanTool.isNull(aa));
		List lsList = new ArrayList();
		System.out.println("lsList = ArrayList "+ BeanTool.isNull(lsList));
		List ls = null;
		System.out.println("lsList = ArrayList "+ BeanTool.isNull(ls));
	}

}
