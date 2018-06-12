package com.autogilmore.throwback.skipUsePackage.manager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.enums.SearchMode;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;

/* 
 * Tests for verifying the SkipUseManager's use of the SkipUseAPIService.class
 * Run and modify these tests to if you plan on using the manager as a starting point for integrating into your code.
 * NOTE: this is ONLY example code for how you could access the SkipUse API. This code may or may not work if the API changes.
*/
public class SkipUseManagerTest {
	// NOTE: Set these values to use your test credentials as these demo ones
	// will be unstable from other people using them and causing failed tests.
	private static final String EMAIL = "basic-demo@skipuse.com";
	private static final String PASSWORD = "password";

	// NOTE: These tests create a temporary member so that the owner and other
	// members won't get test Picks in their history. Change this name if it
	// conflicts with a current member name.
	private static final String TEST_MEMBER = "test member";

	// Manager class under test
	private SkipUseManager manager = SkipUseManager.getInstance();

	// After each test, remove the test member.
	@After
	public void after() {
		try {
			int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
			if (testMemberID != -1)
				manager.deleteMemberByID(testMemberID);
			manager.logout();
		} catch (SkipUseException e) {
			// noop (no operation)
		}
	}

	// The manager has a check to see if the API server is running.
	@Test
	public void test_isAPIServerUp() throws SkipUseException {
		// Test
		boolean isSkipUseserviceUp = manager.isAPIServerUp();

		// Verify
		assertTrue(isSkipUseserviceUp);
	}

	// The manager has a method to login to the API.
	@Test
	public void test_login() throws SkipUseException {
		// Set up
		assertFalse(manager.isLoggedIn());

		// Test
		manager.login(EMAIL, PASSWORD);

		// Verify
		assertTrue(manager.isLoggedIn());
	}

	// There is an automatic sign-in option in case the API times-out and a
	// login is then needed.
	@Test
	public void test_autoSignIn() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		assertTrue(testMemberID > 0);
		manager.logout();
		assertFalse(manager.isLoggedIn());

		// NOTE: this test will fail if you are using the demo login
		// Test
		MemberCategoryList memberCategoryList = manager.getCategoryListForMember(testMemberID);

