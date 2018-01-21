package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {

	private int hour=-1;
	private int minute=0;
	private int seconds=0;
	
	private TimerTask timerTask;
	
	private long period;
	public void start(){
		Calendar calendar = Calendar.getInstance(); 

		/*** 定制每日2:00执行方法 ***/

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, seconds);
		
		Date date=calendar.getTime(); //第一次执行定时任务的时间

		//如果第一次执行定时任务的时间 小于 当前的时间
		//此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
		if (date.before(new Date())) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			date=calendar.getTime();
		}
		
		Timer timer = new Timer();
		
		timer.schedule(timerTask,date,period);
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public int getSeconds() {
		return seconds;
	}
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	public TimerTask getTimerTask() {
		return timerTask;
	}
	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}
	public long getPeriod() {
		return period;
	}
	public void setPeriod(long period) {
		this.period = period;
	}
	
}
