package utils;

/**
 * @author mym-dell
 * url中的参数，带有@,*这类特殊字符时，需要进行编码和解码
 * 
 * 原因：
 * 特殊字符在URL中是不能直接传递的，如果要在URL中传递这些特殊符号，那么就要编码，编码的格式为：
 * %加字符的ASCII码，即一个百分号%，后面跟对应字符的ASCII（16进制）码值。例如 空格的编码值是"%20"。
 * 如果不使用转义字符，这些编码就会当URL中定义的特殊字符处理。
	下表中列出了一些URL特殊符号及编码 十六进制值
	1.+ URL 中+号表示空格 %2B
	2.空格 URL中的空格可以用+号或者编码 %20
	3./ 分隔目录和子目录 %2F
	4.? 分隔实际的 URL 和参数 %3F
	5.% 指定特殊字符 %25
	6.# 表示书签 %23
	7.&URL 中指定的参数间的分隔符 %26
	8.= URL 中指定参数的值 %3D 
	
	
 */
public class UrlEscape {
	
	/**
	 * 编码
	 * 返回的都是小写字母
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		if(s==null)return null;
		StringBuilder sb=new StringBuilder(s.length());
		char[] array=s.toCharArray();
		for(int i=0;i<array.length;i++){
			if(array[i]<'A' || array[i]>'z'){
				sb.append("%");
				String temp=Integer.toHexString((int)array[i]);
				sb.append(temp);
			}else{
				sb.append(array[i]);
			}
		}
		return sb.toString();
	}
	/**
	 * 解码
	 * @param s
	 * @return
	 */
	public static String unescape(String s){
		if(s==null)return null;
		StringBuilder sb=new StringBuilder(s.length());
		char[] array=s.toCharArray();
		for(int i=0;i<array.length;i++){
			if(array[i]=='%'){
				String temp=String.valueOf(array[i+1])+String.valueOf(array[i+2]);
				int tempInt=Integer.parseInt(temp, 16);
				sb.append((char)tempInt);
				i=i+2;
			}else{
				sb.append(array[i]);
			}
		}
		return sb.toString();
	}
	public static void main(String[] args){
		System.out.println(escape("=&#?"));
		System.out.println(unescape("%3D%26%23%3F"));
//		System.out.println((int)'a');
//		System.out.println((int)'A');
//		System.out.println((int)'Z');
//		System.out.println((int)'z');
		
		
	}
}
