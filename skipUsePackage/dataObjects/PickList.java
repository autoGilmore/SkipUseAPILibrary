package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * This is a list of Picks.
*/
public class PickList {

	// Picks (not Pick IDs)
	private List<Pick> pickList = new ArrayList<Pick>();

	public List<Pick> getPickList() {
		return pickList;
	}

	public void setPickList(List<Pick> pickList) {
		if (pickList != null)
			this.pickList = pickList;
	}

	public void add(Pick pick) {
		if (pick != null && !this.pickList.contains(pick))
			this.pickList.add(pick);
	}

}
