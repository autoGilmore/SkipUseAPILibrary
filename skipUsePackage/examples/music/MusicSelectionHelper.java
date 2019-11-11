package com.autogilmore.throwback.skipUsePackage.examples.music;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.autogilmore.throwback.data.PlayerStatus;
import com.autogilmore.throwback.data.songcategory.Category;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.enums.RampMode;
import com.autogilmore.throwback.skipUsePackage.enums.SearchMode;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;
import com.autogilmore.throwback.util.MySystemOut;

public class MusicSelectionHelper {
    // NOTE: This is my system out and logger. You can substitute this usage
    // with just System.out.println for your code.
    private MySystemOut syso = MySystemOut.getInstance();

    // Using the Manager to handle SkipUse requests.
    private final SkipUseManager skipUseManager = SkipUseManager.getInstance();

    // Flags to verify service states.
    private boolean isSkipUseRunning = false;
    private boolean isSkipUseLoggedIn = false;
    private boolean isSkipUseError = false;
    private int skipUseErrorCount = 0;
    private static final int MAX_SKIPUSE_ERRORS = 20;

    // Listening members and song collections.
    private String lastPickQuery = "";
    private List<String> lastCategoryList = new ArrayList<String>();
    private MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
    private Set<Long> getListeningMemberIDSet = new HashSet<>();
    private MemberCategoryList memberCategoryList = new MemberCategoryList();

    // Play-back and selection variables.
    private List<SearchMode> playerSearchModeList = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private long selectedCategoryMemberID = 0;

    // Returned Picks from SkipUse
    private boolean isResendPickQuery = true;
    private PickQuery pickQuery = new PickQuery();
    private List<Pick> returnedPickList = new ArrayList<>();
    private Queue<String> pickIDQueue = new LinkedList<String>();

    // Replay previous song ID
    private String _lastSongID = null;

    public MusicSelectionHelper() {
	initialize();
    }

    public void initialize() {
	isSkipUseRunning = false;
	isSkipUseError = false;
	skipUseErrorCount = 0;
	if (skipUseManager.isAPIServerUp()) {
	    isSkipUseRunning();
	    skipUseLogin();
	}
    }

    // Get the stored collection of song IDs from SkipUse.
    //
    public List<String> getSongIDCollection() {
	if (memberPickIDCollection.getPickIDList().isEmpty()) {
	    if (isSkipUseRunning()) {
		try {
		    memberPickIDCollection = skipUseManager.getPickIDCollection();
		} catch (SkipUseException e) {
		    syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		    isSkipUseError = true;
		}
	    }
	}
	return memberPickIDCollection.getPickIDList();
    }

