package com.autogilmore.throwback.skipUsePackage.service;

import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberListPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberNameList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCountAdvanceList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerProfile;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
import com.autogilmore.throwback.skipUsePackage.enums.AdvancedOption;
import com.autogilmore.throwback.skipUsePackage.enums.ResultOption;
import com.autogilmore.throwback.skipUsePackage.enums.SearchOption;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.service.api.SkipUseAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * A service for handling the API SkipUse server calls.
 * Intended to be used solely by the SkipUseManager. (The manager maintains
 * the SkipUseToken so that it will be in-sync with the server's token.)
*/
public class SkipUseAPIService extends SkipUseAPI {

    // Constructor
    //
    public SkipUseAPIService(String skipUseApiUrl) {
	super(skipUseApiUrl);
    }

    // Calling this will initiate a proxy and log in to the service.
    //
    public void login(String email, String password) throws SkipUseException {
	// start communication with the API server.
	initiateProxy();

	if (isLoggedIn() == false) {
	    JsonNode rootNode = mapper.createObjectNode();
	    ((ObjectNode) rootNode).put("email", email);
	    ((ObjectNode) rootNode).put("password", password);
	    // NOTE: validationCode is used for user sign-up and profile
	    // changes. It is not implemented in this example. Implement
	    // below if needed.
	    // ((ObjectNode) rootNode).put("validationCode",
	    // validationCode);

	    postAndProcess("/login", rootNode, ServerResponse.NAME);
	}
    }

    public void logout() throws SkipUseException {
	try {
	    postAndProcess("/logout", null, ServerResponse.NAME);
	} catch (SkipUseException e) {
	    // ignore token error: this is normal
	    if (!e.getMessage().contains("token was empty"))
		throw new SkipUseException(e.getMessage());
	}
    }

    // Considered to be logged-in when there is a ProxyID and an owner member ID
    // is present.
    //
    public boolean isLoggedIn() {
	if (serverResponseData.getOwnerID() == 0)
	    return false;
	
	if (isServerAPIUp() == false)
	    return false;

	if (isProxyValid() == false)
	    return false;

	return true;
    }

    // Get the message, if any, from the last API call.
    //
    public String getAPIMessage() {
	return serverResponseData.getMessage();
    }

    // Get the error message, if any, from the last API call.
    //
    public String getAPIErrorMessage() {
	return serverResponseData.getErrorMessage();
    }

    // Return the logged in user/owner's member ID.
    // NOTE: the owner ID is not changeable and it is recommended that you
    // create new member ID to represent the owner because IDs can be
    // used publicly.
    // NOTE: Get all member IDs with the getMemberMap() method.
    //
    public long getMyMemberID() {
	return serverResponseData.getOwnerID();
    }

    public Profile getProfile() throws SkipUseException {
	ServerProfile serverProfile = (ServerProfile) getAndProcess("/profile", ServerProfile.NAME);
	return serverProfile.getProfile();
    }

    public void updateProfile(Profile profile) throws SkipUseException {
	if (!profile.getEmail().isEmpty() && !profile.getPassword().isEmpty())
	    throw new SkipUseException("Not allowed to change both email and password at the same time");
	putAndProcess("/profile", profile, ServerResponse.NAME);
    }

    // A value of remaining Nibbles (representation of remaining data that can
    // be used) for the account.
    // returns 0 on error.
    //
    public long getRemainingDataNibbles() {
	try {
	    return Long.valueOf(serverResponseData.getRemainingNibbles());
	} catch (NumberFormatException nfe) {
	    return 0;
	}
    }

    // Set the collection of Pick IDs to be used by all members.
    // NOTE: 0 for memberMemberCollection parameter defaults to the owner's
    // Pick ID collection.
    //
    public ServerMemberCollection setMemberCollection(MemberCollection memberMemberCollection)
	    throws SkipUseException {
	return (ServerMemberCollection) postAndProcess("/collection", memberMemberCollection,
		ServerMemberCollection.NAME);
    }

    // Get a Pick ID Collection.
    // NOTE: 0 for memberMemberCollection parameter defaults to the owner's
    // Pick ID collection.
    //
    public ServerMemberCollection getServerMemberCollection(long memberCollectionID) throws SkipUseException {
	return (ServerMemberCollection) getAndProcess("/collection/" + memberCollectionID, ServerMemberCollection.NAME);
    }

    // Undo the last Pick ID Collection change.
    // NOTE: 0 for memberMemberCollection parameter defaults to the owner's
    // Pick ID collection.
    //
    public ServerMemberCollection undoLastServerMemberCollectionChange(long memberCollectionID)
	    throws SkipUseException {
	return (ServerMemberCollection) postAndProcess("/collection/" + memberCollectionID + " /undo", null,
		ServerMemberCollection.NAME);
    }

    // Set the query for getting Picks from the Pick ID collection (called a
    // 'Pick Query'.)
    // See the PickQuery class for required and optional settings.
    // NOTE: only one Pick Query is stored for the current session. See Access
    // Pass information on how to store PickQueries.
    //
    public ServerPickList setPickQuery(PickQuery pickQuery) throws SkipUseException {
	return (ServerPickList) postAndProcess("/query", pickQuery, ServerPickList.NAME);
    }

    // Get a Pick list from the server. A 'Pick' is a 'Pick ID' that contains
    // more information on that ID.
    // NOTE: a Pick Query must be set first or else it will return a default
    // search.
    //
    public ServerPickList getServerPickList() throws SkipUseException {
	return (ServerPickList) getAndProcess("/query", ServerPickList.NAME);
    }

    // Get all Picks for a member list.
    //
    public ServerPickList getAllServerPickListByMemberID(List<Long> memberIDList, boolean includeCategoryInfo)
	    throws SkipUseException {
	// get the maximum number of Picks... Defaults to the collection size
	PickQuery pickQuery = new PickQuery();
	// include all recently offered Picks...
	pickQuery.setExcludeRecentPicksHours(0);
	// include Picks marked as Stop Using...
	pickQuery.addToSearchOptionList(SearchOption.INCLUDE_STOP_USING);
	// set to 0% to not get new Picks...
	pickQuery.setNewMixInPercentage(0);
	// do not return back Picks if short.
	// for these members...
	pickQuery.setMemberIDList(memberIDList);
	// set the de-bug the Pick Query results if desired
	pickQuery.addToAdvancedOptionList(AdvancedOption.DEBUG_QUERY);
	// If set to 'true' this query could take
	// longer and might cost more to use.
	if (includeCategoryInfo)
	    pickQuery.addToResultOptionList(ResultOption.INCLUDE_CATEGORY_INFO);
	return setPickQuery(pickQuery);
    }

    // Get all Picks for a member.
    //
    public ServerPickList getAllServerPickListByMemberID(long memberID, boolean includeCategoryInfo)
	    throws SkipUseException {
	List<Long> memberIDList = new ArrayList<>();
	memberIDList.add(memberID);
	return getAllServerPickListByMemberID(memberIDList, includeCategoryInfo);
    }

    // Get Pick for a member by Pick ID.
    // Returns null if not found.
    //
    public Pick _getPickByMemberIDAndPickIDAndCollectionID(long memberID, String pickID, long fromMemberIDCollection)
	    throws SkipUseException {
	List<String> pickList = new ArrayList<String>();
	pickList.add(pickID);
	Pick _pick = null;
	ServerPickList serverPickList = getServerPickListByMemberIDAndPickList(memberID, pickList,
		fromMemberIDCollection);
	if (serverPickList.getPickList().size() == 1)
	    _pick = serverPickList.getPickList().get(0);
	return _pick;
    }

    // Get Pick/s from a list of members from a single member's collection.
    //
    public ServerPickList getServerPickListByMemberIDAndPickList(long memberID, List<String> pickIDList,
	    long fromMemberIDCollection) throws SkipUseException {
	List<Long> memberIDList = new ArrayList<Long>();
	memberIDList.add(memberID);
	MemberCollection memberMemberCollection = new MemberCollection(fromMemberIDCollection);
	memberMemberCollection.setPickIDList(pickIDList);
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberMemberCollection, memberIDList);
	return (ServerPickList) postAndProcess("/pick", memberPickIDList, ServerPickList.NAME);
    }

    // Update a member's Pick.
    // NOTE: updating a list of Picks for a member could be performed too.
    //
    public void updatePick(Pick pick) throws SkipUseException {
	PickList pickList = new PickList();
	pickList.add(pick);
	putAndProcess("/pick", pickList, ServerResponse.NAME);
    }

    // Skip, Use or Pass Pick IDs by members.
    // See the API documentation for what each version does to a Pick ID.
    //
    public void skipUsePassMemberPickIDList(SkipUsePass skipUsePass, MemberListPickIDList memberPickIDList)
	    throws SkipUseException {
	postAndProcess("/" + skipUsePass.toString().toLowerCase(), memberPickIDList, ServerResponse.NAME);
    }

    // Skip, Use or Pass Pick ID by member ID.
    //
    public void skipUsePassPickID(SkipUsePass skipUsePass, long memberID, String pickID, long fromMemberIDCollection)
	    throws SkipUseException {
	List<Long> memberIDList = new ArrayList<>();
	memberIDList.add(memberID);
	MemberCollection memberMemberCollection = new MemberCollection(fromMemberIDCollection);
	memberMemberCollection.addPickID(pickID);
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberMemberCollection, memberIDList);
	skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
    }

    // Skip, Use or Pass a Pick.
    // NOTE: the Pick must have a member ID > 0.
    //
    public void skipUsePassPick(SkipUsePass skipUsePass, Pick pick, long fromMemberIDCollection)
	    throws SkipUseException {
	List<Pick> pickList = new ArrayList<>();
	pickList.add(pick);
	skipUsePassPickList(skipUsePass, pickList, fromMemberIDCollection);
    }

    // Skip, Use or Pass Picks.
    // NOTE: Picks must have a member ID > 0.
    // NOTE: it is possible for multiple members too. (not implemented here)
    //
    public void skipUsePassPickList(SkipUsePass skipUsePass, List<Pick> pickList, long fromMemberIDCollection)
	    throws SkipUseException {
	List<Long> memberIDList = new ArrayList<>();
	List<String> pickIDList = new ArrayList<>();
	for (Pick pick : pickList) {
	    memberIDList.add(pick.getMemberID());
	    pickIDList.add(pick.getPickID());
	}
	MemberCollection memberMemberCollection = new MemberCollection(fromMemberIDCollection);
	memberMemberCollection.setPickIDList(pickIDList);
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberMemberCollection, memberIDList);
	skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
    }

    // Update Pick ID Skip and Use counts for members.
    //
    public void pickIDCountAdvance(PickIDCountAdvanceList pickIDCountUpdates) throws SkipUseException {
	postAndProcess("/picks/advance", pickIDCountUpdates, ServerResponse.NAME);
    }

    // Add members to an account.
    //
    public ServerMemberMap addMemberList(MemberNameList memberList) throws SkipUseException {
	return (ServerMemberMap) postAndProcess("/member", memberList, ServerMemberMap.NAME);
    }

    // Get names and ID of the current members.
    //
    public ServerMemberMap getMemberMap() throws SkipUseException {
	return (ServerMemberMap) getAndProcess("/member", ServerMemberMap.NAME);
    }

    // Update a member name by their ID, current and new name.
    //
    public ServerMemberMap updateMemberNameByMemberID(long memberID, String before, String after)
	    throws SkipUseException {
	PatchName patchName = new PatchName(before, after);
	return (ServerMemberMap) patchAndProcess("/member/" + memberID, patchName, ServerMemberMap.NAME);
    }

    // Delete a member by ID.
    //
    public void deleteMemberByID(long memberID) throws SkipUseException {
	deleteAndProcess("/member/" + memberID, null, ServerResponse.NAME);
    }

    // Create a category for a member.
    //
    public ServerMemberCategoryList createCategoryByMemberID(long memberID, String categoryName)
	    throws SkipUseException {
	List<String> categoryNameList = new ArrayList<>();
	categoryNameList.add(categoryName);
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryNameList);
	return createCategoryByMemberID(memberCategoryList);
    }

    // Create member categories by list.
    //
    public ServerMemberCategoryList createCategoryByMemberID(MemberCategoryList categoryNameList)
	    throws SkipUseException {
	if (categoryNameList.getMemberID() <= 0 || categoryNameList.getCategoryList().isEmpty())
	    throw new SkipUseException("There is a problem with a parameter for creating a Category for a Member.");

	ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
	memberCategoryList = (ServerMemberCategoryList) postAndProcess("/category", categoryNameList,
		ServerMemberCategoryList.NAME);
	return memberCategoryList;
    }

    // Return a List of categories created by a member.
    //
    public ServerMemberCategoryList getCategoryListByMemberID(long memberID) throws SkipUseException {
	ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
	memberCategoryList = (ServerMemberCategoryList) getAndProcess("/memberid/" + memberID + "/category",
		ServerMemberCategoryList.NAME);
	return memberCategoryList;
    }

    // Update a category name for a member.
    //
    public ServerMemberCategoryList updateCategoryNameByMemberID(long memberID, String oldCategoryName,
	    String newCategoryName) throws SkipUseException {
	PatchName patchName = new PatchName(oldCategoryName, newCategoryName);
	ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
	memberCategoryList = (ServerMemberCategoryList) patchAndProcess("/memberid/" + memberID + "/category",
		patchName, ServerMemberCategoryList.NAME);
	return memberCategoryList;
    }

    // Delete a category List for a member.
    //
    public void deleteCategoryListByMemberCategoryList(MemberCategoryList memberCategoryList) throws SkipUseException {
	deleteAndProcess("/memberid/" + memberCategoryList.getMemberID() + "/category", memberCategoryList,
		ServerResponse.NAME);
    }

    // Mark Pick IDs with categories by a member.
    //
    public void markCategoryMemberCollection(CategoryMemberCollection categoryMemberCollection)
	    throws SkipUseException {
	postAndProcess("/mark", categoryMemberCollection, ServerResponse.NAME);
    }

    // Un-mark Pick IDs with categories by a member.
    //
    public void unmarkCategoryMemberCollection(CategoryMemberCollection categoryMemberCollection)
	    throws SkipUseException {
	postAndProcess("/unmark", categoryMemberCollection, ServerResponse.NAME);
    }

    // Request a test error message from the server.
    //
    public void errorTest() {
	try {
	    postAndProcess("/errortest", null, ServerMemberCollection.NAME);
	} catch (SkipUseException e) {
	    // ignore
	    e.printStackTrace();
	}
    }

}
