package com.jiguang;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JiguangPush {
    protected static final Logger LOG = LoggerFactory.getLogger(JiguangPush.class);

    private String appKey;
    private String masterSecret;
    /**
     * 设置收听者
     */
    private Audience audience=Audience.all();

    private String alert;
    /**
     * 扩展消息
     */
    private Map<String, String> extras = new HashMap<String, String>();
    private Platform platform=Platform.android_ios();
    public static void main(String[] args) {
        JiguangPush push=new JiguangPush("6863f15c5be031f95b5de21c","130e4cbb7f9e821a26158183");
        push.setAlert("测试");
//        push.setAndroidTitle("安卓测试");
        push.setTag("tag1");
        try {
            push.sendPush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public JiguangPush(String appKey, String masterSecret){
        this.appKey=appKey;
        this.masterSecret=masterSecret;
    }

    public void sendPush() throws Exception {
        cn.jiguang.common.ClientConfig clientConfig = cn.jiguang.common.ClientConfig.getInstance();
        final JPushClient jpushClient = new JPushClient(masterSecret, appKey, null, clientConfig);
        // Here you can use NativeHttpClient or NettyHttpClient or ApacheHttpClient.
        // Call setHttpClient to set httpClient,
        // If you don't invoke this method, default httpClient will use NativeHttpClient.
//        ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null, clientConfig);
//        jpushClient.getPushClient().setHttpClient(httpClient);
        final PushPayload payload = buildPushObject_android_and_ios();
//        // For push, all you need do is to build PushPayload object.
//        PushPayload payload = buildPushObject_all_alias_alert();
        try {
            PushResult result = jpushClient.sendPush(payload);
//            System.out.println(result);

            // 如果使用 NettyHttpClient，需要手动调用 close 方法退出进程
            // If uses NettyHttpClient, call close when finished sending request, otherwise process will not exit.
            // jpushClient.close();
        } catch (Exception e) {
            throw e;
        }
    }
    public void setTag(String ... tags){
        audience=Audience.tag(tags);
    }
    public void setAlias(String[] aliases){
        audience=Audience.alias(aliases);
    }
    public PushPayload buildPushObject_android_and_ios() {
//        extras.put("test", "https://community.jiguang.cn/push");
        return PushPayload.newBuilder()
                .setPlatform(platform)
                .setAudience(audience)
                .setNotification(Notification.newBuilder()
                        .setAlert(alert)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setTitle(androidTitle)
                                .addExtras(extras).build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1)
                                .addExtras(extras).build())
                        .build())
                .build();
    }
    public void ios(){
        platform=Platform.ios();
    }
    public void android(){
        platform=Platform.android();
    }
    public void all(){
        platform=Platform.all();
    }
    public void addExtra(String key,String value){
        extras.put(key,value);
    }
    public void setAlert(String alert) {
        this.alert = alert;
    }
    private String androidTitle;
    public void setAndroidTitle(String androidTitle){
        this.androidTitle=androidTitle;
    }
}
