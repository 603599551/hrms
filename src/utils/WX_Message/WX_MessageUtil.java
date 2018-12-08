package utils.WX_Message;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;


public class WX_MessageUtil implements Constants {
    public static void main(String[] args) {
//        Map<String,TemplateData> param = new HashMap<>();
//            /*
//            {{first.DATA}}
//订单号：{{keyword1.DATA}}
//订单状态：{{keyword2.DATA}}
//时间：{{keyword3.DATA}}
//{{remark.DATA}}
//             */
//        param.put("first",new TemplateData("订单备注通知！","#000000"));
//        param.put("keynote1",new TemplateData("CZ201810260001","#000000"));
//        param.put("keynote2",new TemplateData("充值成功！","#000000"));
////            param.put("keyword3",new TemplateData("2018-10-26","#696969"));
//        param.put("remark",new TemplateData("祝愉快！","#000000"));
//        //新增用户成功 - 推送微信消息
//        senMsg(openId,templateId,appId,appSecret, param);
    }
    public static void senMsg(String openId,String templateId,String clickUrl, Map<String,TemplateData> param){
        if (StringUtils.isEmpty(clickUrl)){
            clickUrl="";
        }
        //用户是否订阅该公众号标识 (0代表此用户没有关注该公众号 1表示关注了该公众号)
        Integer  state= WX_UserUtil.subscribeState(openId);
        System.out.println("state:"+state);
        // 绑定了微信并且关注了服务号的用户 , 注册成功-推送注册短信
        if(state==1){

            //注册的微信-模板Id
            // String regTempId =  WX_TemplateMsgUtil.getWXTemplateMsgId("ywBb70467vr18");
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(param));
            //调用发送微信消息给用户的接口
            WX_TemplateMsgUtil.sendWechatMsgToUser(openId,templateId, clickUrl,
                    "#000000", jsonObject);


            //获取公众号的自动回复规则
//            String urlinfo=getCurrentAutoReplyUrl+"?access_token="+WX_TokenUtil.getWXToken().getAccessToken();
//            JSONObject joinfo = WX_HttpsUtil.httpsRequest(urlinfo, "GET", null);
//            Object o=joinfo.get("is_add_friend_reply_open");
//            // System.out.println("o:"+joinfo);
//            String getTokenUrl = getTokenUrlPre +"?grant_type=client_credential&appid="+ appId + "&secret=" + appSecret + "";
//            JSONObject Token = WX_HttpsUtil.httpsRequest(getTokenUrl, "GET", null);
//            System.out.println("Token:"+Token);


        }
    }

}