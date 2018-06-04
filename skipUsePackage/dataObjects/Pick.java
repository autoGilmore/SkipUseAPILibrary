package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * A 'Pick' is the data that is stored with a Pick ID for a Member.
*/
public class Pick {

	private String myPickID = "";

	// Number of times Skipped.
	private int skipped = 0;

	// Number of times Used.
	private int used = 0;

	// My additional JSON.
	private String myJSON = "";

	// Ignore flag for use during typical Pick queries.
	private boolean isStopUsing = false;

	// Percentage 0-100, where 0 is most Skipped and 100 is most Used.
	private int autoRatePercentage = 50;

	// Faster trending percentage 0-100.
	private int trendingRatePercentage = 50;

	// Flag to indicate no Pick information has been stored yet.
	private boolean isNewPick = true;

	// List of a Member's Categories that are marked for this Pick.
	// Will be empty if includeCategories is not set in PickQuery.
	private List<String> categoryList = new ArrayList<>();

	public Pick() {
	}

	public String getMyPickID() {
		return myPickID;
	}

	public void setMyPickID(String value) {
		this.myPickID = value;
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
		return isStopUsing;
	}

	public void setStopUsing(boolean isStopUsing) {
		this.isStopUsing = isStopUsing;
	}

	public int getAutoRatePercentage() {
		return autoRatePercentage;
	}

	public void setAutoRatePercentage(int autoRatePercentage) {
		this.autoRatePercentage = autoRatePercentage;
	}

	public String getMyJSON() {
		return myJSON;
	}

	public void setMyJSON(String myJSON) {
		this.myJSON = myJSON;
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
		this.categoryList = categoryList;
	}

	public void setNewPick(boolean isNewPick) {
		this.isNewPick = isNewPick;
	}

	public boolean isNewPick() {
		return isNewPick;
	}

}
