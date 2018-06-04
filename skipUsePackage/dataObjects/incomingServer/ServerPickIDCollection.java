package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;

/* 
* The API server response containing a PickIDCollection.
*/
public class ServerPickIDCollection extends ServerResponse {

	// JSON object name
	public static final String NAME = "clientCollection";

	private PickIDCollection pickIDCollection = new PickIDCollection();

	public PickIDCollection getPickIDCollection() {
		return pickIDCollection;
	}

	// Expected server JSON object name (don't rename)
	public void setClientCollection(PickIDCollection clientCollection) {
		this.pickIDCollection = clientCollection;
	}
}
