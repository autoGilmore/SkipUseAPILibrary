package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/*
 * This is a list of Pick IDs.
*/
public class PickIDList {

	// The Pick ID list.
	private List<String> pickIDList = new ArrayList<String>();

	// Flag that Pick IDs are comma + space delimited.
	private boolean splitCSV = false;

	public List<String> getPickIDList() {
		return pickIDList;
	}

	public void addPickID(int pickID) {
		addPickID("" + pickID);
	}

	public void addPickID(String pickID) {
		if (pickID != null) {
			String trimPick = pickID.trim();
			if (!trimPick.isEmpty() && !pickIDList.contains(trimPick.trim())) {
				this.pickIDList.add(trimPick);
			}
		}
	}

	public void setPickIDList(List<String> pickIDList) {
		if (pickIDList != null && !pickIDList.isEmpty()) {
			for (String pick : pickIDList) {
				addPickID(pick);
			}
		}
	}

	public void clearPickIDList() {
		this.pickIDList.clear();
	}

	public boolean isSplitCSV() {
		return this.splitCSV;
	}

	public void setSplitCSV(boolean splitCSV) {
		this.splitCSV = splitCSV;
	}

}
