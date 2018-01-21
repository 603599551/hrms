package utils;

import java.security.MessageDigest;

public class MD5Util {
	public final static String MD5(String s) {
		return MD5(s,null);
	}
	public final static String MD5(String s,String characterEncoding) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = null;
            if(characterEncoding==null){
            	btInput=s.getBytes();
            }else{
            	btInput=s.getBytes(characterEncoding);
            }
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	public static void main(String[] args){
		System.out.println(MD5("appid=wxfd9a480be9494690&body=LEO啊&mch_id=10036662&nonce_str=03404c506201479897bfa5966210a1d1&notify_url=http://122.139.57.147/wxPay/getPayInfo?cid=123456&openid=oSF0AuJOdMJf6fx3WtqiE7A_z4R4&out_trade_no=201412051510&spbill_create_ip=36.48.109.97&total_fee=1&trade_type=JSAPI&key=12d4aaff0e5c4a32ba3f257f52c4619e"));
	}
}
