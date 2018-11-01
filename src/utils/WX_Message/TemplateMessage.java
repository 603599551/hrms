package utils.WX_Message;

import java.util.Map;

/**
 * 注册成功，通知模板消息实体类
 */
public class TemplateMessage {
    private String touser; //用户OpenID
    private String template_id; //模板消息ID
    private String url; //URL置空，在发送后，点模板消息进入一个空白页面（ios），或无法点击（android）。
    private String topcolor; //标题颜色
    private Map<String, TemplateData> templateData; //模板详细信息

    public static TemplateMessage New() {
        return new TemplateMessage();
    }

    /**
     * @return the touser
     */
    public String getTouser() {
        return touser;
    }
    /**
     * @param touser the touser to set
     */
    public void setTouser(String touser) {
        this.touser = touser;
    }
    /**
     * @return the template_id
     */
    public String getTemplate_id() {
        return template_id;
    }
    /**
     * @param template_id the template_id to set
     */
    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return the topcolor
     */
    public String getTopcolor() {
        return topcolor;
    }
    /**
     * @param topcolor the topcolor to set
     */
    public void setTopcolor(String topcolor) {
        this.topcolor = topcolor;
    }
    /**
     * @return the templateData
     */
    public Map<String, TemplateData> getTemplateData() {
        return templateData;
    }
    /**
     * @param templateData the templateData to set
     */
    public void setTemplateData(Map<String, TemplateData> templateData) {
        this.templateData = templateData;
    }



}