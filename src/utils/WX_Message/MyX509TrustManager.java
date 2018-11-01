package utils.WX_Message;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;
/**
 * 微信请求 - 信任管理器
 */

public class MyX509TrustManager implements X509TrustManager{

    public MyX509TrustManager() {
        // TODO Auto-generated constructor stub
    }
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        //        return new X509Certificate[0];
        return  null;
    }

}