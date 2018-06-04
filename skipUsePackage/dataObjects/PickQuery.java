package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.enums.RampMode;
import com.autogilmore.throwback.skipUsePackage.enums.SearchMode;

/* 
 * A Pick Query is used to customize the return of desired Pick IDs.
  * A query should be set in advance before calling the API to get a list of Picks by using the 
  * SkipUse Manger createPickQuery method.
*/
public class PickQuery {

	// **** Check out the SkipUse API documentation for detailed usage. ****

	// For these Member IDs...
	// (No need to set if you don't have any members and
	// you are just searching for yourself)
	private List<Integer> memberIDList = new ArrayList<Integer>();

	// Use this Collection of Pick IDs...
	// (Not required yet because one Collection per account is currently
	// allowed)
	private int collectionID = -1;

	// Return back this many Pick IDs...
	// (Not required but, it's a good idea to set this.)
	private int howMany = 6;

	// Try to send back this percentage (0-100) of new Picks. (isNewPick = true)
	// (A good idea to set this as it mixes-in new Picks to choose from)
	// Set to 0 for no 'new' Picks.
	// Set to 100 for all 'new'Picks.
	// See the isGetMorePicksIfShort setting below.
	private int newMixInPercentage = 30;

	// Option to return Picks even if your other query options can't find the
	// Picks you want.
	// (Not required. But keep this setting in mind if your is not returning
	// what you expect.)
	// Set to 'false' if you want exact results when using categories
	// or newMixInPercentage of 0 or 100 settings.
	// Set to 'true' means you always get back the howMany result setting if
	// available.
	private boolean isGetMorePicksIfShort = true;

	// **** Optional *********

	// Search for Picks by...
	// NORMAL: Standard mix of Picks with more Used count and less Skipped.
	// FAVORITES: Descending by highest auto-rating percentage.
	// STOPUSING: Picks that have been flagged,
	// (Not required. This might ignore the newMixInPercentage setting.)
	private SearchMode searchMode = SearchMode.NORMAL;

	// Returned Picks ordered by autoRatePercentage value:
	// NONE: no ordering.
	// DOWN: auto-rating percentage from High to Low.
	// UP: auto-rating percentage for Low to High.
	// (Not required.)
	private RampMode ramp = RampMode.NONE;

	// Option to not return recently updated Picks in the next query.
	// (Not required, but set to 'false' if you want to see popular Picks
	// every query even if the Pick was just presented.)
	private boolean excludeRecentPicks = true;

	// Option to return Picks that have been flagged to not be included in
	// normal queries.
	// (Not required.)
	private boolean isIncludeStopUsing = false;

	// Option to get the Categories that have been marked for return Picks.
	// (Not required. Slower response time and might cost more because of more
	// server
	// processing.)
	private boolean includeCategories = false;

	// Option to return Picks that have been marked with these Categories...
	// (Not required. This might also costs more to use because of server
	// processing.)
	private List<String> categoryList = new ArrayList<>();

	// **** Only ONE Pick *********

	// Set this Pick ID string ONLY if you are searching for ONE Pick with this
	// Pick ID.
	// Setting this to any thing but empty will change the following parameters.
	// This will return the Pick for ONE MEMBER only.
	// If a Pick is not stored yet, an empty Pick List will be returned.
	private String pickID = "";

	public PickQuery() {
	}

	public PickQuery(int howMany) {
		this.howMany = howMany;
	}

	public int getHowMany() {
		return howMany;
	}

	public void setHowMany(int howMany) {
		this.howMany = howMany;
	}

	public List<Integer> getMemberIDList() {
		return memberIDList;
	}

	public void setMemberIDList(List<Integer> memberIDList) {
		memberIDList.removeAll(Collections.singleton(null));
		this.memberIDList = memberIDList;
	}

	public boolean addToMemberIDList(int memberID) {
		if (!memberIDList.contains(memberID)) {
			return this.memberIDList.add(memberID);
		}
		return false;
	}

	public String getPickID() {
		return this.pickID;
	}

	public int getNewMixInPercentage() {
		return newMixInPercentage;
	}

	public void setNewMixInPercentage(int newMixInPercentage) {
		if (newMixInPercentage >= 0 && newMixInPercentage <= 100)
			this.newMixInPercentage = newMixInPercentage;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		String value = searchMode.trim().toUpperCase();
		if (value != null && !value.isEmpty())
			this.searchMode = SearchMode.valueOf(value);
	}

	public void setSearchMode(SearchMode mode) {
		this.searchMode = mode;
	}

	public List<String> getCategories() {
		return categoryList;
	}

	public void setCategories(List<String> categories) {
		this.categoryList = categories;
	}

	public void addCategory(String category) {
		if (category != null && !this.categoryList.contains(category))
			this.categoryList.add(category);
	}

	public RampMode getRamp() {
		return ramp;
	}

	public void setRamp(RampMode ramp) {
		this.ramp = ramp;
	}

	public Boolean getExcludeRecentPicks() {
		return excludeRecentPicks;
	}

	public void setExcludeRecentPicks(Boolean excludeRecentPicks) {
		this.excludeRecentPicks = excludeRecentPicks;
	}

	public void setIncludeStopUsing(boolean isStopUsing) {
		this.isIncludeStopUsing = isStopUsing;
	}

	public boolean getIsIncludeStopUsing() {
		return this.isIncludeStopUsing;
	}

	public boolean getIsGetMorePicksIfShort() {
		return this.isGetMorePicksIfShort;
	}

	public void setGetMorePicksIfShort(boolean isFillEmptySearchResults) {
		this.isGetMorePicksIfShort = isFillEmptySearchResults;
	}

	public boolean isIncludeCategories() {
		return includeCategories;
	}

	public void setIncludeCategories(boolean includeCategories) {
		this.includeCategories = includeCategories;
	}

	public int getCollectionID() {
		return this.collectionID;
	}

	public void setCollectionID(int collectionID) {
		this.collectionID = collectionID;
	}

	public void setPickID(String pickID) {
		if (pickID != null && !pickID.isEmpty()) {
			this.pickID = pickID;
			// NOTE: these setting are not needed, but show what the query will
			// look like for the server.
			setHowMany(1);
			setExcludeRecentPicks(false);
			setGetMorePicksIfShort(false);
			setSearchMode(SearchMode.NORMAL);
			makeExactQuery();
			// NOTE: you can still choose to add Category info if you want
			setIncludeCategories(true);
		}
	}

	public PickQuery clone(PickQuery original) {
		PickQuery copy = new PickQuery();
		copy.setHowMany(original.getHowMany());
		copy.setSearchMode(original.getSearchMode());
		copy.setNewMixInPercentage(original.getNewMixInPercentage());
		copy.setCategories(new ArrayList<>(original.getCategories()));
		copy.setRamp(original.getRamp());
		copy.setExcludeRecentPicks(original.getExcludeRecentPicks());
		copy.setIncludeStopUsing(original.getIsIncludeStopUsing());
		copy.setMemberIDList(new ArrayList<>(original.getMemberIDList()));
		copy.setGetMorePicksIfShort(original.getIsGetMorePicksIfShort());
		copy.setCollectionID(original.getCollectionID());
		return copy;
	}

	public void makeExactQuery() {
		setGetMorePicksIfShort(false);
		// Ignore 100 settings
		if (getNewMixInPercentage() != 100)
			setNewMixInPercentage(0);
	}

}
