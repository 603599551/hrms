package utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author mym-dell
 * 
 * 读取cookie
 * 
 */
public class CookieUtil {
	
	/**
	 * value的格式是username=65242847@qq.com;password=123456时，key2表示获取username的value值
	 * @param req
	 * @param key
	 * @param key2
	 * @return
	 */
	public static String getValue(HttpServletRequest req,String key,String key2){
		String reStr=null;
		Cookie[] cookieArray=req.getCookies();
		if(cookieArray==null || cookieArray.length==0)return null;
		for(Cookie cookie:cookieArray){
			if(cookie!=null){
				if(key.equals(cookie.getName())){
					String value=cookie.getValue();
					value=UrlEscape.unescape(value);
					if(key2==null || "".equals(key2)){
						reStr=value;
						break;
					}else{
						if(value!=null){
							String[] array=value.split(";");
							if(array!=null && array.length>1){
								for(String str:array){
									if(str!=null){
										String[] keyValue=str.split("=");
										if(keyValue!=null && keyValue.length>=2){
											if(key2.equals(keyValue[0])){
												reStr= keyValue[1];
												break;
											}
										}
									}
								}
							}
						}
					}
				}
				
			}
		}
		return reStr;
	}
}
