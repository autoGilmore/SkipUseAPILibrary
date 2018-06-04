package com.autogilmore.throwback.skipUsePackage.exception;

/* 
 * Custom exception for the messaging of SkipUse processing errors.
*/
public class SkipUseException extends Exception {
	private static final long serialVersionUID = -336068448358510972L;

	private String errCode;

	public SkipUseException() {
		super();
	}

	public SkipUseException(Throwable cause) {
		super(cause);
	}

	public SkipUseException(String message) {
		super(message);
	}

	public SkipUseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SkipUseException(String errCode, String message, Throwable cause) {
		super(message, cause);
		this.errCode = errCode;
	}

	public String getErrCode() {
		return this.errCode;
	}

}
