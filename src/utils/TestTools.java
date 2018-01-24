package utils;

import easy.util.DateTool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTools {

	private static TestTools tt;
	
	private long startTime;
	private long endTime;
	private TestTools(Class clazz,String method,String message){
		startTime=System.currentTimeMillis();
		Date date=new Date(startTime);
		String dateStr= DateTool.getDate(date, "yyyy-MM-dd HH:mm:ss");
		System.out.println();
		System.out.println("TestTools 开始------------------"+dateStr+"---------------------------");
		System.out.println("Class:"+clazz);
		System.out.println("Method:"+method);
		if(message!=null && !"".equals(message)){
			System.out.println(message);
		}
	}
	public static TestTools start(Class clazz,String method,String message){
		tt=new TestTools(clazz,method,message);
		return tt;
	}
	public static void end(String message){
		if(tt==null){
			throw new NullPointerException("请先调用start()函数！");
		}
		tt.endTime=System.currentTimeMillis();
		Date date=new Date(tt.endTime);
		String dateStr=DateTool.getDate(date, "yyyy-MM-dd HH:mm:ss");
		System.out.print("耗时:");
		System.out.print(tt.endTime-tt.startTime);
		System.out.println("ms");
		if(message!=null && !"".equals(message)){
			System.out.println(message);
		}
		System.out.println("TestTools 结束------------------"+dateStr+"---------------------------");
	}
}
