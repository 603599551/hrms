package utils.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class Javascript {
	private HttpServletResponse response;
	private String content="text/html";
	private String charset="UTF-8";
	
	public Javascript(){
	}
	public Javascript(HttpServletResponse response){
		this.response=response;
	}
	public Javascript(HttpServletResponse response,String content,String charset){
		this.response=response;
		this.content=content;
		this.charset=charset;
	}
	/**
	 * 提示信息
	 * @param script
	 * @return
	 * @throws IOException 
	 */
	public void alertAndBack(String script) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("history.go(-1);\r\n");
		strInfo=strInfo.append("</script>\r\n");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	public static String alertAndBackStr(String script) {
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("history.go(-1);\r\n");
		strInfo=strInfo.append("</script>\r\n");
		return strInfo.toString();
	}
	public static String alertStr(String script) {
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("</script>\r\n");
		return strInfo.toString();
	}
	/**
	 * 提示信息,并关闭窗口

	 * @param script
	 * @return
	 * @throws IOException 
	 */
	public void alertAndClose(String script) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("window.close();\r\n");
		strInfo=strInfo.append("</script>\r\n");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	   /**
     * 提示信息,并关闭窗口
     * 刷新父窗口 opener
     * @param script
     * @return
     * @throws IOException 
     */
    public void alertAndCloseAndrefreshOpener(String script) throws IOException{
        StringBuffer strInfo=new StringBuffer();
        strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
        strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
        strInfo=strInfo.append("opener.location.reload();\r\n");
        strInfo=strInfo.append("window.close();\r\n");
        strInfo=strInfo.append("</script>\r\n");
        response.setContentType(content+";charset="+charset);
        try {
            response.getWriter().print(strInfo);
        } catch (IOException e) {
            throw e;
        }
    }
	
	
	/**
	 * 关闭窗口
	 * @throws IOException 
	 *
	 */
	public void closeWindow() throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">");
		strInfo=strInfo.append("window.close();");
		strInfo=strInfo.append("</script>");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 关闭open窗口,并刷新父窗口
	 * @throws IOException 
	 *
	 */
	public void closeOpenWindow() throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">");
		strInfo=strInfo.append("window.close();window.opener.location.reload();");
		strInfo=strInfo.append("</script>");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 提示并刷新

	 * @throws IOException 
	 *
	 */
	public void alertAndReload(String info) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">");
		strInfo=strInfo.append("alert(\'"+info+"\');\r\n");
		strInfo=strInfo.append("history.go(-1);");
		strInfo=strInfo.append("location.reload();");
		strInfo=strInfo.append("</script>");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 直接输出纯文本的方法
	 * @param plainText
	 * @throws IOException 
	 */
	public void outPlainText(String plainText) throws IOException{
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(plainText);
		} catch (IOException e) {
			throw e;
		}		
	}
	/**
	 * 提示信息，并跳转到URL
	 * @param script
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	public void alertAndRedirect(String script,String url) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("location.href='"+url+"';\r\n");
		strInfo=strInfo.append("</script>\r\n");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	public static String alertAndRedirectStr(String script,String url) {
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("location.href='"+url+"';\r\n");
		strInfo=strInfo.append("</script>\r\n");
		return strInfo.toString();
	}
	public static String getAlertAndBack(String script) {
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("alert(\'"+script+"\');\r\n");
		strInfo=strInfo.append("history.go(-1);\r\n");
		strInfo=strInfo.append("</script>\r\n");
		return strInfo.toString();
	}
	public static String getScript(String script) {
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append(script);
		strInfo=strInfo.append("</script>\r\n");
		return strInfo.toString();
	}
	/**
	 * 通过js实现跳转
	 * @param url
	 * @throws IOException
	 */
	public void forward(String url) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append("top.location.href='"+url+"';\r\n");
		strInfo=strInfo.append("</script>\r\n");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
	/**
	 * 输出javascript
	 * @param script
	 * @throws IOException
	 */
	public void outJavaScript(String script) throws IOException{
		StringBuffer strInfo=new StringBuffer();
		strInfo=strInfo.append("<script language=\"JavaScript\">\r\n");
		strInfo=strInfo.append(script);
		strInfo=strInfo.append("</script>\r\n");
		response.setContentType(content+";charset="+charset);
		try {
			response.getWriter().print(strInfo);
		} catch (IOException e) {
			throw e;
		}
	}
}
