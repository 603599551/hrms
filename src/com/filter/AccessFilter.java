package com.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.jfinal.KEY;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import com.jsoft.crm.utils.UserSessionUtil;
import easy.security.MD5Util;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.ShortID;
import easy.util.StringUtils;
import easy.web.UrlKit;

/**
 * @author mym
 * 访问过滤器，有记录日志的功能，所有访问者，都会记录需要在sys_conf表中配置
 * access_log=true，开启访问日志
 * access_log_max_record，日志的最大记录数，默认为2万条
 */
public class AccessFilter implements Filter{

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req=(HttpServletRequest)request;
		HttpServletResponse resp=(HttpServletResponse)response;
		resp.setContentType("text/html;charset=UTF-8");

		String ip=request.getRemoteAddr();
//		System.out.println(getClass()+"::"+ip);
		resp.setHeader("Access-Control-Allow-Origin", "*");
//		resp.setHeader("Access-Control-Allow-Origin", "http://192.168.1.102:8080");
		String domain=UrlKit.getDomain(req);
		req.setAttribute("domain",domain);

		boolean isLogin=isLogin(req,resp);//处理自动登录
		if(isLogin){
			chain.doFilter(request, response);
		}




	}


	/**
	 * 当访问html、jsp时，读取cookie，自动登录
	 * @param req
	 */
	private boolean isLogin(HttpServletRequest req,HttpServletResponse resp){

		String servletPath=req.getServletPath().toLowerCase();  
//		String suffix=servletPath.substring(servletPath.lastIndexOf(".")+1);
//		if(servletPath.indexOf(".")==-1){
//
//		}else
//		Map adminSession=(Map)req.getSession().getAttribute(KEY.SESSION_ADMIN);
//		Map userSession=(Map)req.getSession().getAttribute(KEY.SESSION_USER);
		UserSessionUtil usu=new UserSessionUtil(req);
		if("admin".equals(usu.getUsername())){//访问后台页面
			return true;
		}else if(servletPath.startsWith("/user") ){//访问用户登录后才能访问的目录
			if(usu.getUserId()==null){
				//System.out.println("--com.club.filter.AccessFilter已经登录，不需要自动登录");
				try {
					resp.sendRedirect(UrlKit.getDomain(req)+"/login.jsp");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}else{
				return true;
			}
		}else if(servletPath.startsWith("/api/safe")){
			if(usu.getUserBean()==null){
				Map reMap=new HashMap();
				reMap.put("code",-1000);
				reMap.put("msg","请先登录！");
				try {
					String jsonStr=JSON.toJSONString(reMap);
					resp.getWriter().write(jsonStr);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}else{
				return true;
			}
		}else if(servletPath.startsWith("/index") ){//访问用户登录后才能访问的目录
			if(usu.getUserBean()==null){
				//System.out.println("--com.club.filter.AccessFilter已经登录，不需要自动登录");
				try {
					resp.sendRedirect(UrlKit.getDomain(req)+"/login.html");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}else{
				return true;
			}
		}


//		String username=CookieUtil.getValue(req,"login","username");
//		String password=CookieUtil.getValue(req,"login","password");
//		if(StringUtils.isNotNone(username) && StringUtils.isNotNone(password)){
//			Record db=Db.findFirst("select * from user where email=?",username);
//			if(db!=null){
//				String passworddb=db.getStr("password");
//				String passwordKey=KEY.SYS_CONFIG.get("password_key");
//				String passwordMD5=MD5Util.MD5(password+passwordKey);
//				if(passworddb.equals(password) || passworddb.equals(passwordMD5)){
//					int status=NumberUtils.parseInt(db.get("status"), 0);
//					if(status<100){
//
//					}else{
//						UserBean ub=new UserBean();
//						ub.setEmail(db.getStr("email"));
//						ub.setId(db.getStr("id"));
//						ub.setLoginNum(NumberUtils.parseInt((Integer)db.get("login_num"),0));
//						ub.setRealName(db.getStr("real_name"));
//						ub.setLevel(NumberUtils.parseInt(db.get("level"), 0));
//						ub.setStatus(status);
//						ub.setQqOpenid(db.getStr("qq_openid"));
//						req.getSession().setAttribute(KEY.SESSION_USER, ub);
//						//System.out.println("--com.club.filter.AccessFilter自动登录成功！");
//
//						UserModel um=UserModel.dao;
//						um.addLoginNum(db.getStr("id"));
//						um.updateLastLoginTime(db.getStr("id"), DateTool.GetDateTime());
//					}
//				}else{
//	//				result=KEY.USER.WRONG_PASSWORD;//密码不正确
//	//				jhm.put("msg", "密码不正确");
//				}
//			}else{
//	//			result=-1;//用户名不存在
//	//			jhm.put("msg", "用户名不存在");
//			}
//		}else{
//			//System.out.println("--com.club.filter.AccessFilter没获取到用户名密码！");
//		}
		return true;
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args){
//		String servletPath="/index/";
//		if(servletPath.indexOf(".")==-1){
//			System.out.println(1);
//		}else if((!servletPath.endsWith("html") && !servletPath.endsWith("jsp"))){
//			System.out.println(2);
//		}else{
//			System.out.println(3);
//		}
		String servletPath="/use";
		if(servletPath.length()>=5 && servletPath.subSequence(0, 5).equals("/user")){
			System.out.println(1);
		}else{
			System.out.println(2);
		}
	}
}
