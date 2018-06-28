package com.autogilmore.throwback.skipUsePackage.service;

import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
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

	// Calling this will initiates a proxy and or log in to the service.
	//
	public void login(String email, String password) throws SkipUseException {
		if (getLastServerResponseData().getProxyID().isEmpty())
			initiateProxy();

		if (isLoggedIn() == false) {
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("email", email);
			((ObjectNode) rootNode).put("password", password);
			// NOTE: validationCode is used for user sign-up and profile
			// changes. It is not implemented in this example. Implement below
			// if needed.
			// ((ObjectNode) rootNode).put("validationCode", validationCode);

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
		if (serverResponseData.getMemberID() < 0) {
			return false;
		} else if (!getLastServerResponseData().getProxyID().isEmpty()) {
			return true;
		}
		return false;
	}

	// Return the logged in user/owner's member ID.
	// NOTE: the owner ID is not changeable and it is recommended that you
	// create new member ID to represent the owner because IDs can be
	// used publicly.
	// NOTE: Get all member IDs with the getMemberMap() method.
	//
	public int getMyMemberID() {
		return serverResponseData.getMemberID();
	}

	// Set the collection of Pick IDs to be used by all members.
	//
	public ServerPickIDCollection setPickIDCollection(PickIDCollection pickIDCollection)
			throws SkipUseException {
		return (ServerPickIDCollection) postAndProcess("/collection", pickIDCollection,
				ServerPickIDCollection.NAME);
	}

	// A value of remaining Nibbles (representation of remaining data that can
	// be used) for the account.
	// returns -1 on error.
	//
	public long getRemainingDataNibbles() {
		try {
			return Long.valueOf(serverResponseData.getRemainingNibbles());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	// Get a Pick ID Collection.
	// NOTE: depending on your subscription level or current version, this may
	// be limited to one collection per account.
	//
	public ServerPickIDCollection getServerPickIDCollection() throws SkipUseException {
		return (ServerPickIDCollection) getAndProcess("/collection", ServerPickIDCollection.NAME);
	}

	// Set the query for getting Picks from the Pick ID collection (called a
	// 'PickQuery'.)
	// See the PickQuery class for required and optional settings.
	// NOTE: only one PickQuery is stored for the current session. See Access
	// Pass information on how to store PickQueries.
	//
	public ServerPickList setPickQuery(PickQuery pickQuery) throws SkipUseException {
		return (ServerPickList) postAndProcess("/query", pickQuery, ServerPickList.NAME);
	}

	// Get a Pick list from the server. A 'Pick' is a 'Pick ID' that contains
	// more information on that ID.
	// NOTE: a PickQuery must be set first or else it will return a default
	// search.
	//
	public ServerPickList getServerPickList() throws SkipUseException {
		return (ServerPickList) getAndProcess("/query", ServerPickList.NAME);
	}

	// Get all Picks for a member.
	//
	public ServerPickList getAllServerPickListByMemberID(int memberID) throws SkipUseException {
		// get the maximum number of Picks...
		PickQuery pickQuery = new PickQuery(5000);
		// include all recently offered Picks...
		pickQuery.setExcludeRecentPicks(false);
		// include Picks marked as Stop Using...
		pickQuery.setIncludeStopUsing(true);
		// set to 0% new Picks...
		pickQuery.setNewMixInPercentage(0);
		// return back Picks that may have not been used yet...
		pickQuery.setGetMorePicksIfShort(true);
		// for this member...
		pickQuery.addToMemberIDList(memberID);
		// un-comment below to add the category data too. This query could take
		// longer and might cost more to use.
		// pickQuery.setIncludeCategories(true);
		return setPickQuery(pickQuery);
	}

	// Get Pick for a member by Pick ID.
	// Returns null if not found.
	//
	public Pick _getPickByMemberIDAndPickID(int memberID, String pickID) throws SkipUseException {
		List<String> pickList = new ArrayList<>();
		pickList.add(pickID);
		Pick _pick = null;
		ServerPickList serverPickList = getServerPickListByMemberIDAndPickList(memberID, pickList);
		if (serverPickList.getPickList().size() == 1)
			_pick = serverPickList.getPickList().get(0);
		return _pick;
	}

	// Get Pick/s for a member.
	//
	public ServerPickList getServerPickListByMemberIDAndPickList(int memberID,
			List<String> pickIDList) throws SkipUseException {
		MemberPickIDList memberPickIDList = new MemberPickIDList();
		List<Integer> memberIDList = new ArrayList<>(memberID);
		memberIDList.add(memberID);
		memberPickIDList.setMemberIDList(memberIDList);
		memberPickIDList.setPickIDList(pickIDList);
		return (ServerPickList) postAndProcess("/pick", memberPickIDList, ServerPickList.NAME);
	}

	// Update a member Pick.
	// NOTE: updating a list of Picks for a member could be performed too.
	//
	public void updateMemberPick(Pick pick) throws SkipUseException {
		MemberPickList memberPickList = new MemberPickList();
		memberPickList.setMemberID(pick.getMemberID());
		List<Pick> pickList = new ArrayList<>();
		pickList.add(pick);
		memberPickList.setPickList(pickList);
		patchAndProcess("/pick", memberPickList, ServerResponse.NAME);
	}

	// Skip, Use or Pass Pick IDs by members.
	// See the API documentation for what each version does to a Pick ID.
	//
	public void skipUsePassMemberPickIDList(SkipUsePass skipUsePass,
			MemberPickIDList memberPickIDList) throws SkipUseException {
		postAndProcess("/" + skipUsePass.toString().toLowerCase(), memberPickIDList,
				ServerResponse.NAME);
	}

	// Skip, Use or Pass Pick ID by member ID.
	//
	public void skipUsePassPickID(SkipUsePass skipUsePass, int memberID, String pickID)
			throws SkipUseException {
		List<Integer> memberIDList = new ArrayList<>();
		memberIDList.add(memberID);
		PickIDCollection collection = new PickIDCollection();
		collection.addPickID(pickID);
		MemberPickIDList memberPickIDList = new MemberPickIDList(collection, memberIDList);
		skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
	}

	// Skip, Use or Pass a Pick.
	// NOTE: the Pick must have a member ID > 0.
	//
	public void skipUsePassPick(SkipUsePass skipUsePass, Pick pick) throws SkipUseException {
		List<Pick> pickList = new ArrayList<>();
		pickList.add(pick);
		skipUsePassPickList(skipUsePass, pickList);
	}

	// Skip, Use or Pass Picks.
	// NOTE: Picks must have a member ID > 0.
	// NOTE: it is possible for multiple members too. (not implemented here)
	//
	public void skipUsePassPickList(SkipUsePass skipUsePass, List<Pick> pickList)
			throws SkipUseException {
		MemberPickIDList memberPickIDList = new MemberPickIDList();
		List<Integer> memberIDList = new ArrayList<>();
		List<String> pickIDList = new ArrayList<>();
		for (Pick pick : pickList) {
			memberIDList.add(pick.getMemberID());
			pickIDList.add(pick.getPickID());
		}
		memberPickIDList.setMemberIDList(memberIDList);
		memberPickIDList.setPickIDList(pickIDList);
		skipUsePassMemberPickIDList(skipUsePass, memberPickIDList);
	}

	// Add members to an account.
	//
	public ServerMemberMap addMemberList(MemberList memberList) throws SkipUseException {
		return (ServerMemberMap) postAndProcess("/member", memberList, ServerMemberMap.NAME);
	}

	// Get names and ID of the current members.
	//
	public ServerMemberMap getMemberMap() throws SkipUseException {
		return (ServerMemberMap) getAndProcess("/member", ServerMemberMap.NAME);
	}

	// Update a member name by their ID, current and new name.
	//
	public ServerMemberMap updateMemberNameByMemberID(int memberID, String before, String after)
			throws SkipUseException {
		PatchName patchName = new PatchName(before, after);
		return (ServerMemberMap) patchAndProcess("/member/" + memberID, patchName,
				ServerMemberMap.NAME);
	}

	// Delete a member by ID.
	//
	public void deleteMemberByID(int memberID) throws SkipUseException {
		deleteAndProcess("/member/" + memberID, null, ServerResponse.NAME);
	}

	// Create a category for a member.
	//
	public ServerMemberCategoryList createCategoryByMemberID(int memberID, String categoryName)
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
			throw new SkipUseException(
					"There is a problem with a parameter for creating a Category for a Member.");

		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) postAndProcess("/category",
				categoryNameList, ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Return a List of categories created by a member.
	//
	public ServerMemberCategoryList getCategoryListByMemberID(int memberID)
			throws SkipUseException {
		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) getAndProcess(
				"/memberid/" + memberID + "/category", ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Update a category name for a member.
	//
	public ServerMemberCategoryList updateCategoryNameByMemberID(int memberID,
			String oldCategoryName, String newCategoryName) throws SkipUseException {
		PatchName patchName = new PatchName(oldCategoryName, newCategoryName);
		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) patchAndProcess(
				"/memberid/" + memberID + "/category", patchName, ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Delete a category List for a member.
	//
	public void deleteCategoryListByMemberCategoryList(MemberCategoryList memberCategoryList)
			throws SkipUseException {
		deleteAndProcess("/memberid/" + memberCategoryList.getMemberID() + "/category",
				memberCategoryList, ServerResponse.NAME);
	}

	// Mark Pick IDs with categories by a member.
	//
	public void markCategoryPickIDCollection(CategoryPickIDCollection categoryPickIDCollection)
			throws SkipUseException {
		postAndProcess("/mark", categoryPickIDCollection, ServerResponse.NAME);
	}

	// Un-mark Pick IDs with categories by a member.
	//
	public void unmarkCategoryPickIDCollection(CategoryPickIDCollection categoryPickIDCollection)
			throws SkipUseException {
		postAndProcess("/unmark", categoryPickIDCollection, ServerResponse.NAME);
	}

	// Request a test error message from the server.
	//
	public void errorTest() {
		try {
			postAndProcess("/errortest", null, ServerPickIDCollection.NAME);
		} catch (SkipUseException e) {
			// ignore
			e.printStackTrace();
		}
	}

}
