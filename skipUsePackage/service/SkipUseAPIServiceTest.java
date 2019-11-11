package com.autogilmore.throwback.skipUsePackage.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberListPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberNameList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;

public class SkipUseAPIServiceTest {
    // NOTE: Set the SkipUseAPI URL here. See API documentation for more
    // information.
    private final String SKIP_USE_API_URL = SkipUseProperties.SKIP_USE_API_URL;

    // Service under test
    private SkipUseAPIService service = new SkipUseAPIService(SKIP_USE_API_URL);

    // NOTE: Set these to use your own test credentials as the demo account is
    // unstable from other people's usage.
    private static final String TEST_EMAIL = SkipUseProperties.TEST_SKIP_USE_EMAIL;
    private static final String TEST_PASSWORD = SkipUseProperties.TEST_SKIP_USE_PASSWORD;

    // NOTE: These tests will create a temporary test members so that the owner
    // and other members won't get test Picks added in their history. Change
    // this name if it conflicts one of your current member's name.
    private static final String TEST_MEMBER_BOB = "Bob";
    private static final String TEST_MEMBER_SUE = "Sue";

    private static final boolean INCLUDE_CATEGORY_INFO = false;

    // After each test, remove the test member.
    @After
    public void after() {
	try {
	    SkipUseManager manager = SkipUseManager.getInstance();
	    long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
	    if (testMemberID != 0)
		service.deleteMemberByID(testMemberID);
	    testMemberID = manager.getMemberIDByName(TEST_MEMBER_SUE);
	    if (testMemberID != 0)
		service.deleteMemberByID(testMemberID);
	    service.logout();
	} catch (SkipUseException e) {
	    // noop (no operation)
	}
    }

    @Test
    public void test_checkServerConnection() {
	// Set up
	assertTrue("Should have a connection", service.isServerAPIUp());
	// use wrong API URL
	service = new SkipUseAPIService("http://skipuseapi/v-hee-hee");

	// Test
	// Verify
	assertFalse("Should NOT have a connection", service.isServerAPIUp());
    }

    @Test
    public void test_login() throws SkipUseException {
	// Set up
	assertFalse(service.isLoggedIn());

	// Test
	service.login(TEST_EMAIL, TEST_PASSWORD);

	// Verify
	assertTrue(service.isLoggedIn());
    }

    @Test
    public void test_login_badEmail() throws SkipUseException {
	// Set up
	assertFalse(service.isLoggedIn());

	// Test
	try {
	    service.login("Zoyinky", TEST_PASSWORD);
	    fail("An error should be thrown");
	} catch (SkipUseException e) {
	    // Verify
	    assertTrue("was: " + e.getMessage(), e.getMessage().contains("email address"));
	}
    }

    @Test
    public void test_login_badLogin() throws SkipUseException {
	// Test
	try {
	    service.login("not@registered.com", TEST_PASSWORD);
	    fail("An error should be thrown");
	} catch (SkipUseException e) {
	    // Verify
	    assertTrue("was: " + e.getMessage(), e.getMessage().contains("ERROR CODE UM-"));
	}
    }

    @Test
    public void test_logout() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// Test
	service.logout();

