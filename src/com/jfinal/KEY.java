package com.jfinal;

import java.util.HashMap;
import java.util.Map;

public class KEY {
	public static final Map<String,String> SYS_CONFIG=new HashMap<String,String>();
	public static final int PAGE_SIZE=20;
	public static final String SESSION_ADMIN="session_admin";
	public static final String SESSION_USER="session_user";
	public static final String SITE_TITLE="site_title";
	public static final String SITE_KEYWORDS="site_keywords";
	public static final String SITE_DESCRIPTION="site_description";
	public static final String DOMAIN="domain";
	public static final String DOMAIN_SLASH="domain_slash";
	
	public static final String TABLE_USER="user";
	public static final String IMAGE_UPDATE_TEMP="/temp";
	
	
	public static final String VERIFY_CODE="VERIFY_CODE";
	
	public static final int ACCESS_LOG_MAX_RECORD=20000;
	public static final int ACCESS_LOG_MIN_RECORD=10000;
	public static class USER{
		
		
		/**
		 * 密码和确认密码不相同
		 */
		public static final int PASSWORD_NOT_EQUALS=-105;
		/**
		 * 登录名为空
		 */
		public static final int LOGIN_NAME_NOT_EMPTY=-102;
		/**
		 * 密码少于6位
		 */
		public static final int PASSWORD_LESS_6=-103;
		/**
		 * 密码不能为空
		 */
		public static final int PASSWORD_NOT_EMPTY=-104;
		/**
		 * 原密码不能为空
		 */
		public static final int OLD_PASSWORD_NOT_NONE=-111;
		
		/**
		 * 新密码不能为空
		 */
		public static final int NEW_PASSWORD_NOT_NONE=-112;
		
		/**
		 * 确认新密码不能为空
		 */
		public static final int NEW_PASSWORD2_NOT_NONE=-113;
		
		/**
		 * 用户未登录
		 */
		public static final int NOT_LOGIN=CODE.USER_NOT_LOGIN;
		
		/**
		 * 密码错误
		 */
		public static final int WRONG_PASSWORD=-2;
		
		/**
		 * 用户不存在
		 */
		public static final int USER_DOES_NOT_EXIST=-120;
		
		/**
		 * email不能为空
		 */
		public static final int EMAIL_NOT_NONE=-100;
		
		/**
		 * 只能浏览，不能发文章
		 */
		public static final int LEVEL_VIEWER=100;
		
		/**
		 * 普通用户，可以发文章
		 */
		public static final int LEVEL_USER=500;
		
		/**
		 * 管理员
		 */
		public static final int LEVEL_ADMIN=1000;
	}
	public static class CODE{
		
		public static final int SUCCESS=1;
		/**
		 * 未登录
		 */
		public static final int USER_NOT_LOGIN=-1001;
		/**
		 * 级别不够
		 */
		public static final int USER_LEVEL_LOW=-200;
		
		
		/**
		 * 不是创建者，不能继续操作
		 */
		public static final int USER_NOT_CREATOR=-2001;
		
		/**
		 * 删除失败
		 */
		public static final int DB_DELETE_FAILS=-3001;
		/**
		 * 查询不到记录
		 */
		public static final int DB_NOT_QUERY_RESULT=-3002;
		
		/**
		 * 保存失败
		 */
		public static final int DB_SAVE_ERROR=-3003;

		/**
		db_tb不能为空
		 */
		public static final int DB_TB_NOT_EMPTY=-3004;

		public static final int ERROR=-10000;
		
	}

}
