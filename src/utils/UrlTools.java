package utils;

import javax.servlet.http.HttpServletRequest;

public class UrlTools {

	/**
	 * 把相对路径转换成全路径
	 * @param request
	 * @param url
	 * @return
	 */
	public static String getAllUrl(HttpServletRequest request,String relativeUrl){
		return getDomain(request)+relativeUrl;
	}
	/**
	 * 获取当前的域名
	 * @param request
	 * @return
	 */
	public static String getDomain(HttpServletRequest request){
		String realPath1 =null;
		if(request.getServerPort()==80){
			realPath1 = "http://" + request.getServerName() + request.getContextPath();
		}else{
			realPath1 = "http://" + request.getServerName() +":"+request.getServerPort()+ request.getContextPath();
		}
		return realPath1;
	}
	/**
	 * 将标准url的/&?等字符转换成转义字符
	 * @param url
	 * @return
	 */
	public static String escape(String url){
		char[] array=url.toCharArray();
		StringBuffer newUrl=new StringBuffer(url.length());
		for(char c:array){
			if(c==' '){
				newUrl.append("%20");
			}else if(c=='"'){
				newUrl.append("%22");
			}else if(c=='#'){
				newUrl.append("%23");
			}else if(c=='%'){
				newUrl.append("%25");
			}else if(c=='&'){
				newUrl.append("%26");
			}else if(c=='('){
				newUrl.append("%28");
			}else if(c==')'){
				newUrl.append("%29");
			}else if(c=='+'){
				newUrl.append("%2B");
			}else if(c==','){
				newUrl.append("%2C");
			}else if(c=='/'){
				newUrl.append("%2F");
			}else if(c==':'){
				newUrl.append("%3A");
			}else if(c==';'){
				newUrl.append("%3B");
			}else if(c=='<'){
				newUrl.append("%3C");
			}else if(c=='='){
				newUrl.append("%3D");
			}else if(c=='>'){
				newUrl.append("%3E");
			}else if(c=='?'){
				newUrl.append("%3F");
			}else if(c=='@'){
				newUrl.append("%40");
			}else if(c=='\\'){
				newUrl.append("%5C");
			}else if(c=='|'){
				newUrl.append("%7C");
			}else{
				newUrl.append(c);
			}
		}
		return newUrl.toString();
	}
}
