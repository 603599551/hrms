package utils;

/*
 * 创建日期 2007-03-14
 *
 * 功能  取日期时间工具
 *
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 说明: 取日期时间工具
 * 
 */
public class DateTool {
	public static final int MINUTE=60;//一分钟的秒数
	public static final int HOUR=MINUTE*60;//一小时的秒数
	public static final int DAY=24*HOUR;//一天的秒数
	public static final TimeZone TZ_SHANGHAI=TimeZone.getTimeZone("Asia/Shanghai");
	public DateTool() {
	}
	/**
	 * 
	 * @param date Date型
	 * @param format String型。例：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getDate(Date date,String format){
		SimpleDateFormat sdf=new SimpleDateFormat(format);
		sdf.setTimeZone(TZ_SHANGHAI);
		String var=sdf.format(date);
		return var;
	}
	/**
	 * @see 取得当前日期（格式为：yyyy-MM-dd）
	 * @return String
	 */
	public static String GetDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TZ_SHANGHAI);
		String sDate = sdf.format(new Date());
		return sDate;
	}

	/**
	 * @see 取得当前时间（格式为：yyy-MM-dd HH:mm:ss）
	 * @return String
	 */
	public static String GetDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TZ_SHANGHAI);
		String sDate = sdf.format(new Date());
		return sDate;
	}

	/**
	 * @see 按指定格式取得当前时间()
	 * @return String
	 */
	public static String GetTimeFormat(String strFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
		sdf.setTimeZone(TZ_SHANGHAI);
		String sDate = sdf.format(new Date());
		return sDate;
	}

	/**
	 * @see 取得指定时间的给定格式()
	 * @return String
	 * @throws ParseException
	 */
	public String SetDateFormat(String myDate, String strFormat)
			throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
		sdf.setTimeZone(TZ_SHANGHAI);
		String sDate = sdf.format(sdf.parse(myDate));

		return sDate;
	}

	public String FormatDateTime(String strDateTime, String strFormat) {
		String sDateTime = strDateTime;
		try {
			Calendar Cal = parseDateTime(strDateTime);
			SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
			sdf.setTimeZone(TZ_SHANGHAI);
			sDateTime = sdf.format(Cal.getTime());
		} catch (Exception e) {

		}
		return sDateTime;
	}

	public static Calendar parseDateTime(String baseDate) {
		Calendar cal = null;
		cal = new GregorianCalendar();
		int yy = Integer.parseInt(baseDate.substring(0, 4));
		int mm = Integer.parseInt(baseDate.substring(5, 7)) - 1;
		int dd = Integer.parseInt(baseDate.substring(8, 10));
		int hh = 0;
		int mi = 0;
		int ss = 0;
		if (baseDate.length() > 12) {
			hh = Integer.parseInt(baseDate.substring(11, 13));
			mi = Integer.parseInt(baseDate.substring(14, 16));
			ss = Integer.parseInt(baseDate.substring(17, 19));
		}
		cal.set(yy, mm, dd, hh, mi, ss);
		return cal;
	}

	public int getDay(String strDate) {
		Calendar cal = parseDateTime(strDate);
		return cal.get(Calendar.DATE);
	}

	public int getMonth(String strDate) {
		Calendar cal = parseDateTime(strDate);

		return cal.get(Calendar.MONTH) + 1;
	}

	public int getWeekDay(String strDate) {
		Calendar cal = parseDateTime(strDate);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public String getWeekDayName(String strDate) {
		String mName[] = { "日", "一", "二", "三", "四", "五", "六" };
		int iWeek = getWeekDay(strDate);
		iWeek = iWeek - 1;
		return "星期" + mName[iWeek];
	}

	public int getYear(String strDate) {
		Calendar cal = parseDateTime(strDate);
		return cal.get(Calendar.YEAR) + 1900;
	}

	public String DateAdd(String strDate, int iCount, int iType) {
		Calendar Cal = parseDateTime(strDate);
		int pType = 0;
		if (iType == 0) {
			pType = 1;
		} else if (iType == 1) {
			pType = 2;
		} else if (iType == 2) {
			pType = 5;
		} else if (iType == 3) {
			pType = 10;
		} else if (iType == 4) {
			pType = 12;
		} else if (iType == 5) {
			pType = 13;
		}
		Cal.add(pType, iCount);
		SimpleDateFormat sdf = null;
		if (iType <= 2)
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		else
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TZ_SHANGHAI);
		String sDate = sdf.format(Cal.getTime());
		return sDate;
	}

	public String DateAdd(String strOption, int iDays, String strStartDate) {
		if (!strOption.equals("d"))
			;
		return strStartDate;
	}

	public int DateDiff(String strDateBegin, String strDateEnd, int iType) {
		Calendar calBegin = parseDateTime(strDateBegin);
		Calendar calEnd = parseDateTime(strDateEnd);
		long lBegin = calBegin.getTimeInMillis();
		long lEnd = calEnd.getTimeInMillis();
		int ss = (int) ((lBegin - lEnd) / 1000L);
		int min = ss / 60;
		int hour = min / 60;
		int day = hour / 24;
		if (iType == 0)
			return hour;
		if (iType == 1)
			return min;
		if (iType == 2)
			return day;
		else
			return -1;
	}
	/**
	 * 日期减法计算，
	 * 返回为数组，数组长度为5，
	 * 第一个元素是数组，1表示第一时间大于第二时间，0表示相等，-1表示第一时间小于第二时间，
	 * 第二个元素是间隔的天数，
	 * 第三个元素是间隔的小时数，
	 * 第四个元素是间隔的分钟数，
	 * 第五个元素是间隔的秒数
	 * @param calBegin
	 * @param calEnd
	 * @return
	 */
	public static int[] DateDiff(Calendar calBegin, Calendar calEnd) {
		int reCompare=0;
		int reDays=0;//天
		int reHours=0;//时
		int reMinutes=0;//分
		int reSeconds=0;//秒
		
		long lBegin = calBegin.getTimeInMillis();
		long lEnd = calEnd.getTimeInMillis();
		long ss =  ((lBegin - lEnd) / 1000L);//间隔的秒
		if(0<ss){
			reCompare=1;
		}else if(ss==0){
			reCompare=0;
		}else {
			reCompare=-1;
		}
		ss=Math.abs(ss);
		if(DAY<=ss){//间隔时间超过1天
			int temp=0;
			reDays=(int)ss/DAY;
			temp=(int)ss%DAY;
			reHours=(int)temp/HOUR;
			temp=(int)temp%HOUR;
			reMinutes=(int)temp/MINUTE;
			reSeconds=(int)temp%MINUTE;
		}else if(HOUR<=ss && ss<DAY){//间隔时间超过1小时，但不超过1天
			int temp=0;
			reHours=(int)ss/HOUR;
			temp=(int)ss%HOUR;
			reMinutes=(int)temp/MINUTE;
			reSeconds=(int)temp%MINUTE;
		}else if(MINUTE<=ss && ss<HOUR){//间隔时间超过1分钟，但不超过1小时
			reMinutes=(int)ss/MINUTE;
			reSeconds=(int)ss%MINUTE;
		}
		
		return new int[]{reCompare,reDays,reHours,reMinutes,reSeconds};
	}
	/**
	 * 日期减法计算，
	 * 返回为数组，数组长度为5，
	 * 第一个元素是数组，1表示第一时间大于第二时间，0表示相等，-1表示第一时间小于第二时间，
	 * 第二个元素是间隔的天数，
	 * 第三个元素是间隔的小时数，
	 * 第四个元素是间隔的分钟数，
	 * 第五个元素是间隔的秒数
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int[] DateDiff(Date begin, Date end) {
		int reCompare=0;
		int reDays=0;//天
		int reHours=0;//时
		int reMinutes=0;//分
		int reSeconds=0;//秒
		
		long lBegin = begin.getTime();
		long lEnd = end.getTime();
		long ss =  ((lBegin - lEnd) / 1000L);//间隔的秒
		if(0<ss){
			reCompare=1;
		}else if(ss==0){
			reCompare=0;
		}else {
			reCompare=-1;
		}
		ss=Math.abs(ss);
		if(DAY<=ss){//间隔时间超过1天
			int temp=0;
			reDays=(int)ss/DAY;
			temp=(int)ss%DAY;
			reHours=(int)temp/HOUR;
			temp=(int)temp%HOUR;
			reMinutes=(int)temp/MINUTE;
			reSeconds=(int)temp%MINUTE;
		}else if(HOUR<=ss && ss<DAY){//间隔时间超过1小时，但不超过1天
			int temp=0;
			reHours=(int)ss/HOUR;
			temp=(int)ss%HOUR;
			reMinutes=(int)temp/MINUTE;
			reSeconds=(int)temp%MINUTE;
		}else if(MINUTE<=ss && ss<HOUR){//间隔时间超过1分钟，但不超过1小时
			reMinutes=(int)ss/MINUTE;
			reSeconds=(int)ss%MINUTE;
		}
		
		return new int[]{reCompare,reDays,reHours,reMinutes,reSeconds};
	}

	/***************************************************************************
	 * @功能 判断某年是否为闰年
	 * @return boolean
	 * @throws ParseException
	 **************************************************************************/
	public boolean isLeapYear(int yearNum) {
		boolean isLeep = false;
		/** 判断是否为闰年，赋值给一标识符flag */
		if ((yearNum % 4 == 0) && (yearNum % 100 != 0)) {
			isLeep = true;
		} else if (yearNum % 400 == 0) {
			isLeep = true;
		} else {
			isLeep = false;
		}
		return isLeep;
	}

	/***************************************************************************
	 * @功能 计算当前日期某年的第几周
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public int getWeekNumOfYear() {
		Calendar calendar = Calendar.getInstance();
		int iWeekNum = calendar.get(Calendar.WEEK_OF_YEAR);
		return iWeekNum;
	}

	/***************************************************************************
	 * @功能 计算指定日期某年的第几周
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public int getWeekNumOfYearDay(String strDate) throws ParseException {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		format.setTimeZone(TZ_SHANGHAI);
		Date curDate = format.parse(strDate);
		calendar.setTime(curDate);
		int iWeekNum = calendar.get(Calendar.WEEK_OF_YEAR);
		return iWeekNum;
	}

	/***************************************************************************
	 * @功能 计算某年某周的开始日期
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public String getYearWeekFirstDay(int yearNum, int weekNum)
			throws ParseException {
		Calendar cal = Calendar.getInstance(); 
		int month = cal.get(Calendar.MONTH)+1;
		int temp=yearNum; 
		if(weekNum==1&&month!=1){
			cal.set(Calendar.YEAR, yearNum+1);
			temp = yearNum;
		}
		int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
		int dayofyear = cal.get(Calendar.DAY_OF_YEAR);
		if((dayofweek)>dayofyear){
			cal.set(Calendar.YEAR, yearNum);
			temp = yearNum-1;
		}
		cal.set(Calendar.WEEK_OF_YEAR, weekNum);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		// 分别取得当前日期的年、月、日
		String tempYear = Integer.toString(temp);
		String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
		String tempDay = Integer.toString(cal.get(Calendar.DATE));
		String tempDate = tempYear + "-" + tempMonth + "-" + tempDay;
		return SetDateFormat(tempDate, "yyyy-MM-dd");

	}

	/***************************************************************************
	 * @功能 计算某年某周的结束日期
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public String getYearWeekEndDay(int yearNum, int weekNum)
			throws ParseException {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH)+1;
		int dayofweek = Calendar.DAY_OF_WEEK;
		if(weekNum==1&&month!=1)
			yearNum = yearNum+1;
		cal.set(Calendar.YEAR, yearNum);
		cal.set(Calendar.WEEK_OF_YEAR, weekNum );
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		// 分别取得当前日期的年、月、日
		String tempYear = Integer.toString(yearNum);
		String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
		String tempDay = Integer.toString(cal.get(Calendar.DATE));
		String tempDate = tempYear + "-" + tempMonth + "-" + tempDay;
		return SetDateFormat(tempDate, "yyyy-MM-dd");
	}

	/***************************************************************************
	 * @功能 计算某年某月的开始日期
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public String getYearMonthFirstDay(int yearNum, int monthNum)
			throws ParseException {

		// 分别取得当前日期的年、月、日
		String tempYear = Integer.toString(yearNum);
		String tempMonth = Integer.toString(monthNum);
		String tempDay = "1";
		String tempDate = tempYear + "-" + tempMonth + "-" + tempDay;
		return SetDateFormat(tempDate, "yyyy-MM-dd");

	}

	/***************************************************************************
	 * @功能 计算某年某月的结束日期
	 * @return interger
	 * @throws ParseException
	 **************************************************************************/
	public String getYearMonthEndDay(int yearNum, int monthNum)
			throws ParseException {

		// 分别取得当前日期的年、月、日
		String tempYear = Integer.toString(yearNum);
		String tempMonth = Integer.toString(monthNum);
		String tempDay = "31";
		if (tempMonth.equals("1") || tempMonth.equals("3")
				|| tempMonth.equals("5") || tempMonth.equals("7")
				|| tempMonth.equals("8") || tempMonth.equals("10")
				|| tempMonth.equals("12")) {
			tempDay = "31";
		}
		if (tempMonth.equals("4") || tempMonth.equals("6")
				|| tempMonth.equals("9") || tempMonth.equals("11")) {
			tempDay = "30";
		}
		if (tempMonth.equals("2")) {
			if (isLeapYear(yearNum)) {
				tempDay = "29";
			} else {
				tempDay = "28";
			}
		}
		// System.out.println("tempDay:" + tempDay);
		String tempDate = tempYear + "-" + tempMonth + "-" + tempDay;
		return SetDateFormat(tempDate, "yyyy-MM-dd");

	}
	/**
	* 时间前推或后推分钟,其中JJ表示分钟.
	*/
	public static String getPreTime(String sj1, String jj) {
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	format.setTimeZone(TZ_SHANGHAI);
	String mydate1 = "";
	try {
	   Date date1 = format.parse(sj1);
	   long Time = (date1.getTime() / 1000) + Integer.parseInt(jj) * 60;
	   date1.setTime(Time * 1000);
	   mydate1 = format.format(date1);
	} catch (Exception e) {
		e.printStackTrace();
	}
		return mydate1;
	}
	/**
	 * 获取短时间格式
	 * 返回小时前、分钟后等人性化时间格式
	 * @param dateStr
	 * @param pattern
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String getShortDateTime(String dateStr,String pattern) throws ParseException{
		return getShortDateTime(dateStr,pattern,new Date());
	}
	/**
	 * 获取短时间格式
	 * 返回小时前、分钟后等人性化时间格式
	 * @param dateStr
	 * @param pattern
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static String getShortDateTime(String dateStr,String pattern,Date date) throws ParseException{
		String reStr=null;
		SimpleDateFormat sdf=new SimpleDateFormat(pattern);
		Date dateSrcDate=sdf.parse(dateStr);
		
		Calendar cln=Calendar.getInstance();
		cln.setTime(date);
		long d=date.getTime()-dateSrcDate.getTime();
		long minue=d/1000/60;
		String qian="前";
		boolean b=true;
		if(minue<0){
			minue=-minue;
			qian="后";
			b=false;
		}
		if(minue<3){
			reStr="刚刚";
		}else if(minue<=59){
			reStr=minue+"分钟"+qian;
		}else{
			long hour=minue/60;
			if(hour<=23){
				reStr=hour+"小时"+qian;
			}else{
				long day=hour/24;
				if(day==1){
					if(b){
						reStr="昨天";
					}else{
						reStr="明天";
					}
				}else if(2<=day && day<=3){
					reStr=day+"天"+qian;
				}else{
					Calendar srcCln=Calendar.getInstance();
					srcCln.setTime(dateSrcDate);
					String format="MM-dd HH:mm";
					if(srcCln.get(Calendar.YEAR)!=cln.get(Calendar.YEAR)){
						format="yyyy-MM-dd HH:mm";
					}
					SimpleDateFormat sdf2=new SimpleDateFormat(format);
					reStr=sdf2.format(dateSrcDate);
				}
			}
		}
		return reStr;
	}
	
	public static void main(String args[]){
		String newStr = DateTool.getPreTime("2008-09-09 10:59:59", "1");
//		newStr=DateTool.GetDateTime();
		System.out.println("-->"+newStr);
	}
}
