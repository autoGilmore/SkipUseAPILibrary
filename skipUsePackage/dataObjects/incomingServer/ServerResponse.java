package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import org.springframework.http.HttpStatus;

/* 
* The API server response containing additional data.
*/
public class ServerResponse extends ServerSession {
	// JSON object name
	public static final String NAME = "status";

	private HttpStatus status = HttpStatus.EXPECTATION_FAILED;
	private String remainingNibbles = "";
	private String memberName = "";
	private int memberID = -1;
	private String errorMessage = "";
	private boolean confirmRequired = false;
	private boolean validationCodeRequired;
	private String message = "";

	public HttpStatus getStatus() {
		return status;
	}

	// Expected server JSON object name (don't rename)
	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public String getRemainingNibbles() {
		return remainingNibbles;
	}

	public void setRemainingNibbles(String remainingNibbles) {
		this.remainingNibbles = remainingNibbles;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public int getMemberID() {
		return memberID;
	}

	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isConfirmRequired() {
		return confirmRequired;
	}

	public void setConfirmRequired(boolean confirmRequired) {
		this.confirmRequired = confirmRequired;
	}

	public boolean isValidationCodeRequired() {
		return validationCodeRequired;
	}

	public void setValidationCodeRequired(boolean validationCodeRequired) {
		this.validationCodeRequired = validationCodeRequired;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
