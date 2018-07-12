package com.autogilmore.throwback.skipUsePackage.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDList;
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

public class SkipUseAPIServiceTest {
	// NOTE: Set the SkipUseAPI URL here. See API documentation for more
	// information.
	private final String SKIP_USE_API_URL = SkipUseProperties.SKIP_USE_API_URL;

	// NOTE: Set these to use your own test credentials as the demo account is
	// unstable from other people's usage.
	private static final String EMAIL = SkipUseProperties.TEST_SKIP_USE_EMAIL;
	private static final String PASSWORD = SkipUseProperties.TEST_SKIP_USE_PASSWORD;

	private SkipUseAPIService service = new SkipUseAPIService(SKIP_USE_API_URL);

	@After
	public void after() {
		try {
			service.logout();
		} catch (SkipUseException e) {
			// noop
		}
	}

	@Test
	public void test_login() throws SkipUseException {
		// Set up
		assertFalse(service.isLoggedIn());

		// Test
		service.login(EMAIL, PASSWORD);

		// Verify
		assertTrue(service.isLoggedIn());
	}

	@Test
	public void test_login_badEmail() throws SkipUseException {
		// Set up
		assertFalse(service.isLoggedIn());

		// Test
		try {
			service.login("Zoyinky", PASSWORD);
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
			service.login("not@registered.com", PASSWORD);
			fail("An error should be thrown");
		} catch (SkipUseException e) {
			// Verify
			assertTrue("was: " + e.getMessage(),
					e.getMessage().contains("Password or email was incorrect"));
		}
	}

	@Test
	public void test_logout() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		// Test
		service.logout();

