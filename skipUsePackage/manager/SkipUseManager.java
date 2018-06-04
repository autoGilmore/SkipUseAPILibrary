package com.autogilmore.throwback.skipUsePackage.manager;

import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseAPIService;

/* 
 * This is a Singleton manager for common usage of the SkipUse API. 
 * This manager uses and abstracts the SkipUseAPIService.
 * Use THIS manager and NOT the SkipUseAPIService as it will keep the service SkipUseToken in sync with the server.
*/
public class SkipUseManager {
	// Set the SkipUseAPI URL here. See the API documentation for more
	// information.
	private static final String SKIP_USE_API_URL = "http://www.skipuseapi.com/v1";

	private SkipUseAPIService service = new SkipUseAPIService(SKIP_USE_API_URL);

	// Automatic log-in option.
	private final boolean IS_AUTOMATIC_LOGIN = true;
	private String autoLoginEmail = "";
	private String autoLoginPassword = "";

	// Stored server response data.
	private int myMemberID = -1;
	private ServerPickIDCollection serverPickIDCollection = new ServerPickIDCollection();
	private ServerMemberMap serverMemberMap = new ServerMemberMap();
	private ServerMemberCategoryList serverMemberCategoryList = new ServerMemberCategoryList();

	private static SkipUseManager instance;

	/**
	 * A private Constructor prevents any other class from instantiating.
	 */
	private SkipUseManager() {
	}

	/**
	 * The static initializer that constructs the instance of the class.
	 */
	static {
		instance = new SkipUseManager();
	}

	/** Get the Static 'instance' method */
	public static SkipUseManager getInstance() {
		return instance;
	}

	// Returns 'true' if the API server is found. Else, throws an error.
	//
	public boolean isAPIServerUp() throws SkipUseException {
		return service.checkServerConnection();
	}

	// Establishes a proxy ID and SkipUse token for communication and then logs
	// in the user.
	// Throws an error if unsuccessful.
	//
	public void login(String email, String password) throws SkipUseException {
		service.login(email, password);
		if (IS_AUTOMATIC_LOGIN) {
			this.autoLoginEmail = email;
			this.autoLoginPassword = password;
		}
	}

	// Returns 'true' if logged in or 'false' otherwise.
	//
	public boolean isLoggedIn() {
		return service.isLoggedIn();
	}

	// Logs out of the mircoservice.
	//
	public void logout() throws SkipUseException {
		service.logout();

		resetStoredServerData();
	}

	// Add Member by name for the Account.
	// Duplicate names are ignored.
	//
	public void addMemberName(String memberName) throws SkipUseException {
		if (getMemberIDByName(memberName) == -1) {
			MemberList memberList = new MemberList();
			memberList.addMemberName(memberName);
			serverMemberMap = service.addMemberList(memberList);
		}
	}

	// Returns the logged in user's member ID or -1 otherwise.
	//
	public int getOwnerMemberID() throws SkipUseException {
		if (myMemberID == -1) {
			automaticLogin();
			myMemberID = service.getMyMemberID();
		}
		return myMemberID;
	}

	// Get a Member's ID by their name.
	// Returns -1 if not found.
	// Case-sensitive names required. The account owner's member name will NOT
	// be in the Member Map (The owner is not a Member of their account.)
	//
	public int getMemberIDByName(String name) throws SkipUseException {
		if (name != null && !name.isEmpty()) {
			if (serverMemberMap.getMemberIDMap().size() == 0) {
				automaticLogin();
				serverMemberMap = service.getMemberMap();
			}
			if (serverMemberMap.getMemberIDMap().containsKey(name))
				return serverMemberMap.getMemberIDMap().get(name);
		}
		return -1;
	}

	public void updateMemberNameByID(int memberID, String beforeName, String afterName)
			throws SkipUseException {
		if (serverMemberMap.getMemberIDMap().containsKey(beforeName)
				&& serverMemberMap.getMemberIDMap().get(beforeName) == memberID) {
			automaticLogin();
			serverMemberMap = service.updateMemberNameByMemberID(memberID, beforeName, afterName);
		} else {
			throw new SkipUseException("Mismatch of Member name and ID or name not found.");
		}
	}

	// Delete a Member by their ID.
	// This will also delete their Picks and Categories.
	// You can recreate Member, but you cannot restore their data after being
	// deleted.
	//
	public void deleteMemberByID(int memberID) throws SkipUseException {
		if (serverMemberMap.getMemberIDMap().containsValue(memberID)) {
			automaticLogin();
			service.deleteMemberByID(memberID);
			serverMemberMap = new ServerMemberMap();
		}
	}

