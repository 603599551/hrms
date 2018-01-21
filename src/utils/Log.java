package utils;

import easy.util.DateTool;

public class Log {
	public static final int DEBUG=100;
	public static final int WARN=200;
	public static final int ERROR=500;
	private Class obj;
	private int level=DEBUG;
	private boolean show=true;
	public boolean isShow() {
		return show;
	}
	/**
	 * 设置是否显示日志
	 * @param show
	 */
	public void setShow(boolean show) {
		this.show = show;
	}
	public int getLevel() {
		return level;
	}
	/**
	 * 设置日志级别
	 * @param level
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	public Log(Class clazz){
		this.obj=clazz;
	}
	public void start(){
		if(show){
			System.out.println();
			Class[] array=obj.getClasses();
			System.out.println(obj.getName()+" report--------"+DateTool.GetDateTime()+"------------------------------");
		}
	}
	public void end(){
		if(show){
			System.out.println("--------------------------------------------------------------------------------");
		}
	}
	public void debug(String msg){
		println(DEBUG,msg);
	}
	public void warn(String msg){
		println(WARN,msg);
	}
	public void error(String msg){
		println(ERROR,msg);
	}
	
	private void println(int _level,String msg){
		if(show && _level>=level){
			System.out.println(msg);
		}
	}
}
