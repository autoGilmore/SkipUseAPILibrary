package com.autogilmore.throwback.skipUsePackage.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

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
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;

public class SkipUseAPIServiceTest {
	// Set the SkipUseAPI URL here. See API documentation for more information.
	private static final String SKIP_USE_API_URL = "http://www.skipuseapi.com/v1";

	// Set these to use your own test credentials as the demo account is
	// unstable from other people's usage.
	private static final String EMAIL = "basic-demo@skipuse.com";
	private static final String PASSWORD = "password";

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
	public void getServerPickListByMemberID() throws SkipUseException {
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
		assertTrue(!pick.getMyPickID().isEmpty());
		int startingPickSkipCount = pick.getSkipped();

		MemberPickIDList memberPickIDList = new MemberPickIDList();
		int memberID = service.getMyMemberID();
		assertTrue(memberID > 0);
		memberPickIDList.addMemberID(memberID);
		memberPickIDList.addPickID(pick.getMyPickID());

		// Test
		service.skipUsePassPick(SkipUsePass.SKIP, memberPickIDList);

		// Verify
		ServerPickList updatedServerPickList = service.getServerPickList();
		List<Pick> updatePickList = updatedServerPickList.getPickList();
		assertTrue("was: " + pickList.size(), pickList.size() > 0);
		Pick updatedPick = null;
		for (Pick element : updatePickList) {
			if (element.getMyPickID().equals(pick.getMyPickID())) {
				updatedPick = element;
				break;
			}
		}
		assertNotNull("Should have found the updated pick", updatedPick);
		assertTrue(updatedPick.getSkipped() == startingPickSkipCount + 1);
	}

	@Test
	public void updatePickByMemberID_changeIsStopUsing() throws SkipUseException {
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
		service.updatePickByMemberID(memberID, pick);

		// Verify
		ServerPickList updatedServerPickList = service.getAllServerPickListByMemberID(memberID);
		assertNotNull(updatedServerPickList);
		assertTrue(serverPickList.getPickList().size() > 0);
		assertTrue("same pickID?",
				serverPickList.getPickList().get(0).getMyPickID().equals(pick.getMyPickID()));
		assertTrue("Should now be changed",
				serverPickList.getPickList().get(0).isStopUsing() != beforeIsStopUsing);
	}

	@Test
	public void test_createMember() throws SkipUseException {
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
		assertTrue(serverPickList.getPickList().get(0).getMyPickID().equals("B"));
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
		assertTrue(serverPickList.getPickList().get(0).getMyPickID().equals("B"));
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