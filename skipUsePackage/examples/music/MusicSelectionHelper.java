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
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
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
	private boolean isSkipUseError = false;

	// Listening members and song collections.
	private PickQuery lastPickQuery = new PickQuery();
	private PickIDCollection pickIDCollection = new PickIDCollection();
	private Set<Integer> getListeningMemberIDSet = new HashSet<>();
	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	// Play-back and selection variables.
	private PlayMode playMode = PlayMode.ANY;
	private String selectedCategoryName = "";
	private int selectedCategoryMemberID = -1;

	// Returned Picks from SkipUse
	private PickQuery pickQuery = new PickQuery(10);
	private List<Pick> returnedPickList = new ArrayList<>();
	private Queue<String> pickIDQueue = new LinkedList<String>();

	public MusicSelectionHelper() {
		initialize();
	}

	public void initialize() {
		isSkipUseRunning = false;
		isSkipUseError = false;
		skipUseLogin();
		isSkipUseRunning();
	}

	// Get the stored collection of song IDs from SkipUse.
	//
	public List<String> getSongIDCollection() {
		if (pickIDCollection.getPickIDList().isEmpty()) {
			if (isSkipUseRunning()) {
				try {
					pickIDCollection = skipUseManager.getPickIDCollection();
				} catch (SkipUseException e) {
					syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
					isSkipUseError = true;
				}
			}
		}
		return pickIDCollection.getPickIDList();
	}

	// Add a collection of song IDs to SkipUse.
	//
	public void setSongIDCollection(List<String> songIDList, boolean isCommaSpaceDelimted) {
		try {
			syso.logInfo("MusicSelectionHelper " + syso.getLineNumber()
					+ " Try setSongIDCollection  songIDList.size: " + songIDList.size()
					+ " isCommaSpaceDelimted: " + isCommaSpaceDelimted);
			pickIDCollection = skipUseManager.addPickIDCollection(songIDList, isCommaSpaceDelimted);
		} catch (SkipUseException e) {
			syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
			isSkipUseError = true;
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
		if (!pickQuery.toString().equals(lastPickQuery.toString())) {
			pickIDQueue.clear();
			loadPickQueue();
		} else if (pickIDQueue.isEmpty()) {
			loadPickQueue();
		}

		// load a song ID from the Pick queue
		_songID = pickIDQueue.poll();

		if (_songID == null)
			syso.logInfo(
					"Failed to get a song ID. NOTE: You could add code to get a random song ID to return.");

		return _songID;
	}

	// Load Pick Queue
	//
	public void loadPickQueue() {
		loadPickListByPickQuery();
		for (Pick pick : returnedPickList) {
			if (!pickIDQueue.contains(pick.getPickID())) {
				pickIDQueue.add(pick.getPickID());
				syso.logInfo("PickIDQueue loading: " + pick.getPickID());
			}
			if (pickIDQueue.size() >= pickQuery.getHowMany())
				break;
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
	public int getListeningCategoryMemberID() {
		return selectedCategoryMemberID;
	}

	// Set the ONE member ID to use when listening by category.
	//
	public void setListeningCategoryMemberID(int memberID) {
		this.selectedCategoryMemberID = memberID;
	}

	// Get the selected category.
	//
	public String getSelectedCategory() {
		return selectedCategoryName;
	}

	// Set the selected category for use with the PlayMode:CATEGORY
	// to play back for ONE member.
	//
	public String setSelectedCategory(String selectedCategory) {
		if (selectedCategory == null)
			selectedCategory = "";
		return this.selectedCategoryName = selectedCategory;
	}

	// Create a category for a member.
	// NOTE: no commas are allowed in the category name.
	//
	public void createCategoryForMember(int memberID, String category) {
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
	public List<String> getCategoryListForMember(int memberID) {
		List<String> categoryList = new ArrayList<String>();
		if (isSkipUseRunning()) {
			try {
				MemberCategoryList memberCategoryList = skipUseManager
						.getCategoryListForMember(memberID);
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
	public void markSongIDWithCategoryTrueFalse(int memberID, String songID, String category,
			boolean isMark) {
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
	public void toggleSongCategory(String songID, int memberID, Category toggleCategory) {
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
				markSongIDWithCategoryTrueFalse(memberID,
						PlayerStatus._getCurrentSongFile().getID() + "", categoryName, true);
			}
		}
	}

	// Get all the song Picks for a member.
	//
	public List<Pick> getAllPickListByMemberID(int memberID) {
		List<Pick> pickList = new ArrayList<Pick>();
		if (isSkipUseRunning()) {
			try {
				pickList = skipUseManager.getAllPickListByMemberID(memberID);
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
	public Pick _getPickByMemberIDAndSongID(int memberID, String songID) {
		Pick _pick = null;
		if (isSkipUseRunning() && songID != null) {
			try {
				syso.logInfo("MusicSelectionHelper " + syso.getLineNumber()
						+ " Try _getPickByMemberIDAndSongID  memberID: " + memberID + " songID: "
						+ songID);
				_pick = skipUseManager._getPickByMemberIDAndPickID(memberID, "" + songID);
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
	public void skipUsePass(SkipUsePass skipUsePass, int memberID, String songID) {
		if (isSkipUseRunning()) {
			try {
				syso.logInfo("MusicSelectionHelper " + syso.getLineNumber() + " Try "
						+ skipUsePass.toString() + " memberID: " + memberID + " songID: " + songID);
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
				skipUseManager.skipUsePassPickList(skipUsePass, pickList);
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
	public PlayMode getPlayMode() {
		return playMode;
	}

	// Set the play mode.
	// Setting the play mode determines what songs are return from SkipUse.
	//
	public void setPlayMode(PlayMode playMode) {
		if (playMode != null)
			this.playMode = playMode;
	}

	// Get the member ID for a user ID.
	// NOTE: this is just to help IF you have user IDs and want to use them as
	// member IDs.
	//
	public int getSkipUseMemberID(int userID) {
		int skipUseMemberID = -1;
		String memberName = "" + userID;
		try {
			skipUseMemberID = skipUseManager.getMemberIDByName(memberName);

			// add member if needed
			if (skipUseMemberID == -1) {
				skipUseManager.addMemberName(memberName);
				skipUseMemberID = skipUseManager.getMemberIDByName(memberName);
			}
		} catch (SkipUseException e) {
			syso.logInfo(e.getMessage());
		}
		if (skipUseMemberID < 0)
			syso.logInfo("Failed to get Member ID.");

		return skipUseMemberID;
	}

	// Get the current listening members.
	//
	public List<Integer> getListeningMemberIDSet() {
		return getListeningMemberIDSet.stream().collect(Collectors.toList());
	}

	// Add a listening member by ID.
	//
	public void addListeningMemberID(int memberID) {
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
		// If an error occurred, check that SkipUse is still running by reseting
		// the running flag.
		if (isSkipUseError || isSkipUseRunning == false) {
			isSkipUseError = false;
			syso.logInfo("SkipUse: reseting and checking.");
			isSkipUseRunning = false;

			if (isSkipUseRunning == false) {
				try {
					if (skipUseManager.isAPIServerUp()) {
						// Need to log in?
						skipUseLogin();
						// Are we logged in now?
						isSkipUseRunning = skipUseManager.isLoggedIn();
					}
				} catch (SkipUseException e) {
					syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
				}
			}
		}
		return isSkipUseRunning;
	}

	// If a Pick has been changed, remove it from the current list to avoid
	// stale data.
	//
	private void removePickFromCurrentPickList(Pick pick) {
		removePickFromCurrentPickList(pick.getMemberID(), pick.getPickID());
	}

	private void removePickFromCurrentPickList(int memberID, String pickID) {
		Pick _foundPick = getCurrentPickList().stream()
				.filter(p -> p.getPickID().equals(pickID) && p.getMemberID() == memberID)
				.findFirst().orElse(null);
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
				// if this query is the same no need to set it, just re-use
				// it.
				if (!pickQuery.toString().equals(lastPickQuery.toString())) {
					lastPickQuery = pickQuery;
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
			// create a Pick Query to get back ONE song ID.
			// NOTE: you could save data Nibbles here by getting more than
			// one song ID back. Here we assume an average listening time of 30
			// minutes.
			pickQuery = new PickQuery(12);
			// include categories that have been marked for the Picks.
			pickQuery.setIncludeCategories(true);
			// for now, set to get only Picks we currently have stored.
			pickQuery.setNewMixInPercentage(0);
			// don't send back Picks we recently updated.
			pickQuery.setExcludeRecentPicks(true);

			// add our listening members.
			pickQuery.setMemberIDList(getListeningMemberIDSet());

			// alter the Pick Query by the Play Mode.
			if (getPlayMode() == PlayMode.FAVORITE) {
				// get favorites.
				pickQuery.setSearchMode(SearchMode.FAVORITES);
			} else if (PlayerStatus.getPlayMode() == PlayMode.CATEGORY
					&& !getSelectedCategory().isEmpty()) {
				// get by a selected category.

				// NOTE: getting only ONE member's category, because members
				// can have different categories.
				loadMemberCategoryList();

				// see if SkipUse has the category for the member.
				String _foundSkipUseCategory = memberCategoryList.getCategoryList().stream()
						.filter(c -> c.equals(getSelectedCategory())).findFirst().orElse(null);

				List<String> categories = new ArrayList<String>();
				if (_foundSkipUseCategory != null) {
					categories.add(_foundSkipUseCategory);
				} else {
					// automatically add the category, if not in member's
					// list.
					try {
						skipUseManager.createCategoryForMember(getListeningCategoryMemberID(),
								getSelectedCategory());
					} catch (SkipUseException e) {
						syso.logError("MusicSelectionHelper " + syso.getLineNumber(),
								e.getMessage());
						isSkipUseError = true;
					}
					// NOTE: member does not have this category, so no song
					// will be returned anyway. Don't add to categories
					// query list.
				}
				// look for this category.
				pickQuery.setCategories(categories);
			} else {
				// just search as normal, but let's mixing in some new songs.
				pickQuery.setNewMixInPercentage(35);
			}
		}
	}

	private void loadMemberCategoryList() {
		try {
			memberCategoryList = skipUseManager
					.getCategoryListForMember(getListeningCategoryMemberID());
		} catch (SkipUseException e) {
			syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
			isSkipUseError = true;
		}
	}

	// Login to SkipUse.
	//
	private void skipUseLogin() {
		try {
			if (skipUseManager.isLoggedIn() == false) {
				syso.logInfo("not logged in... attempting to log in...");
				skipUseManager.login(SkipUseProperties.SKIP_USE_EMAIL,
						SkipUseProperties.SKIP_USE_PASSWORD);
			}
		} catch (SkipUseException e) {
			syso.logError("MusicSelectionHelper " + syso.getLineNumber(), e.getMessage());
			isSkipUseError = true;
		}
	}

}
