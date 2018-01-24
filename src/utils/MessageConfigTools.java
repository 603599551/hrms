package utils;

import easy.util.PathTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author mym
 * 读取文本文件
 * 文本文件的格式为： key=value
 */
public class MessageConfigTools extends HashMap<String,String>{

	
	public String get(String key,String defaultValue){
		String value=get(key);
		if(value==null || "".equals(value)){
			return defaultValue;
		}else{
			return value;
		}
	}
	public void readFile(File file) throws IOException{
		BufferedReader bufferedReader=null;
		String temp="";
		try {
			bufferedReader=new BufferedReader(new FileReader(file));
			while ((temp = bufferedReader.readLine()) != null) {
				int pos=temp.indexOf("=");
				String key=temp.substring(0, pos);
				String value=temp.substring(pos+1 );
				put(key, value);
			}
		} catch (IOException e) {
			throw e;
		} finally{
			if(bufferedReader!=null)
				bufferedReader.close();
		}
	}
	public void readFile(File file,String charset) throws IOException{
		BufferedReader bufferedReader=null;
		String temp="";
		try {
			bufferedReader=new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
			while ((temp = bufferedReader.readLine()) != null) {
				int pos=temp.indexOf("=");
				String key=temp.substring(0, pos);
				String value=temp.substring(pos+1 );
				put(key, value);
			}
		} catch (IOException e) {
			throw e;
		} finally{
			if(bufferedReader!=null)
				bufferedReader.close();
		}
	}
	/**
	 * 默认读取的WEB-INF/message.config文件
	 * @throws IOException 
	 */
	public void readFile() throws IOException{
		File file= PathTools.getWebRootPath("WEB-INF/message.config");
		readFile(file);
	}
}
