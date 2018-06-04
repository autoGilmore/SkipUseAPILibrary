package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * A 'PickIDCollection' is a list of Pick IDs.
*/
public class PickIDCollection {

	// The Collection's identification number.
	private int collectionID = -1;

	// The name for the Collection of Pick IDs.
	private String collectionName = "";

	// The Pick ID list.
	private List<String> pickIDList = new ArrayList<String>();

	// Flag that Pick IDs a comma-delimited.
	private boolean isSplitCSV = false;

	public PickIDCollection() {
	}

	public PickIDCollection(String collectionName) {
		setCollectionName(collectionName);
	}

	public int getCollectionID() {
		return this.collectionID;
	}

	public void setCollectionID(int collectionID) {
		this.collectionID = collectionID;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public List<String> getPickIDList() {
		return pickIDList;
	}

	public void setPickIDList(List<String> pickIDList) {
		if (pickIDList != null && !pickIDList.isEmpty()) {
			for (String pick : pickIDList) {
				addPickID(pick);
			}
		}
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

	public void clearPickIDList() {
		this.pickIDList.clear();
	}

	public boolean isSplitCSV() {
		return this.isSplitCSV;
	}

	public void setSplitCSV(boolean splitCSV) {
		this.isSplitCSV = splitCSV;
	}

}