	// Stores a Pick ID collection.
	//
	public PickIDCollection addPickIDCollection(PickIDCollection pickIDCollection)
			throws SkipUseException {
		return addPickIDCollection(pickIDCollection.getCollectionName(),
				pickIDCollection.getPickIDList(), pickIDCollection.isSplitCSV());
	}

	// Stores a Pick ID collection.
	// Pass in a name for the collection and a list of Strings PickIDs.
	// Set the 'isSplitByComma' to 'true' if the list is comma-delimited.
	//
	public PickIDCollection addPickIDCollection(String collectionName, List<String> collectionList,
			boolean isSplitByComma) throws SkipUseException {
		serverPickIDCollection = new ServerPickIDCollection();
		automaticLogin();
		PickIDCollection pickIDCollection = new PickIDCollection(collectionName);
		pickIDCollection.setSplitCSV(isSplitByComma);
		pickIDCollection.setPickIDList(collectionList);
		serverPickIDCollection = service.setPickIDCollection(pickIDCollection);
		return serverPickIDCollection.getPickIDCollection();
	}

	// Get the Pick ID collection.
	//
	public PickIDCollection getPickIDCollection() throws SkipUseException {
		if (serverPickIDCollection.getPickIDCollection().getPickIDList().isEmpty()) {
			automaticLogin();
			serverPickIDCollection = service.getServerPickIDCollection();
		}
		return serverPickIDCollection.getPickIDCollection();
	}

