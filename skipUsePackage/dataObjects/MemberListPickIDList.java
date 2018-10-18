package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* 
 * This is a list of Member IDs and Pick IDs.
*/
public class MemberListPickIDList {

	// For these Member IDs...
	private List<Integer> memberIDList = new ArrayList<Integer>();

	// Use these Pick IDs...
	private List<String> pickIDList = new ArrayList<String>();

	// Split the Pick IDs by comma + space separator.
	private boolean splitCSV = false;

	public MemberListPickIDList() {
	}

	public MemberListPickIDList(PickIDCollection collection, List<Integer> memberIDList) {
		setMemberIDList(memberIDList);
		setPickIDList(collection.getPickIDList());
	}

	public List<Integer> getMemberIDList() {
		return memberIDList;
	}

	public void setMemberIDList(List<Integer> memberIDList) {
		memberIDList.removeAll(Collections.singleton(null));
		this.memberIDList = memberIDList;
	}

	public void addMemberID(int memberID) {
		if (memberID > -1 && !this.memberIDList.contains(memberID))
			this.memberIDList.add(memberID);
	}

	public List<String> getPickIDList() {
		return pickIDList;
	}

	public void setPickIDList(List<String> pickIDList) {
		if (pickIDList != null && !pickIDList.isEmpty()) {
			for (String setItem : pickIDList) {
				addPickID(setItem);
			}
		}
	}

	public void addPickID(String pickID) {
		if (pickID != null) {
			String trimPick = pickID.trim();
			if (!trimPick.isEmpty() && !pickIDList.contains(trimPick.trim())) {
				this.pickIDList.add(trimPick);
			}
		}
	}

	public void clearPickIDList() {
		this.pickIDList.clear();
	}

	public boolean isSplitCSV() {
		return this.splitCSV;
	}

	public void setSplitCSV(boolean splitCSV) {
		this.splitCSV = splitCSV;
	}

}
