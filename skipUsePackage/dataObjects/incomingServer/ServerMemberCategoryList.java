package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;

/* 
* The API server response containing a MemberCategoryList.
*/
public class ServerMemberCategoryList extends ServerResponse {

	// JSON object name
	public static final String NAME = "clientMemberCategoryList";

	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	public MemberCategoryList getMemberCategoryList() {
		return memberCategoryList;
	}

	// Expected server JSON object name (don't rename)
	public void setClientMemberCategoryList(MemberCategoryList clientMemberCategoryList) {
		this.memberCategoryList = clientMemberCategoryList;
	}
}
