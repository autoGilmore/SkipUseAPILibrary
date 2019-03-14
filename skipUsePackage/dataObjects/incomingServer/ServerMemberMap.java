package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import java.util.HashMap;
import java.util.Map;

/* 
* The API server response containing a MemberCategoryList.
*/
public class ServerMemberMap extends ServerResponse {

	// JSON object name
	public static final String NAME = "memberIDMap";

	// Map of Member name and their ID.
	private Map<String, Long> memberIDMap = new HashMap<String, Long>();

	public Map<String, Long> getMemberIDMap() {
		return memberIDMap;
	}

	// Expected server JSON object name (don't rename)
	public void setMemberIDMap(Map<String, Long> memberIDMap) {
		this.memberIDMap = memberIDMap;
	}

}
