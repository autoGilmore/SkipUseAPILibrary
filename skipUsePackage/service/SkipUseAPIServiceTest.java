package com.autogilmore.throwback.skipUsePackage.service;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberListPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberNameList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCountAdvance;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCountAdvanceList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
import com.autogilmore.throwback.skipUsePackage.enums.AdvancedOption;
import com.autogilmore.throwback.skipUsePackage.enums.ResultOption;
import com.autogilmore.throwback.skipUsePackage.enums.SearchOption;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SkipUseAPIServiceTest {
	// NOTE: Set the SkipUseAPI URL here. See API documentation for more
	// information about the endpoint.
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
			SkipUseManager manager = SkipUseManager.INSTANCE;
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
	}

	@Test
	public void test_login_badEmail() {
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
	public void test_login_badLogin() {
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
	}

	@Test
	public void test_getProfile() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		// Test
		Profile profile = service.getProfile();
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify - if this is failing, try running the update Profile test first
		assertEquals(SkipUseProperties.OWNER_NAME, profile.getOwnerName());
		assertEquals(SkipUseProperties.TEST_SKIP_USE_EMAIL, profile.getEmail());
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		profile = service.getProfile();

		// Verify
		assertEquals("was: " + profile.getOwnerName(), SkipUseProperties.OWNER_NAME, profile.getOwnerName());
		assertEquals("was: " + profile.getEmail(), SkipUseProperties.TEST_SKIP_USE_EMAIL, profile.getEmail());
		assertTrue(profile.getPassword().isEmpty());
	}

	@Test
	public void test_setMemberCollection() throws SkipUseException {
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
		assertEquals(4, collectionPickIDList.size());

		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setCollectionName(collectionName);
		memberMemberCollection.setPickIDList(collectionPickIDList);
		memberMemberCollection.setSplitCSV(false);

		// Test
		service.setMemberCollection(memberMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		ServerMemberCollection foundServerCollection = service.getServerMemberCollection(0);
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getMemberCollection().getPickIDList();
		assertNotNull(foundCollectionList);
		// should not include duplicate
		assertEquals("was: " + foundCollectionList.size(), 3, foundCollectionList.size());
	}

	@Test
	public void test_setMemberCollection_splitCommaPlusSpace() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A, B, C");
		collectionList.add("D,E,F");

		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setCollectionName(collectionName);
		memberMemberCollection.setPickIDList(collectionList);
		// split
		memberMemberCollection.setSplitCSV(true);

		// Test
		service.setMemberCollection(memberMemberCollection);

		// Verify
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		ServerMemberCollection foundServerCollection = service.getServerMemberCollection(0);
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getMemberCollection().getPickIDList();
		assertNotNull(foundCollectionList);
		assertEquals(4, foundCollectionList.size());
		assertTrue(foundCollectionList.contains("A"));
		assertTrue(foundCollectionList.contains("B"));
		assertTrue(foundCollectionList.contains("C"));
		assertTrue(foundCollectionList.contains("D,E,F"));
	}

	@Test
	public void test_getServerMemberCollection() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A");
		collectionList.add("B");
		collectionList.add("C");

		MemberCollection pickCollection = new MemberCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(false);
		service.setMemberCollection(pickCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test
		ServerMemberCollection foundServerCollection = service.getServerMemberCollection(0);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getMemberCollection().getPickIDList();
		assertNotNull(foundCollectionList);
		assertEquals(3, foundCollectionList.size());
	}

	@Test
	public void test_setPickQuery() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A, B, C");

		MemberCollection pickCollection = new MemberCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(true);
		service.setMemberCollection(pickCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		PickQuery pickQuery = new PickQuery();
		pickQuery.setHowMany(2);
		pickQuery.addToSearchOptionList(SearchOption.GET_MORE_IF_SHORT);

		// Test
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(serverPickList);
		List<Pick> pickList = serverPickList.getPickList();
		assertEquals("was: " + pickList.size(), 2, pickList.size());
	}

	@Test
	public void test_setPickQuery_debug() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A, B, C");

		MemberCollection pickCollection = new MemberCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(true);
		ServerMemberCollection createdCollection = service.setMemberCollection(pickCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		long memberID = createdCollection.getOwnerID();

		// create a pick (if needed) by using the Pass
		MemberListPickIDList memberPickIDList = new MemberListPickIDList(createdCollection.getMemberCollection());
		memberPickIDList.addMemberID(memberID);
		service.skipUsePassMemberPickIDList(SkipUsePass.PASS, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		PickQuery pickQuery = new PickQuery();
		pickQuery.addToAdvancedOptionList(AdvancedOption.DEBUG_QUERY);
		pickQuery.setMemberCollectionID(memberID);
		// test - various search mode options
		pickQuery.addToSearchOptionList(SearchOption.BALANCED);
		pickQuery.addToSearchOptionList(SearchOption.RANDOM);
		pickQuery.addToResultOptionList(ResultOption.MERGE);

		// Test
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(serverPickList);
		String debugMessage = service.getAPIMessage();
		assertFalse("There should be a debug message", debugMessage.isEmpty());
		assertTrue("Should see a Member ID reference", debugMessage.contains(memberID + ""));
		assertTrue("Should see a RANDOM search mode reference", debugMessage.contains(SearchOption.RANDOM.name()));
		assertTrue("Should see a BALANCED search mode reference", debugMessage.contains(SearchOption.BALANCED.name()));
		assertTrue("Should see a MERGE combine mode reference", debugMessage.contains(ResultOption.MERGE.name()));
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
		MemberCollection memberMemberCollection = new MemberCollection(bobMemberID);
		memberMemberCollection.setCollectionName("test collection");
		memberMemberCollection.addPickID(pickID);
		service.setMemberCollection(memberMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		// store the pick
		service.skipUsePassPickID(SkipUsePass.PASS, bobMemberID, pickID, bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		// set a Pick Query
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(bobMemberID);
		pickQuery.setMemberCollectionID(bobMemberID);
		pickQuery.setExcludeRecentPicksHours(0);
		pickQuery.makeExactQuery();
		service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test
		ServerPickList serverPickList = service.getServerPickList();
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

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
		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setMemberID(bobMemberID);
		memberMemberCollection.setCollectionName("test collection");
		memberMemberCollection.addPickID(pickID);
		service.setMemberCollection(memberMemberCollection);

		// Test: pick not stored yet
		Pick _foundPick = service._getPickByMemberIDAndPickIDAndCollectionID(bobMemberID, pickID, bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNull(_foundPick);

		// store the pick
		service.skipUsePassPickID(SkipUsePass.PASS, bobMemberID, pickID, bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test: pick is now found
		_foundPick = service._getPickByMemberIDAndPickIDAndCollectionID(bobMemberID, pickID, bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(_foundPick);
		assertEquals(_foundPick.getPickID(), pickID);

		// Clean-up
		service.deleteMemberByID(bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
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
		collectionList.add("A" + date);
		collectionList.add("B" + date);
		collectionList.add("C" + date);
		collectionList.add("D" + date);
		
		MemberCollection pickCollection = new MemberCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setMemberID(memberID);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(true);
		service.setMemberCollection(pickCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		List<String> foundCollectionList = service.getServerMemberCollection(memberID).getMemberCollection()
				.getPickIDList();
		assertEquals("was: " + foundCollectionList.size(), 4, foundCollectionList.size());

		// Test: empty
		ServerPickList serverPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList,
				memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(serverPickList);
		List<Pick> pickList = serverPickList.getPickList();
		assertEquals("Should not have any stored Picks yet. was: " + pickList.size(), 0, pickList.size());

		// add picks
		List<Long> memberIDList = new ArrayList<>();
		memberIDList.add(memberID);
		MemberListPickIDList memberPickIDList = new MemberListPickIDList(pickCollection, memberIDList);
		service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test: w/Picks
		serverPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList, memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(serverPickList);
		pickList = serverPickList.getPickList();
		assertEquals("Should have all the stored Picks. was: " + pickList.size(), pickList.size(), collectionList.size());
		
		// mark one as stop using
		Pick pick  = pickList.get(0);
		pick.setStopUsing(true);
		service.updatePick(pick);
		
		// verify - even the stop use is returned
		serverPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList, memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(serverPickList);
		pickList = serverPickList.getPickList();
		assertEquals("Should have all the stored Picks. was: " + pickList.size(), pickList.size(), collectionList.size());
		assertTrue("Should have the stop using Pick.",
				pickList.get(0).isStopUsing());
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
		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setCollectionName("test collection");
		memberMemberCollection.addPickID(testPickID);
		service.setMemberCollection(memberMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// create a pick (if needed) by using the Pass
		MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberMemberCollection);
		memberPickIDList.addMemberID(bobMemberID);
		service.skipUsePassMemberPickIDList(SkipUsePass.PASS, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(bobMemberID);
		pickQuery.setExcludeRecentPicksHours(0);
		pickQuery.makeExactQuery();
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		List<Pick> pickList = serverPickList.getPickList();
		assertTrue("was: " + pickList.size(), pickList.size() > 0);

		Pick startPick = pickList.get(0);
		assertNotNull(startPick);
		assertFalse(startPick.getPickID().isEmpty());
		long startingPickSkipCount = startPick.getSkipped();
		long startingPickUseCount = startPick.getUsed();

		// Test: Skip
		service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

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
		assertEquals(updatedPick.getSkipped(), startingPickSkipCount + 1);
		assertEquals(updatedPick.getUsed(), startingPickUseCount);

		// Test: Use
		service.skipUsePassMemberPickIDList(SkipUsePass.USE, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		updatedServerPickList = service.getServerPickList();
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
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
		assertEquals(updatedPick.getSkipped(), startingPickSkipCount + 1);
		assertEquals(updatedPick.getUsed(), startingPickUseCount + 1);

		// Test: Pass
		service.skipUsePassMemberPickIDList(SkipUsePass.PASS, memberPickIDList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		updatedServerPickList = service.getServerPickList();
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
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
		assertEquals(updatedPick.getSkipped(), startingPickSkipCount + 1);
		assertEquals(updatedPick.getUsed(), startingPickUseCount + 1);
	}

	@Test
	public void test_pickIDCountAdvance() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		long bobMemberID = addTestMember(TEST_MEMBER_BOB);
		long sueMemberID = addTestMember(TEST_MEMBER_SUE);
		long collectionID = bobMemberID;

		String collectionName = "Bob's collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A");
		collectionList.add("B");
		collectionList.add("C");

		MemberCollection memberCollection = new MemberCollection();
		memberCollection.setMemberID(bobMemberID);
		memberCollection.setCollectionName(collectionName);
		memberCollection.setPickIDList(collectionList);
		memberCollection.setSplitCSV(true);
		service.setMemberCollection(memberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		List<String> foundCollectionList = service.getServerMemberCollection(0).getMemberCollection().getPickIDList();
		assertTrue("was: " + foundCollectionList.size(), foundCollectionList.size() > 0);

		PickQuery pickQuery = new PickQuery();
		pickQuery.setMemberCollectionID(bobMemberID);
		pickQuery.addToMemberIDList(bobMemberID);
		pickQuery.setPickIDList(collectionList);
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		List<Pick> pickList = serverPickList.getPickList();
		// let's get Sue's Pick too
		pickQuery.getMemberIDList().clear();
		pickQuery.addToMemberIDList(sueMemberID);
		serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		pickList.addAll(serverPickList.getPickList());

		long startingBobAPickSkipCount = 0L;
		long startingBobAPickUseCount = 0L;
		long startingSueBPickSkipCount = 0L;
		long startingSueBPickUseCount = 0L;
		long startingSueCPickSkipCount = 0L;
		long startingSueCPickUseCount = 0L;

		for (Pick pick : pickList) {
			if (pick.getMemberID() == bobMemberID) {
				if (pick.getPickID().equals("A")) {
					startingBobAPickSkipCount = pick.getSkipped();
					startingBobAPickUseCount = pick.getUsed();
				}
			} else if (pick.getMemberID() == sueMemberID) {
				if (pick.getPickID().equals("B")) {
					startingSueBPickSkipCount = pick.getSkipped();
					startingSueBPickUseCount = pick.getUsed();
					continue;
				}
				if (pick.getPickID().equals("C")) {
					startingSueCPickSkipCount = pick.getSkipped();
					startingSueCPickUseCount = pick.getUsed();
				}
			}
		}

		// a list of Pick ID counts to update
		PickIDCountAdvanceList pickIDCountUpdates = new PickIDCountAdvanceList();
		// update right away so we can check the count
		pickIDCountUpdates.setUpdateASAP(true);

		// have Bob advance the "A" Pick ID counts
		PickIDCountAdvance pickIDCountAdvance = new PickIDCountAdvance(collectionID, bobMemberID, "A", 12, 3);

		// have Sue advance the "B" and "C" Pick ID counts
		pickIDCountUpdates.addCountAdvance(new PickIDCountAdvance(collectionID, sueMemberID, "B", 4, 4));
		pickIDCountUpdates.addCountAdvance(new PickIDCountAdvance(collectionID, sueMemberID, "C", 1, 13));
		pickIDCountUpdates.addCountAdvance(pickIDCountAdvance);

		// Test - updating two members
		service.pickIDCountAdvance(pickIDCountUpdates);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// verify - Pick ID counts where advanced
		pickQuery.getMemberIDList().clear();
		pickQuery.addToMemberIDList(bobMemberID);
		ServerPickList updatedServerPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		List<Pick> updatePickList = updatedServerPickList.getPickList();
		// let's get Sue's Pick too
		pickQuery.getMemberIDList().clear();
		pickQuery.addToMemberIDList(sueMemberID);
		updatedServerPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		updatePickList.addAll(updatedServerPickList.getPickList());

		int foundPicks = 0;
		for (Pick updatedPick : updatePickList) {
			if (updatedPick.getMemberID() == bobMemberID) {
				if (updatedPick.getPickID().equals("A")) {
					assertEquals(updatedPick.getSkipped(), startingBobAPickSkipCount + 12);
					assertEquals(updatedPick.getUsed(), startingBobAPickUseCount + 3);
					foundPicks++;
				}
			} else if (updatedPick.getMemberID() == sueMemberID) {
				if (updatedPick.getPickID().equals("B")) {
					assertEquals(updatedPick.getSkipped(), startingSueBPickSkipCount + 4);
					assertEquals(updatedPick.getUsed(), startingSueBPickUseCount + 4);
					foundPicks++;
					continue;
				}
				if (updatedPick.getPickID().equals("C")) {
					assertEquals(updatedPick.getSkipped(), startingSueCPickSkipCount + 1);
					assertEquals(updatedPick.getUsed(), startingSueCPickUseCount + 13);
					foundPicks++;
				}
			}
		}
		assertEquals("was: " + foundPicks, 3, foundPicks);
	}

	@Test
	public void test_updateMemberPick_changeIsStopUsing() throws SkipUseException {
		// Set up
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());

		long memberID = addTestMember(TEST_MEMBER_BOB);
		ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID, INCLUDE_CATEGORY_INFO);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(serverPickList);
		assertTrue("Create some Picks first", serverPickList.getPickList().size() > 0);

		Pick pick = serverPickList.getPickList().get(0);
		assertEquals("It should be the member's Pick", pick.getMemberID(), memberID);
		boolean beforeIsStopUsing = pick.isStopUsing();

		// change
		pick.setStopUsing(!beforeIsStopUsing);

		// Test
		service.updatePick(pick);

		// Verify
		ServerPickList updatedServerPickList = service.getAllServerPickListByMemberID(memberID, INCLUDE_CATEGORY_INFO);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(updatedServerPickList);
		assertTrue(serverPickList.getPickList().size() > 0);
		assertEquals("same pickID?", serverPickList.getPickList().get(0).getPickID(), pick.getPickID());
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);

		// create another member
		MemberNameList newMemberList = new MemberNameList();
		newMemberList.addMemberName(TEST_MEMBER_SUE);

		// Test
		serverMemberMap = service.addMemberList(newMemberList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

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
		if (serverMemberMap.getMemberIDMap().get("Bob-ster") != null) {
			service.deleteMemberByID(serverMemberMap.getMemberIDMap().get("Bob-ster"));
		}

		// Test
		serverMemberMap = service.updateMemberNameByMemberID(bobMemberID, TEST_MEMBER_BOB, "Bob-ster");
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		serverMemberMap = service.addMemberList(memberList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertEquals((long) serverMemberMap.getMemberIDMap().get("Bob-ster"), bobMemberID);
		assertEquals((long) serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE), sueMemberID);

		// Clean-up
		service.deleteMemberByID(bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		service.deleteMemberByID(sueMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB) > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE) > 0);
		long bobMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB);
		long sueMemberID = serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE);
		assertTrue(bobMemberID != sueMemberID);

		// Test
		service.deleteMemberByID(bobMemberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		serverMemberMap = service.getMemberMap();
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertNull(serverMemberMap.getMemberIDMap().get(TEST_MEMBER_BOB));
		assertEquals((long) serverMemberMap.getMemberIDMap().get(TEST_MEMBER_SUE), sueMemberID);

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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

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
			assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		}

		// Test
		ServerMemberCategoryList foundServerMemberCategoryList = service.createCategoryByMemberID(memberID,
				testCategoryName);

		// Verify
		assertNotNull(foundServerMemberCategoryList);
		assertEquals(1, foundServerMemberCategoryList.getMemberCategoryList().getCategoryList().size());
		assertEquals(foundServerMemberCategoryList.getMemberCategoryList().getCategoryList().get(0), testCategoryName);
		assertEquals(foundServerMemberCategoryList.getMemberCategoryList().getMemberID(), memberID);
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test
		ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		assertNotNull(serverMemberCategoryList);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().size() > 0);
		assertEquals(serverMemberCategoryList.getMemberCategoryList().getMemberID(), memberID);
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
			if (e.getMessage().contains("have a category named") == false) {
				fail(e.getMessage());
			}
		}

		// Test
		service.updateCategoryNameByMemberID(memberID, oldCategoryName, newCategoryName);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		ServerMemberCategoryList serverMemberCategoryList = service.getCategoryListByMemberID(memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertEquals(false, serverMemberCategoryList.getMemberCategoryList().getCategoryList().isEmpty());
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
			assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
			assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().contains(categoryToDelete));
		} catch (SkipUseException e) {
			if (e.getMessage().contains("have a category named") == false) {
				fail(e.getMessage());
			}
		}

		// Test
		service.deleteCategoryListByMemberCategoryList(memberCategoryList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		ServerMemberCategoryList updatedServerMemberCategoryList = service.getCategoryListByMemberID(memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertFalse(updatedServerMemberCategoryList.getMemberCategoryList().getCategoryList()
				.contains(categoryToDelete));
	}

	@Test
	public void test_markCategoryMemberCollection() throws SkipUseException {
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertEquals(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0), testCategoryName);

		// create pick collection
		String collectionName = "My collection";
		List<String> collectionPickIDList = new ArrayList<>();
		collectionPickIDList.add("A, B, C");
		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setCollectionName(collectionName);
		memberMemberCollection.setPickIDList(collectionPickIDList);
		memberMemberCollection.setSplitCSV(true);
		ServerMemberCollection createdCollection = service.setMemberCollection(memberMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Mark the pick with the category
		CategoryMemberCollection categoryMemberCollection = new CategoryMemberCollection();
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		categoryMemberCollection.setMemberCategoryList(memberCategoryList);
		MemberCollection chooseMemberCollection = new MemberCollection();
		chooseMemberCollection.setCollectionName(createdCollection.getMemberCollection().getCollectionName());
		chooseMemberCollection.addPickID("B");
		categoryMemberCollection.setMemberCollection(chooseMemberCollection);

		// Test
		service.markCategoryMemberCollection(categoryMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		// look for Picks by category
		PickQuery pickQuery = new PickQuery();
		// look for the category
		pickQuery.setCategoryList(categoryList);
		// this member
		pickQuery.addToMemberIDList(memberID);
		pickQuery.setHowMany(50);
		// get the exact query amount
		// do not add new Picks
		pickQuery.setNewMixInPercentage(0);
		// from the member's collection
		pickQuery.setMemberCollectionID(createdCollection.getMemberCollection().getMemberID());
		// include the category info for the Picks
		pickQuery.addToResultOptionList(ResultOption.INCLUDE_CATEGORY_INFO);

		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(serverPickList);
		assertEquals(1, serverPickList.getPickList().size());
		assertEquals("B", serverPickList.getPickList().get(0).getPickID());
		assertEquals(1, serverPickList.getPickList().get(0).getCategoryList().size());
		assertEquals(serverPickList.getPickList().get(0).getCategoryList().get(0), testCategoryName);
	}

	@Test
	public void test_unmarkCategoryMemberCollection() throws SkipUseException {
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
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertEquals(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0), testCategoryName);

		// create pick collection
		String collectionName = "My collection";
		List<String> collectionPickIDList = new ArrayList<>();
		collectionPickIDList.add("A, B, C");
		MemberCollection memberMemberCollection = new MemberCollection();
		memberMemberCollection.setCollectionName(collectionName);
		memberMemberCollection.setPickIDList(collectionPickIDList);
		memberMemberCollection.setSplitCSV(true);
		ServerMemberCollection createdCollection = service.setMemberCollection(memberMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Mark the pick with the category
		CategoryMemberCollection categoryMemberCollection = new CategoryMemberCollection();
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		categoryMemberCollection.setMemberCategoryList(memberCategoryList);
		MemberCollection chooseMemberCollection = new MemberCollection();
		chooseMemberCollection.setCollectionName(createdCollection.getMemberCollection().getCollectionName());
		chooseMemberCollection.addPickID("B");
		categoryMemberCollection.setMemberCollection(chooseMemberCollection);
		service.markCategoryMemberCollection(categoryMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		PickQuery pickQuery = new PickQuery();
		// this member
		pickQuery.addToMemberIDList(memberID);
		// look for the category
		pickQuery.setCategoryList(categoryList);
		pickQuery.setHowMany(50);
		// get the exact query amount
		// do not add new Picks
		pickQuery.setNewMixInPercentage(0);
		// from the member's collection
		pickQuery.setMemberCollectionID(createdCollection.getMemberCollection().getMemberID());
		// include the category info for the Picks
		pickQuery.addToResultOptionList(ResultOption.INCLUDE_CATEGORY_INFO);

		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(serverPickList);
		assertEquals(1, serverPickList.getPickList().size());
		assertEquals("B", serverPickList.getPickList().get(0).getPickID());
		assertEquals(1, serverPickList.getPickList().get(0).getCategoryList().size());
		assertEquals(serverPickList.getPickList().get(0).getCategoryList().get(0), testCategoryName);

		// Test
		service.unmarkCategoryMemberCollection(categoryMemberCollection);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Verify
		serverPickList = service.setPickQuery(pickQuery);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		assertNotNull(serverPickList);
		assertEquals(0, serverPickList.getPickList().size());
	}

	// If an error is given by the server, you can see the message in the
	// server's response. You should still be able to communicate with the
	// server if the error was not severe.
	//
	@Test
	public void test_errorTest() throws SkipUseException {
		service.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(service.isLoggedIn());
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());

		// Test
		service.errorTest();

		// Verify
		ServerResponse serverResponse = service.serverResponseData;
		assertNotNull(serverResponse);
		assertSame(serverResponse.getStatus(), HttpStatus.BAD_REQUEST);
		assertFalse("There should be an error message", serverResponse.getErrorMessage().isEmpty());
		assertFalse("There should be a message with help", serverResponse.getMessage().isEmpty());

		// should still have all the other communication parts
		assertTrue(serverResponse.getOwnerID() > 0);
		assertFalse(serverResponse.getOwnerName().isEmpty());
		assertFalse(serverResponse.getProxyID().isEmpty());
		assertFalse(serverResponse.getSkipUseToken().isEmpty());

		// Verify can still use the service
		ServerMemberCollection foundServerCollection = service.getServerMemberCollection(0);
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getMemberCollection().getPickIDList();
		assertNotNull(foundCollectionList);
	}

	private long addTestMember(String memberName) throws SkipUseException {
		MemberNameList memberList = new MemberNameList();
		memberList.addMemberName(memberName);
		ServerMemberMap serverMemberMap = service.addMemberList(memberList);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		return serverMemberMap.getMemberIDMap().get(memberName);
	}

	private void deleteTestCategory(long memberID, String categoryName) throws SkipUseException {
		ServerMemberCategoryList currentServerMemberCategoryList = service.getCategoryListByMemberID(memberID);
		assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		List<String> currentCategoryList = currentServerMemberCategoryList.getMemberCategoryList().getCategoryList();
		List<String> categoryList = new ArrayList<>();
		categoryList.add(categoryName);
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		if (currentCategoryList.contains(categoryName)) {
			service.deleteCategoryListByMemberCategoryList(memberCategoryList);
			assertTrue(service.getAPIErrorMessage(), service.getAPIErrorMessage().isEmpty());
		}
	}
}
