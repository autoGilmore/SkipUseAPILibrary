package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;

/* 
* The API server response containing a list of Picks.
*/
public class ServerPickList extends ServerResponse {

	// JSON object name
	public static final String NAME = "pickList";

	private List<Pick> pickList = new ArrayList<Pick>();

	public void addPick(Pick pick) {
		this.pickList.add(pick);
	}

	public void setPickList(List<Pick> pickList) {
		this.pickList = pickList;
	}

	// Expected server JSON object name (don't rename)
	public List<Pick> getPickList() {
		return pickList;
	}

}
