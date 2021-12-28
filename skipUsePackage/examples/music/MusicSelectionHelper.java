package com.autogilmore.throwback.skipUsePackage.examples.music;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.autogilmore.throwback.data.PlayerStatus;
import com.autogilmore.throwback.data.songcategory.Category;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.enums.CategoryOption;
import com.autogilmore.throwback.skipUsePackage.enums.ResultOption;
import com.autogilmore.throwback.skipUsePackage.enums.SearchOption;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;
import com.autogilmore.throwback.util.MySystemOut;

public class MusicSelectionHelper {
    // NOTE: This is my system out and logger. You can substitute this usage
    // with just System.out.println for your code.
    private MySystemOut syso = MySystemOut.INSTANCE;

    // Using the Manager to handle SkipUse requests.
    SkipUseManager skipUseManager = SkipUseManager.INSTANCE;

    // Don't get Pick until after this many days
    private final int EXCLUDE_DAYS = 4;

    // Flags to verify service states.
    private boolean isSkipUseAPIRunning = false;
    private boolean isSkipUseLoggedIn = false;
    private boolean isSkipUseError = false;
    private int skipUseErrorCount = 0;
    private static final int MAX_SKIPUSE_ERRORS = 20;
    private boolean isSkipUseFatalError = false;

    // Listening members and song collections.
    private String lastPickQuery = "";
    private MemberCollection memberCollection = new MemberCollection();
    private Set<Long> listeningMemberIDSet = new HashSet<>();
    private Set<String> memberCategorySet = new HashSet<String>();

    // Play-back and selection variables.
    private List<SearchOption> playerSearchOptionList = new ArrayList<>();
    private List<ResultOption> playerResultOptionList = new ArrayList<>();

    private List<String> selectedCategoryList = new ArrayList<>();
    private List<String> lastSelectedCategoryList = new ArrayList<String>();

    // Returned Picks from SkipUse
    private boolean isResendPickQuery = true;
    private PickQuery pickQuery = new PickQuery();
    private List<Pick> returnedPickList = new ArrayList<>();
    private Map<String, LocalDate> getPickIDHistoryMap = new HashMap<String, LocalDate>();
    private Queue<Pick> pickQueue = new LinkedList<Pick>();

    public MusicSelectionHelper() {
	initialize();
    }

    public void initialize() {
	reset();
	syso.logInfo("MS initialize...");
	isSkipUseRunning();
    }

    public void reset() {
	syso.logInfo("MS reset values...");
	isSkipUseAPIRunning = false;
	isSkipUseError = false;
	skipUseErrorCount = 0;
	isSkipUseLoggedIn = false;
	isSkipUseFatalError = false;
	lastPickQuery = "";
	memberCollection = new MemberCollection();
	listeningMemberIDSet = new HashSet<>();
	memberCategorySet = new HashSet<>();
	playerSearchOptionList = new ArrayList<>();
	playerResultOptionList = new ArrayList<>();
	selectedCategoryList = new ArrayList<>();
	lastSelectedCategoryList = new ArrayList<String>();
	isResendPickQuery = true;
	pickQuery = new PickQuery();
	returnedPickList = new ArrayList<>();
	getPickIDHistoryMap = new HashMap<String, LocalDate>();
	pickQueue = new LinkedList<Pick>();
    }

    // Get the stored collection of song IDs from SkipUse.
    //
    public List<String> getSongIDCollection() {
	if (memberCollection.getPickIDList().isEmpty()) {
	    try {
		memberCollection = skipUseManager.getMemberCollection(0L);
	    } catch (SkipUseException e) {
		syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
		PlayerStatus.addToErrorMessageQueue(e.getMessage());
		isSkipUseError = true;
	    }
	}
	return memberCollection.getPickIDList();
    }

