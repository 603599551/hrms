package utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Crypt {

	private static String hexStr = "0123456789ABCDEF";

	private static String[] binaryArray = { "0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111" };

	private static String Algorithm = "DES"; // 定义 加密算法,可用

	// DES,DESede,Blowfish

	static boolean debug = false;

	static {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
	}

	// 生成密钥, 注意此步骤时间比较长
	public static byte[] getKey() throws Exception {
		KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
		SecretKey deskey = keygen.generateKey();
		if (debug)
			System.out.println("生成密钥:" + byte2hex(deskey.getEncoded()));
		return deskey.getEncoded();
	}

	// 加密
	public static byte[] encode(byte[] input, byte[] key) throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
		if (debug) {
			System.out.println("加密前的二进串:" + byte2hex(input));
			System.out.println("加密前的字符串:" + new String(input));
		}
		Cipher c1 = Cipher.getInstance(Algorithm);
		c1.init(Cipher.ENCRYPT_MODE, deskey);
		byte[] cipherByte = c1.doFinal(input);
		if (debug)
			System.out.println("加密后的二进串:" + byte2hex(cipherByte));
		return cipherByte;
	}

	// 加密
	public static String encodeToString(String input, String key)
			throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key.getBytes(),
				Algorithm);
		if (debug) {
			System.out.println("加密前的二进串:" + byte2hex(input.getBytes()));
			System.out.println("加密前的字符串:" + new String(input));
		}
		Cipher c1 = Cipher.getInstance(Algorithm);
		c1.init(Cipher.ENCRYPT_MODE, deskey);
		byte[] cipherByte = c1.doFinal(input.getBytes());
		if (debug)
			System.out.println("加密后的二进串:" + new String(cipherByte));
		return new String(Base64.encode(cipherByte));
	}

	// 解密
	public static byte[] decode(byte[] input, byte[] key) throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
		if (debug)
			System.out.println("解密前的信息:" + byte2hex(input));
		Cipher c1 = Cipher.getInstance(Algorithm);
		c1.init(Cipher.DECRYPT_MODE, deskey);
		byte[] clearByte = c1.doFinal(input);
		if (debug) {
			System.out.println("解密后的二进串:" + byte2hex(clearByte));
			System.out.println("解密后的字符串:" + (new String(clearByte)));
		}
		return clearByte;
	}

	// 解密
	public static String decodeToString(String input, String key)
			throws Exception {
		SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key.getBytes(),
				Algorithm);
		byte[] bytes = Base64.decode(input);
		if (debug)
			System.out.println("解密前的信息:" + byte2hex(bytes));
		Cipher c1 = Cipher.getInstance(Algorithm);
		c1.init(Cipher.DECRYPT_MODE, deskey);
		byte[] clearByte = c1.doFinal(bytes);
		if (debug) {
			System.out.println("解密后的二进串:" + byte2hex(clearByte));
			System.out.println("解密后的字符串:" + (new String(clearByte)));
		}
		return (new String(clearByte));
	}

	// md5()信息摘要, 不可逆
	public static byte[] md5(byte[] input) throws Exception {
		java.security.MessageDigest alg = java.security.MessageDigest
				.getInstance("MD5"); // or "SHA-1"
		if (debug) {
			System.out.println("摘要前的二进串:" + byte2hex(input));
			System.out.println("摘要前的字符串:" + new String(input));
		}
		alg.update(input);
		byte[] digest = alg.digest();
		if (debug)
			System.out.println("摘要后的二进串:" + byte2hex(digest));
		return digest;
	}

	// 字节码转换成16进制字符串
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			// if (n < b.length - 1)
			// hs = hs + ":";
		}
		return hs.toLowerCase();
	}

	/**
	 * 
	 * @param str
	 * @return 转换为二进制字符串
	 */
	public static String bytes2BinaryStr(byte[] bArray) {

		String outStr = "";
		int pos = 0;
		for (byte b : bArray) {
			// 高四位
			pos = (b & 0xF0) >> 4;
			outStr += binaryArray[pos];
			// 低四位
			pos = b & 0x0F;
			outStr += binaryArray[pos];
		}
		return outStr;

	}

	/**
	 * 
	 * @param bytes
	 * @return 将二进制转换为十六进制字符输出
	 */
	public static String BinaryToHexString(byte[] bytes) {

		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex + " ";
		}
		return result;
	}

	/**
	 * 
	 * @param hexString
	 * @return 将十六进制转换为字节数组
	 */
	public static byte[] HexStringToBinary(String hexString) {
		// hexString的长度对2取整，作为bytes的长度
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}

	public static void main(String[] args) throws Exception {
		debug = true;
		// byte[] key = getKey();
		decodeToString(encodeToString("123", "zhanglei"), "zhanglei");
		md5("测试加密".getBytes());
	}
}