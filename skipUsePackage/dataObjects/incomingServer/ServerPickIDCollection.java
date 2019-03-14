package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDCollection;

/* 
* The API server response containing a PickIDCollection.
*/
public class ServerPickIDCollection extends ServerResponse {

	// JSON object name
	public static final String NAME = "pickIDCollection";

	private MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();

	public MemberPickIDCollection getPickIDCollection() {
		return memberPickIDCollection;
	}

	// Expected server JSON object name (don't rename)
	public void setPickIDCollection(MemberPickIDCollection clientCollection) {
		this.memberPickIDCollection = clientCollection;
	}
}
