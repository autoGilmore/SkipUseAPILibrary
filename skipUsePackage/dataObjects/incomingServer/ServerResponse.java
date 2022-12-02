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
    private String ownerName = "";
    private long ownerID = 0;
    private String errorMessage = "";
    private boolean followUpRequired = false;
    private boolean validationCodeRequired;
    private String message = "";

    public HttpStatus getStatus() {
        return status;
    }

    // Expected server JSON object name (don't rename)
    public void setStatus(HttpStatus status) {
        if (status == null) {
            status = HttpStatus.EXPECTATION_FAILED;
        }
        this.status = status;
    }

    public String getRemainingNibbles() {
        return remainingNibbles;
    }

    public void setRemainingNibbles(String remainingNibbles) {
        if (remainingNibbles == null) {
            remainingNibbles = "";
        }
        this.remainingNibbles = remainingNibbles;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null) {
            ownerName = "";
        }
        this.ownerName = ownerName;
    }

    public long getOwnerID() {
        return this.ownerID;
    }

    public void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "";
        }
        this.errorMessage = errorMessage;
    }

    public boolean isFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
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
        if (message == null) {
            message = "";
        }
        this.message = message;
    }
}