		// Verify
		assertFalse(service.isLoggedIn());
	}

	@Test
	public void test_setPickIDCollection() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionIDList = new ArrayList<>();
		collectionIDList.add("A");
		collectionIDList.add("B");
		collectionIDList.add("C");
		// ignore duplicate
		collectionIDList.add("C");
		assertTrue(collectionIDList.size() == 4);

		PickIDCollection pickIDCollection = new PickIDCollection();
		pickIDCollection.setCollectionName(collectionName);
		pickIDCollection.setPickIDList(collectionIDList);
		pickIDCollection.setSplitCSV(false);

		// Test
		service.setPickIDCollection(pickIDCollection);

		// Verify
		ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection();
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getPickIDCollection()
				.getPickIDList();
		assertNotNull(foundCollectionList);
		// should not include duplicate
		assertTrue("was: " + foundCollectionList.size(), foundCollectionList.size() == 3);
	}

	@Test
	public void test_setPickIDCollection_splitCommas() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A,B,C");

		PickIDCollection pickIDCollection = new PickIDCollection();
		pickIDCollection.setCollectionName(collectionName);
		pickIDCollection.setPickIDList(collectionList);
		// split
		pickIDCollection.setSplitCSV(true);

		// Test
		service.setPickIDCollection(pickIDCollection);

		// Verify
		ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection();
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getPickIDCollection()
				.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 3);
	}

	@Test
	public void test_getServerPickIDCollection() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A");
		collectionList.add("B");
		collectionList.add("C");

		PickIDCollection pickCollection = new PickIDCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(false);
		service.setPickIDCollection(pickCollection);

		// Test
		ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection();

		// Verify
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getPickIDCollection()
				.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 3);
	}

	@Test
	public void test_setPickQuery() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A,B,C");

		PickIDCollection pickCollection = new PickIDCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(true);
		service.setPickIDCollection(pickCollection);

		PickQuery pickQuery = new PickQuery();
		pickQuery.setHowMany(2);

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
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

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
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();

		// Test
		ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID);

		// Verify
		assertNotNull(serverPickList);
	}

	@Test
	public void test__getPickByMemberIDAndPickID() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();

		// test Pick ID
		String pickID = "<a href=\"http://www.skipuse.com\" target=\"_blank\" title=\"SkipUse Home Page\">SkipUse</a>";

		// set collection
		PickIDCollection pickIDCollection = new PickIDCollection("test collection");
		pickIDCollection.addPickID(pickID);
		service.setPickIDCollection(pickIDCollection);

		// Test: pick not stored yet
		Pick _foundPick = service._getPickByMemberIDAndPickID(memberID, pickID);

		// Verify
		assertTrue(_foundPick == null);

		// store the pick
		service.skipUsePassPickID(SkipUsePass.PASS, memberID, pickID);

		// Test: pick is now found
		_foundPick = service._getPickByMemberIDAndPickID(memberID, pickID);
		assertNotNull(_foundPick);
		assertTrue(_foundPick.getPickID().equals(pickID));
	}

	@Test
	public void test_getServerPickListByMemberIDAndPickList() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A");
		collectionList.add("B");
		collectionList.add("C");
		collectionList.add("D");

		PickIDCollection pickCollection = new PickIDCollection();
		pickCollection.setCollectionName(collectionName);
		pickCollection.setPickIDList(collectionList);
		pickCollection.setSplitCSV(true);
		service.setPickIDCollection(pickCollection);
		assertTrue(
				"was: " + service.getServerPickIDCollection().getPickIDCollection().getPickIDList()
						.size(),
				service.getServerPickIDCollection().getPickIDCollection().getPickIDList()
						.size() == 4);

		// Test: empty
		ServerPickList memberPickList = service.getServerPickListByMemberIDAndPickList(memberID,
				collectionList);

		// Verify
		assertNotNull(memberPickList);
		List<Pick> pickList = memberPickList.getPickList();
		assertTrue("Should not have any store Picks yet. was: " + pickList.size(),
				pickList.size() == 0);

		// add picks
		List<Integer> memberIDList = new ArrayList<>();
		memberIDList.add(memberID);
		MemberPickIDList memberPickIDList = new MemberPickIDList(pickCollection, memberIDList);
		memberPickIDList.setSplitCSV(true);
		service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);

		// Test: w/Picks
		memberPickList = service.getServerPickListByMemberIDAndPickList(memberID, collectionList);

		// Verify
		assertNotNull(memberPickList);
		pickList = memberPickList.getPickList();
		assertTrue("Should have all the store Picks. was: " + pickList.size(),
				pickList.size() == collectionList.size());

	}

	@Test
	public void test_skipUsePassPick() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		PickQuery pickQuery = new PickQuery();
		pickQuery.setHowMany(5);
		pickQuery.setExcludeRecentPicks(false);
		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		List<Pick> pickList = serverPickList.getPickList();
		assertTrue("was: " + pickList.size(), pickList.size() > 0);

		Pick pick = pickList.get(0);
		assertNotNull(pick);
		assertTrue(!pick.getPickID().isEmpty());
		int startingPickSkipCount = pick.getSkipped();
		int startingPickUseCount = pick.getUsed();

		MemberPickIDList memberPickIDList = new MemberPickIDList();
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);
		memberPickIDList.addMemberID(memberID);
		memberPickIDList.addPickID(pick.getPickID());

		// Test: Skip
		service.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);

		// Verify
		ServerPickList updatedServerPickList = service.getServerPickList();
		List<Pick> updatePickList = updatedServerPickList.getPickList();
		assertTrue("was: " + pickList.size(), pickList.size() > 0);
		Pick updatedPick = null;
		for (Pick updatePick : updatePickList) {
			if (updatePick.getPickID().equals(pick.getPickID())) {
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
			if (updatePick.getPickID().equals(pick.getPickID())) {
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
			if (updatePick.getPickID().equals(pick.getPickID())) {
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
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		ServerPickList serverPickList = service.getAllServerPickListByMemberID(memberID);
		assertNotNull(serverPickList);
		assertTrue("Create some Picks first", serverPickList.getPickList().size() > 0);

		Pick pick = serverPickList.getPickList().get(0);
		boolean beforeIsStopUsing = pick.isStopUsing();

		// change
		pick.setStopUsing(!beforeIsStopUsing);

		// Test
		service.updateMemberPick(pick);

		// Verify
		ServerPickList updatedServerPickList = service.getAllServerPickListByMemberID(memberID);
		assertNotNull(updatedServerPickList);
		assertTrue(serverPickList.getPickList().size() > 0);
		assertTrue("same pickID?",
				serverPickList.getPickList().get(0).getPickID().equals(pick.getPickID()));
		assertTrue("Should now be changed",
				serverPickList.getPickList().get(0).isStopUsing() != beforeIsStopUsing);
	}

	@Test
	public void test_addMemberList() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		// create a member
		MemberList memberList = new MemberList();
		memberList.addMemberName("Bob");

		// Test
		ServerMemberMap serverMemberMap = service.addMemberList(memberList);

		// Verify
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob") > 0);

		// create another member
		MemberList newMemberList = new MemberList();
		newMemberList.addMemberName("Hank");

		// Test
		serverMemberMap = service.addMemberList(newMemberList);

		// Verify
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob") > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Hank") > 0);
	}

	@Test
	public void test_updateMemberNameByMemberID() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		// create a member
		MemberList memberList = new MemberList();
		memberList.addMemberName("Bob");
		memberList.addMemberName("Hank");

		ServerMemberMap serverMemberMap = service.addMemberList(memberList);

		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob") > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Hank") > 0);
		int bobMemberID = serverMemberMap.getMemberIDMap().get("Bob");
		int hankMemberID = serverMemberMap.getMemberIDMap().get("Hank");
		assertTrue(bobMemberID != hankMemberID);

		// Test
		serverMemberMap = service.updateMemberNameByMemberID(bobMemberID, "Bob", "Bob-ster");

		// Verify
		serverMemberMap = service.addMemberList(memberList);

		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob-ster") == bobMemberID);
		assertTrue(serverMemberMap.getMemberIDMap().get("Hank") == hankMemberID);
	}

	@Test
	public void test_deleteMemberByID() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		// create a member
		MemberList memberList = new MemberList();
		memberList.addMemberName("Bob");
		memberList.addMemberName("Hank");

		ServerMemberMap serverMemberMap = service.addMemberList(memberList);

		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob") > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Hank") > 0);
		int bobMemberID = serverMemberMap.getMemberIDMap().get("Bob");
		int hankMemberID = serverMemberMap.getMemberIDMap().get("Hank");
		assertTrue(bobMemberID != hankMemberID);

		// Test
		service.deleteMemberByID(bobMemberID);

		// Verify
		serverMemberMap = service.getMemberMap();
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob-ster") == null);
		assertTrue(serverMemberMap.getMemberIDMap().get("Hank") == hankMemberID);
	}

	@Test
	public void test_deleteMemberByID_yourself() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int ownerMemberID = service.getMyMemberID();

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
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		MemberList memberList = new MemberList();
		memberList.addMemberName("Bob");
		ServerMemberMap serverMemberMap = service.addMemberList(memberList);
		assertTrue(serverMemberMap.getMemberIDMap().size() > 0);
		assertTrue(serverMemberMap.getMemberIDMap().get("Bob") > 0);

		// Test
		ServerMemberMap FoundServerMemberMap = service.getMemberMap();

		// Verify
		assertTrue(FoundServerMemberMap.getMemberIDMap().size() > 0);
		assertTrue(FoundServerMemberMap.getMemberIDMap().get("Bob") > 0);
	}

	@Test
	public void test_createCategoryByMemberID() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);
		String testCategoryName = "Cat's with funny hats";
		ServerMemberCategoryList serverMemberCategoryList = service
				.getCategoryListByMemberID(memberID);

		if (serverMemberCategoryList.getMemberCategoryList().getCategoryList()
				.contains(testCategoryName)) {
			MemberCategoryList memberCategoryList = new MemberCategoryList(memberID,
					serverMemberCategoryList.getMemberCategoryList().getCategoryList());
			service.deleteCategoryListByMemberCategoryList(memberCategoryList);
		}

		// Test
		ServerMemberCategoryList foundServerMemberCategoryList = service
				.createCategoryByMemberID(memberID, testCategoryName);

		// Verify
		assertNotNull(foundServerMemberCategoryList);
		assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getCategoryList()
				.size() == 1);
		assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getCategoryList().get(0)
				.equals(testCategoryName));
		assertTrue(foundServerMemberCategoryList.getMemberCategoryList().getMemberID() == memberID);
	}

	@Test
	public void test_getCategoryListByMemberID() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);
		String testCategoryName = "Cat's with funny hats";
		// delete previous test update if needed
		deleteTestCategory(memberID, testCategoryName);
		service.createCategoryByMemberID(memberID, testCategoryName);

		// Test
		ServerMemberCategoryList serverMemberCategoryList = service
				.getCategoryListByMemberID(memberID);

		// Verify
		assertNotNull(serverMemberCategoryList);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().size() > 0);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getMemberID() == memberID);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList()
				.contains(testCategoryName));
	}

	@Test
	public void test_updateCategoryNameByMemberID() throws SkipUseException {
		// Set up
		String oldCategoryName = "My Musick";
		String newCategoryName = "My Music";
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
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
		ServerMemberCategoryList serverMemberCategoryList = service
				.getCategoryListByMemberID(memberID);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList()
				.isEmpty() == false);
	}

	@Test
	public void test_deleteCategoryListByMemberCategoryList() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);

		List<String> categoryList = new ArrayList<>();
		String categoryToDelete = "delete me category";
		categoryList.add(categoryToDelete);
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);

		// first add the category
		try {
			ServerMemberCategoryList serverMemberCategoryList = service
					.createCategoryByMemberID(memberID, categoryToDelete);
			assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList()
					.contains(categoryToDelete));
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
		ServerMemberCategoryList updatedServerMemberCategoryList = service
				.getCategoryListByMemberID(memberID);
		assertTrue(updatedServerMemberCategoryList.getMemberCategoryList().getCategoryList()
				.contains(categoryToDelete) == false);
	}

	@Test
	public void test_markCategoryPickIDCollection() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);

		String testCategoryName = "Cat's with funny hats";

		// delete previous test update if needed
		deleteTestCategory(memberID, testCategoryName);

		// create a category
		List<String> categoryList = new ArrayList<>();
		categoryList.add(testCategoryName);
		ServerMemberCategoryList serverMemberCategoryList = service
				.createCategoryByMemberID(memberID, testCategoryName);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0)
				.equals(testCategoryName));

		// create pick collection
		String collectionName = "My collection";
		List<String> collectionIDList = new ArrayList<>();
		collectionIDList.add("A,B,C");
		PickIDCollection pickIDCollection = new PickIDCollection();
		pickIDCollection.setCollectionName(collectionName);
		pickIDCollection.setPickIDList(collectionIDList);
		pickIDCollection.setSplitCSV(true);
		ServerPickIDCollection createdCollection = service.setPickIDCollection(pickIDCollection);

		// Mark the pick with the category
		CategoryPickIDCollection categoryPickIDCollection = new CategoryPickIDCollection();
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
		PickIDCollection choosePickIDCollection = new PickIDCollection(
				createdCollection.getPickIDCollection().getCollectionName());
		choosePickIDCollection.addPickID("B");
		categoryPickIDCollection.setPickIDCollection(choosePickIDCollection);

		// Test
		service.markCategoryPickIDCollection(categoryPickIDCollection);

		// Verify
		// look for Picks by category
		PickQuery pickQuery = new PickQuery();
		// look for the category
		pickQuery.setCategories(categoryList);
		pickQuery.setHowMany(50);
		// get the exact query amount
		pickQuery.setGetMorePicksIfShort(false);
		// do not add new Picks
		pickQuery.setNewMixInPercentage(0);
		// from the collection
		pickQuery.setCollectionID(createdCollection.getPickIDCollection().getCollectionID());
		// include the category info for the Picks
		pickQuery.setIncludeCategories(true);

		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertNotNull(serverPickList);
		assertTrue(serverPickList.getPickList().size() == 1);
		assertTrue(serverPickList.getPickList().get(0).getPickID().equals("B"));
		assertTrue(serverPickList.getPickList().get(0).getCategoryList().size() == 1);
		assertTrue(serverPickList.getPickList().get(0).getCategoryList().get(0)
				.equals(testCategoryName));

	}

	@Test
	public void test_unmarkCategoryPickIDCollection() throws SkipUseException {
		// Set up
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);

		String testCategoryName = "Cat's with funny hats";

		// delete previous test update if needed
		deleteTestCategory(memberID, testCategoryName);

		// create a category
		List<String> categoryList = new ArrayList<>();
		categoryList.add(testCategoryName);
		ServerMemberCategoryList serverMemberCategoryList = service
				.createCategoryByMemberID(memberID, testCategoryName);
		assertTrue(serverMemberCategoryList.getMemberCategoryList().getCategoryList().get(0)
				.equals(testCategoryName));

		// create pick collection
		String collectionName = "My collection";
		List<String> collectionIDList = new ArrayList<>();
		collectionIDList.add("A,B,C");
		PickIDCollection pickIDCollection = new PickIDCollection();
		pickIDCollection.setCollectionName(collectionName);
		pickIDCollection.setPickIDList(collectionIDList);
		pickIDCollection.setSplitCSV(true);
		ServerPickIDCollection createdCollection = service.setPickIDCollection(pickIDCollection);

		// Mark the pick with the category
		CategoryPickIDCollection categoryPickIDCollection = new CategoryPickIDCollection();
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		categoryPickIDCollection.setMemberCategoryList(memberCategoryList);
		PickIDCollection choosePickIDCollection = new PickIDCollection(
				createdCollection.getPickIDCollection().getCollectionName());
		choosePickIDCollection.addPickID("B");
		categoryPickIDCollection.setPickIDCollection(choosePickIDCollection);

		service.markCategoryPickIDCollection(categoryPickIDCollection);

		PickQuery pickQuery = new PickQuery();
		// look for the category
		pickQuery.setCategories(categoryList);
		pickQuery.setHowMany(50);
		// get the exact query amount
		pickQuery.setGetMorePicksIfShort(false);
		// do not add new Picks
		pickQuery.setNewMixInPercentage(0);
		// from the collection
		pickQuery.setCollectionID(createdCollection.getPickIDCollection().getCollectionID());
		// include the category info for the Picks
		pickQuery.setIncludeCategories(true);

		ServerPickList serverPickList = service.setPickQuery(pickQuery);
		assertNotNull(serverPickList);
		assertTrue(serverPickList.getPickList().size() == 1);
		assertTrue(serverPickList.getPickList().get(0).getPickID().equals("B"));
		assertTrue(serverPickList.getPickList().get(0).getCategoryList().size() == 1);
		assertTrue(serverPickList.getPickList().get(0).getCategoryList().get(0)
				.equals(testCategoryName));

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
		service.login(EMAIL, PASSWORD);
		assertTrue(service.isLoggedIn());

		// Test
		service.errorTest();

		// Verify
		ServerResponse serverResponse = service.getLastServerResponseData();
		assertNotNull(serverResponse);
		assertTrue(serverResponse.getStatus() == HttpStatus.BAD_REQUEST);
		assertFalse("There should be an error message", serverResponse.getErrorMessage().isEmpty());
		assertFalse("There should be a message with help", serverResponse.getMessage().isEmpty());

		// should still have all the other communication parts
		assertTrue(serverResponse.getMemberID() > -1);
		assertFalse(serverResponse.getMemberName().isEmpty());
		assertFalse(serverResponse.getProxyID().isEmpty());
		assertFalse(serverResponse.getSkipUseToken().isEmpty());

		// Verify can still use the service
		ServerPickIDCollection foundServerCollection = service.getServerPickIDCollection();
		assertNotNull(foundServerCollection);
		List<String> foundCollectionList = foundServerCollection.getPickIDCollection()
				.getPickIDList();
		assertNotNull(foundCollectionList);
	}

	private void deleteTestCategory(int memberID, String categoryName) throws SkipUseException {
		ServerMemberCategoryList currentServerMemberCategoryList = service
				.getCategoryListByMemberID(memberID);
		List<String> currentCategoryList = currentServerMemberCategoryList.getMemberCategoryList()
				.getCategoryList();
		List<String> categoryList = new ArrayList<>();
		categoryList.add(categoryName);
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryList);
		if (currentCategoryList.contains(categoryName))
			service.deleteCategoryListByMemberCategoryList(memberCategoryList);
	}

}
