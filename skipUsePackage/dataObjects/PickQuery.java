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

    // Required: Use this member's ID to reference their Collection of Pick IDs.
    // NOTE: 0 defaults to the owner's collection.
    private long memberCollectionID = 0;

    // Required: For these Member IDs.
    // (No need to set if you don't have any members and
    // you are just searching for yourself)
    private List<Long> memberIDList = new ArrayList<Long>();

    // Optional: Return back this many Pick IDs.
    // (Not required but, it's a good idea to set this.)
    private int howMany = SkipUseManager.MAX_PICK_ID_LIST_SIZE;

    // Optional: Try to send back this percentage (0-100) of new Picks. (isNewPick =
    // true)
    // (A good idea to set this as it mixes-in new Picks to choose from)
    // Set to 0 for no 'new' Picks, only existing Picks.
    // Set to 100 for all 'new'Picks, no existing Picks.
    // NOTE: See the isGetMorePicksIfShort setting below.
    private int newMixInPercentage = 0;

    // Optional: to return Picks even if your other query options can't find the
    // Picks you want.
    // (Not required. But keep this setting in mind if your is not returning
    // what you expect.)
    // Set to 'false' if you want exact results when using categories
    // or newMixInPercentage of 0 or 100 settings.
    // Set to 'true' means you always get back the howMany result setting if
    // available.
    private boolean getMorePicksIfShort = false;

    // **** Additional options *********

    // Available Search Modes for Picks include:
    // NORMAL: Standard mix of Picks with more Used count and less Skipped.
    // BALANCED: Picks by presented by count used vs the auto-percentage expected to
    // be used. Picks are returned by how much they are not in balance.
    // RACING: Think of the Tortoise and the Hare story where Picks race to be
    // chosen. The Picks in the lead are chosen.Their speed is determined by the
    // auto-percentage and their track placement by when they were last updated.

    // Other search modes to help find specific Picks:
    // FAVORITE: Descending Picks by highest auto-rating percentage.
    // WORST: Ascending Picks by lowest auto-rating percentage.
    // STOPUSING: Picks that have been flagged stopUsing

    // Additional modifiers:
    // RESET: re-orders Picks when using the NORMAL search mode. (use ONCE to reset
    // if another search mode has changed the the NORMAL ordering)
    private List<String> searchModeList = new ArrayList<String>();

    // Optional: set to have Search Modes search by the Time Of Day.
    // Not all search modes use this option. This option works well for finding
    // Favorite Picks at the current time of day, where the Picks might not be
    // favorites at a different time of day.
    // NOTE: NORMAL and BALANCED search modes do not use this option.
    private boolean useTimeOfDay = false;

    // Optional to order returned Picks by:
    // NONE: no ordering.
    // RATE_DOWN: auto-rating percentage from High to Low.
    // RATE_UP: auto-rating percentage for Low to High.
    // OLDEST: oldest last time stamp updated Picks first
    // NEWEST: newest last time stamp updated Picks first
    private String ramp = RampMode.NONE.toString();

    // Optional to not return recently updated Picks in the next query.
    // Set the number of hours to ignore a Pick since it was last updated.
    private int excludeRecentPicksHours = 0;

    // Optional to return Picks that have been flagged to not be included in
    // normal queries.
    private boolean includeStopUsing = false;

    // Optional to get the Categories that have been marked for return Picks.
    // (Slower response time and might cost more because of more
    // server processing.)
    private boolean includeCategories = false;

    // Optional to return only Picks that have been marked with these Categories.
    // NOTE: Remember to set 'includeCategories' to 'true' if you want to see
    // category information.
    // >> You can also include the 'category option' words below, by placing them a
    // in the list.
    // Category Modes:
    // ANY: this is the default. Picks will be returned with or without any
    // categories.
    // NONE: only Picks with no categories will be returned.
    // NOT: only Picks that do not that this category will be returned.
    // (Using categories might costs more to use because of the additional server
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

    // See what is happening with your Pick Query. Returns a detailed message about
    // what settings were used to return the Picks List.
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

    public List<String> getSearchModeList() {
	return searchModeList;
    }

    public void addToSearchModeList(String searchMode) {
	if (searchMode != null && !searchMode.isEmpty()) {
	    String value = searchMode.trim().toUpperCase();
	    if (!value.isEmpty() && !this.searchModeList.contains(value))
		this.searchModeList.add(value);
	}
    }

    public void setOneSearchModeList(SearchMode searchMode) {
	if (searchMode != null) {
	    this.searchModeList.clear();
	    addToSearchModeList(searchMode.toString());
	}
    }

    public void setSearchModeList(List<SearchMode> searchModeList) {
	if (searchModeList != null) {
	    for (SearchMode sm : searchModeList)
		addToSearchModeList(sm.toString());
	}
    }

    public List<String> getCategoryList() {
	return categoryList;
    }

    public void setCategoryList(List<String> categoryList) {
	this.categoryList = categoryList;
    }

    public void addToCategoryList(String category) {
	if (category != null && !this.categoryList.contains(category))
	    this.categoryList.add(category);
    }

    public String getRamp() {
	return ramp;
    }

    public void setRamp(RampMode ramp) {
	this.ramp = ramp.toString();
    }

    public int getExcludeRecentPicksHours() {
	return excludeRecentPicksHours;
    }

    public void setExcludeRecentPicksHours(int excludeRecentPicksHours) {
	this.excludeRecentPicksHours = excludeRecentPicksHours;
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

    public boolean isUseTimeOfDay() {
	return useTimeOfDay;
    }

    public void setUseTimeOfDay(boolean useTimeOfDay) {
	this.useTimeOfDay = useTimeOfDay;
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
		+ getMemberCollectionID() + ", getHowMany=" + getHowMany() + ", getNewMixInPercentage="
		+ getNewMixInPercentage() + ", isGetMorePicksIfShort=" + (isGetMorePicksIfShort() ? "1" : "0")
		+ ", getSearchModeList=" + getSearchModeList() + ", getRamp=" + getRamp()
		+ ", isExcludeRecentPicksHours=" + getExcludeRecentPicksHours() + ", isIncludeStopUsing="
		+ (isIncludeStopUsing() ? "1" : "0") + ", isIncludeCategories=" + (isIncludeCategories() ? "1" : "0")
		+ ", getCategories.size=" + getCategoryList().toString() + ", getPickID=" + getPickID()
		+ ", getPickIDList=" + getPickIDList().toString();
    }

}
