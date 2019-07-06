package com.autogilmore.throwback.skipUsePackage.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberNameList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberListPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseAPIService;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;

/* 
 * This is a Singleton and a manager for common usage of the SkipUse API microservice.
 * This manager uses and abstracts the SkipUseAPIService.
 * Use THIS manager and NOT the SkipUseAPIService as it will keep the service SkipUseToken in sync with the server.
*/
public class SkipUseManager {
    // Set the SkipUseAPI URL here. See the www.SkipUse.com API documentation
    // for more information.
    private static final String SKIP_USE_API_URL = SkipUseProperties.SKIP_USE_API_URL;

    // Using the API service.
    private final SkipUseAPIService service = new SkipUseAPIService(SKIP_USE_API_URL);

    // Automatic log-in option.
    private final boolean IS_AUTOMATIC_LOGIN = true;
    private String autoLoginEmail = "";
    private String autoLoginPassword = "";

    // Stored server response data.
    private long myMemberID = 0;
    private ServerMemberMap serverMemberMap = new ServerMemberMap();
    private Map<Long, ServerMemberCategoryList> memberIDCategoryListMap = new WeakHashMap<Long, ServerMemberCategoryList>();

    // SkipUse limits.
    // max Pick ID size.
    public static final int MAX_PICK_ID_LIST_SIZE = 10000;

    // Split Pick IDs in collection by comma + space
    public static final String PICKID_CSV_DELIMITER = ", ";

    // Manager singleton instance.
    private static SkipUseManager instance;

    /** A private Constructor prevents any other class from instantiating. */
    private SkipUseManager() {
    }

    /** The static initializer that constructs the instance of the class. */
    static {
	instance = new SkipUseManager();
    }

    /** Get the Static 'instance' method */
    public static SkipUseManager getInstance() {
	return instance;
    }

    // Return 'true' if the API server is found. Else, throws an error.
    //
    public boolean isAPIServerUp() {
	return service.isServerAPIUp();
    }

    // Establish a proxy ID and SkipUseToken with the server and then logs-in
    // the user.
    // Throws an error if unsuccessful.
    //
    public void login(String email, String password) throws SkipUseException {
	try {
	    // try to logout in order to reset session data if needed
	    logout();
	} catch (SkipUseException e) {
	    // ignore any errors
	}
	service.login(email, password);
	if (IS_AUTOMATIC_LOGIN) {
	    this.autoLoginEmail = email;
	    this.autoLoginPassword = password;
	}
    }

    // Return 'true' if user is logged in or 'false' if not.
    //
    public boolean isLoggedIn() {
	return service.isLoggedIn();
    }

    // Log out of SKipUse.
    //
    public void logout() throws SkipUseException {
	service.logout();
	resetStoredServerData();
    }

    // Add member by name for the account.
    // Duplicate names are ignored.
    //
    public void addMemberName(String memberName) throws SkipUseException {
	if (getMemberIDByName(memberName) == 0) {
	    MemberNameList memberList = new MemberNameList();
	    memberList.addMemberName(memberName);
	    automaticLogin();
	    serverMemberMap = service.addMemberList(memberList);
	}
    }

    // Return the logged in user's member ID or 0 otherwise.
    //
    public long getOwnerMemberID() throws SkipUseException {
	if (myMemberID == 0) {
	    automaticLogin();
	    myMemberID = service.getMyMemberID();
	}
	return myMemberID;
    }

    // Get a member's ID by their name.
    // Returns 0 if not found.
    // Case-sensitive names required. The account owner's member name will NOT
    // be in the Member Map (the owner is not a Member of their account) use
    // getOwnerMemberID instead.
    //
    public long getMemberIDByName(String name) throws SkipUseException {
	if (name != null && !name.isEmpty()) {
	    if (getServerMemberMap().getMemberIDMap().containsKey(name))
		return getServerMemberMap().getMemberIDMap().get(name);
	}
	return 0;
    }

