package com.autogilmore.throwback.skipUsePackage.examples.music;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

public class MusicSelectionHelper {
	// Using the Manager to handle SkipUse requests.
	private SkipUseManager skipUseManager = SkipUseManager.getInstance();

	// Flags to verify service states.
	private boolean isSkipUseRunning = false;
	private boolean isSkipUseError = true;

	// Listening members and song collections.
	private PickQuery lastPickQuery = new PickQuery();
	private PickIDCollection pickIDCollection = new PickIDCollection();
	private List<Pick> pickList = new ArrayList<Pick>();
	private Set<Integer> getListeningMemberIDSet = new HashSet<>();

	// Play-back and selection variables.
	private PlayMode playMode = PlayMode.ANY;
	private String selectedCategoryName = "";
	private int selectedCategoryMemberID = -1;

	public MusicSelectionHelper() {
		skipUseLogin();
	}

	// Get the stored collection of song IDs from SkipUse.
	//
	public List<String> getSongIDCollection() {
		if (pickIDCollection.getPickIDList().isEmpty()) {
			if (isSkipUseRunning()) {
				try {
					pickIDCollection = skipUseManager.getPickIDCollection();
				} catch (SkipUseException e) {
					System.out.println(e.getMessage());
					isSkipUseError = true;
				}
			}
		}
		return pickIDCollection.getPickIDList();
	}

	// Add a collection of song IDs to SkipUse.
	//
	public void setSongIDCollection(String collectionName, List<String> songIDList,
			boolean isCommaSpaceDelimted) {
		try {
			pickIDCollection = skipUseManager.addPickIDCollection(collectionName, songIDList,
					isCommaSpaceDelimted);
		} catch (SkipUseException e) {
			System.out.println(e.getMessage());
			isSkipUseError = true;
		}
	}

	// Get the next song ID to play.
	// Returns null if the service is not running or an error occurred.
	//
	public String _getNextSongID() {
		String _songID = null;
		if (isSkipUseRunning()) {
			try {
				// create a Pick Query to get back ONE song ID.
				// NOTE: you could save data Nibbles here by getting more than
				// one song ID back.
				PickQuery pickQuery = new PickQuery(1);
				// don't get the categories information, we'll get it later if
				// needed.
				pickQuery.setIncludeCategories(false);
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
					MemberCategoryList memberCategoryList = skipUseManager
							.getCategoryListForMember(getListeningCategoryMemberID());

					// see if SkipUse has the category for the member.
					String _foundSkipUseCategory = memberCategoryList.getCategoryList().stream()
							.filter(c -> c.equals(getSelectedCategory())).findFirst().orElse(null);

					List<String> categories = new ArrayList<String>();
					if (_foundSkipUseCategory != null) {
						categories.add(_foundSkipUseCategory);
					} else {
						// automatically add the category, if not in member's
						// list.
						skipUseManager.createCategoryForMember(getListeningCategoryMemberID(),
								getSelectedCategory());
						// NOTE: member does not have this category, so no song
						// will be returned anyway, don't add to categories
						// query list.
					}
					// look for this category.
					pickQuery.setCategories(categories);
				} else {
					// else, just search as normal, but let's mixing in some new
					// songs.
					pickQuery.setNewMixInPercentage(35);
				}

				// if this query is the same, no need to set it, just get it.
				if (pickQuery != lastPickQuery) {
					lastPickQuery = pickQuery;
					pickList = skipUseManager.setPickQuery(pickQuery);
				} else {
					pickList = skipUseManager.getPickQuery();
				}

				// get the song ID.
				if (pickList.size() > 0) {
					Pick pick = pickList.get(0);
					_songID = pick.getPickID();
				} else {
					System.out.println("No song ID was returned.");
				}
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
				isSkipUseError = true;
			}
		}

		if (_songID == null) {
			System.out.println(
					"Failed to get a song ID. You could add code to get a random song ID to return.");
		}

		// NOTE: you could pass back the Pick instead, if you wanted to get
		// fancy.
		return _songID;
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
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
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
				System.out.println(e.getMessage());
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
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
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
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
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
					System.out.println("unmark category: " + categoryName);
					markPickWithCategoryTrueFalse(_pick, categoryName, false);
				} else {
					// mark it.
					System.out.println("mark category: " + categoryName);
					markPickWithCategoryTrueFalse(_pick, categoryName, true);
				}
			} else {
				// create a Pick and mark it.
				System.out.println("create new pick and mark category: " + categoryName);
				markSongIDWithCategoryTrueFalse(memberID,
						PlayerStatus._getCurrentSongFile().getId() + "", categoryName, true);
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
				System.out.println(e.getMessage());
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
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
				isSkipUseError = true;
			}
		}
	}

	// Get song Pick information for a member by the song ID.
	// Returns null if not found.
	//
	public Pick _getPickByMemberIDAndSongID(int memberID, String songID) {
		Pick _currentPick = null;
		if (isSkipUseRunning() && songID != null) {
			try {
				_currentPick = skipUseManager._getPickByMemberIDAndPickID(memberID, "" + songID);
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
				isSkipUseError = true;
			}
		}
		return _currentPick;
	}

	// Update a Pick ID for a member.
	//
	public void skipUsePass(SkipUsePass skipUsePass, int memberID, String songID) {
		if (isSkipUseRunning()) {
			try {
				skipUseManager.skipUsePass(skipUsePass, memberID, songID);
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
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
			} catch (SkipUseException e) {
				System.out.println(e.getMessage());
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

	// Get the current listening members.
	//
	public List<Integer> getListeningMemberIDSet() {
		return getListeningMemberIDSet.stream().collect(Collectors.toList());
	}

	// Add a listening member by ID.
	//
	public void setListeningMemberID(int memberID) {
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
		if (isSkipUseError) {
			isSkipUseError = false;
			System.out.println("There was a SkipUse error, reseting and checking.");
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
					System.out.println(e.getMessage());
				}
			}
		}
		return isSkipUseRunning;
	}

	// Login to SkipUse.
	//
	private void skipUseLogin() {
		try {
			if (skipUseManager.isLoggedIn() == false) {
				System.out.println("not logged in... attempting to log in...");
				skipUseManager.login(SkipUseProperties.SKIP_USE_EMAIL,
						SkipUseProperties.SKIP_USE_PASSWORD);
			}
		} catch (SkipUseException e) {
			System.out.println(e.getMessage());
			isSkipUseError = true;
		}
	}

}