	// Get all a member's Pick IDs from the set Pick ID collection.
	// If you want category information, un-comment out the query parameter in
	// the service function.
	//
	public List<Pick> getAllPickListByMemberID(int memberID) throws SkipUseException {
		ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID);
		return serverPickList.getPickList();
	}

	// Get a list of Picks from a PickQuery.
	//
	public List<Pick> getMemberPickListByPickQuery(PickQuery pickQuery) throws SkipUseException {
		automaticLogin();
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		return serverPickList.getPickList();
	}

	// Get a list of Picks from a previously set PickQuery.
	//
	public List<Pick> getMemberPickListByPickQuery() throws SkipUseException {
		automaticLogin();
		ServerPickList serverPickList = service.getServerPickList();
		return serverPickList.getPickList();
	}

	// Get a Pick by Member ID and the Pick ID.
	// Return null if error from not found
	//
	public Pick _getPickByMemberIDAndPickID(int memberID, String pickID) throws SkipUseException {
		automaticLogin();
		Pick _pick = null;
		try {
			ServerPickList serverPickList = service._getPickByMemberIDAndPickID(memberID, pickID);
			if (serverPickList.getPickList().size() == 1)
				_pick = serverPickList.getPickList().get(0);
		} catch (SkipUseException e) {
			// No pick stored error
		}
		return _pick;
	}

	// Create a category for a member.
	// Pass in the member's ID and the String name for the category.
	// Category names may not contain the comma ',' character.
	//
	public MemberCategoryList createCategoryForMember(int memberID, String categoryName)
			throws SkipUseException {
		automaticLogin();
		// load the category list if we don't have it yet
		if (serverMemberCategoryList.getMemberCategoryList().getCategoryList().isEmpty())
			getCategoryListForMember(memberID);
		// do we need to add it?
		if (!serverMemberCategoryList.getMemberCategoryList().getCategoryList()
				.contains(categoryName))
			serverMemberCategoryList = service.createCategoryByMemberID(memberID, categoryName);
		return serverMemberCategoryList.getMemberCategoryList();
	}

	// Returns a String List of the member's Categories.
	//
	public MemberCategoryList getCategoryListForMember(int memberID) throws SkipUseException {
		automaticLogin();
		serverMemberCategoryList = service.getCategoryListByMemberID(memberID);
		return serverMemberCategoryList.getMemberCategoryList();
	}

	// Returns a String List of the member's Categories.
	//
	public MemberCategoryList updateCategoryNameForMember(int memberID, PatchName patchName)
			throws SkipUseException {
		automaticLogin();
		serverMemberCategoryList = service.updateCategoryNameByMemberID(memberID,
				patchName.getBeforeName(), patchName.getAfterName());
		return serverMemberCategoryList.getMemberCategoryList();
	}

	// Delete Categories by passing in the member ID and a list of Categories.
	//
	public void deleteCategoryListForMember(MemberCategoryList memberCategoryList)
			throws SkipUseException {
		automaticLogin();
		service.deleteCategoryListByMemberCategoryList(memberCategoryList);
		serverMemberCategoryList = new ServerMemberCategoryList();
	}

	// Mark or Unmark a Pick with an existing category. Pass in the member ID,
	// pickID and category name. Set isMarkWithCategory to 'true' to mark the
	// Pick.
	//
	public void markPickIDListWithCategoryTrueFalse(int memberID, String pickID,
			String categoryName, boolean isMarkWithCategory) throws SkipUseException {
		PickIDCollection pickIDCollection = new PickIDCollection(
				getPickIDCollection().getCollectionName());
		pickIDCollection.addPickID(pickID);
		markPickIDListWithCategoryTrueFalse(memberID, pickIDCollection, categoryName,
				isMarkWithCategory);
	}

	// Mark or Unmark a list of Picks with an existing category.
	//
	public void markPickIDListWithCategoryTrueFalse(int memberID, PickIDCollection pickIDCollection,
			String categoryName, boolean isMarkWithCategory) throws SkipUseException {
		CategoryPickIDCollection categoryPickIDCollection = new CategoryPickIDCollection();
		List<String> categoryList = new ArrayList<>();
		categoryList.add(categoryName);
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
		categoryPickIDCollection.setPickIDCollection(pickIDCollection);
		automaticLogin();
		if (isMarkWithCategory) {
			service.markCategoryPickIDCollection(categoryPickIDCollection);
		} else {
			service.unmarkCategoryPickIDCollection(categoryPickIDCollection);
		}
	}

	// Update a Member's Pick ID 'isStopUsing' flag.
	//
	public void setStopUsingByMemberIDPickIDTrueFalse(int memberID, String pickID,
			boolean isStopUsing) throws SkipUseException {
		ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID);
		List<Pick> memberPickList = serverPickList.getPickList();
		Pick foundPick = memberPickList.stream().filter(p -> p.getMyPickID().equals(pickID))
				.findFirst().orElse(null);
		if (foundPick != null) {
			foundPick.setStopUsing(isStopUsing);
			service.updatePickByMemberID(memberID, foundPick);
		} else {
			throw new SkipUseException("The pickID: '" + pickID
					+ "' is not in the PickIDCollection. You might need to add it first.");
		}
	}

	// Update JSON and StopUsing Options for a Member's Pick
	//
	public void updatePickByMemberID(int memberID, Pick pick) throws SkipUseException {
		Pick newPick = new Pick();
		newPick.setMyJSON(pick.getMyJSON());
		newPick.setStopUsing(pick.isStopUsing());
		// NOTE: Other fields may not be changeable, check the API doc.
		service.updatePickByMemberID(memberID, pick);
	}

	// Skip, Use or Pass a Pick ID List for a member.
	//
	public void skipUsePass(SkipUsePass skipUsePass, int memberID, String pickID)
			throws SkipUseException {
		PickIDCollection collection = new PickIDCollection(
				getPickIDCollection().getCollectionName());
		collection.addPickID(pickID);
		List<Integer> memberIDList = new ArrayList<>();
		memberIDList.add(memberID);
		MemberPickIDList memberPickIDList = new MemberPickIDList(collection, memberIDList);
		skipUsePass(skipUsePass, memberPickIDList);
	}

	// Skip, Use or Pass a Pick ID List for a member.
	//
	public void skipUsePass(SkipUsePass skipUsePass, MemberPickIDList memberPickIDList)
			throws SkipUseException {
		automaticLogin();
		service.skipUsePassPick(skipUsePass, memberPickIDList);
	}

	// Helper to automatically log-in.
	//
	private void automaticLogin() throws SkipUseException {
		if (isLoggedIn() == false) {
			if (IS_AUTOMATIC_LOGIN) {
				login(autoLoginEmail, autoLoginPassword);
			} else {
				throw new SkipUseException("You need to login to the SkipUse microservice.");
			}
		}
		if (isLoggedIn() == false)
			throw new SkipUseException("Failed to log-in. Check your settings.");

		if (service.getRemainingDataNibbles() < 1)
			throw new SkipUseException("This account is out of data nibbles.");
	}

	// Reset stored server data
	//
	private void resetStoredServerData() {
		myMemberID = -1;
		serverPickIDCollection = new ServerPickIDCollection();
		serverMemberMap = new ServerMemberMap();
		serverMemberCategoryList = new ServerMemberCategoryList();
	}
}
