package utils;

import java.io.File;

public class PathTools {

	/**
	 * 获取class路径
	 * 适用于应用程序和web程序
	 * @return
	 */
	public static File getClassPath(){
		return getClassPath("");
	}
	public static File getClassPath(String subPath){
		String path=Thread.currentThread().getContextClassLoader().getResource("").getPath();
		return new File(path,subPath);
	}
	public static File getWebRootPath(){
		return getWebRootPath("");
	}
	/**
	 * 获取web根路径下面的子路径
	 * @return
	 */
	public static File getWebRootPath(String subPath){
		return new File(getClassPath().getParentFile().getParentFile(),subPath);
	}
}
