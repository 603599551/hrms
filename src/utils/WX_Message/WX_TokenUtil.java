package utils.WX_Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


public class WX_TokenUtil implements Constants {

    private static Logger log = LoggerFactory.getLogger(WX_TokenUtil.class);
    /**
     *  获得微信 AccessToken
     * access_token是公众号的全局唯一接口调用凭据，公众号调用各接口时都需使用access_token。
     * 开发者需要access_token的有效期目前为2个小时，需定时刷新，重复获取将导致上次获取
     * 的access_token失效。
     * （此处我是把token存在Redis里面了）
     */

    public static AccessToken getWXToken() {

        String tokenUrl = getTokenUrlPre+"?grant_type=client_credential&appid="+ appId+"&secret="+ appSecret;
        JSONObject jsonObject = WX_HttpsUtil.httpsRequest(tokenUrl, "GET", null);
        System.out.println("jsonObject:"+jsonObject);
        AccessToken access_token = new AccessToken();
        if (null != jsonObject) {
            try {
                access_token.setAccessToken(jsonObject.getString("access_token"));
                access_token.setExpiresin(jsonObject.getInteger("expires_in"));
            } catch (JSONException e) {
                access_token = null;
                // 获取token失败
                log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
            }
        }

        return access_token;
    }

}