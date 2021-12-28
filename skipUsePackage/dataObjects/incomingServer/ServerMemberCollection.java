package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCollection;

/* 
* The API server response containing a MemberCollection.
*/
public class ServerMemberCollection extends ServerResponse {

	// JSON object name
	public static final String NAME = "memberCollection";

	private MemberCollection memberCollection = new MemberCollection();

	public MemberCollection getMemberCollection() {
		return memberCollection;
	}

	// Expected server JSON object name (don't rename)
	public void setMemberCollection(MemberCollection clientMemberCollection) {
		this.memberCollection = clientMemberCollection;
	}
}
