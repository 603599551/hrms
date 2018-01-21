package utils;

import java.util.UUID;

public class UUIDTool {

	public static String getUUID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
