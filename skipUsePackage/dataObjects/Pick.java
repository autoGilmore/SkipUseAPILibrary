package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/* 
 * A 'Pick' is the stored data for a member's usage of a Pick ID.
*/
public class Pick {

	// Your Pick ID.
	private String pickID = "";

	// The member ID the Pick belongs to.
	private int memberID = -1;

	// Number of times Skipped.
	private int skipped = 0;

	// Number of times Used.
	private int used = 0;

	// My additional JSON.
	private String json = "";

	// Ignore flag for use during typical Pick queries.
	private boolean stopUsing = false;

	// Percentage 0-100, where 0 is most Skipped and 100 is most Used.
	private int autoRatePercentage = 50;

	// Faster trending percentage 0-100.
	private int trendingRatePercentage = 50;

	// Flag to indicate no Pick information has been stored yet.
	private boolean newPick = true;

	// List of a member's categories that are marked for this Pick.
	// Will be empty if includeCategories is not set in PickQuery.
	private List<String> categoryList = new ArrayList<String>();

	// Timestamp when Pick was last updated. Rounding by minutes.
	@JsonProperty("lastUpdated")
	private Timestamp _lastUpdated;

	public Pick() {
	}

	public String getPickID() {
		return pickID;
	}

	public void setPickID(String value) {
		if (value != null)
			this.pickID = value;
	}

	public int getMemberID() {
		return this.memberID;
	}

	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public int getSkipped() {
		return skipped;
	}

	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}

	public boolean isStopUsing() {
		return stopUsing;
	}

	public void setStopUsing(boolean stopUsing) {
		this.stopUsing = stopUsing;
	}

	public int getAutoRatePercentage() {
		return autoRatePercentage;
	}

	public void setAutoRatePercentage(int autoRatePercentage) {
		this.autoRatePercentage = autoRatePercentage;
	}

	public String getJSON() {
		return json;
	}

	public void setJSON(String json) {
		if (json != null)
			this.json = json;
	}

	public int getTrendingRatePercentage() {
		return trendingRatePercentage;
	}

	public void setTrendingRatePercentage(int trendingRatePercentage) {
		this.trendingRatePercentage = trendingRatePercentage;
	}

	public List<String> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<String> categoryList) {
		if (categoryList != null)
			this.categoryList = categoryList;
	}

	public void setNewPick(boolean newPick) {
		this.newPick = newPick;
	}

	public boolean isNewPick() {
		return newPick;
	}

	public Timestamp _getLastUpdated() {
		return _lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		if (lastUpdated != null)
			this._lastUpdated = lastUpdated;
	}
}
