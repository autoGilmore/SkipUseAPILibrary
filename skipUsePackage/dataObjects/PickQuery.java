package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.enums.AdvancedOption;
import com.autogilmore.throwback.skipUsePackage.enums.ResultOption;
import com.autogilmore.throwback.skipUsePackage.enums.SearchOption;
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

    // Optional: Do not return recently updated Picks in the next query.
    // Set the number of hours to ignore a Pick since it was last updated.
    private int excludeRecentPicksHours = 0;

    // Optional Pick Query Key-words: There are key-words you can provide to alter
    // how Picks are Searched for and how the results are returned. There are 3
    // optional lists to provide this information: SearchOptionList,
    // ResultOptionList and AdvancedOptionList.

    // The SearchOptionList is where you provide how you want to search for Picks:
    private List<SearchOption> searchOptionList = new ArrayList<SearchOption>();
    // In the searchOptionList you can provide various options for how Picks
    // are searched for and how the results are returned. Choose the options to add
    // to the list below.
    // The available search options are located in the SearchOption.java enum.

    // You can provide 2 ways to search for Pick:
    // SearchOption.NORMAL: The default. A mix with more of the previously Used and
    // less Skipped Picks.
    // SearchOption.RANDOM: Returns Picks randomly by their weighted auto-percentage rating.
    // SearchOption.BALANCED: Returns Picks that have been Used too few times then
    // what is expected based the auto-rating values.
    // SearchOption.RACING: Think of the Tortoise and the Hare story where Picks
    // race to be chosen. The Picks in the lead are chosen for the result. Their
    // speed is determined by the auto-percentage and their track placement by when
    // they were last updated.
    // SearchOption.FAVORITE: Descending Picks by highest auto-rating percentage.
    // SearchOption.WORST: Ascending Picks by lowest auto-rating percentage.
    // SearchOption.STOP_USING_ONLY: Picks that have been flagged stopUsing.

    // There are additional modifier key-words you can provide as well.
    // SearchOption.USE_TIME_OF_DAY: Not all searching modes use this option. This
    // option works well for finding Favorite Picks at the current time of day,
    // where the Picks might not be favorites at a different time of day.
    // NOTE: NORMAL and BALANCED search options do not use this option.
    // SearchOption.GET_MORE_IF_SHORT: Return Picks even if your other query options
    // can't find the Picks you want. (keep this option in mind if is not returning
    // what you expect.
    // SearchOption.INCLUDE_STOP_USING: If there are Picks marked with the
    // STOP_USING flag they normally will not be returned. Use this option if you
    // would like to include these Picks in the results.
    // SearchOption.ENHANCE: This may provide better Pick results based on what is
    // actively being Skipped and Used; attempting to match Picks with the live
    // trends.

    // The ResultOptionList is where you provide key-words to alter the results from
    // from the SearchOptions.
    private List<ResultOption> resultOptionList = new ArrayList<ResultOption>();
    // The available options are located in the ResultOption.java enum.

    // Result ordering options:
    // ResultOption.RAMP_NONE: no ordering.
    // ResultOption.RAMP_RATE_DOWN: auto-rating percentage from High to Low.
    // ResultOption.RAMP_RATE_UP: auto-rating percentage for Low to High.
    // ResultOption.RAMP_OLDEST: oldest last time stamp updated Picks first
    // ResultOption.RAMP_NEWEST: newest last time stamp updated Picks first

    // Combining results option when using multiple Search options:
    // ResultOption.BLEND: The default, which combines the agreed upon best results
    // together.
    // ResultOption.MERGE: Treats results equally when combining together.

    // ResultOption.INCLUDE_CATEGORY_INFO: Returns any category information
    // associated with the Picks returned. (Slower response time and might cost more
    // because of additional server processing.)

    // The AdvancedOptionList is where you provide key-words for other options.
    private List<AdvancedOption> advancedOptionList = new ArrayList<AdvancedOption>();
    // The available options are located in the AdvancedOption.java enum.

    // AdvancedOption.DEBUG_QUERY: See what is happening with your Pick Query.
    // Returns a detailed message about what settings were used to return the Pick
    // List.
    // AdvancedOption.RESET: re-orders Picks to work with SearchOption.NORMAL search
    // option if it is no longer returning expected results. See API documentation
    // before using this option.

    // You can also limit the search results by providing which Picks marked with
    // Categories you want to be returned.
    private List<String> categoryList = new ArrayList<String>();
    // Optional: Return only Picks that have been marked with these Categories.
    // NOTE: Remember to set the 'INCLUDE_CATEGORY_INFO' in the ResultOptionList if
    // you want to see category information as well. You can also include the
    // Category Options below. The available options are located in the
    // CategoryOption.java enum.

    // Category options:
    // CategoryOption.ANY: this is the default. Picks will be returned with or
    // without any categories.
    // CategoryOption.NONE: only Picks with no categories will be returned.
    // CategoryOption.NOT: only Picks that do not that this category will be returned.
    // (Using categories might cost more to use because of the additional server
    // processing.)

    // You can also limit the result to only return Pick IDs that are in this
    // pickIDList:
    // IMPORTANT: Pick IDs in this list must already be in the collection or they
    // will be ignored.
    private List<String> pickIDList = new ArrayList<String>();

    // **** Return Only ONE Pick *********
    // Optional: Set this Pick ID string ONLY if you are searching for ONE Pick with
    // this Pick ID. If a Pick is not stored yet, an empty Pick List will be
    // returned with a the Pick's member ID set to 0.
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

    public List<Long> getMemberIDList() {
	return memberIDList;
    }

    public void setMemberIDList(List<Long> memberIDList) {
	this.memberIDList.clear();
	if (memberIDList != null) {
	    memberIDList.removeAll(Collections.singleton(null));
	    this.memberIDList = memberIDList;
	}
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
	}
    }

    public int getNewMixInPercentage() {
	return newMixInPercentage;
    }

    public void setNewMixInPercentage(int newMixInPercentage) {
	if (newMixInPercentage >= 0 && newMixInPercentage <= 100)
	    this.newMixInPercentage = newMixInPercentage;
    }

    public List<SearchOption> getSearchOptionList() {
	return searchOptionList;
    }

    public void setOneSearchOptionList(SearchOption searchOption) {
	if (searchOption != null) {
	    this.searchOptionList.clear();
	    addToSearchOptionList(searchOption);
	}
    }

    public void setSearchOptionList(List<SearchOption> searchOptionList) {
	if (searchOptionList != null) {
	    for (SearchOption searchOption : searchOptionList)
		addToSearchOptionList(searchOption);
	}
    }

    public void addToSearchOptionList(SearchOption searchOption) {
	if (searchOption != null && !getSearchOptionList().contains(searchOption))
	    this.searchOptionList.add(searchOption);
    }

    public List<String> getCategoryList() {
	return categoryList;
    }

    public void setCategoryList(List<String> categoryList) {
	this.categoryList = categoryList;
    }

    public void addToCategoryList(String category) {
	if (category != null && !getCategoryList().contains(category))
	    this.categoryList.add(category);
    }

    public List<ResultOption> getResultOptionList() {
	return resultOptionList;
    }

    public void setResultOptionList(List<ResultOption> resultOptionList) {
	if (resultOptionList != null) {
	    for (ResultOption resultOption : resultOptionList)
		addToResultOptionList(resultOption);
	}
    }

    public void addToResultOptionList(ResultOption resultOption) {
	if (resultOption != null && !getResultOptionList().contains(resultOption))
	    this.resultOptionList.add(resultOption);
    }

    public List<AdvancedOption> getAdvancedOptionList() {
	return advancedOptionList;
    }

    public void setAdvancedOptionList(List<AdvancedOption> advancedOptionList) {
	this.advancedOptionList = advancedOptionList;
    }

    public void addToAdvancedOptionList(AdvancedOption advancedOption) {
	if (advancedOption != null && !getAdvancedOptionList().contains(advancedOption)) {
	    this.advancedOptionList.add(advancedOption);
	}
    }

    public int getExcludeRecentPicksHours() {
	return excludeRecentPicksHours;
    }

    public void setExcludeRecentPicksHours(int excludeRecentPicksHours) {
	this.excludeRecentPicksHours = excludeRecentPicksHours;
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

    public void makeExactQuery() {
	getSearchOptionList().remove(SearchOption.GET_MORE_IF_SHORT);
	// Ignore 100 settings
	if (getNewMixInPercentage() != 100)
	    setNewMixInPercentage(0);
    }

    @Override
    public String toString() {
	return "getMemberIDList.size=" + getMemberIDList().toString() + ", getMemberCollectionID="
		+ getMemberCollectionID() + ", getHowMany=" + getHowMany() + ", getNewMixInPercentage="
		+ getNewMixInPercentage() + ", isExcludeRecentPicksHours=" + getExcludeRecentPicksHours()
		+ ", getSearchOptionList=" + getSearchOptionList().toString() + ", getResultOptionList="
		+ getResultOptionList().toString() + ", getAdvancedOptionList=" + getAdvancedOptionList().toString()
		+ ", getCategoriesList=" + getCategoryList().toString() + ", getPickID=" + getPickID()
		+ ", getPickIDList=" + getPickIDList().toString();
    }
}
