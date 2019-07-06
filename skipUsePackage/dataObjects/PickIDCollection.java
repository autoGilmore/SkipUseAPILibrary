package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/* 
 * A 'PickIDCollection' is a list of Pick IDs for a member.
*/
public class PickIDCollection {

	// The member's ID for their collection.
	private int memberID = -1;

	// The name for the Collection of Pick IDs.
	private String collectionName = "";

	// The Pick ID list.
	private List<String> pickIDList = new ArrayList<String>();

	// Flag that Pick IDs a comma + space delimited.
	private boolean splitCSV = false;

	// Timestamp when Pick was last updated. Rounding by minutes.
	@JsonProperty("lastUpdated")
	private Timestamp _lastUpdated;

	public PickIDCollection() {
		// NOTE: currently only one collection is allowed. Using a default name.
		setCollectionName(SkipUseProperties.PICK_ID_COLLECTION_NAME);
	}

	public int getMemberID() {
		return this.memberID;
	}

	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		if (collectionName != null)
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
		return this.splitCSV;
	}

	public void setSplitCSV(boolean splitCSV) {
		this.splitCSV = splitCSV;
	}

	public Timestamp _getLastUpdated() {
		return _lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		if (lastUpdated != null)
			this._lastUpdated = lastUpdated;
	}

}
