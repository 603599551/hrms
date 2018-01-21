package com.jsoft.crm.utils;

import javax.servlet.http.HttpServletRequest;

public class UrlKit {

	/**
	 * ��ȡurl����ʽ��http://ip:port/contextPath�����port��80������ʾ
	 * @param request
	 * @return
	 */
	public static String getURL(HttpServletRequest request){
		String basePath = request.getScheme()+"://"+request.getServerName();//http://
		String path = request.getContextPath();//���ص��������ģ�һ������¾��ǹ�����
		int port=request.getServerPort();//�˿�
		if("/".equals(path)){
			path="";
		}
		if(port!=80){
			basePath=basePath+":"+port;
		}
		basePath=basePath+path;
//		String basePath2 = basePath;
//		basePath=basePath+"/";
		return basePath;
	}
	public static String getDomain(HttpServletRequest request){
		return getURL(request);
	}
	/**
	 * ��β��б��
	 * @param request
	 * @return
	 */
	public static String getDomainSlash(HttpServletRequest request){
		return getURL2(request);
	}
	/**
	 * ��ȡurl����ʽ��http://ip:port/contextPath/�����port��80������ʾ
	 * @param request
	 * @return
	 */
	public static String getURL2(HttpServletRequest request){
		return getURL(request)+"/";
	}
}