    // Update a member's name.
    //
    public void updateMemberNameByID(long memberID, String beforeName, String afterName) throws SkipUseException {
	if (getServerMemberMap().getMemberIDMap().containsKey(beforeName)
		&& getServerMemberMap().getMemberIDMap().get(beforeName) == memberID) {
	    automaticLogin();
	    setServerMemberMap(service.updateMemberNameByMemberID(memberID, beforeName, afterName));
	} else {
	    throw new SkipUseException("Mismatch of member name and ID or name not found.");
	}
    }

    // Delete a member by their ID.
    // This will also deletes the member's Picks and categories.
    // NOTE: You can recreate member, but you cannot restore their data after
    // being deleted.
    //
    public void deleteMemberByID(long memberID) throws SkipUseException {
	if (getServerMemberMap().getMemberIDMap().containsValue(memberID)) {
	    automaticLogin();
	    service.deleteMemberByID(memberID);
	    // reset the member ID map so a reload is triggered.
	    setServerMemberMap(new ServerMemberMap());
	}
    }

    // Store a Pick ID collection using the PickIDCollection.
    //
    public MemberPickIDCollection addPickIDCollection(MemberPickIDCollection memberPickIDCollection)
	    throws SkipUseException {
	automaticLogin();
	return service.setPickIDCollection(memberPickIDCollection).getPickIDCollection();
    }

    // Store a Pick ID collection.
    // Pass in a name for the collection and a list of Pick IDs.
    // Set the 'isSplitByCommaPlusSpace' to 'true' if the list should comma +
    // space delimited and split into separate values.
    //
    public MemberPickIDCollection addPickIDCollection(List<String> collectionList, boolean isSplitByCommaPlusSpace)
	    throws SkipUseException {
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setSplitCSV(isSplitByCommaPlusSpace);
	memberPickIDCollection.setPickIDList(collectionList);
	return addPickIDCollection(memberPickIDCollection);
    }

    // Revert a collection with the previous change.
    //
    public void undoLastPickIDCollectionChange(long memberCollectionID) throws SkipUseException {
	automaticLogin();
	service.undoLastServerPickIDCollectionChange(memberCollectionID);
    }

    // Get the Pick ID collection.
    //
    public MemberPickIDCollection getPickIDCollection() throws SkipUseException {
	automaticLogin();
	ServerPickIDCollection serverPickIDCollection = service.getServerPickIDCollection(0);
	return serverPickIDCollection.getPickIDCollection();
    }

    // Get all of a member's Pick IDs from the set Pick ID collection.
    // NOTE: If you want category information, un-comment out the query
    // parameter in the getAllServerPickListByMemberID service function.
    //
    public List<Pick> getAllPickListByMemberID(long memberID, boolean includeCategoryInfo) throws SkipUseException {
	List<Long> memberIDList = new ArrayList<>();
	memberIDList.add(memberID);
	return getAllPickListByMemberIDList(memberIDList, includeCategoryInfo);
    }

