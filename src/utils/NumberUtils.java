package utils;

import java.math.BigDecimal;

public class NumberUtils {

	/**
	 * 将o转换为int型数字，如果转换失败就返回defaultValue
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static int parseInt(Object o,int defaultValue){
		int returnValue=defaultValue;
		if(o!=null){
			try{
				returnValue=Integer.parseInt(o.toString());
			}catch(Exception e){
				
			}
		}
		return returnValue;
	}
//	public static int parseInt(String s,int defaultValue){
//		int returnValue=defaultValue;
//		try{
//			returnValue=Integer.parseInt(s);
//		}catch(Exception e){
//			
//		}
//		return returnValue;
//	}
//	public static float parseFloat(String s,float defaultValue){
//		float returnValue=defaultValue;
//		try{
//			returnValue=Float.parseFloat(s);
//		}catch(Exception e){
//			
//		}
//		return returnValue;
//	}
	public static float parseFloat(Object o,float defaultValue){
		float returnValue=defaultValue;
		if(o!=null){
			try{
				returnValue=Float.parseFloat(o.toString());
			}catch(Exception e){
				
			}
		}
		return returnValue;
	}
//	public static double parseDouble(String s,double defaultValue){
//		double returnValue=defaultValue;
//		try{
//			returnValue=Double.parseDouble(s);
//		}catch(Exception e){
//			
//		}
//		return returnValue;
//	}
	public static double parseDouble(Object o,double defaultValue){
		double returnValue=defaultValue;
		if(o!=null){
			try{
				returnValue=Double.parseDouble(o.toString());
			}catch(Exception e){
				
			}
		}
		return returnValue;
	}
	/**
	 * 保留小数点后2位（四舍五入）
	 * @param d
	 * @return
	 */
	public static double getMoney(double d){
		double returnValue=0;
		returnValue= new BigDecimal(d).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		return returnValue;
	}
	/**
	 * 保留小数点后2位（四舍五入）
	 * @param d
	 * @return
	 */
	public static float getMoney(float d){
		float returnValue=0;
		returnValue= new BigDecimal(d).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
		return returnValue;
	}
	/**
	 * 如果小数点后全是0，则只保留整数
	 * @param d
	 * @return
	 */
	public static String getMoneyStr(double d){
		String returnValue="0";
		double temp= new BigDecimal(d).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		returnValue=temp+"";
		if(temp % 1.0 == 0){
			returnValue =String.valueOf((long)temp);
		}
		return returnValue;
	}
	public static String getMoneyStr(float f){
		String returnValue="0";
		float temp= new BigDecimal(f).setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
		returnValue=temp+"";
		if(temp % 1.0 == 0){
			returnValue =String.valueOf((long)temp);
		}
		return returnValue;
	}
	public static void main(String[] args){
		int i=NumberUtils.parseInt(null, 0);
		System.out.println(i);
	}
}