		// Verify
		assertTrue("Should automatically log-in", manager.isLoggedIn());
		List<String> categoryList = memberCategoryList.getCategoryList();
		assertNotNull(categoryList);
	}

	// The manager should still work after an error occurs.
	@Test
	public void test_afterAnError() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		assertTrue(testMemberID > 0);

		try {
			manager.getCategoryListForMember(-1);
			fail("Expected to throw an error");
		} catch (SkipUseException e) {
			// Ignore
		}

		// Test
		MemberCategoryList memberCategoryList = manager.getCategoryListForMember(testMemberID);

		// Verify
		assertTrue("Should automatically log-in", manager.isLoggedIn());
		List<String> categoryList = memberCategoryList.getCategoryList();
		assertNotNull(categoryList);
	}

	// The service has a logout method and isLoggedIn as well.
	@Test
	public void test_logout() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());

		// Test
		manager.logout();

		// Verify
		assertFalse(manager.isLoggedIn());
	}

	// Members can be added with the addMemberName method.
	@Test
	public void test_addMemberName() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());

		// Test
		manager.addMemberName("Bub");
		int bubMemberID = manager.getMemberIDByName("Bub");

		// Verify
		assertTrue(bubMemberID > 0);
		assertTrue(bubMemberID != manager.getOwnerMemberID());
	}

	// Each member has their own ID and the account owner has a permanent member
	// ID.
	@Test
	public void test_getMemberIDByName() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName("Bub");

		// Test
		int testMemberID = manager.getMemberIDByName("Bub");

		// Verify
		assertTrue(testMemberID > 0);
		assertTrue("The owner's ID is different than a member's ID",
				manager.getOwnerMemberID() != testMemberID);
	}

	// If a member is not found, -1 is returned for an ID.
	@Test
	public void test_getMemberIDByName_notFound() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());

		// Test
		int testMemberID = manager.getMemberIDByName("Dewey");

		// Verify
		assertTrue(testMemberID == -1);
	}

	// A member name can be updated.
	@Test
	public void test_updateMemberNameByID() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName("Dewey");
		int testMemberID = manager.getMemberIDByName("Dewey");
		assertTrue(testMemberID > 0);

		// Test
		manager.updateMemberNameByID(testMemberID, "Dewey", "Louie");

		// Verify
		assertTrue(manager.getMemberIDByName("Dewey") == -1);
		assertTrue(manager.getMemberIDByName("Louie") == testMemberID);
	}

	// Remove all of a member's data by their ID.
	@Test
	public void test_deleteMemberByID() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName("Dewey");
		int testMemberID = manager.getMemberIDByName("Dewey");
		assertTrue(testMemberID > 0);

		// Test
		manager.deleteMemberByID(testMemberID);

		// Verify
		testMemberID = manager.getMemberIDByName("Dewey");
		assertTrue(testMemberID == -1);
	}

	// A collection is a Pick ID list that is shared among all members. Set it
	// using the addPickIDCollection method. The collection can be
	// comma-delimited and will be separated into separate entries when the last
	// parameter of addPickIDCollection is set to 'true'.
	@Test
	public void test_addPickIDCollection() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A,B,C,D");
		collectionList.add("D");
		collectionList.add("C");

		// Test
		PickIDCollection pickIDCollection = manager.addPickIDCollection(collectionName,
				collectionList, true);

		// Verify
		List<String> foundCollectionList = pickIDCollection.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 4);
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("D")));
	}

	// Get the current collection by calling getPickIDCollection method.
	@Test
	public void test_getPickIDCollection() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A,B,C, D");
		collectionList.add("D");
		collectionList.add("C");
		PickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionName,
				collectionList, false);
		assertTrue(createPickIDCollection.getCollectionID() > 0);

		// Test
		PickIDCollection foundPickIDCollection = manager.getPickIDCollection();

		// Verify
		List<String> foundCollectionList = foundPickIDCollection.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 3);
		assertTrue(foundPickIDCollection.getCollectionName().equals(collectionName));
		// see that the addPickIDCollection was set to 'false' an was not split
		// by a comma
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("A,B,C, D")));
	}

	// Each member has their own history of a Pick ID from the Pick ID
	// collection called a 'Pick'. A Pick has additional data such as it's
	// popularity, stats and optional JSON value.
	// Get all Picks for a member by calling the getAllPickListByMemberID
	// method.
	@Test
	public void test_getAllPickListByMemberID() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		assertTrue(testMemberID > 0);
		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add("A,B,C, D");
		PickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionName,
				collectionList, true);
		assertTrue(createPickIDCollection.getPickIDList().size() == 4);

		// create a "Skipped" Pick which will now not be considered 'new'
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, "C");

		// Test
		List<Pick> allPickMemberPickList = manager.getAllPickListByMemberID(testMemberID);

		// Verify
		assertNotNull(allPickMemberPickList);
		assertTrue(allPickMemberPickList.size() == 4);
		boolean isAFound = false;
		boolean isBFound = false;
		boolean isCFound = false;
		boolean isDFound = false;
		for (Pick pick : allPickMemberPickList) {
			if (pick.getMyPickID().equals("A")) {
				isAFound = true;
				assertTrue("Pick should be new", pick.isNewPick());
			}
			if (pick.getMyPickID().equals("B")) {
				isBFound = true;
				assertTrue("Pick should be new", pick.isNewPick());
			}
			if (pick.getMyPickID().equals("C")) {
				isCFound = true;
				assertFalse("Pick should NOT be new", pick.isNewPick());
			}
			if (pick.getMyPickID().equals("D")) {
				isDFound = true;
				assertTrue("Pick should be new", pick.isNewPick());
			}
		}
		assertTrue(isAFound);
		assertTrue(isBFound);
		assertTrue(isCFound);
		assertTrue(isDFound);
	}

	// A Pick Query is way to get back Picks for member/s. The query has many
	// combinations to find Picks. One example is looking for Picks that have
	// been marked as not to be used indicated by the Pick 'isStopUsing' flag.
	@Test
	public void test_getMemberPickListByPickQuery() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		String pickID = "E";

		// underscores are used here indicate that a variable or method could be
		// or possibly return null.
		Pick _currentPick = null;
		List<Pick> currentPickList = manager.getAllPickListByMemberID(testMemberID);
		for (Pick foundPick : currentPickList) {
			if (foundPick.getMyPickID().equals(pickID))
				_currentPick = foundPick;
		}

		// don't have a Pick yet so create it.
		if (_currentPick == null) {
			PickIDCollection pickIDCollection = manager.getPickIDCollection();
			pickIDCollection.addPickID(pickID);
			manager.addPickIDCollection(pickIDCollection);
		}

		// mark the pick 'stop using' to find by the query
		manager.setStopUsingByMemberIDPickIDTrueFalse(testMemberID, pickID, true);

		// the PickQuery:
		PickQuery pickQuery = new PickQuery();
		// looking for this member
		pickQuery.addToMemberIDList(testMemberID);
		// for Picks marked as 'StopUsing'
		pickQuery.setSearchMode(SearchMode.STOPUSING);
		// don't send back more if none are found
		pickQuery.setGetMorePicksIfShort(false);

		// Test
		List<Pick> pickList = manager.getMemberPickListByPickQuery(pickQuery);

		// Verify
		assertTrue(pickList.size() > 0);
		boolean isPickFound = false;
		for (Pick pick : pickList) {
			if (pick.getMyPickID().equals(pickID)) {
				isPickFound = true;
				assertTrue("Pick should be marked 'stop using'", pick.isStopUsing());
			}
		}
		assertTrue(isPickFound);

		// NOTE: once the PickQuery has been set, the regular GET method
		// getMemberPickListByPickQuery should return the
		// same query result.

		// Test
		pickList = manager.getMemberPickListByPickQuery();

		// Verify
		isPickFound = false;
		for (Pick pick : pickList) {
			if (pick.getMyPickID().equals(pickID)) {
				isPickFound = true;
			}
		}
		assertTrue(isPickFound);
	}

	// You can also just get a Pick for a member by the Pick ID.
	@Test
	public void test_getPickByMemberIDAndPickID() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		String pickID = "adfg";
		String collectionName = "My collection";
		List<String> collectionList = new ArrayList<>();
		collectionList.add(pickID);
		PickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionName,
				collectionList, false);
		assertTrue(createPickIDCollection.getCollectionID() > 0);

		// Test
		// NOTE: the underscore on the method indicating a null could be
		// returned.
		Pick _pick = manager._getPickByMemberIDAndPickID(testMemberID, pickID);

		// Verify
		assertTrue(_pick == null);

		// create a Pick
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, pickID);

		// Test with a Pick
		_pick = manager._getPickByMemberIDAndPickID(testMemberID, pickID);
		assertNotNull(_pick);
		assertTrue(_pick.getMyPickID().equals(pickID));
		assertTrue(_pick.getSkipped() > 0);
	}

	// Member's can add categories for their Picks.
	@Test
	public void test_createCategoryForMember() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		// Test
		MemberCategoryList memberCategoryList = manager.createCategoryForMember(testMemberID,
				"Bingo");

		// Verify
		List<String> categoryNameList = memberCategoryList.getCategoryList();
		assertNotNull(categoryNameList);
		assertTrue(categoryNameList.size() == 1);
		assertTrue(categoryNameList.get(0).equals("Bingo"));
		assertTrue(memberCategoryList.getMemberID() == testMemberID);
	}

	// Get a member's category list by using the getCategoryListForMember
	// method.
	@Test
	public void test_getCategoryListForMember() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		// add a category
		manager.createCategoryForMember(testMemberID, "Bingo");

		// Test
		MemberCategoryList memberCategoryList = manager.getCategoryListForMember(testMemberID);

		// Verify
		List<String> memberCategoryNameList = memberCategoryList.getCategoryList();
		assertNotNull(memberCategoryNameList);
		assertTrue(memberCategoryNameList.size() == 1);
		assertTrue(memberCategoryNameList.get(0).equals("Bingo"));
		assertTrue(memberCategoryList.getMemberID() == testMemberID);

		// Test: owner's categories
		assertTrue(testMemberID != manager.getOwnerMemberID());
		MemberCategoryList ownerCategoryList = manager
				.getCategoryListForMember(manager.getOwnerMemberID());

		// Verify: different than the member's categories
		assertTrue(ownerCategoryList.getMemberID() == manager.getOwnerMemberID());
		List<String> ownerCategoryNameList = ownerCategoryList.getCategoryList();
		assertTrue("Owner should not have this category.",
				ownerCategoryNameList.contains("Bingo") == false);
	}

	// A member's Pick can be updated to mark the 'stop using' or add additional
	// JSON data.
	@Test
	public void test_updatePickByMemberID_createAPickIfNewPickID() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		// create a shared Pick ID collection
		List<String> collectionList = new ArrayList<>();
		Date date = new Date();
		String pickID = date.toString();
		collectionList.add(pickID);
		manager.addPickIDCollection("new collection", collectionList, true);

		// get new a Pick by the PickQuery
		PickQuery pickQuery = new PickQuery();
		// for this member
		pickQuery.addToMemberIDList(testMemberID);
		// one Pick
		pickQuery.setHowMany(1);
		// it must be a new Pick
		pickQuery.setNewMixInPercentage(100);
		// return exactly this many with this criteria
		pickQuery.makeExactQuery();

		// make the query
		List<Pick> pickList = manager.getMemberPickListByPickQuery(pickQuery);
		Pick testPick = pickList.stream().filter(t -> t.getMyPickID().equals(date.toString()))
				.findFirst().orElse(null);
		assertNotNull(testPick);
		assertTrue(testPick.isNewPick());

		// pick attributes to update:
		Pick updateThisPick = new Pick();
		updateThisPick.setMyPickID(testPick.getMyPickID());
		// additional JSON you can set for your app
		updateThisPick.setMyJSON("{\"hello\": \"world\"}");
		// flag a pick to 'stop using' in normal queries
		updateThisPick.setStopUsing(true);

		// you can't change these values. they will be ignored by the API
		updateThisPick.setSkipped(20);
		updateThisPick.setUsed(30);
		updateThisPick.setAutoRatePercentage(200);
		updateThisPick.setTrendingRatePercentage(130);
		List<String> categoryList = new ArrayList<>();
		categoryList.add("Add categories");
		categoryList.add("using the");
		categoryList.add("create category for member method instead");
		updateThisPick.setCategoryList(categoryList);
		updateThisPick.setNewPick(true);

		// Test
		manager.updatePickByMemberID(testMemberID, updateThisPick);

		// Verify
		List<Pick> updatedPickList = manager.getAllPickListByMemberID(testMemberID);
		Pick updatedPick = updatedPickList.stream().filter(t -> t.getMyPickID().equals(pickID))
				.findFirst().orElse(null);
		assertNotNull("Should find the Pick", updatedPick);
		assertTrue(updatedPick.getMyPickID().equals(updateThisPick.getMyPickID()));

		// attributes we changed
		assertTrue("was: " + updatedPick.getMyJSON(),
				updatedPick.getMyJSON().equals("{\"hello\":\"world\"}"));
		assertTrue(updatedPick.isStopUsing() == updateThisPick.isStopUsing());

		// can't change these
		assertTrue(updatedPick.getAutoRatePercentage() != updateThisPick.getAutoRatePercentage());
		assertTrue(updatedPick.getTrendingRatePercentage() != updateThisPick
				.getTrendingRatePercentage());
		assertFalse(updatedPick.isNewPick());
		assertFalse(updatedPick.getCategoryList().contains("Add categories"));
		assertFalse(updatedPick.getSkipped() == updateThisPick.getSkipped());
		assertFalse(updatedPick.getUsed() == updateThisPick.getUsed());
	}

	// A member can update a category's name.
	@Test
	public void test_updateCategoryNameForMember() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		// Test
		manager.createCategoryForMember(testMemberID, "Bingo");

		// change the category name
		PatchName patchName = new PatchName("Bingo", "Bingo was his name - Oh!");

		// Test
		manager.updateCategoryNameForMember(testMemberID, patchName);

		// Verify
		currentMemberCategoryList = manager.getCategoryListForMember(testMemberID);
		assertTrue(currentMemberCategoryList.getCategoryList().size() == 1);
		assertTrue(currentMemberCategoryList.getCategoryList().get(0)
				.equals("Bingo was his name - Oh!"));
	}

	// Members can delete categories too.
	@Test
	public void test_deleteCategoryListForMember() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		MemberCategoryList memberCategoryList = manager.createCategoryForMember(testMemberID,
				"Bingo");

		List<String> categoryNameList = memberCategoryList.getCategoryList();
		assertNotNull(categoryNameList);
		assertTrue(categoryNameList.size() == 1);
		assertTrue(categoryNameList.get(0).equals("Bingo"));
		assertTrue(memberCategoryList.getMemberID() == testMemberID);

		// Test
		manager.deleteCategoryListForMember(memberCategoryList);

		// Verify
		categoryNameList = manager.getCategoryListForMember(testMemberID).getCategoryList();
		assertNotNull(categoryNameList);
		assertTrue(categoryNameList.size() == 0);
	}

	// To mark and un-mark a Pick with a category, use the
	// markPickIDListWithCategoryTrueFalse method.
	@Test
	public void test_markPickIDListWithCategoryTrueFalse() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		// add collection
		PickIDCollection pickIDCollection = new PickIDCollection("test collection");
		String pickID = "my Pick ID";
		pickIDCollection.addPickID(pickID);
		manager.addPickIDCollection(pickIDCollection);
		// add category
		String categoryName = "Bingo";
		manager.createCategoryForMember(testMemberID, categoryName);

		// get the picks with the category
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(testMemberID);
		pickQuery.addCategory(categoryName);
		pickQuery.makeExactQuery();

		// check we don't have any marked Picks yet
		List<Pick> pickList = manager.getMemberPickListByPickQuery(pickQuery);
		Pick _havePick = pickList.stream().filter(p -> p.getMyPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertTrue(_havePick == null);

		// Test
		manager.markPickIDListWithCategoryTrueFalse(testMemberID, pickID, categoryName, true);

		// Verify
		pickQuery.setIncludeCategories(true);
		pickList = manager.getMemberPickListByPickQuery(pickQuery);

		Pick markedPick = pickList.stream().filter(p -> p.getMyPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(markedPick);
		assertTrue(markedPick.getCategoryList().contains(categoryName));

		// now, unmark it.
		// Test
		manager.markPickIDListWithCategoryTrueFalse(testMemberID, pickID, categoryName, false);

		// Verify
		pickList = manager.getMemberPickListByPickQuery(pickQuery);

		Pick unmarkedPick = pickList.stream().filter(p -> p.getMyPickID().equals(pickID))
				.findFirst().orElse(null);
		assertTrue(unmarkedPick == null);
	}

	// Another way to simply stop using a Pick is to use the
	// setStopUsingByMemberIDPickIDTrueFalse method. Setting this is a simple
	// way to remove a Pick if a member no longer wants to get it in normal
	// usage.
	@Test
	public void test_setStopUsingByMemberIDPickIDTrueFalse() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);

		// add collection
		String pickID = "Normal Pick";
		String stopUsingPickID = "Stop Using Pick";
		PickIDCollection pickIDCollection = new PickIDCollection("test collection");
		pickIDCollection.addPickID(pickID);
		pickIDCollection.addPickID(stopUsingPickID);
		manager.addPickIDCollection(pickIDCollection);

		boolean isStopUsing = true;

		// Test
		manager.setStopUsingByMemberIDPickIDTrueFalse(testMemberID, stopUsingPickID, isStopUsing);

		// Verify
		List<Pick> pickList = manager.getAllPickListByMemberID(testMemberID);
		Pick reqularPick = pickList.stream().filter(t -> t.getMyPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(reqularPick);
		assertTrue(reqularPick.isStopUsing() == false);

		Pick stopUsingPick = pickList.stream().filter(t -> t.getMyPickID().equals(stopUsingPickID))
				.findFirst().orElse(null);
		assertTrue(stopUsingPick.isStopUsing());

		// and verify stop using is not included in a normal pick query
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(testMemberID);
		List<Pick> foundPickList = manager.getMemberPickListByPickQuery(pickQuery);
		stopUsingPick = foundPickList.stream().filter(t -> t.getMyPickID().equals(stopUsingPickID))
				.findFirst().orElse(null);
		assertTrue("Should not be included in normal queries", stopUsingPick == null);
	}

	// The skipUsePass method is how Picks are changed for future PickQueries.
	// The more a member's Pick is 'Skipped', it will be presented less-often in
	// normal queries. And when a Pick is 'used', it will show up more often.
	// The Pick skip and used count is changed when this occurs. Pick's also
	// have an auto-rate and a trending-rate percentage to show the popularity.
	@Test
	public void test_skipUsePass() throws SkipUseException {
		// Set up
		manager.login(EMAIL, PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER);
		int testMemberID = manager.getMemberIDByName(TEST_MEMBER);
		PickIDCollection pickIDCollection = manager.getPickIDCollection();
		assertTrue(pickIDCollection.getPickIDList().size() > 0);
		String pickID = pickIDCollection.getPickIDList().get(0);
		assertTrue(pickID.length() > 0);
		List<Pick> pickList = manager.getAllPickListByMemberID(testMemberID);
		Pick testPick = pickList.stream().filter(t -> t.getMyPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(testPick);
		int startingSkipCount = testPick.getSkipped();
		int startingTrendingRatePercentage = testPick.getTrendingRatePercentage();

		// Test
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, pickID);

		// Verify
		List<Pick> updatedPickList = manager.getAllPickListByMemberID(testMemberID);
		Pick foundPick = updatedPickList.stream().filter(t -> t.getMyPickID().equals(pickID))
				.findFirst().orElse(null);
		assertNotNull(foundPick);
		int endSkipCount = foundPick.getSkipped();
		assertTrue(endSkipCount == startingSkipCount + 1);
		assertTrue("The trending percentage will not change until the next update",
				foundPick.getTrendingRatePercentage() == startingTrendingRatePercentage);

		// try a 'Used'
		// Test
		manager.skipUsePass(SkipUsePass.USE, testMemberID, pickID);
		updatedPickList = manager.getAllPickListByMemberID(testMemberID);
		foundPick = updatedPickList.stream().filter(t -> t.getMyPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(foundPick);
		assertTrue(foundPick.getSkipped() == startingSkipCount + 1);
		assertTrue(foundPick.getUsed() == 1);
		assertTrue("After a 'use' the trending percentage should raise",
				foundPick.getTrendingRatePercentage() > startingTrendingRatePercentage);
	}
}
