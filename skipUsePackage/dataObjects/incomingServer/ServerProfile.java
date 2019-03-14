package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;

/* 
* The API server response with the account owner's profile.
*/
public class ServerProfile extends ServerResponse {

	// JSON object name
	public static final String NAME = "clientProfile";

	private Profile profile = new Profile();

	public Profile getProfile() {
		return profile;
	}

	public void setClientProfile(Profile clientProfile) {
		if (clientProfile != null)
			this.profile = clientProfile;
	}
}