    // Add a collection of song IDs to SkipUse.
    //
    public void setSongIDCollection(List<String> songIDList, boolean isCommaSpaceDelimted) {
	memberCollection = new MemberCollection();
	try {
	    syso.logInfo("MusicSelectionHelper " + syso.getLineNumber() + " Try setSongIDCollection  songIDList.size: "
		    + songIDList.size() + " isCommaSpaceDelimted: " + isCommaSpaceDelimted);
	    memberCollection = skipUseManager.addMemberCollection(0L, songIDList, isCommaSpaceDelimted);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Get the next song ID to play.
    // Returns null if the service is not running or an error occurred.
    //
    public Pick _getNextPick() {

	// check the pickQuery to see if it changed
	loadPickQuery();

	// did the Pick Query change? is the queue empty? Then load new songs
	if (isResendPickQuery || !pickQuery.toString().equals(lastPickQuery) || pickQueue.isEmpty()) {
	    loadPickQueue();
	}

	// load a song ID from the Pick queue
	Pick _nextPick = null;
	String _songID = null;
	boolean isFindPick = true;

	while (isFindPick) {
	    if (!pickQueue.isEmpty()) {
		_nextPick = pickQueue.poll();
		String pickID = _nextPick.getPickID();
		// check we have not used this Pick before the timelimit
		if (getPickIDHistoryMap.containsKey(pickID)) {
		    // we have seen this Pick before
		    if (LocalDate.now().minusDays(EXCLUDE_DAYS).isAfter(getPickIDHistoryMap.get(pickID))) {
			// looks good, update our map
			getPickIDHistoryMap.replace(pickID, LocalDate.now());
			_songID = pickID;
		    } else {
			syso.logError(syso.getLineNumber() + " MusicSelectionHelper",
				"A song was returned before the PickQuery time exclusion Going to PASS the Pick. was: "
					+ pickID);
			// NOTE: stop the Pick from being sent back again by passing on it.
			skipUsePassAddToSendUpdates(SkipUsePass.PASS, _nextPick.getMemberID(), pickID);
		    }
		} else {
		    // looks new, saving it to compare.
		    getPickIDHistoryMap.put(pickID, LocalDate.now());
		    _songID = pickID;
		}

		isFindPick = _songID == null;
	    } else {
		isFindPick = false;
	    }
	}

	if (_songID == null)
	    syso.logInfo("Failed to get a song ID. NOTE: You could add code to get a random song ID to return.");

	return _nextPick;
    }

    // Load Pick Queue
    //
    public void loadPickQueue() {
	loadPickListByPickQuery();
	for (Pick pick : returnedPickList) {
	    String pickID = pick.getPickID();
	    if (!pickQueue.contains(pick)) {
		pickQueue.add(pick);
		syso.logInfo("PickIDQueue loading: " + pickID);
	    }

	    // if we get back a owner's Pick, something went wrong
	    try {
		if (pick.getMemberID() == skipUseManager.getOwnerMemberID()) {
		    syso.logError(syso.getLineNumber() + " MusicSelectionHelper",
			    "Did not get back member Picks. Check member IDs in PickQuery.");
		    pickQueue.clear();
		    isSkipUseError = true;
		    break;
		}
	    } catch (SkipUseException e) {
		syso.logError(syso.getLineNumber() + " MusicSelectionHelper" + syso.getLineNumber(), e.getMessage());
		PlayerStatus.addToErrorMessageQueue(e.getMessage());
		isSkipUseError = true;
	    }

	    // if we get back too many Picks, just break out
	    if (returnedPickList.size() > pickQuery.getHowMany()) {
		syso.logError(syso.getLineNumber() + " MusicSelectionHelper" + syso.getLineNumber(),
			"returnedPickList size is larger than the pickQuery setting, stop added the returned Picks to the queue.");
		break;
	    }
	}
    }

    public boolean isLoadPickQueueNeeded() {
	return isSkipUseRunning() && pickQueue.size() < 3;
    }

    // Get the current Pick List.
    //
    public List<Pick> getCurrentPickList() {
	return returnedPickList;
    }

    // Get the selected categories.
    //
    public List<String> getSelectedCategoryList() {
	return selectedCategoryList;
    }

    // Set the selected category for use with the PlayMode:CATEGORY
    // to play back for members.
    //
    public void setSelectedCategoryList(List<String> categoryList) {
	if (categoryList != null) {
	    this.selectedCategoryList = categoryList;
	} else {
	    this.selectedCategoryList.clear();
	}
    }

    // Get the categories for a member.
    //
    public List<String> getCategoryListForMember(long memberID) {
	List<String> categoryList = new ArrayList<String>();
	try {
	    MemberCategoryList memberCategoryList = skipUseManager.getCategoryListForMember(memberID);
	    categoryList = memberCategoryList.getCategoryList();
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
	return categoryList;
    }

    // Mark or un-mark a song ID using a member's category.
    //
    public void toggleSongCategory(String songID, long memberID, Category toggleCategory) {
	String categoryName = toggleCategory.toString();

	// create Category if needed.
	List<String> categoryList = getCategoryListForMember(memberID);
	if (!categoryList.contains(categoryName))
	    createCategoryForMember(memberID, categoryName);

	Pick _pick = _getPickByMemberIDAndSongID(memberID, songID);
	if (_pick != null) {
	    if (_pick.getCategoryList().contains(categoryName)) {
		// un-mark category.
		syso.logInfo("unmark category: " + categoryName);
		markPickWithCategoryTrueFalse(0L, _pick, categoryName, false);
	    } else {
		// mark it.
		syso.logInfo("mark category: " + categoryName);
		markPickWithCategoryTrueFalse(0L, _pick, categoryName, true);
	    }
	} else {
	    // create a Pick and mark it.
	    syso.logInfo("create new pick and mark category: " + categoryName);
	    markSongIDWithCategoryTrueFalse(memberID, PlayerStatus._getCurrentSongFile().getID() + "", categoryName,
		    true);
	}
    }

    // Get all the song Picks for a member.
    //
    public List<Pick> getAllPickListByMemberID(long memberID) {
	List<Pick> pickList = new ArrayList<Pick>();
	try {
	    boolean includeCategoryInfo = true;
	    pickList = skipUseManager.getAllPickListByMemberID(memberID, includeCategoryInfo);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
	return pickList;
    }

    // Update a Pick.
    //
    public void updateMemberPick(Pick pick) {
	try {
	    skipUseManager.updateMemberPick(pick);
	    removePickFromCurrentPickList(pick);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Get song Pick information for a member by the song ID.
    // Returns null if not found.
    //
    public Pick _getPickByMemberIDAndSongID(long memberID, String songID) {
	Pick _pick = null;
	if (songID != null) {
	    try {
		syso.logInfo("MusicSelectionHelper " + syso.getLineNumber()
			+ " Try _getPickByMemberIDAndSongID  memberID: " + memberID + " songID: " + songID);
		_pick = skipUseManager._getPickByMemberIDAndPickIDAndCollectionID(memberID, "" + songID,
			skipUseManager.getOwnerMemberID());
	    } catch (SkipUseException e) {
		syso.logError(syso.getLineNumber() + " MusicSelectionHelper",
			e.getMessage() + " : memberID: " + memberID + " songID: " + songID);
		PlayerStatus.addToErrorMessageQueue(e.getMessage());
		isSkipUseError = true;
	    }
	}
	return _pick;
    }

    // Update a Pick ID for a member.
    //
    public void skipUsePassAddToSendUpdates(SkipUsePass skipUsePass, long memberID, String songID) {
	try {
	    syso.logInfo("MusicSelectionHelper " + syso.getLineNumber() + " Try " + skipUsePass.toString()
		    + " memberID: " + memberID + " songID: " + songID);
	    int addSkipCNT = skipUsePass.equals(SkipUsePass.SKIP) ? 1 : 0;
	    int addUseCNT = skipUsePass.equals(SkipUsePass.USE) ? 1 : 0;
	    skipUseManager.addToPickIDCount(skipUseManager.getOwnerMemberID(), memberID, songID, addSkipCNT, addUseCNT);
	    removePickFromCurrentPickList(memberID, songID);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Update Picks.
    //
    public void skipUsePassPickList(SkipUsePass skipUsePass, List<Pick> pickList) {
	for (Pick pick : pickList) {
	    skipUsePassAddToSendUpdates(skipUsePass, pick.getMemberID(), pick.getPickID());
	}
    }

    // Send collected Pick ID count updates.
    //
    public void skipUsePassSendUpdates(boolean updateASAP) {
	try {
	    syso.logInfo("MusicSelectionHelper : send Pick ID count batch updates.");
	    skipUseManager.pickIDCountAdvance(updateASAP);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Get the search option list.
    //
    public List<SearchOption> getPlayerSearchOptionList() {
	return playerSearchOptionList;
    }

    // Set just one search option list.
    //
    public void addOnePlayerSearchOptionList(SearchOption searchOption) {
	if (searchOption != null) {
	    playerSearchOptionList.clear();
	    addToPlayerSearchOptionList(searchOption);
	}
    }

    // Add to the search option list.
    //
    public void addToPlayerSearchOptionList(SearchOption searchOption) {
	if (searchOption != null && !playerSearchOptionList.contains(searchOption)) {
	    playerSearchOptionList.add(searchOption);
	}
    }

    // Set the search option list.
    //
    public void setPlaySearchOptionList(List<SearchOption> playerSearchOptionList) {
	clearPlayerSearchOptionList();
	for (SearchOption searchOption : playerSearchOptionList) {
	    addToPlayerSearchOptionList(searchOption);
	}
    }

    // Get the result option list.
    //
    public List<ResultOption> getPlayerResultOptionList() {
	return playerResultOptionList;
    }

    // Clear the search options for the player
    //
    public void clearPlayerSearchOptionList() {
	playerSearchOptionList.clear();
    }

    // Set the result option list.
    //
    public void setPlayerResultOptionList(List<ResultOption> resultOptionList) {
	clearPlayerResultOptionList();
	for (ResultOption resultOption : resultOptionList)
	    addToPlayerResultOptionList(resultOption);
    }

    // Add to the result option list.
    //
    public void addToPlayerResultOptionList(ResultOption resultOption) {
	if (resultOption != null && !getPlayerResultOptionList().contains(resultOption)) {
	    playerResultOptionList.add(resultOption);
	}
    }

    // Clear the search options for the player
    //
    public void clearPlayerResultOptionList() {
	playerResultOptionList.clear();
    }

    // Get the member ID for a user ID.
    // NOTE: this is just to help IF you have user IDs and want to use them as
    // member IDs.
    //
    public long getSkipUseMemberID(long userID) {
	long skipUseMemberID = 0;
	String memberName = "" + userID;
	try {
	    skipUseMemberID = skipUseManager.getMemberIDByName(memberName);

	    // add member if needed
	    if (skipUseMemberID == 0) {
		skipUseManager.addMemberName(memberName);
		skipUseMemberID = skipUseManager.getMemberIDByName(memberName);
	    }
	} catch (SkipUseException e) {
	    syso.logInfo(e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	}

	if (skipUseMemberID == 0)
	    syso.logInfo("Failed to get Member ID.");

	return skipUseMemberID;
    }

    // Get the current listening members.
    //
    public List<Long> getListeningMemberIDSet() {
	if (listeningMemberIDSet.isEmpty()) {
	    syso.logInfo("Warning: no Member ID set for Pick Query.");
	    PlayerStatus.addToErrorMessageQueue("Warning: no Member ID set for Pick Query.");
	    isSkipUseError = true;
	}
	return listeningMemberIDSet.stream().collect(Collectors.toList());
    }

    // Add a listening member by ID.
    //
    public void addListeningMemberID(long memberID) {
	this.listeningMemberIDSet.add(memberID);
    }

    // Reset the listening member IDs.
    //
    public void clearListeningMemberIDs() {
	this.listeningMemberIDSet.clear();
    }

    // Check that the SkipUse service is running. Login if needed.
    //
    public boolean isSkipUseRunning() {
	if (isSkipUseFatalError)
	    return false;

	// If an error occurred, check that SkipUse is still running and that
	// user is logged in
	if (isSkipUseError || isSkipUseAPIRunning == false) {
	    skipUseErrorCount++;
	    isSkipUseError = false;
	    isResendPickQuery = true;

	    // Are we logged in?
	    isSkipUseLoggedIn = skipUseManager.isLoggedIn();

	    if (isSkipUseAPIRunning == false)
		isSkipUseAPIRunning = skipUseManager.isAPIServerUp();

	    if (isSkipUseAPIRunning && isSkipUseLoggedIn == false) {
		syso.logInfo("not logged in....");

		// Need to log in?
		try {
		    syso.logInfo("server is up, try to log in....");
		    skipUseManager.login(SkipUseProperties.SKIP_USE_EMAIL, SkipUseProperties.SKIP_USE_PASSWORD);
		    // Are we logged in now?
		    isSkipUseLoggedIn = skipUseManager.isLoggedIn();
		} catch (Exception e) {
		    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
		}
	    }
	} else {
	    skipUseErrorCount = 0;
	}

	// NOTE: if too many errors occur, stop and consider not-running
	if (skipUseErrorCount >= MAX_SKIPUSE_ERRORS) {
	    isSkipUseAPIRunning = false;
	    isSkipUseFatalError = true;
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper",
		    "Too many errors, stop trying to use SkipUse");
	}
	if (!isSkipUseAPIRunning)
	    syso.logInfo("SkipUseAPI not running");

	if (!isSkipUseLoggedIn)
	    syso.logInfo("Not logged in");

	if (isSkipUseFatalError)
	    syso.logInfo("Fatal Error");

	return isSkipUseAPIRunning && isSkipUseLoggedIn && !isSkipUseFatalError;
    }

    // Create a category for a member.
    // NOTE: no commas are allowed in the category name.
    //
    private void createCategoryForMember(long memberID, String category) {
	try {
	    skipUseManager.createCategoryForMember(memberID, category);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
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
	returnedPickList.clear();
	if (isSkipUseRunning()) {
	    try {
		// if this query is the same, no need to set it, just re-use
		// it.
		if (!isResendPickQuery && pickQuery.toString().equals(lastPickQuery)) {
		    syso.logInfo(syso.getLineNumber() + " MusicSelectionHelper : getPickQuery");
		    returnedPickList = skipUseManager.getPickQuery();
		}

		if (returnedPickList.isEmpty()) {
		    syso.logInfo(syso.getLineNumber() + " MusicSelectionHelper : setPickQuery");
		    isResendPickQuery = false;
		    // NOTE: clear out the Pick ID queue too
		    pickQueue.clear();
		    lastPickQuery = pickQuery.toString();
		    returnedPickList = skipUseManager.setPickQuery(pickQuery);
		}
	    } catch (SkipUseException e) {
		syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
		PlayerStatus.addToErrorMessageQueue(e.getMessage());
		isSkipUseError = true;
	    }
	}
	if (returnedPickList.isEmpty()) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper",
		    "loadPick did not return a PickList. setting to resend PickQuery");
	}
    }

    // Load Pick Query
    //
    private void loadPickQuery() {
	// create a Pick Query to get back song IDs.
	// NOTE: you could save data Nibbles here by getting more than
	// one song ID back.
	pickQuery = new PickQuery(8);
	// set how we get our Picks.
	pickQuery.setSearchOptionList(getPlayerSearchOptionList());
	pickQuery.setResultOptionList(getPlayerResultOptionList());
	// include categories that have been marked for the Picks.
	pickQuery.addToResultOptionList(ResultOption.INCLUDE_CATEGORY_INFO);
	// don't send back Picks we recently updated.
	pickQuery.setExcludeRecentPicksHours(EXCLUDE_DAYS * 24);
	// get more Picks if the search comes up short.
	pickQuery.addToSearchOptionList(SearchOption.GET_MORE_IF_SHORT);
	// let's mix-in some new songs.
	pickQuery.setNewMixInPercentage(20);
	// add our listening members.
	pickQuery.setMemberIDList(getListeningMemberIDSet());

	if (pickQuery.getMemberIDList().isEmpty())
	    syso.error("There are no member IDs set for the Pick Query");

	if (memberCategorySet.isEmpty()) {
	    loadMemberCategoryList();
	}

	if (!getSelectedCategoryList().isEmpty()) {
	    boolean hasSelectedCategoryChange = !getSelectedCategoryList().toString()
		    .equals(lastSelectedCategoryList.toString());
	    if (hasSelectedCategoryChange) {
		lastSelectedCategoryList.clear();
		lastSelectedCategoryList.addAll(getSelectedCategoryList());
	    }
	    boolean hasCategoryOption = false;
	    for (String categoryName : getSelectedCategoryList()) {
		// Can't add reserved category option words as categories
		try {
		    CategoryOption.valueOf(categoryName);
		    hasCategoryOption = true;
		} catch (Exception e) {
		    // not a CategoryOption, automatically add the category, if not in member's
		    // list.
		    if (hasSelectedCategoryChange && !memberCategorySet.contains(categoryName)) {
			for (Long memberId : listeningMemberIDSet) {
			    createCategoryForMember(memberId, categoryName);
			}
			memberCategorySet.add(categoryName);
		    }
		}
	    }

	    // if we just have Categories and no CategoryOption, then do not mix-in new
	    // Picks
	    if (!hasCategoryOption) {
		pickQuery.setNewMixInPercentage(0);
		pickQuery.getSearchOptionList().remove(SearchOption.GET_MORE_IF_SHORT);
	    }
	}

	// set to look for these categories.
	pickQuery.setCategoryList(getSelectedCategoryList());
    }

    // Get category names for the listening members.
    //
    private void loadMemberCategoryList() {
	try {
	    memberCategorySet.clear();
	    for (Long memberId : listeningMemberIDSet) {
		memberCategorySet.addAll(skipUseManager.getCategoryListForMember(memberId).getCategoryList());
	    }
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Mark or un-mark a song Pick with a category.
    //
    private void markPickWithCategoryTrueFalse(long memberIDCollection, Pick pick, String category, boolean isMark) {
	try {
	    skipUseManager.markPickWithCategoryTrueFalse(memberIDCollection, pick, category, isMark);
	    removePickFromCurrentPickList(pick);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }

    // Mark a song ID with a category for a member.
    //
    private void markSongIDWithCategoryTrueFalse(long memberID, String songID, String category, boolean isMark) {
	try {
	    skipUseManager.markPickIDWithCategoryTrueFalse(0L, memberID, songID, category, true);
	    removePickFromCurrentPickList(memberID, songID);
	} catch (SkipUseException e) {
	    syso.logError(syso.getLineNumber() + " MusicSelectionHelper", e.getMessage());
	    PlayerStatus.addToErrorMessageQueue(e.getMessage());
	    isSkipUseError = true;
	}
    }
}