    // Add a collection of song IDs to SkipUse.
    //
    public void setSongIDCollection(List<String> songIDList, boolean isCommaSpaceDelimted) {
	memberPickIDCollection = new MemberPickIDCollection();
	if (isSkipUseRunning()) {
	    try {
		syso.logInfo(
			"MusicSelectionHelper " + syso.getLineNumber() + " Try setSongIDCollection  songIDList.size: "
				+ songIDList.size() + " isCommaSpaceDelimted: " + isCommaSpaceDelimted);
		memberPickIDCollection = skipUseManager.addPickIDCollection(songIDList, isCommaSpaceDelimted);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Get the next song ID to play.
    // Returns null if the service is not running or an error occurred.
    //
    public String _getNextSongID() {
	String _songID = null;

	// check the pickQuery to see if it changed
	loadPickQuery();

	// did the Pick Query change? is the queue empty? Then load new songs
	if (isResendPickQuery || !pickQuery.toString().equals(lastPickQuery) || pickIDQueue.isEmpty()) {
	    loadPickQueue();
	}

	// load a song ID from the Pick queue
	_songID = pickIDQueue.poll();
	_lastSongID = _songID;

	if (_songID == null)
	    syso.logInfo("Failed to get a song ID. NOTE: You could add code to get a random song ID to return.");

	return _songID;
    }

    // Get Pick Queue
    public Queue<String> getPickIDQueue() {
	return pickIDQueue;
    }

    // Load Pick Queue
    //
    public void loadPickQueue() {
	if (isSkipUseRunning()) {
	    loadPickListByPickQuery();
	    for (Pick pick : returnedPickList) {
		if (!pickIDQueue.contains(pick.getPickID())) {
		    // if there is a lastSongID ignore it in returned results
		    if (_lastSongID != null) {
			if (!_lastSongID.equalsIgnoreCase(pick.getPickID())) {
			    pickIDQueue.add(pick.getPickID());
			    syso.logInfo("PickIDQueue loading: " + pick.getPickID());
			}
		    } else {
			pickIDQueue.add(pick.getPickID());
			syso.logInfo("PickIDQueue loading: " + pick.getPickID());
		    }
		}

		// if we get back a owner's Pick, something went wrong
		try {
		    if (pick.getMemberID() == skipUseManager.getOwnerMemberID()) {
			syso.logError("MusicSelectionHelper " + syso.getLineNumber(),
				"Did not get back member Picks. Check member IDs in PickQuery.");
			pickIDQueue.clear();
			isSkipUseError = true;
			break;
		    }
		} catch (SkipUseException e) {
		    syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		    isSkipUseError = true;
		}

		// if we get back too many Picks, just break out
		if (pickIDQueue.size() >= pickQuery.getHowMany())
		    break;
	    }
	}
    }

    public boolean isLoadPickQueueNeeded() {
	return pickIDQueue.size() <= 1;
    }

    // Get the current Pick List.
    //
    public List<Pick> getCurrentPickList() {
	return returnedPickList;
    }

    // Get the ONE member ID for listening by category.
    //
    public long getListeningCategoryMemberID() {
	return selectedCategoryMemberID;
    }

    // Set the ONE member ID to use when listening by category.
    //
    public void setListeningCategoryMemberID(long memberID) {
	this.selectedCategoryMemberID = memberID;
    }

    // Get the selected categories.
    //
    public List<String> getCategoryList() {
	return categoryList;
    }

    // Set the selected category for use with the PlayMode:CATEGORY
    // to play back for ONE member.
    //
    public void setCategoryList(List<String> categoryList) {
	if (categoryList != null)
	    this.categoryList = categoryList;
    }

    // Create a category for a member.
    // NOTE: no commas are allowed in the category name.
    //
    public void createCategoryForMember(long memberID, String category) {
	if (isSkipUseRunning()) {
	    try {
		skipUseManager.createCategoryForMember(memberID, category);
		memberCategoryList = new MemberCategoryList();
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Get the categories for a member.
    //
    public List<String> getCategoryListForMember(long memberID) {
	List<String> categoryList = new ArrayList<String>();
	if (isSkipUseRunning()) {
	    try {
		MemberCategoryList memberCategoryList = skipUseManager.getCategoryListForMember(memberID);
		categoryList = memberCategoryList.getCategoryList();
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
	return categoryList;
    }

    // Mark or un-mark a song Pick with a category.
    //
    public void markPickWithCategoryTrueFalse(Pick pick, String category, boolean isMark) {
	if (isSkipUseRunning()) {
	    try {
		// create category if needed
		if (isMark && !getCategoryListForMember(pick.getMemberID()).contains(category))
		    createCategoryForMember(pick.getMemberID(), category);

		skipUseManager.markPickWithCategoryTrueFalse(pick, category, isMark);
		removePickFromCurrentPickList(pick);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Mark a song ID with a category for a member.
    //
    public void markSongIDWithCategoryTrueFalse(long memberID, String songID, String category, boolean isMark) {
	if (isSkipUseRunning()) {
	    try {
		// create category if needed
		if (isMark && !getCategoryListForMember(memberID).contains(category))
		    createCategoryForMember(memberID, category);

		skipUseManager.markPickIDWithCategoryTrueFalse(memberID, songID, category, true);
		removePickFromCurrentPickList(memberID, songID);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Mark or un-mark a song ID using a member's category.
    //
    public void toggleSongCategory(String songID, long memberID, Category toggleCategory) {
	if (isSkipUseRunning()) {
	    String categoryName = toggleCategory.toString();

	    List<String> categoryList = getCategoryListForMember(memberID);

	    // create Category if needed.
	    if (!categoryList.contains(categoryName))
		createCategoryForMember(memberID, categoryName);

	    Pick _pick = _getPickByMemberIDAndSongID(memberID, songID);
	    if (_pick != null) {
		if (_pick.getCategoryList().contains(categoryName)) {
		    // un-mark category.
		    syso.logInfo("unmark category: " + categoryName);
		    markPickWithCategoryTrueFalse(_pick, categoryName, false);
		} else {
		    // mark it.
		    syso.logInfo("mark category: " + categoryName);
		    markPickWithCategoryTrueFalse(_pick, categoryName, true);
		}
	    } else {
		// create a Pick and mark it.
		syso.logInfo("create new pick and mark category: " + categoryName);
		markSongIDWithCategoryTrueFalse(memberID, PlayerStatus._getCurrentSongFile().getID() + "", categoryName,
			true);
	    }
	}
    }

    // Get all the song Picks for a member.
    //
    public List<Pick> getAllPickListByMemberID(long memberID) {
	List<Pick> pickList = new ArrayList<Pick>();
	if (isSkipUseRunning()) {
	    try {
		boolean includeCategoryInfo = true;
		pickList = skipUseManager.getAllPickListByMemberID(memberID, includeCategoryInfo);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
	return pickList;
    }

    // Update a Pick.
    //
    public void updateMemberPick(Pick pick) {
	if (isSkipUseRunning()) {
	    try {
		skipUseManager.updateMemberPick(pick);
		removePickFromCurrentPickList(pick);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Get song Pick information for a member by the song ID.
    // Returns null if not found.
    //
    public Pick _getPickByMemberIDAndSongID(long memberID, String songID) {
	Pick _pick = null;
	if (isSkipUseRunning() && songID != null) {
	    try {
		syso.logInfo("MusicSelectionHelper " + syso.getLineNumber()
			+ " Try _getPickByMemberIDAndSongID  memberID: " + memberID + " songID: " + songID);
		_pick = skipUseManager._getPickByMemberIDAndPickIDAndCollectionID(memberID, "" + songID,
			skipUseManager.getOwnerMemberID());
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(),
			e.getMessage() + " : memberID: " + memberID + " songID: " + songID);
		isSkipUseError = true;
	    }
	}
	return _pick;
    }

    // Update a Pick ID for a member.
    //
    public void skipUsePass(SkipUsePass skipUsePass, long memberID, String songID) {
	if (isSkipUseRunning()) {
	    try {
		syso.logInfo("MusicSelectionHelper " + syso.getLineNumber() + " Try " + skipUsePass.toString()
			+ " memberID: " + memberID + " songID: " + songID);
		skipUseManager.skipUsePass(skipUsePass, memberID, songID);
		removePickFromCurrentPickList(memberID, songID);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Update Picks.
    //
    public void skipUsePassPickList(SkipUsePass skipUsePass, List<Pick> pickList) {
	if (isSkipUseRunning()) {
	    try {
		skipUseManager.skipUsePassPickList(skipUsePass, pickList, skipUseManager.getOwnerMemberID());
		for (Pick pick : pickList)
		    removePickFromCurrentPickList(pick);
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Get the play mode.
    //
    public List<SearchMode> getPlayerSearchModeList() {
	return playerSearchModeList;
    }

    // Set the play search mode.
    // Setting the search mode determines what songs are return from SkipUse.
    //
    public void addOnePlayerSearchModeList(SearchMode searchMode) {
	if (searchMode != null) {
	    playerSearchModeList.clear();
	    addToPlayerSearchModeList(searchMode);
	}
    }

    // Set the play search mode.
    // Setting the search mode determines what songs are return from SkipUse.
    //
    public void addToPlayerSearchModeList(SearchMode searchMode) {
	if (searchMode != null) {
	    playerSearchModeList.add(searchMode);
	}
    }

    public void setPlaySearchModeList(List<SearchMode> playerSearchModeList) {
	clearPlayerSearchModeList();
	for (SearchMode searchMode : playerSearchModeList)
	    addToPlayerSearchModeList(searchMode);
    }

    // Clear the search modes for the player
    //
    public void clearPlayerSearchModeList() {
	playerSearchModeList.clear();
    }

    // Get the member ID for a user ID.
    // NOTE: this is just to help IF you have user IDs and want to use them as
    // member IDs.
    //
    public long getSkipUseMemberID(long userID) {
	long skipUseMemberID = 0;
	String memberName = "" + userID;
	if (isSkipUseRunning()) {
	    try {
		skipUseMemberID = skipUseManager.getMemberIDByName(memberName);

		// add member if needed
		if (skipUseMemberID == 0) {
		    skipUseManager.addMemberName(memberName);
		    skipUseMemberID = skipUseManager.getMemberIDByName(memberName);
		}
	    } catch (SkipUseException e) {
		syso.logInfo(e.getMessage());
	    }
	}
	if (skipUseMemberID == 0)
	    syso.logInfo("Failed to get Member ID.");

	return skipUseMemberID;
    }

    // Get the current listening members.
    //
    public List<Long> getListeningMemberIDSet() {
	if (getListeningMemberIDSet.size() == 0)
	    syso.logInfo("Warning: no Member ID set for Pick Query.");
	return getListeningMemberIDSet.stream().collect(Collectors.toList());
    }

    // Add a listening member by ID.
    //
    public void addListeningMemberID(long memberID) {
	this.getListeningMemberIDSet.add(memberID);
    }

    // Reset the listening member IDs.
    //
    public void clearListeningMemberIDs() {
	this.getListeningMemberIDSet.clear();
    }

    // Check that the SkipUse service is running. Login if needed.
    //
    public boolean isSkipUseRunning() {
	// If an error occurred, check that SkipUse is still running and that
	// user is logged in
	if (isSkipUseError || isSkipUseRunning == false) {
	    skipUseErrorCount++;
	    isSkipUseError = false;

	    if (skipUseManager.isAPIServerUp()) {
		isSkipUseRunning = true;
		// Are we logged in?
		isSkipUseLoggedIn = skipUseManager.isLoggedIn();
		// Need to log in?
		skipUseLogin();
	    } else {
		syso.logInfo("SkipUse: not running, not logged in");
		isSkipUseLoggedIn = false;
		isSkipUseRunning = false;
	    }
	} else {
	    skipUseErrorCount = 0;
	}

	// NOTE: if too many errors occur, stop and consider not-running
	if (skipUseErrorCount >= MAX_SKIPUSE_ERRORS)
	    isSkipUseRunning = false;

	return isSkipUseRunning && isSkipUseLoggedIn;
    }

    // If a Pick has been changed, remove it from the current list to avoid
    // stale data.
    //
    private void removePickFromCurrentPickList(Pick pick) {
	removePickFromCurrentPickList(pick.getMemberID(), pick.getPickID());
    }

    private void removePickFromCurrentPickList(long memberID, String pickID) {
	Pick _foundPick = getCurrentPickList().stream()
		.filter(p -> p.getPickID().equals(pickID) && p.getMemberID() == memberID).findFirst().orElse(null);
	if (_foundPick != null) {
	    getCurrentPickList().remove(_foundPick);
	}
    }

    // Load Pick List by Pick Query
    //
    private void loadPickListByPickQuery() {
	if (isSkipUseRunning()) {
	    returnedPickList.clear();
	    try {
		// if this query is the same, no need to set it, just re-use
		// it.
		if (isResendPickQuery || !pickQuery.toString().equals(lastPickQuery)) {
		    isResendPickQuery = false;
		    // NOTE: clear out the Pick ID queue too
		    pickIDQueue.clear();
		    lastPickQuery = pickQuery.toString();
		    returnedPickList = skipUseManager.setPickQuery(pickQuery);
		} else {
		    returnedPickList = skipUseManager.getPickQuery();
		}
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Load Pick Query
    //
    private void loadPickQuery() {
	if (isSkipUseRunning()) {
	    // create a Pick Query to get back song IDs.
	    // NOTE: you could save data Nibbles here by getting more than
	    // one song ID back.
	    pickQuery = new PickQuery(6);
	    // set how we get our Picks.
	    pickQuery.setSearchModeList(getPlayerSearchModeList());
	    // include categories that have been marked for the Picks.
	    pickQuery.setIncludeCategories(true);
	    // don't send back Picks we recently updated.
	    pickQuery.setExcludeRecentPicksHours(24);
	    // ramp High to Low percentages.
	    pickQuery.setRamp(RampMode.RATE_DOWN);
	    // get more Picks if the search comes up short.
	    pickQuery.setGetMorePicksIfShort(true);
	    // let's mix-in some new songs.
	    pickQuery.setNewMixInPercentage(20);
	    // add our listening members.
	    pickQuery.setMemberIDList(getListeningMemberIDSet());

	    if (pickQuery.getMemberIDList().isEmpty())
		syso.error("There are no member IDs set for the Pick Query");

	    // use the time of day option for RACING search mode
	    if (getPlayerSearchModeList().contains(SearchMode.RACING))
		pickQuery.setUseTimeOfDay(true);

	    // check that the categories are stored by SkipUse
	    if (!getCategoryList().isEmpty() && !getCategoryList().toString().equals(lastCategoryList.toString())) {
		lastCategoryList = getCategoryList();
		// NOTE: getting only ONE member's categories, because members
		// can have different categories.
		loadMemberCategoryList();

		// automatically add the category, if not in member's list.
		for (String categoryName : getCategoryList()) {
		    // Can't add reserved category mode words as categories
		    if (!categoryName.equalsIgnoreCase("ANY") && !categoryName.equalsIgnoreCase("NONE")
			    && !categoryName.equalsIgnoreCase("NOT") && !categoryName.equalsIgnoreCase("EACH_CATEGORY")
			    && !categoryName.equalsIgnoreCase("PICK")) {
			String _foundSkipUseCategory = memberCategoryList.getCategoryList().stream()
				.filter(c -> c.equals(categoryName)).findFirst().orElse(null);
			if (_foundSkipUseCategory == null) {
			    try {
				skipUseManager.createCategoryForMember(getListeningCategoryMemberID(), categoryName);
			    } catch (SkipUseException e) {
				syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
				isSkipUseError = true;
			    }
			}
		    }
		}

	    }

	    // when listening by categories, set to get only Picks we currently have stored.
	    if (getCategoryList().size() > 1) {
		if (!getCategoryList().contains("ANY") && !getCategoryList().contains("NONE")
			&& !getCategoryList().contains("NOT"))
		    pickQuery.setNewMixInPercentage(0);
	    }

	    // set to look for these categories.
	    pickQuery.setCategoryList(getCategoryList());
	}
    }

    // Get category names for the listening members.
    //
    private void loadMemberCategoryList() {
	if (isSkipUseRunning()) {
	    try {
		memberCategoryList = skipUseManager.getCategoryListForMember(getListeningCategoryMemberID());
	    } catch (SkipUseException e) {
		syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
		isSkipUseError = true;
	    }
	}
    }

    // Login to SkipUse.
    //
    private void skipUseLogin() {
	try {
	    if (isSkipUseLoggedIn == false) {
		syso.logInfo("not logged in... attempting to log in...");
		skipUseManager.login(SkipUseProperties.SKIP_USE_EMAIL, SkipUseProperties.SKIP_USE_PASSWORD);
		// NOTE: the stored Pick Query is no longer valid, send it again
		isResendPickQuery = true;
		// logged in now?
		isSkipUseLoggedIn = skipUseManager.isLoggedIn();
	    }
	} catch (SkipUseException e) {
	    syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
	    isSkipUseError = true;
	}
    }
}
