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
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberListPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Profile;
import com.autogilmore.throwback.skipUsePackage.enums.SearchMode;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;

/* 
 * Tests for verifying the SkipUseManager's use of the SkipUseAPIService.class
 * Run and modify these tests to if you plan on using the manager as a starting point for integrating into your code.
 * NOTE: this is ONLY example code for how you could access the SkipUse API. This code may or may not work if the API changes.
*/
public class SkipUseManagerTest {
	// Manager class under test.
	private final SkipUseManager manager = SkipUseManager.getInstance();

	// NOTE: Set these values to use your test credentials as these demo ones
	// will be unstable from other people using them and causing failed tests.
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
			long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
			if (testMemberID != 0)
				manager.deleteMemberByID(testMemberID);
			testMemberID = manager.getMemberIDByName(TEST_MEMBER_SUE);
			if (testMemberID != 0)
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
		manager.login(TEST_EMAIL, TEST_PASSWORD);

		// Verify
		assertTrue(manager.isLoggedIn());
	}

	// There is an automatic sign-in option in case the API times-out and a
	// login is then needed.
	@Test
	public void test_autoSignIn() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		// after login add a member.
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID > 0);

		// logout.
		manager.logout();
		assertFalse(manager.isLoggedIn());

		// then try to use a function, while not logged in.
		// NOTE: this test will fail if you are using the demo login to test
		// with.

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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID > 0);

		try {
			manager.getCategoryListForMember(1);
			fail("Expected to throw an error");
		} catch (SkipUseException e) {
			// ignore.
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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		// Test
		manager.addMemberName(TEST_MEMBER_BOB);
		long bobMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// Verify
		assertTrue(bobMemberID > 0);
		assertTrue(bobMemberID != manager.getOwnerMemberID());
	}

	// Each member has their own ID and the account owner has a permanent member
	// ID.
	@Test
	public void test_getMemberIDByName() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);

		// Test
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// Verify
		assertTrue(testMemberID > 0);
		assertTrue("The owner's ID is different than a member's ID",
				manager.getOwnerMemberID() != testMemberID);
	}

	// If a member is not found, 0 is returned for an ID.
	@Test
	public void test_getMemberIDByName_notFound() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		// testing by the name 'Dewey' If it exists, fail the test. Change this
		// name to a non-member name if needed.
		long deweyMemberID = manager.getMemberIDByName("Dewey");
		if (deweyMemberID != 0)
			fail("We need a member name that does not exist for this test");

		// Test
		long testMemberID = manager.getMemberIDByName("Dewey");

		// Verify
		assertTrue(testMemberID == 0);
	}

	// A member name can be updated.
	@Test
	public void test_updateMemberNameByID() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		manager.addMemberName(TEST_MEMBER_BOB);
		long bobMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(bobMemberID > 0);

		// remove Sue if needed.
		long sueMemberID = manager.getMemberIDByName(TEST_MEMBER_SUE);
		if (sueMemberID != 0)
			manager.deleteMemberByID(sueMemberID);

		// Test
		manager.updateMemberNameByID(bobMemberID, TEST_MEMBER_BOB, TEST_MEMBER_SUE);

		// Verify
		assertTrue(manager.getMemberIDByName(TEST_MEMBER_BOB) == 0);
		assertTrue(manager.getMemberIDByName(TEST_MEMBER_SUE) == bobMemberID);
	}

	// Remove a member by their ID.
	@Test
	public void test_deleteMemberByID() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID > 0);

		// Test
		manager.deleteMemberByID(testMemberID);

		// Verify
		testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID == 0);
	}

	// A collection is a Pick ID list that is shared among all members. Set it
	// using the addPickIDCollection method. The collection can be
	// comma + space delimited and will be separated into separate entries when
	// the last parameter of addPickIDCollection is set to 'true'.
	@Test
	public void test_addPickIDCollection() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		List<String> collectionList = new ArrayList<String>();
		// you can split Picks by using a comma and a space.
		// NOTE: this won't be split.
		collectionList.add("A,B,C,D");
		// NOTE: this will be split.
		collectionList.add("D, C");

		// Test
		MemberPickIDCollection memberPickIDCollection = manager.addPickIDCollection(collectionList,
				true);

		// Verify
		List<String> foundCollectionList = memberPickIDCollection.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 3);
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("A,B,C,D")));
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("D")));
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("C")));
	}

	// If you make a mistake with your Pick ID collection, you might be able to
	// undo the last change.
	@Test
	public void test_setPickIDCollection_undo() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		String collectionName = "My collection";
		List<String> collectionPickIDList = new ArrayList<>();
		collectionPickIDList.add("A, B, C");
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(
				manager.getOwnerMemberID());
		memberPickIDCollection.setCollectionName(collectionName);
		memberPickIDCollection.setPickIDList(collectionPickIDList);
		// don't split it by comma
		memberPickIDCollection.setSplitCSV(false);

		// set the collection.
		manager.addPickIDCollection(memberPickIDCollection);
		MemberPickIDCollection foundPickIDCollection = manager.getPickIDCollection();
		assertNotNull(foundPickIDCollection);
		// we have one long Pick: A, B, C.
		assertTrue("was: " + foundPickIDCollection.getPickIDList().get(0),
				foundPickIDCollection.getPickIDList().get(0).equals("A, B, C"));

		// now let's split it by 'accident.'
		memberPickIDCollection.setSplitCSV(true);

		// make the change.
		manager.addPickIDCollection(memberPickIDCollection);

		foundPickIDCollection = manager.getPickIDCollection();
		// whoops, we now have 3 Pick IDs.
		assertTrue("was: " + foundPickIDCollection.getPickIDList().size(),
				foundPickIDCollection.getPickIDList().size() == 3);

		// let's undo the last change for the owner's collection.
		long ownerCollectionID = manager.getOwnerMemberID();

		// Test
		manager.undoLastPickIDCollectionChange(ownerCollectionID);

		// Verify
		foundPickIDCollection = manager.getPickIDCollection();
		// the last comma split change should now be undone.
		assertTrue("was: " + foundPickIDCollection.getPickIDList().size(),
				foundPickIDCollection.getPickIDList().size() == 1);
		assertTrue("was: " + foundPickIDCollection.getPickIDList().get(0),
				foundPickIDCollection.getPickIDList().get(0).equals("A, B, C"));
	}

	// Get the current collection by calling getPickIDCollection method.
	@Test
	public void test_getPickIDCollection() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		List<String> collectionList = new ArrayList<String>();
		collectionList.add("A,B,C, D");
		collectionList.add("D");
		collectionList.add("C");
		MemberPickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionList,
				false);
		assertTrue(createPickIDCollection.getMemberCollectionID() > 0);

		// Test
		MemberPickIDCollection foundPickIDCollection = manager.getPickIDCollection();

		// Verify
		List<String> foundCollectionList = foundPickIDCollection.getPickIDList();
		assertNotNull(foundCollectionList);
		assertTrue(foundCollectionList.size() == 3);
		assertTrue(foundPickIDCollection.getCollectionName()
				.equals(SkipUseProperties.PICK_ID_COLLECTION_NAME));
		// see that the addPickIDCollection was set to 'false' an was not split
		// by a comma.
		assertTrue(foundCollectionList.stream().anyMatch(t -> t.equals("A,B,C, D")));
	}

	// Each member has their own history of a Pick ID from the Pick ID
	// collection called a 'Pick'. A Pick has additional data such as it's
	// popularity, stats and optional JSON value.
	// Get all Picks created for a member by calling the
	// getAllPickListByMemberID method.
	@Test
	public void test_getAllPickListByMemberID() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID > 0);

		List<String> collectionList = new ArrayList<String>();
		collectionList.add("A, B, C, D");
		MemberPickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionList,
				true);
		assertTrue("Should have 4 Picks in the collection",
				createPickIDCollection.getPickIDList().size() == 4);

		// create a "Skipped" Pick which will now not be considered 'new.'
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, "B");
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, "C");

		// Test
		List<Pick> allPickMemberPickList = manager.getAllPickListByMemberID(testMemberID,
				INCLUDE_CATEGORY_INFO);

		// Verify
		assertTrue("Should have created two Picks", allPickMemberPickList.size() == 2);
		boolean isAFound = false;
		boolean isBFound = false;
		boolean isCFound = false;
		boolean isDFound = false;
		for (Pick pick : allPickMemberPickList) {
			if (pick.getPickID().equals("A")) {
				isAFound = true;
			}
			if (pick.getPickID().equals("B")) {
				isBFound = true;
				assertFalse("Pick should NOT be new", pick.isNewPick());
			}
			if (pick.getPickID().equals("C")) {
				isCFound = true;
				assertFalse("Pick should NOT be new", pick.isNewPick());
			}
			if (pick.getPickID().equals("D")) {
				isDFound = true;
			}
		}
		assertFalse(isAFound);
		assertTrue(isBFound);
		assertTrue(isCFound);
		assertFalse(isDFound);
	}

	// A Pick Query is way to get back Picks for member/s. The Pick Query has
	// many combinations of query options to find Picks. One example is looking
	// for Picks that have been marked as not to be used which is indicated by
	// the Pick 'isStopUsing' flag.
	@Test
	public void test_setPickQuery() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		String pickID = "meenie";
		List<String> collectionList = new ArrayList<String>();
		collectionList.add("eenie, meenie, miney, mo");
		MemberPickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionList,
				true);
		assertTrue(createPickIDCollection.getPickIDList().size() == 4);
		assertTrue(createPickIDCollection.getPickIDList().contains(pickID));

		// NOTE: _underscores are used in this code to indicate that a variable
		// or method could be, or possibly return null.

		// mark the Pick 'stopusing' so that we can find it with a PickQuery.
		manager.setStopUsingByMemberIDPickIDTrueFalse(testMemberID, pickID, true);

		// we should now have a Pick to test with
		Pick _pick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);
		assertNotNull("we should have a Pick", _pick);
		assertTrue(_pick.getPickID().equals(pickID));
		assertTrue(_pick.isStopUsing());

		// let's find the Pick with a PickQuery:
		PickQuery pickQuery = new PickQuery();
		// looking for this member.
		pickQuery.addToMemberIDList(testMemberID);
		// for Picks marked as 'StopUsing.'
		pickQuery.setSearchMode(SearchMode.STOPUSING);

		// Test
		// set PickQuery.
		List<Pick> pickList = manager.setPickQuery(pickQuery);

		// Verify
		int firstQueryPickSize = pickList.size();
		assertTrue("there should be at least 1 Picks for this test", firstQueryPickSize >= 1);
		boolean isPickFound = false;
		for (Pick pick : pickList) {
			if (pick.getPickID().equals(pickID)) {
				isPickFound = true;
				assertTrue("Pick should be marked 'stop using'", pick.isStopUsing());
			}
		}
		assertTrue(isPickFound);

		// NOTE: once the PickQuery has been 'set', we can use the regular GET
		// method getMemberPickListByPickQuery to get same query result.

		// Test
		pickList = manager.getPickQuery();

		// Verify
		isPickFound = false;
		for (Pick pick : pickList) {
			if (pick.getPickID().equals(pickID)) {
				isPickFound = true;
			}
		}
		assertTrue(isPickFound);
		assertTrue("Same number of Picks found", pickList.size() == firstQueryPickSize);

		// using other get Pick methods should not change the 'set' PickQuery.
		// Let's find our Pick again and then make sure the 'set' PickQuery
		// still works.
		_pick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);
		assertNotNull("should still find the Pick", _pick);

		// try the GET query again.
		pickList = manager.getPickQuery();

		// Verify
		isPickFound = false;
		for (Pick pick : pickList) {
			if (pick.getPickID().equals(pickID)) {
				isPickFound = true;
			}
		}
		assertTrue(isPickFound);
		assertTrue("there should be no Pick Query change", pickList.size() == firstQueryPickSize);

	}

	// The 'SearchMode.FAVORITES' PickQuery option searches for Picks for
	// multiple members for the highest auto-rating and times used count.
	@Test
	public void test_setPickQuery_favoritesForMembers() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		manager.addMemberName(TEST_MEMBER_SUE);
		long memberBobID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		long memberSueID = manager.getMemberIDByName(TEST_MEMBER_SUE);

		// setting the collection.
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();
		memberPickIDCollection.setCollectionName("Colorful Collection");
		memberPickIDCollection.addPickID("Blue, Green, Red, Orange, Purple, Yellow");
		memberPickIDCollection.setSplitCSV(true);
		manager.addPickIDCollection(memberPickIDCollection);

		// choose favorite colors for Bob.
		// Skip and Use a few times to change the Pick auto-ratings.
		memberPickIDCollection.clearPickIDList();
		memberPickIDCollection.addPickID("Red, Green, Yellow");
		MemberListPickIDList bobPickIDList = new MemberListPickIDList(memberPickIDCollection);
		bobPickIDList.addMemberID(memberBobID);
		bobPickIDList.setSplitCSV(true);
		manager.skipUsePassMemberPickIDList(SkipUsePass.USE, bobPickIDList);
		manager.skipUsePassMemberPickIDList(SkipUsePass.USE, bobPickIDList);
		memberPickIDCollection.clearPickIDList();
		memberPickIDCollection.addPickID("Purple, Orange, Blue,");
		manager.skipUsePassMemberPickIDList(SkipUsePass.SKIP, bobPickIDList);
		manager.skipUsePassMemberPickIDList(SkipUsePass.SKIP, bobPickIDList);

		// choose favorite colors for the Sue.
		MemberListPickIDList suePickIDList = new MemberListPickIDList(memberPickIDCollection);
		suePickIDList.addMemberID(memberSueID);
		suePickIDList.setSplitCSV(true);
		memberPickIDCollection.clearPickIDList();
		memberPickIDCollection.addPickID("Orange, Red, Yellow");
		manager.skipUsePassMemberPickIDList(SkipUsePass.USE, suePickIDList);
		manager.skipUsePassMemberPickIDList(SkipUsePass.USE, suePickIDList);
		memberPickIDCollection.clearPickIDList();
		memberPickIDCollection.addPickID("Blue, Green, Purple");
		manager.skipUsePassMemberPickIDList(SkipUsePass.SKIP, suePickIDList);
		manager.skipUsePassMemberPickIDList(SkipUsePass.SKIP, suePickIDList);

		// let's find their common favorites with a PickQuery:
		PickQuery pickQuery = new PickQuery();
		// look for these members.
		pickQuery.addToMemberIDList(memberBobID);
		pickQuery.addToMemberIDList(memberSueID);
		// look for favorites.
		pickQuery.setSearchMode(SearchMode.FAVORITES);
		// get the top four colors.
		pickQuery.setHowMany(4);
		// don't include new Picks or send back more if none are found.
		pickQuery.makeExactQuery();

		// Test
		List<Pick> pickList = manager.setPickQuery(pickQuery);

		// Verify
		assertTrue("4 Picks should be found", pickList.size() == 4);
		boolean isRedFound = false;
		boolean isYellowFound = false;
		boolean isBobPickFound = false;
		boolean isSuePickFound = false;
		for (Pick pick : pickList) {
			if (pick.getPickID().equals("Red")) {
				isRedFound = true;
			} else if (pick.getPickID().equals("Yellow")) {
				isYellowFound = true;
			}
			if (pick.getMemberID() == memberBobID) {
				isBobPickFound = true;
			} else if (pick.getMemberID() == memberSueID) {
				isSuePickFound = true;
			} else {
				fail("something went wrong");
			}

		}
		assertTrue("Red should be a favorite", isRedFound);
		assertTrue("Yellow should be a favorite", isYellowFound);
		assertTrue(isBobPickFound);
		assertTrue(isSuePickFound);
	}

	@Test
	public void test_setPickQuery_onlySomePickIDs() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// set the collection for our Picks.
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(
				manager.getOwnerMemberID());
		memberPickIDCollection.setCollectionName("Colorful Collection");
		memberPickIDCollection.addPickID("Blue, Green, Red, Orange, Purple, Yellow");
		memberPickIDCollection.setSplitCSV(true);
		manager.addPickIDCollection(memberPickIDCollection);

		// create all the Picks for the member in the collection
		MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberPickIDCollection);
		memberPickIDList.addMemberID(testMemberID);
		manager.skipUsePassMemberPickIDList(SkipUsePass.USE, memberPickIDList);

		assertTrue(
				manager.getAllPickListByMemberID(testMemberID, INCLUDE_CATEGORY_INFO).size() == 6);

		// let's start with the default PickQuery for our test member
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(testMemberID);
		List<Pick> pickList = manager.setPickQuery(pickQuery);
		// the default PickQuery returns all the stored Picks
		assertTrue("All 6 color Picks should be found", pickList.size() == 6);

		// now let's Pick Query just a few from the collection
		pickQuery.addToPickIDList("Red");
		pickQuery.addToPickIDList("Yellow");
		pickQuery.addToPickIDList("Orange");
		// wait, this is not in our collection
		pickQuery.addToPickIDList("Not a Color");

		// Test
		pickList = manager.setPickQuery(pickQuery);

		// Verify
		assertTrue("3 Picks should be found", pickList.size() == 3);
		boolean isRedFound = false;
		boolean isYellowFound = false;
		boolean isOrangeFound = false;
		boolean isNotAColorFound = false;
		boolean isMemberIDFound = false;
		for (Pick pick : pickList) {
			if (pick.getPickID().equals("Red")) {
				isRedFound = true;
			} else if (pick.getPickID().equals("Yellow")) {
				isYellowFound = true;
			} else if (pick.getPickID().equals("Orange")) {
				isOrangeFound = true;

			} else if (pick.getPickID().equals("Not a Color")) {
				isNotAColorFound = true;
			}
			if (pick.getMemberID() == testMemberID)
				isMemberIDFound = true;
		}

		// we found our member...
		assertTrue(isMemberIDFound);
		// the colors we sent in the Pick Query
		assertTrue("Red should be found", isRedFound);
		assertTrue("Yellow should be found", isYellowFound);
		assertTrue("Orange should be found", isOrangeFound);
		// but not the Pick ID that is not in our collection
		assertFalse("Not a Color should NOT be found", isNotAColorFound);

		// NOTE: If you are searching from a large Pick ID collection for a
		// small number of Picks to return, using the Pick Query by PickIDList
		// can be faster and have less nibble-cost to you.
	}

	// You can just get a single Pick for a member by the Pick ID.
	@Test
	public void test_getPickByMemberIDAndPickID() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());

		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		String pickID = "With a Pick Pick here. And a Pick Pick there.";
		List<String> collectionList = new ArrayList<String>();
		collectionList.add(pickID);
		MemberPickIDCollection createPickIDCollection = manager.addPickIDCollection(collectionList,
				false);
		assertTrue(createPickIDCollection.getPickIDList().size() == 1);

		// Test : no Pick found
		// NOTE: the underscore symbol on the method indicates a null Pick could
		// be returned.
		Pick _pick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);

		// Verify
		assertTrue(_pick == null);

		// create a Pick.
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, pickID);

		// Test : a Pick is found
		_pick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);
		assertNotNull(_pick);
		assertTrue(_pick.getPickID().equals(pickID));
		assertTrue(_pick.getSkipped() == 1);
		assertTrue(_pick.getMemberID() == testMemberID);

		// remove the Pick ID from the collection.
		collectionList.clear();
		createPickIDCollection = manager.addPickIDCollection(collectionList, false);
		assertTrue(createPickIDCollection.getPickIDList().size() == 0);

		// NOTE: the Pick is still there, but if it is not in the current
		// collection, it will NOT be available even when using a PickQuery to
		// search by. The collection acts as a filter for which Pick are allowed
		// to be used or not.
		_pick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);
		assertTrue(_pick == null);
	}

	// Members can add categories for their Picks.
	@Test
	public void test_createCategoryForMember() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		// add a category.
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
	public void test_updateMemberPick_createAPickIfNewPickID() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// create a shared Pick ID collection.
		List<String> collectionList = new ArrayList<String>();
		Date date = new Date();
		String pickID = "TestPickID=" + date.toString();
		collectionList.add(pickID);
		manager.addPickIDCollection(collectionList, true);

		// get new a Pick by the PickQuery.
		PickQuery pickQuery = new PickQuery();
		// for this member.
		pickQuery.addToMemberIDList(testMemberID);
		// one Pick.
		pickQuery.setHowMany(1);
		// it must be a new Pick.
		pickQuery.setNewMixInPercentage(100);
		// return exactly this many with this criteria.
		pickQuery.makeExactQuery();

		// make the query.
		List<Pick> pickList = manager.setPickQuery(pickQuery);
		Pick _testPick = pickList.stream().filter(t -> t.getPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(_testPick);
		assertTrue(_testPick.isNewPick());
		assertTrue("Should not have a timestamp", _testPick._getLastUpdated() == null);

		// pick attributes to update:
		Pick updateThisPick = new Pick();
		updateThisPick.setMemberID(testMemberID);
		updateThisPick.setPickID(_testPick.getPickID());
		// additional JSON you can set for your app.
		updateThisPick.setJSON("{\"hello\": \"world\"}");
		// flag a pick to 'stop using' in normal queries.
		updateThisPick.setStopUsing(true);

		// you can't change these values, they will be ignored by the API.
		updateThisPick.setSkipped(20);
		updateThisPick.setUsed(30);
		updateThisPick.setAutoRatePercentage(200);
		List<String> categoryList = new ArrayList<String>();
		categoryList.add("Add categories");
		categoryList.add("using the");
		categoryList.add("create category for member method instead");
		updateThisPick.setCategoryList(categoryList);
		updateThisPick.setNewPick(true);
		updateThisPick.setLastUpdated(new java.sql.Timestamp(new java.util.Date().getTime()));

		// Test
		manager.updateMemberPick(updateThisPick);

		// Verify
		List<Pick> updatedPickList = manager.getAllPickListByMemberID(testMemberID,
				INCLUDE_CATEGORY_INFO);
		Pick _updatedPick = updatedPickList.stream().filter(t -> t.getPickID().equals(pickID))
				.findFirst().orElse(null);
		assertNotNull("Should find the Pick", _updatedPick);
		assertTrue(_updatedPick.getPickID().equals(updateThisPick.getPickID()));

		// attributes we changed.
		assertTrue("was: " + _updatedPick.getJSON(),
				_updatedPick.getJSON().equals("{\"hello\":\"world\"}"));
		assertTrue(_updatedPick.isStopUsing() == updateThisPick.isStopUsing());

		// can't change these.
		assertTrue(_updatedPick.getAutoRatePercentage() != updateThisPick.getAutoRatePercentage());
		assertFalse(_updatedPick.isNewPick());
		assertFalse(_updatedPick.getCategoryList().contains("Add categories"));
		assertFalse(_updatedPick.getSkipped() == updateThisPick.getSkipped());
		assertFalse(_updatedPick.getUsed() == updateThisPick.getUsed());
		assertNotNull("Should now have a timestamp", _updatedPick._getLastUpdated());
		assertTrue(_updatedPick._getLastUpdated().getTime() != updateThisPick._getLastUpdated()
				.getTime());
	}

	// A member can update a category name.
	@Test
	public void test_updateCategoryNameForMember() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		MemberCategoryList currentMemberCategoryList = manager
				.getCategoryListForMember(testMemberID);
		if (currentMemberCategoryList.getCategoryList().size() > 0)
			manager.deleteCategoryListForMember(currentMemberCategoryList);

		// Test
		manager.createCategoryForMember(testMemberID, "Bingo");

		// change the category name.
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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

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
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// add collection
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(
				manager.getOwnerMemberID());
		memberPickIDCollection.setCollectionName("test collection");
		String pickID = "my Pick ID";
		memberPickIDCollection.addPickID(pickID);
		manager.addPickIDCollection(memberPickIDCollection);
		// add category.
		String categoryName = "Bingo";
		manager.createCategoryForMember(testMemberID, categoryName);

		// get the Picks with the category.
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(testMemberID);
		pickQuery.addCategory(categoryName);
		pickQuery.makeExactQuery();

		// check we don't have any marked Picks yet.
		List<Pick> pickList = manager.setPickQuery(pickQuery);
		Pick _havePick = pickList.stream().filter(p -> p.getPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertTrue(_havePick == null);

		// Test
		manager.markPickIDListWithCategoryTrueFalse(testMemberID, memberPickIDCollection,
				categoryName, true);

		// Verify
		pickQuery.setIncludeCategories(true);
		pickList = manager.setPickQuery(pickQuery);

		Pick _markedPick = pickList.stream().filter(p -> p.getPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(_markedPick);
		assertTrue(_markedPick.getCategoryList().contains(categoryName));

		// now, unmark it. You can also use the markPickWithCategoryTrueFalse()
		// method when you already have a Pick.

		// Test
		manager.markPickWithCategoryTrueFalse(_markedPick, categoryName, false);

		// Verify
		pickList = manager.setPickQuery(pickQuery);

		Pick _unmarkedPick = pickList.stream().filter(p -> p.getPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertTrue(_unmarkedPick == null);
	}

	// Another way to simply 'stop using' a Pick is to use the
	// setStopUsingByMemberIDPickIDTrueFalse method. Setting this is a simple
	// way to remove a Pick if a member no longer wants to get it in normal
	// Pick queries.
	@Test
	public void test_setStopUsingByMemberIDPickIDTrueFalse() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// add test Picks to collection.
		String keepUsingPickID = "Normal Pick";
		String stopUsingPickID = "Stop Using Pick";
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(
				manager.getOwnerMemberID());
		memberPickIDCollection.setCollectionName("test collection");
		memberPickIDCollection.addPickID(keepUsingPickID);
		memberPickIDCollection.addPickID(stopUsingPickID);
		manager.addPickIDCollection(memberPickIDCollection);

		// first check that we don't have the Picks
		assertTrue(manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, keepUsingPickID,
				testMemberID) == null);
		assertTrue(manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, stopUsingPickID,
				testMemberID) == null);

		// create the Picks
		manager.skipUsePass(SkipUsePass.USE, testMemberID, keepUsingPickID);
		manager.skipUsePass(SkipUsePass.USE, testMemberID, stopUsingPickID);

		// check that we now have the Picks
		List<Pick> pickList = manager.getAllPickListByMemberID(testMemberID, INCLUDE_CATEGORY_INFO);
		Pick _reqularPick = pickList.stream().filter(t -> t.getPickID().equals(keepUsingPickID))
				.findFirst().orElse(null);
		assertNotNull(_reqularPick);
		assertTrue(_reqularPick.isStopUsing() == false);
		Pick _stopUsingPick = pickList.stream().filter(t -> t.getPickID().equals(stopUsingPickID))
				.findFirst().orElse(null);
		assertNotNull(_stopUsingPick);
		assertTrue(_stopUsingPick.isStopUsing() == false);

		// set the stopUsingPickID to 'stop using'
		boolean isStopUsing = true;

		// Test
		manager.setStopUsingByMemberIDPickIDTrueFalse(testMemberID, stopUsingPickID, isStopUsing);

		// Verify

		// the Pick is marked stop using
		assertTrue(manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, stopUsingPickID,
				testMemberID).isStopUsing());

		// stop using is not included in a normal PickQuery.
		// get all Picks for this member ID
		PickQuery pickQuery = new PickQuery();
		pickQuery.addToMemberIDList(testMemberID);

		List<Pick> foundPickList = manager.setPickQuery(pickQuery);
		_stopUsingPick = foundPickList.stream().filter(t -> t.getPickID().equals(stopUsingPickID))
				.findFirst().orElse(null);
		assertTrue("StopUsing PickID should NOT be included in normal queries",
				_stopUsingPick == null);
	}

	// The skipUsePass method is how Picks are changed for future PickQueries.
	// The more a member's Pick is 'Skipped', it will be presented less-often in
	// normal queries. And when a Pick is 'used', it will show up more often.
	// The Pick skip and used count is changed when this occurs. Pick's also
	// have an auto-rate and a trending-rate percentage to show the popularity
	// of usage.
	@Test
	public void test_skipUsePass() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// add a collection.
		List<String> collectionList = new ArrayList<String>();
		collectionList.add("brand new Pick");
		MemberPickIDCollection memberPickIDCollection = manager.addPickIDCollection(collectionList,
				true);
		assertTrue(memberPickIDCollection.getPickIDList().size() > 0);
		String pickID = memberPickIDCollection.getPickIDList().get(0);
		assertTrue(pickID.length() > 0);
		Pick _newPick = manager._getPickByMemberIDAndPickIDAndCollectionID(testMemberID, pickID,
				testMemberID);
		assertTrue("This should be a brand new Pick", _newPick == null);

		// Test
		manager.skipUsePass(SkipUsePass.SKIP, testMemberID, pickID);

		// Verify
		List<Pick> updatedPickList = manager.getAllPickListByMemberID(testMemberID,
				INCLUDE_CATEGORY_INFO);
		Pick _foundPick = updatedPickList.stream().filter(t -> t.getPickID().equals(pickID))
				.findFirst().orElse(null);
		assertNotNull(_foundPick);
		long startingSkipCount = _foundPick.getSkipped();
		assertTrue("The Pick should have been Skipped once", startingSkipCount == 1);

		// Test: try a 'Use'
		manager.skipUsePass(SkipUsePass.USE, testMemberID, pickID);
		updatedPickList = manager.getAllPickListByMemberID(testMemberID, INCLUDE_CATEGORY_INFO);
		_foundPick = updatedPickList.stream().filter(t -> t.getPickID().equals(pickID)).findFirst()
				.orElse(null);
		assertNotNull(_foundPick);
		assertTrue("Skipped count should not have changed", _foundPick.getSkipped() == 1);
		assertTrue("Used count should now be 1", _foundPick.getUsed() == 1);
	}

	// Another way to update Picks (Skip, Use, Pass) is by using the
	// skipUsePassMemberPickIDList method.
	@Test
	public void test_skipUsePassMemberPickIDList() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);

		// setting the collection.
		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection(
				manager.getOwnerMemberID());
		memberPickIDCollection.setCollectionName("Dog Breed Collection");
		memberPickIDCollection.addPickID(
				"Pomeranian, German Shepherd, Golden Retriever, Labrador Retriever, Old English Sheepdog, SaintBernard, Chihuahua, Border Collie, Australian Shepherd");
		memberPickIDCollection.setSplitCSV(true);
		manager.addPickIDCollection(memberPickIDCollection);

		// 'Skip' some Picks for the test member
		memberPickIDCollection.clearPickIDList();
		memberPickIDCollection.addPickID("Golden Retriever, Labrador Retriever");
		MemberListPickIDList memberPickIDList = new MemberListPickIDList(memberPickIDCollection);
		memberPickIDList.addMemberID(testMemberID);

		// Test
		manager.skipUsePassMemberPickIDList(SkipUsePass.SKIP, memberPickIDList);

		// Verify
		List<Pick> allPickMemberPickList = manager.getAllPickListByMemberID(testMemberID,
				INCLUDE_CATEGORY_INFO);
		assertNotNull(allPickMemberPickList);
		assertTrue(allPickMemberPickList.size() > 0);
		boolean isDog1Found = false;
		boolean isDog2Found = false;
		for (Pick pick : allPickMemberPickList) {
			if (pick.getPickID().equals("Golden Retriever")) {
				isDog1Found = true;
				assertTrue("Pick should be skipped", pick.getSkipped() == 1);
			}
			if (pick.getPickID().equals("Labrador Retriever")) {
				isDog2Found = true;
				assertTrue("Pick should be skipped", pick.getSkipped() == 1);
			}
		}
		assertTrue(isDog1Found);
		assertTrue(isDog2Found);
	}

	// Things to know about collections.
	@Test
	public void test_youCannotDoThisWithPickIDCollection() {
		// Set up
		long fromMemberIDCollection = 0;
		try {
			manager.login(TEST_EMAIL, TEST_PASSWORD);
			assertTrue(manager.isLoggedIn());
			manager.addMemberName(TEST_MEMBER_BOB);
			fromMemberIDCollection = manager.getOwnerMemberID();
		} catch (SkipUseException e) {
			fail("This should not error");
		}

		MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();

		// a collection must reference a member's collection of Pick ID by
		// setting a member ID.
		try {
			memberPickIDCollection.setMemberCollectionID(1234);
			manager.addPickIDCollection(memberPickIDCollection);
			fail("an error should be thrown");
		} catch (SkipUseException e) {
			assertTrue("was: " + e.getMessage(), e.getMessage().contains("Check your memberID"));
			memberPickIDCollection.setMemberCollectionID(fromMemberIDCollection);
		}

		// a collection must have a name.
		memberPickIDCollection.setCollectionName("");
		try {
			manager.addPickIDCollection(memberPickIDCollection);
			fail("an error should be thrown");
		} catch (SkipUseException e) {
			assertTrue("was: " + e.getMessage(),
					e.getMessage().contains("collectionName: property may not be empty"));
			memberPickIDCollection.setCollectionName("Add a collection name, maybe");
		}

		// a collection name may not have a comma in it.
		try {
			manager.addPickIDCollection(memberPickIDCollection);
			fail("an error should be thrown");
		} catch (SkipUseException e) {
			assertTrue("was: " + e.getMessage(), e.getMessage()
					.contains("a collection name must not contain a comma character"));
			memberPickIDCollection.setCollectionName("No comma name");
		}

		// the '@@@' set of characters is not allowed in a Pick ID collection.
		try {
			memberPickIDCollection.addPickID("@@@");
			manager.addPickIDCollection(memberPickIDCollection);
			fail("an error should be thrown");
		} catch (SkipUseException e) {
			assertTrue("was: " + e.getMessage(),
					e.getMessage().contains("A Pick ID may not contain ' @@@ ' characters"));
		}
	}

	// Here are some other not-so-common API options that are available.

	// User profile: By using the Profile data object, you can update the
	// account owner's name, email address and password.
	@Test
	public void test_updateProfile_ownerName() throws SkipUseException {
		// Set up
		manager.login(TEST_EMAIL, TEST_PASSWORD);
		assertTrue(manager.isLoggedIn());
		manager.addMemberName(TEST_MEMBER_BOB);

		Profile profile = manager.getProfile();

		// change the account owner's name
		profile.setOwnerName("My new name!");

		// Test
		manager.updateProfile(profile);

		// Verify
		Profile updatedProfile = manager.getProfile();
		assertTrue(updatedProfile.getOwnerName().equals("My new name!"));

		// we should set the name back now
		profile.setOwnerName(SkipUseProperties.OWNER_NAME);
		manager.updateProfile(profile);
		assertTrue(manager.getProfile().getOwnerName().equals(SkipUseProperties.OWNER_NAME));
	}
}
