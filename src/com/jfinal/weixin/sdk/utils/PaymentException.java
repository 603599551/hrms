package com.jfinal.weixin.sdk.utils;

/**
 * 支付异常
 * @author L.cm
 *
 */
public class PaymentException extends Exception {
	private static final long serialVersionUID = 6615472990468215919L;

	private final String returnCode;
	private final String returnMsg;
	
	public PaymentException(String returnCode, String returnMsg) {
		super();
		this.returnCode = returnCode;
		this.returnMsg = returnMsg;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public String getReturnMsg() {
		return returnMsg;
	}
}
