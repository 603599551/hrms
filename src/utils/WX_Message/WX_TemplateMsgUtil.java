package utils.WX_Message;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;

public class WX_TemplateMsgUtil implements Constants{

    private static Logger log = LoggerFactory.getLogger(WX_TemplateMsgUtil.class);

    /**
     * 封装模板详细信息
     * @return
     */
    public static JSONObject packJsonmsg(Map<String, TemplateData> param) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String,TemplateData> entry : param.entrySet()) {
            JSONObject keyJson = new JSONObject();
            TemplateData  dta=  entry.getValue();
            keyJson.put("value",dta.getValue());
            keyJson.put("color", dta.getColor());
            json.put(entry.getKey(), keyJson);
        }
        return json;
    }

    /**
     * 根据模板的编号 新增并获取模板ID
     * @param templateSerialNumber 模板库中模板的 "编号"
     * @return 模板ID
     */
    public static String getWXTemplateMsgId(String templateSerialNumber){
        String tmpurl = addTemplateUrl+"?access_token="+ WX_TokenUtil.getWXToken().getAccessToken();
        JSONObject json = new JSONObject();
        json.put("template_id_short", templateSerialNumber);
        JSONObject result = WX_HttpsUtil.httpsRequest(tmpurl, "POST", json.toString());
        JSONObject resultJson = new JSONObject(result);
        String errmsg = (String) resultJson.get("errmsg");
        log.info("获取模板编号返回信息：" + errmsg);
        if(!"ok".equals(errmsg)){
            return "error";
        }
        String templateId = (String) resultJson.get("template_id");
        return templateId;
    }

    /**
     * 根据模板ID 删除模板消息
     * @param templateId 模板ID
     * @return
     */
    public static String deleteWXTemplateMsgById(String templateId){
        String tmpurl = delTemplateUrl+"?access_token="+ WX_TokenUtil.getWXToken().getAccessToken();
        JSONObject json = new JSONObject();
        json.put("template_id", templateId);
        try{
            JSONObject result = WX_HttpsUtil.httpsRequest(tmpurl, "POST", json.toString());
            JSONObject resultJson = new JSONObject(result);
            log.info("删除"+templateId+"模板消息,返回CODE："+ resultJson.get("errcode"));
            String errmsg = (String) resultJson.get("errmsg");
            if(!"ok".equals(errmsg)){
                return "error";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return "success";
    }


    /**
     * 发送微信消息(模板消息)
     * @param touser 用户 OpenID
     * @param templatId 模板消息ID
     * @param clickurl URL置空，则在发送后，点击模板消息会进入一个空白页面（ios），或无法点击（android）。
     * @param topcolor 标题颜色
     * @param data 详细内容
     * @return
     */
    public static String sendWechatMsgToUser(String touser, String templatId, String clickurl, String topcolor, JSONObject data) {
        String tmpurl = sendMessageUrl+"?access_token="+ WX_TokenUtil.getWXToken().getAccessToken();
        JSONObject json = new JSONObject();
        json.put("touser", touser);
        json.put("template_id", templatId);
        json.put("url", clickurl);
        json.put("topcolor", topcolor);
        json.put("data", data);
        try{
            JSONObject result = WX_HttpsUtil.httpsRequest(tmpurl, "POST", json.toString());
            JSONObject resultJson = new JSONObject(result);
            System.out.println("发送微信消息返回信息：" + resultJson.get("errcode") + "----" + resultJson.get("errmsg"));
            String errmsg = (String) resultJson.get("errmsg");
            if(!"ok".equals(errmsg)){  //如果为errmsg为ok，则代表发送成功，公众号推送信息给用户了。
                return "error";
            }
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }finally {
            if(templatId!=null) {
                //删除新增的 微信模板
                //deleteWXTemplateMsgById(templatId);
            }
        }
        return "success";
    }

}