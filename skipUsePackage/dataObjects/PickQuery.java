package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.enums.RampMode;
import com.autogilmore.throwback.skipUsePackage.enums.SearchMode;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;

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
	private List<Long> memberIDList = new ArrayList<Long>();

	// Use this member's ID to reference their Collection of Pick IDs...
	// NOTE: 0 defaults to the owner's collection.
	private long memberCollectionID = 0;

	// Return back this many Pick IDs...
	// (Not required but, it's a good idea to set this.)
	private int howMany = SkipUseManager.MAX_PICK_ID_LIST_SIZE;

	// Try to send back this percentage (0-100) of new Picks. (isNewPick = true)
	// (A good idea to set this as it mixes-in new Picks to choose from)
	// Set to 0 for no 'new' Picks.
	// Set to 100 for all 'new'Picks.
	// See the isGetMorePicksIfShort setting below.
	private int newMixInPercentage = 0;

	// Option to return Picks even if your other query options can't find the
	// Picks you want.
	// (Not required. But keep this setting in mind if your is not returning
	// what you expect.)
	// Set to 'false' if you want exact results when using categories
	// or newMixInPercentage of 0 or 100 settings.
	// Set to 'true' means you always get back the howMany result setting if
	// available.
	private boolean getMorePicksIfShort = false;

	// **** Optional *********

	// Optional to search for Picks by...
	// NORMAL: Standard mix of Picks with more Used count and less Skipped.
	// FAVORITES: Descending by highest auto-rating percentage.
	// STOPUSING: Picks that have been flagged,
	// (This might ignore the newMixInPercentage setting.)
	// TEMPORAL: get Picks by the time-of-day they are Skipped or Used.
	// This could be useful if Picks are Skipped or Used based on the
	// time-of-day. An example would be if user's want slow songs at night but
	// faster songs during the day. (Don't use this if time-of-use is not a
	// factor. An example could be determining which batteries are holding a
	// charge after use. The time the battery was used has no factor on it's
	// capacity.)
	private SearchMode searchMode = SearchMode.NORMAL;

	// Optional to order returned Picks by:
	// NONE: no ordering.
	// RATE_DOWN: auto-rating percentage from High to Low.
	// RATE_UP: auto-rating percentage for Low to High.
	// OLDEST: oldest last timestamp updated Picks first
	// NEWEST: newest last timestamp updated Picks first
	private RampMode ramp = RampMode.NONE;

	// Optional to not return recently updated Picks in the next query.
	// (Set to 'false' if you want to see popular Picks
	// every query even if the Pick was just presented.)
	private boolean excludeRecentPicks = false;

	// Optional to return Picks that have been flagged to not be included in
	// normal queries.
	private boolean includeStopUsing = false;

	// Optional to get the Categories that have been marked for return Picks.
	// (Slower response time and might cost more because of more
	// server processing.)
	private boolean includeCategories = false;

	// Optional to return Picks that have been marked with these Categories...
	// (This might also costs more to use because of server
	// processing.)
	private List<String> categoryList = new ArrayList<String>();

	// Optional to only return Pick IDs that are in this list: IMPORTANT: Pick
	// IDs in this list must already be in the collection or they will be
	// ignored.
	private List<String> pickIDList = new ArrayList<String>();

	// **** Only ONE Pick *********

	// Set this Pick ID string ONLY if you are searching for ONE Pick with this
	// Pick ID.
	// NOTE: This will return the Pick for ONE MEMBER only.
	// NOTE: Setting this will change/ignore the the other query parameters to
	// the following:
	// this.addThisManyNewValues = 0;
	// this.excludeStopUsing = false;
	// this.ramp = RampMode.NONE;
	// this.searchMode = SearchMode.NORMAL;
	// this.categoryMode = CategoryMode.ANY;
	// this.getMorePicksIfShort = false;
	// If a Pick is not stored yet, an empty Pick List will be returned with a
	// the Pick's member ID set to 0.
	private String pickID = "";

	private boolean debugQuery = false;

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

	public List<Long> getMemberIDList() {
		return memberIDList;
	}

	public void setMemberIDList(List<Long> memberIDList) {
		memberIDList.removeAll(Collections.singleton(null));
		this.memberIDList = memberIDList;
	}

	public boolean addToMemberIDList(long memberID) {
		if (!memberIDList.contains(memberID)) {
			return this.memberIDList.add(memberID);
		}
		return false;
	}

	public String getPickID() {
		return this.pickID;
	}

	public void setPickID(String pickID) {
		if (pickID != null && !pickID.isEmpty()) {
			this.pickID = pickID;
			// NOTE: these setting are not needed, but show what the query will
			// look like for the server.
			// setHowMany(1);
			// setExcludeRecentPicks(false);
			// setGetMorePicksIfShort(false);
			// setSearchMode(SearchMode.NORMAL);
			// makeExactQuery();
			// NOTE: you can still choose to add Category info if you want
			setIncludeCategories(true);
		}
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

	public boolean isExcludeRecentPicks() {
		return excludeRecentPicks;
	}

	public void setExcludeRecentPicks(boolean excludeRecentPicks) {
		this.excludeRecentPicks = excludeRecentPicks;
	}

	public boolean isIncludeStopUsing() {
		return this.includeStopUsing;
	}

	public void setIncludeStopUsing(boolean isStopUsing) {
		this.includeStopUsing = isStopUsing;
	}

	public boolean isGetMorePicksIfShort() {
		return this.getMorePicksIfShort;
	}

	public void setGetMorePicksIfShort(boolean isFillEmptySearchResults) {
		this.getMorePicksIfShort = isFillEmptySearchResults;
	}

	public boolean isIncludeCategories() {
		return includeCategories;
	}

	public void setIncludeCategories(boolean includeCategories) {
		this.includeCategories = includeCategories;
	}

	public long getMemberCollectionID() {
		return this.memberCollectionID;
	}

	public void setMemberCollectionID(long fromMemberIDCollection) {
		this.memberCollectionID = fromMemberIDCollection;
	}

	public List<String> getPickIDList() {
		return pickIDList;
	}

	public void addToPickIDList(String pickID) {
		if (pickID != null && !this.pickIDList.contains(pickID))
			this.pickIDList.add(pickID);
	}

	public void setPickIDList(List<String> pickIDList) {
		this.pickIDList = pickIDList;
	}

	public boolean isDebugQuery() {
		return debugQuery;
	}

	public void setDebugQuery(boolean debugQuery) {
		this.debugQuery = debugQuery;
	}

	public void makeExactQuery() {
		setGetMorePicksIfShort(false);
		// Ignore 100 settings
		if (getNewMixInPercentage() != 100)
			setNewMixInPercentage(0);
	}

	@Override
	public String toString() {
		return "getMemberIDList.size=" + getMemberIDList().toString() + ", getMemberCollectionID="
				+ getMemberCollectionID() + ", getHowMany=" + getHowMany()
				+ ", getNewMixInPercentage=" + getNewMixInPercentage() + ", isGetMorePicksIfShort="
				+ (isGetMorePicksIfShort() ? "1" : "0") + ", getSearchMode=" + getSearchMode()
				+ ", getRamp=" + getRamp() + ", isExcludeRecentPicks="
				+ (isExcludeRecentPicks() ? "1" : "0") + ", isIncludeStopUsing="
				+ (isIncludeStopUsing() ? "1" : "0") + ", isIncludeCategories="
				+ (isIncludeCategories() ? "1" : "0") + ", getCategories.size="
				+ getCategories().toString() + ", getPickID=" + getPickID() + ", getPickIDList="
				+ getPickIDList().toString();
	}

}