    // Get all Picks for a list of member IDs.
    //
    public List<Pick> getAllPickListByMemberIDList(List<Long> memberIDList, boolean includeCategoryInfo)
	    throws SkipUseException {
	automaticLogin();
	ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberIDList, includeCategoryInfo);
	return serverPickList.getPickList();
    }

    // Set a Pick Query for future GET getPickQuery requests and
    // return a list of Picks.
    //
    public List<Pick> setPickQuery(PickQuery pickQuery) throws SkipUseException {
	automaticLogin();
	ServerPickList serverPickList = service.setPickQuery(pickQuery);
	return serverPickList.getPickList();
    }

    // Get a list of Picks for a session-stored Pick Query or get the default
    // Pick Query if not set.
    //
    public List<Pick> getPickQuery() throws SkipUseException {
	automaticLogin();
	ServerPickList serverPickList = service.getServerPickList();
	return serverPickList.getPickList();
    }

    // Get a Pick by member ID and the Pick ID and from a member's collection.
    // Return null if not found.
    //
    public Pick _getPickByMemberIDAndPickIDAndCollectionID(long memberID, String pickID, long fromMemberIDCollection)
	    throws SkipUseException {
	automaticLogin();
	return service._getPickByMemberIDAndPickIDAndCollectionID(memberID, pickID, fromMemberIDCollection);
    }

    // Create a category for a member.
    // Pass in the member's ID and the name for the category.
    // NOTE: Category names may not contain the comma ',' character.
    //
    public MemberCategoryList createCategoryForMember(long memberID, String categoryName) throws SkipUseException {
	automaticLogin();

	// do we have the category yet?
	MemberCategoryList memberCategoryList = getMemberCategoryList(memberID);

	// do we need to add it?
	if (!memberCategoryList.getCategoryList().contains(categoryName)) {
	    ServerMemberCategoryList serverMemberCategoryList = service.createCategoryByMemberID(memberID,
		    categoryName);
	    memberIDCategoryListMap.put(memberID, serverMemberCategoryList);
	}

	return getMemberCategoryList(memberID);
    }

    // Return a list of the member's categories.
    //
    public MemberCategoryList getCategoryListForMember(long memberID) throws SkipUseException {
	automaticLogin();
	return getMemberCategoryList(memberID);
    }

    // Update a category name for a member.
    // Return a list of the member's categories.
    //
    public MemberCategoryList updateCategoryNameForMember(long memberID, PatchName patchName) throws SkipUseException {
	automaticLogin();
	ServerMemberCategoryList serverMemberCategoryList = service.updateCategoryNameByMemberID(memberID,
		patchName.getBeforeName(), patchName.getAfterName());
	memberIDCategoryListMap.put(memberID, serverMemberCategoryList);
	return getMemberCategoryList(memberID);
    }

    // Delete member categories by passing in the member ID with a list of
    // categories.
    //
    public void deleteCategoryListForMember(MemberCategoryList memberCategoryList) throws SkipUseException {
	automaticLogin();
	service.deleteCategoryListByMemberCategoryList(memberCategoryList);
	memberIDCategoryListMap.remove(memberCategoryList.getMemberID());
	getMemberCategoryList(memberCategoryList.getMemberID());
    }

    // Mark or un-mark a Pick with an existing category. Pass in the member ID,
    // pickID and category name. Set 'isMarkWithCategory' to 'true' to mark the
    // Pick with the category.
    //
    public void markPickWithCategoryTrueFalse(Pick pick, String categoryName, boolean isMarkWithCategory)
	    throws SkipUseException {
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.addPickID(pick.getPickID());
	markPickIDListWithCategoryTrueFalse(pick.getMemberID(), memberPickIDCollection, categoryName,
		isMarkWithCategory);
    }

    // Mark or un-mark a Pick by ID for member.
    //
    public void markPickIDWithCategoryTrueFalse(long memberID, String pickID, String categoryName,
	    boolean isMarkWithCategory) throws SkipUseException {
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.addPickID(pickID);
	markPickIDListWithCategoryTrueFalse(memberID, memberPickIDCollection, categoryName, isMarkWithCategory);
    }

    // Mark or un-mark a collection of Pick IDs with a category.
    //
    public void markPickIDListWithCategoryTrueFalse(long memberID, MemberPickIDCollection memberPickIDCollection,
	    String categoryName, boolean isMarkWithCategory) throws SkipUseException {
	CategoryMemberPickIDCollection categoryPickIDCollection = new CategoryMemberPickIDCollection();
	List<String> categoryList = new ArrayList<String>();
	categoryList.add(categoryName);
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
	categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
	categoryPickIDCollection.setMemberPickIDCollection(memberPickIDCollection);
	automaticLogin();
	if (isMarkWithCategory) {
	    service.markCategoryPickIDCollection(categoryPickIDCollection);
	} else {
	    service.unmarkCategoryPickIDCollection(categoryPickIDCollection);
	}
    }

    // Update a member's Pick ID 'isStopUsing' flag.
    // NOTE: the 'isStopUsing = true' flag removes the Pick from normal Pick
    // Queries.
    //
    public void setStopUsingByMemberIDPickIDTrueFalse(long memberID, String pickID, boolean stopUsing)
	    throws SkipUseException {
	boolean includeCategoryInfo = false;
	ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID, includeCategoryInfo);
	Pick _foundPick = serverPickList.getPickList().stream().filter(p -> p.getPickID().equals(pickID)).findFirst()
		.orElse(null);

	// do we have the Pick to update? If not, create a new one and update it
	if (_foundPick == null) {
	    _foundPick = new Pick();
	    _foundPick.setPickID(pickID);
	}

	// if the pick is new, lets put our member ID on it.
	if (_foundPick.getMemberID() == 0)
	    _foundPick.setMemberID(memberID);
	_foundPick.setStopUsing(stopUsing);
	updateMemberPick(_foundPick);
    }

    // Update JSON and StopUsing options for a member's Pick
    //
    public void updateMemberPick(Pick pick) throws SkipUseException {
	if (pick != null) {
	    if (pick.getMemberID() > 0) {
		pick.setJSON(pick.getJSON());
		pick.setStopUsing(pick.isStopUsing());
		// NOTE: Other fields may not be changeable, check the API doc.
		service.updatePick(pick);
	    } else {
		throw new SkipUseException("The Pick member ID was invalid. It was: pick.getMemberID()");
	    }
	} else {
	    throw new SkipUseException("The Pick was null. You might need to add it first.");
	}
    }

    // Skip, Use or Pass a Pick ID for a member.
    //
    public void skipUsePass(SkipUsePass skipUsePass, long memberID, String pickID) throws SkipUseException {
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.addPickID(pickID);
	List<Long> memberIDList = new ArrayList<Long>();
	memberIDList.add(memberID);
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberPickIDCollection, memberIDList);
	skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
    }

    // Skip, Use or Pass a Pick ID List for a member.
    //
    public void skipUsePassMemberPickIDList(SkipUsePass skipUsePass, MemberListPickIDList memberPickIDList)
	    throws SkipUseException {
	automaticLogin();
	service.skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
    }

    // Skip, Use or Pass a Pick ID List for a member from another member's
    // collection.
    //
    public void skipUsePassPickList(SkipUsePass skipUsePass, List<Pick> pickList, long fromMemberIDCollection)
	    throws SkipUseException {
	automaticLogin();
	service.skipUsePassPickList(skipUsePass, pickList, fromMemberIDCollection);
    }

    public Profile getProfile() throws SkipUseException {
	automaticLogin();
	return service.getProfile();
    }

    public void updateProfile(Profile profile) throws SkipUseException {
	automaticLogin();
	service.updateProfile(profile);
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

    // Return the cached member category list. If empty, get and store it.
    //
    private MemberCategoryList getMemberCategoryList(long memberID) throws SkipUseException {
	if (!memberIDCategoryListMap.containsKey(memberID)
		|| memberIDCategoryListMap.get(memberID).getMemberCategoryList().getCategoryList().isEmpty()) {
	    ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);
	    memberIDCategoryListMap.put(memberID, serverMemberCategoryList);
	}
	return memberIDCategoryListMap.get(memberID).getMemberCategoryList();
    }

    // Reset stored server data.
    //
    private void resetStoredServerData() {
	myMemberID = 0;
	setServerMemberMap(new ServerMemberMap());
	memberIDCategoryListMap.clear();
    }

    private ServerMemberMap getServerMemberMap() throws SkipUseException {
	if (serverMemberMap.getMemberIDMap().size() == 0) {
	    automaticLogin();
	    setServerMemberMap(service.getMemberMap());
	}
	return serverMemberMap;
    }

    private void setServerMemberMap(ServerMemberMap serverMemberMap) {
	if (serverMemberMap != null)
	    this.serverMemberMap = serverMemberMap;
    }
}
