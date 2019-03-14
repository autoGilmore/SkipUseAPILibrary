package com.autogilmore.throwback.skipUsePackage.examples.music;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.manager.SkipUseManager;

public class MusicSelectionHelperTest {

	private MusicSelectionHelper musicSelectionHelper = new MusicSelectionHelper();

	private static final String TEST_MEMBER_BOB = "Bob";

	@Test
	public void testMusicSelectionHelper() {
		// Test
		// Verify
		assertTrue("We need a running instance for testing",
				musicSelectionHelper.isSkipUseRunning());
	}

	@Test
	public void testInitialize() throws SkipUseException {
		// logout to have initialize log back in
		SkipUseManager manager = SkipUseManager.getInstance();
		manager.logout();
		assertFalse("We should be logged out for start of test", manager.isLoggedIn());

		// Test
		musicSelectionHelper.initialize();

		// Verify
		assertTrue("initialize should log back in / helper",
				musicSelectionHelper.isSkipUseRunning());
		assertTrue("initialize should log back in / manager", manager.isLoggedIn());
	}

	@Test
	public void testGetSongIDCollection() {
		boolean isCommaSpaceDelimted = false;
		List<String> songIDList = new ArrayList<>();
		songIDList.add("song1");
		songIDList.add("song2");
		songIDList.add("song3");
		songIDList.add("song4");
		musicSelectionHelper.setSongIDCollection(songIDList, isCommaSpaceDelimted);

		// Test
		List<String> foundSongList = musicSelectionHelper.getSongIDCollection();

		// Verify
		assertTrue("there should be 4 song IDs", foundSongList.size() == 4);
	}

	@Test
	public void testSetSongIDCollection() {
		boolean isCommaSpaceDelimted = true;
		List<String> songIDList = new ArrayList<>();
		songIDList.add("song1, song2, song3, song4");

		// Test
		musicSelectionHelper.setSongIDCollection(songIDList, isCommaSpaceDelimted);

		// Verify
		List<String> foundSongList = musicSelectionHelper.getSongIDCollection();
		assertTrue("there should be 4 song IDs", foundSongList.size() == 4);
	}

	@Test
	public void test_getNextSongID() throws SkipUseException {
		// Set up
		SkipUseManager manager = SkipUseManager.getInstance();
		boolean isCommaSpaceDelimted = true;
		List<String> songIDList = new ArrayList<>();
		songIDList.add("song1, song2, song3, song4");
		musicSelectionHelper.setSongIDCollection(songIDList, isCommaSpaceDelimted);
		List<String> foundSongList = musicSelectionHelper.getSongIDCollection();
		assertTrue("there should be 4 song IDs", foundSongList.size() == 4);
		// set listening member ID
		manager.addMemberName(TEST_MEMBER_BOB);
		long testMemberID = manager.getMemberIDByName(TEST_MEMBER_BOB);
		assertTrue(testMemberID > 0);
		musicSelectionHelper.addListeningMemberID(testMemberID);

		// Test
		String _songID = musicSelectionHelper._getNextSongID();

		// Verify
		assertNotNull(_songID);
		assertTrue("song ID should be one of song list", foundSongList.contains(_songID));
	}

	@Test
	public void testgetPickIDQueue() {

		// Test
		Queue<String> pickIDQueue = musicSelectionHelper.getPickIDQueue();

		// Verify
		assertTrue("should not have anything in the queue yet", pickIDQueue.size() == 0);
	}

}
