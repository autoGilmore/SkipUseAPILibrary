package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

/* 
* The API server response containing the proxy ID and SkipUseToken.
* The proxy ID is a code representing the user's session.
* The SkipUseToken is a changing token that is passed back and forth from the server to the client. 
* This token must be updated each time when communicating with the server.
*/
public class ServerSession {

    private String proxyID = "";

    private String skipUseToken = "";

    public String getProxyID() {
	return proxyID;
    }

    public void setProxyID(String proxyID) {
	if (proxyID == null) {
	    proxyID = "";
	}
	this.proxyID = proxyID;
    }

    public String getSkipUseToken() {
	return skipUseToken;
    }

    public void setSkipUseToken(String skipUseToken) {
	if (skipUseToken == null) {
	    skipUseToken = "";
	}
	this.skipUseToken = skipUseToken;
    }

}