	// Verify
	assertFalse(service.isLoggedIn());
    }

    @Test
    public void test_getProfile() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// Test
	Profile profile = service.getProfile();

	// Verify

	// if this is failing, try running the update Profile test first
	assertTrue(profile.getOwnerName().equals(SkipUseProperties.OWNER_NAME));
	assertTrue(profile.getEmail().equals(SkipUseProperties.TEST_SKIP_USE_EMAIL));
	assertTrue(profile.getPassword().isEmpty());
    }

    @Test
    public void test_updateProfile() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());
	Profile profile = service.getProfile();

	profile.setOwnerName(SkipUseProperties.OWNER_NAME);

	// Test
	service.updateProfile(profile);
	profile = service.getProfile();

	// Verify
	assertTrue("was: " + profile.getOwnerName(), profile.getOwnerName().equals(SkipUseProperties.OWNER_NAME));
	assertTrue("was: " + profile.getEmail(), profile.getEmail().equals(SkipUseProperties.TEST_SKIP_USE_EMAIL));
	assertTrue(profile.getPassword().isEmpty());
    }

    @Test
    public void test_setPickIDCollection() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	String collectionName = "My collection";
	List<String> collectionPickIDList = new ArrayList<>();
	collectionPickIDList.add("A");
	collectionPickIDList.add("B");
	collectionPickIDList.add("C");
	// ignore duplicate
	collectionPickIDList.add("C");
	assertTrue(collectionPickIDList.size() == 4);

	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName(collectionName);
	memberPickIDCollection.setPickIDList(collectionPickIDList);
	memberPickIDCollection.setSplitCSV(false);

	// Test
	service.setPickIDCollection(memberPickIDCollection);

	// Verify
	ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection(0);
	assertNotNull(foundServerCollection);
	List<String> foundCollectionList = foundServerCollection.getPickIDCollection().getPickIDList();
	assertNotNull(foundCollectionList);
	// should not include duplicate
	assertTrue("was: " + foundCollectionList.size(), foundCollectionList.size() == 3);
    }

    @Test
    public void test_setPickIDCollection_splitCommaPlusSpace() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	String collectionName = "My collection";
	List<String> collectionList = new ArrayList<>();
	collectionList.add("A, B, C");
	collectionList.add("D,E,F");

	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName(collectionName);
	memberPickIDCollection.setPickIDList(collectionList);
	// split
	memberPickIDCollection.setSplitCSV(true);

	// Test
	service.setPickIDCollection(memberPickIDCollection);

	// Verify
	ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection(0);
	assertNotNull(foundServerCollection);
	List<String> foundCollectionList = foundServerCollection.getPickIDCollection().getPickIDList();
	assertNotNull(foundCollectionList);
	assertTrue(foundCollectionList.size() == 4);
	assertTrue(foundCollectionList.contains("A"));
	assertTrue(foundCollectionList.contains("B"));
	assertTrue(foundCollectionList.contains("C"));
	assertTrue(foundCollectionList.contains("D,E,F"));
    }

    @Test
    public void test_getServerPickIDCollection() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	String collectionName = "My collection";
	List<String> collectionList = new ArrayList<>();
	collectionList.add("A");
	collectionList.add("B");
	collectionList.add("C");

	MemberPickIDCollection pickCollection = new MemberPickIDCollection();
	pickCollection.setCollectionName(collectionName);
	pickCollection.setPickIDList(collectionList);
	pickCollection.setSplitCSV(false);
	service.setPickIDCollection(pickCollection);

	// Test
	ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection(0);

	// Verify
	assertNotNull(foundServerCollection);
	List<String> foundCollectionList = foundServerCollection.getPickIDCollection().getPickIDList();
	assertNotNull(foundCollectionList);
	assertTrue(foundCollectionList.size() == 3);
    }

    @Test
    public void test_setPickQuery() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	String collectionName = "My collection";
	List<String> collectionList = new ArrayList<>();
	collectionList.add("A, B, C");

	MemberPickIDCollection pickCollection = new MemberPickIDCollection();
	pickCollection.setCollectionName(collectionName);
	pickCollection.setPickIDList(collectionList);
	pickCollection.setSplitCSV(true);
	service.setPickIDCollection(pickCollection);

	PickQuery pickQuery = new PickQuery();
	pickQuery.setHowMany(2);
	pickQuery.setGetMorePicksIfShort(true);

	// Test
	ServerPickList serverPickList = service.setPickQuery(pickQuery);

	// Verify
	assertNotNull(serverPickList);
	List<Pick> pickList = serverPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() == 2);
    }

    @Test
    public void test_getServerPickList() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// create a member
	long bobMemberID = addTestMember(TEST_MEMBER_BOB);
	// test Pick ID
	String pickID = "209456830495603849003948560934";
	// set collection
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(bobMemberID);
	memberPickIDCollection.setCollectionName("test collection");
	memberPickIDCollection.addPickID(pickID);
	service.setPickIDCollection(memberPickIDCollection);
	// store the pick
	service.skipUsePassPickID(SkipUsePass.PASS, bobMemberID, pickID, bobMemberID);
	// set a Pick Query
	PickQuery pickQuery = new PickQuery();
	pickQuery.addToMemberIDList(bobMemberID);
	pickQuery.setMemberCollectionID(bobMemberID);
	pickQuery.setExcludeRecentPicksHours(0);
	pickQuery.makeExactQuery();
	service.setPickQuery(pickQuery);

	// Test
	ServerPickList serverPickList = service.getServerPickList();

	// Verify
	assertNotNull(serverPickList);
	List<Pick> pickList = serverPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() > 0);
    }

    @Test
    public void test_getAllServerPickListByMemberID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);

	// Test
	ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID, INCLUDE_CATEGORY_INFO);

	// Verify
	assertNotNull(serverPickList);
    }

    @Test
    public void test__getPickByMemberIDAndPickIDAndCollectionID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// create a member
	long bobMemberID = addTestMember(TEST_MEMBER_BOB);

	// test Pick ID
	String pickID = "<a href=\"http://www.skipuse.com\" target=\"_blank\" title=\"SkipUse Home Page\">SkipUse</a>";

	// set collection
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName("test collection");
	memberPickIDCollection.addPickID(pickID);
	service.setPickIDCollection(memberPickIDCollection);

	// Test: pick not stored yet
	Pick _foundPick = service._getPickByMemberIDAndPickIDAndCollectionID(bobMemberID, pickID, bobMemberID);

	// Verify
	assertTrue(_foundPick == null);

	// store the pick
	service.skipUsePassPickID(SkipUsePass.PASS, bobMemberID, pickID, bobMemberID);

	// Test: pick is now found
	_foundPick = service._getPickByMemberIDAndPickIDAndCollectionID(bobMemberID, pickID, bobMemberID);
	assertNotNull(_foundPick);
	assertTrue(_foundPick.getPickID().equals(pickID));

	// Clean-up
	service.deleteMemberByID(bobMemberID);
    }

    @Test
    public void test_getServerPickListByMemberIDAndPickList() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);

	String collectionName = "My collection";
	List<String> collectionList = new ArrayList<>();
	Date date = new Date();
	collectionList.add("A" + date.toString());
	collectionList.add("B" + date.toString());
	collectionList.add("C" + date.toString());
	collectionList.add("D" + date.toString());

	MemberPickIDCollection pickCollection = new MemberPickIDCollection();
	pickCollection.setCollectionName(collectionName);
	pickCollection.setPickIDList(collectionList);
	pickCollection.setSplitCSV(true);
	service.setPickIDCollection(pickCollection);
	List<String> foundCollectionList = service.getServerPickIDCollection(0).getPickIDCollection().getPickIDList();
	assertTrue("was: " + foundCollectionList.size(), foundCollectionList.size() == 4);

	// Test: empty
	ServerPickList serverPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList,
		memberID);

	// Verify
	assertNotNull(serverPickList);
	List<Pick> pickList = serverPickList.getPickList();
	assertTrue("Should not have any store Picks yet. was: " + pickList.size(), pickList.size() == 0);

	// add picks
	List<Long> memberIDList = new ArrayList<>();
	memberIDList.add(memberID);
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(pickCollection, memberIDList);
	service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);

	// Test: w/Picks
	serverPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList, memberID);

	// Verify
	assertNotNull(serverPickList);
	pickList = serverPickList.getPickList();
	assertTrue("Should have all the store Picks. was: " + pickList.size(),
		pickList.size() == collectionList.size());

    }

    @Test
    public void test_skipUsePassPick() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// test Pick
	String testPickID = "1";

	// test member
	long bobMemberID = addTestMember(TEST_MEMBER_BOB);

	// set collection
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName("test collection");
	memberPickIDCollection.addPickID(testPickID);
	service.setPickIDCollection(memberPickIDCollection);

	// create a pick (if needed) by using the Pass
	MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberPickIDCollection);
	memberPickIDList.addMemberID(bobMemberID);
	service.skipUsePassMemberPickIDList(SkipUsePass.PASS, memberPickIDList);

	PickQuery pickQuery = new PickQuery();
	pickQuery.addToMemberIDList(bobMemberID);
	pickQuery.setExcludeRecentPicksHours(0);
	pickQuery.makeExactQuery();
	ServerPickList serverPickList = service.setPickQuery(pickQuery);
	List<Pick> pickList = serverPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() > 0);

	Pick startPick = pickList.get(0);
	assertNotNull(startPick);
	assertTrue(!startPick.getPickID().isEmpty());
	long startingPickSkipCount = startPick.getSkipped();
	long startingPickUseCount = startPick.getUsed();

	// Test: Skip
	service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);

	// Verify
	ServerPickList updatedServerPickList = service.getServerPickList();
	List<Pick> updatePickList = updatedServerPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() > 0);
	Pick updatedPick = null;
	for (Pick updatePick : updatePickList) {
	    if (updatePick.getPickID().equals(startPick.getPickID())) {
		updatedPick = updatePick;
		break;
	    }
	}
	assertNotNull("Should have found the updated pick", updatedPick);
	assertTrue(updatedPick.getSkipped() == startingPickSkipCount + 1);
	assertTrue(updatedPick.getUsed() == startingPickUseCount);

	// Test: Use
	service.skipUsePassMemberPickIDList(SkipUsePass.USE, memberPickIDList);

	// Verify
	updatedServerPickList = service.getServerPickList();
	updatePickList = updatedServerPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() > 0);
	updatedPick = null;
	for (Pick updatePick : updatePickList) {
	    if (updatePick.getPickID().equals(startPick.getPickID())) {
		updatedPick = updatePick;
		break;
	    }
	}
	assertNotNull("Should have found the updated pick", updatedPick);
	assertTrue(updatedPick.getSkipped() == startingPickSkipCount + 1);
	assertTrue(updatedPick.getUsed() == startingPickUseCount + 1);

	// Test: Pass
	service.skipUsePassMemberPickIDList(SkipUsePass.PASS, memberPickIDList);

	// Verify
	updatedServerPickList = service.getServerPickList();
	updatePickList = updatedServerPickList.getPickList();
	assertTrue("was: " + pickList.size(), pickList.size() > 0);
	updatedPick = null;
	for (Pick updatePick : updatePickList) {
	    if (updatePick.getPickID().equals(startPick.getPickID())) {
		updatedPick = updatePick;
		break;
	    }
	}
	assertNotNull("Should have found the updated pick", updatedPick);
	assertTrue(updatedPick.getSkipped() == startingPickSkipCount + 1);
	assertTrue(updatedPick.getUsed() == startingPickUseCount + 1);

    }

    @Test
    public void test_updateMemberPick_changeIsStopUsing() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID, INCLUDE_CATEGORY_INFO);
	assertNotNull(serverPickList);
	assertTrue("Create some Picks first", serverPickList.getPickList().size() > 0);

	Pick pick = serverPickList.getPickList().get(0);
	assertTrue("It should be the member's Pick", pick.getMemberID() == memberID);
	boolean beforeIsStopUsing = pick.isStopUsing();

	// change
	pick.setStopUsing(!beforeIsStopUsing);

	// Test
	service.updatePick(pick);

	// Verify
	ServerPickList updatedServerPickList = service.getAllServerPickListByMemberID(memberID, INCLUDE_CATEGORY_INFO);
	assertNotNull(updatedServerPickList);
	assertTrue(serverPickList.getPickList().size() > 0);
	assertTrue("same pickID?", serverPickList.getPickList().get(0).getPickID().equals(pick.getPickID()));
	assertTrue("Should now be changed", serverPickList.getPickList().get(0).isStopUsing() != beforeIsStopUsing);
    }

    @Test
    public void test_addMemberList() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// create a member
	MemberNameList memberList = new MemberNameList();
	memberList.addMemberName(TEST_MEMBER_BOB);

	// Test
	ServerMemberMap serverMemberMap = service.addMemberList(memberList);

	// Verify
	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);

	// create another member
	MemberNameList newMemberList = new MemberNameList();
	newMemberList.addMemberName(TEST_MEMBER_SUE);

	// Test
	serverMemberMap = service.addMemberList(newMemberList);

	// Verify
	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) > 0);
    }

    @Test
    public void test_updateMemberNameByMemberID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// create a member
	MemberNameList memberList = new MemberNameList();
	memberList.addMemberName(TEST_MEMBER_BOB);
	memberList.addMemberName(TEST_MEMBER_SUE);

	ServerMemberMap serverMemberMap = service.addMemberList(memberList);

	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) > 0);
	long bobMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB);
	long sueMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE);
	assertTrue(bobMemberID != sueMemberID);

	// remove the previous test member name change if needed
	if (serverMemberMap.getMemberIDMap().get("Bob-ster") != null)
	    service.deleteMemberByID(serverMemberMap.getMemberIDMap().get("Bob-ster"));

	// Test
	serverMemberMap = service.updateMemberNameByMemberID(bobMemberID, TEST_MEMBER_BOB, "Bob-ster");

	// Verify
	serverMemberMap = service.addMemberList(memberList);

	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get("Bob-ster") == bobMemberID);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) == sueMemberID);

	// Clean-up
	service.deleteMemberByID(bobMemberID);
	service.deleteMemberByID(sueMemberID);
    }

    @Test
    public void test_deleteMemberByID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());
	String TEST_MEMBER_BOB = "Bob";
	String TEST_MEMBER_SUE = "Sue";

	// create a member
	MemberNameList memberList = new MemberNameList();
	memberList.addMemberName(TEST_MEMBER_BOB);
	memberList.addMemberName(TEST_MEMBER_SUE);

	ServerMemberMap serverMemberMap = service.addMemberList(memberList);

	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) > 0);
	long bobMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB);
	long sueMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE);
	assertTrue(bobMemberID != sueMemberID);

	// Test
	service.deleteMemberByID(bobMemberID);

	// Verify
	serverMemberMap = service.getMemberMap();
	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) == null);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) == sueMemberID);

	// Clean-up
	service.deleteMemberByID(bobMemberID);
	service.deleteMemberByID(sueMemberID);
    }

    @Test
    public void test_deleteMemberByID_yourself() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());
	long ownerMemberID = service.getMyMemberID();

	try {
	    // Test
	    service.deleteMemberByID(ownerMemberID);
	    fail("an error should be thrown");
	} catch (SkipUseException e) {
	    // Verify
	    assertTrue(e.getMessage().contains("You can not delete yourself"));
	}
    }

    @Test
    public void test_getMemberMap() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());
	MemberNameList memberList = new MemberNameList();
	String TEST_MEMBER_BOB = "Bob";
	memberList.addMemberName(TEST_MEMBER_BOB);
	ServerMemberMap serverMemberMap = service.addMemberList(memberList);
	assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
	assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);

	// Test
	ServerMemberMap FoundServerMemberMap = service.getMemberMap();

	// Verify
	assertTrue(FoundServerMemberMap.getMemberIDMap().size() > 0);
	assertTrue(FoundServerMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);
    }

    @Test
    public void test_createCategoryByMemberID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);
	String testCategoryName = "Cat's with funny hats";
	ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);

	if (serverMemberCategoryList.getMemberCategoryList().getCategoryList().contains(testCategoryName)) {
	    MemberCategoryList memberCategoryList = new MemberCategoryList(memberID,
		    serverMemberCategoryList.getMemberCategoryList().getCategoryList());
	    service.deleteCategoryListByMemberCategoryList(memberCategoryList);
	}

	// Test
	ServerMemberCategoryList foundServerMemberCategoryList = service.createCategoryByMemberID(memberID,
		testCategoryName);

	// Verify
	assertNotNull(foundServerMemberCategoryList);
	assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getCategoryList().size() == 1);
	assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getCategoryList().get(0)
		.equals(testCategoryName));
	assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getMemberID() == memberID);
    }

    @Test
    public void test_getCategoryListByMemberID() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);
	String testCategoryName = "Cat's with funny hats";
	// delete previous test update if needed
	deleteTestCategory(memberID, testCategoryName);
	service.createCategoryByMemberID(memberID, testCategoryName);

	// Test
	ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);

	// Verify
	assertNotNull(serverMemberCategoryList);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().size() > 0);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getMemberID() == memberID);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().contains(testCategoryName));
    }

    @Test
    public void test_updateCategoryNameByMemberID() throws SkipUseException {
	// Set up
	String oldCategoryName = "My Musick";
	String newCategoryName = "My Music";
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);

	// delete previous test update if needed
	deleteTestCategory(memberID, oldCategoryName);
	deleteTestCategory(memberID, newCategoryName);

	// create the category to update
	try {
	    service.createCategoryByMemberID(memberID, oldCategoryName);
	} catch (SkipUseException e) {
	    if (e.getMessage().contains("have a category named")) {
		// noop
	    } else {
		fail(e.getMessage());
	    }
	}

	// Test
	service.updateCategoryNameByMemberID(memberID, oldCategoryName, newCategoryName);

	// Verify
	ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().isEmpty() == false);
    }

    @Test
    public void test_deleteCategoryListByMemberCategoryList() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);

	List<String> categoryList = new ArrayList<>();
	String categoryToDelete = "delete me category";
	categoryList.add(categoryToDelete);
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);

	// first add the category
	try {
	    ServerMemberCategoryList serverMemberCategoryList = service.createCategoryByMemberID(memberID,
		    categoryToDelete);
	    assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().contains(categoryToDelete));
	} catch (SkipUseException e) {
	    if (e.getMessage().contains("have a category named")) {
		// noop
	    } else {
		fail(e.getMessage());
	    }
	}

	// Test
	service.deleteCategoryListByMemberCategoryList(memberCategoryList);

	// Verify
	ServerMemberCategoryList updatedServerMemberCategoryList = service.getCategoryListByMemberID(memberID);
	assertTrue(updatedServerMemberCategoryList.getMemberCategoryList().getCategoryList()
		.contains(categoryToDelete) == false);
    }

    @Test
    public void test_markCategoryPickIDCollection() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);

	String testCategoryName = "Cat's with funny hats";

	// delete previous test update if needed
	deleteTestCategory(memberID, testCategoryName);

	// create a category
	List<String> categoryList = new ArrayList<>();
	categoryList.add(testCategoryName);
	ServerMemberCategoryList serverMemberCategoryList = service.createCategoryByMemberID(memberID,
		testCategoryName);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0).equals(testCategoryName));

	// create pick collection
	String collectionName = "My collection";
	List<String> collectionPickIDList = new ArrayList<>();
	collectionPickIDList.add("A, B, C");
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName(collectionName);
	memberPickIDCollection.setPickIDList(collectionPickIDList);
	memberPickIDCollection.setSplitCSV(true);
	ServerPickIDCollection createdCollection = service.setPickIDCollection(memberPickIDCollection);

	// Mark the pick with the category
	CategoryMemberPickIDCollection categoryPickIDCollection = new CategoryMemberPickIDCollection();
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
	categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
	MemberPickIDCollection choosePickIDCollection = new MemberPickIDCollection();
	choosePickIDCollection.setCollectionName(createdCollection.getPickIDCollection().getCollectionName());
	choosePickIDCollection.addPickID("B");
	categoryPickIDCollection.setMemberPickIDCollection(choosePickIDCollection);

	// Test
	service.markCategoryPickIDCollection(categoryPickIDCollection);

	// Verify
	// look for Picks by category
	PickQuery pickQuery = new PickQuery();
	// look for the category
	pickQuery.setCategoryList(categoryList);
	// this member
	pickQuery.addToMemberIDList(memberID);
	pickQuery.setHowMany(50);
	// get the exact query amount
	pickQuery.setGetMorePicksIfShort(false);
	// do not add new Picks
	pickQuery.setNewMixInPercentage(0);
	// from the member's collection
	pickQuery.setMemberCollectionID(createdCollection.getPickIDCollection().getMemberCollectionID());
	// include the category info for the Picks
	pickQuery.setIncludeCategories(true);

	ServerPickList serverPickList = service.setPickQuery(pickQuery);
	assertNotNull(serverPickList);
	assertTrue(serverPickList.getPickList().size() == 1);
	assertTrue(serverPickList.getPickList().get(0).getPickID().equals("B"));
	assertTrue(serverPickList.getPickList().get(0).getCategoryList().size() == 1);
	assertTrue(serverPickList.getPickList().get(0).getCategoryList().get(0).equals(testCategoryName));

    }

    @Test
    public void test_unmarkCategoryPickIDCollection() throws SkipUseException {
	// Set up
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	long memberID = addTestMember(TEST_MEMBER_BOB);
	assertTrue(memberID > 0);

	String testCategoryName = "Cat's with funny hats";

	// delete previous test update if needed
	deleteTestCategory(memberID, testCategoryName);

	// create a category
	List<String> categoryList = new ArrayList<>();
	categoryList.add(testCategoryName);
	ServerMemberCategoryList serverMemberCategoryList = service.createCategoryByMemberID(memberID,
		testCategoryName);
	assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0).equals(testCategoryName));

	// create pick collection
	String collectionName = "My collection";
	List<String> collectionPickIDList = new ArrayList<>();
	collectionPickIDList.add("A, B, C");
	MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
	memberPickIDCollection.setCollectionName(collectionName);
	memberPickIDCollection.setPickIDList(collectionPickIDList);
	memberPickIDCollection.setSplitCSV(true);
	ServerPickIDCollection createdCollection = service.setPickIDCollection(memberPickIDCollection);

	// Mark the pick with the category
	CategoryMemberPickIDCollection categoryPickIDCollection = new CategoryMemberPickIDCollection();
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
	categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
	MemberPickIDCollection choosePickIDCollection = new MemberPickIDCollection();
	choosePickIDCollection.setCollectionName(createdCollection.getPickIDCollection().getCollectionName());
	choosePickIDCollection.addPickID("B");
	categoryPickIDCollection.setMemberPickIDCollection(choosePickIDCollection);

	service.markCategoryPickIDCollection(categoryPickIDCollection);

	PickQuery pickQuery = new PickQuery();
	// this member
	pickQuery.addToMemberIDList(memberID);
	// look for the category
	pickQuery.setCategoryList(categoryList);
	pickQuery.setHowMany(50);
	// get the exact query amount
	pickQuery.setGetMorePicksIfShort(false);
	// do not add new Picks
	pickQuery.setNewMixInPercentage(0);
	// from the member's collection
	pickQuery.setMemberCollectionID(createdCollection.getPickIDCollection().getMemberCollectionID());
	// include the category info for the Picks
	pickQuery.setIncludeCategories(true);

	ServerPickList serverPickList = service.setPickQuery(pickQuery);
	assertNotNull(serverPickList);
	assertTrue(serverPickList.getPickList().size() == 1);
	assertTrue(serverPickList.getPickList().get(0).getPickID().equals("B"));
	assertTrue(serverPickList.getPickList().get(0).getCategoryList().size() == 1);
	assertTrue(serverPickList.getPickList().get(0).getCategoryList().get(0).equals(testCategoryName));

	// Test
	service.unmarkCategoryPickIDCollection(categoryPickIDCollection);

	// Verify
	serverPickList = service.setPickQuery(pickQuery);
	assertNotNull(serverPickList);
	assertTrue(serverPickList.getPickList().size() == 0);

    }

    // If an error is given by the server, you can see the message in the
    // server's response. You should still be able to communicate with the
    // server if the error was not severe.
    //
    @Test
    public void test_errorTest() throws SkipUseException {
	service.login(TEST_EMAIL, TEST_PASSWORD);
	assertTrue(service.isLoggedIn());

	// Test
	service.errorTest();

	// Verify
	ServerResponse serverResponse = service.serverResponseData;
	assertNotNull(serverResponse);
	assertTrue(serverResponse.getStatus() == HttpStatus.BAD_REQUEST);
	assertFalse("There should be an error message", serverResponse.getErrorMessage().isEmpty());
	assertFalse("There should be a message with help", serverResponse.getMessage().isEmpty());

	// should still have all the other communication parts
	assertTrue(serverResponse.getOwnerID() > 0);
	assertFalse(serverResponse.getOwnerName().isEmpty());
	assertFalse(serverResponse.getProxyID().isEmpty());
	assertFalse(serverResponse.getSkipUseToken().isEmpty());

	// Verify can still use the service
	ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection(0);
	assertNotNull(foundServerCollection);
	List<String> foundCollectionList = foundServerCollection.getPickIDCollection().getPickIDList();
	assertNotNull(foundCollectionList);
    }

    private long addTestMember(String memberName) throws SkipUseException {
	MemberNameList memberList = new MemberNameList();
	memberList.addMemberName(memberName);
	ServerMemberMap serverMemberMap = service.addMemberList(memberList);
	return serverMemberMap.getMemberIDMap().get(memberName);
    }

    private void deleteTestCategory(long memberID, String categoryName) throws SkipUseException {
	ServerMemberCategoryList currentServerMemberCategoryList = service.getCategoryListByMemberID(memberID);
	List<String> currentCategoryList = currentServerMemberCategoryList.getMemberCategoryList().getCategoryList();
	List<String> categoryList = new ArrayList<>();
	categoryList.add(categoryName);
	MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
	if (currentCategoryList.contains(categoryName))
	    service.deleteCategoryListByMemberCategoryList(memberCategoryList);
    }

}
