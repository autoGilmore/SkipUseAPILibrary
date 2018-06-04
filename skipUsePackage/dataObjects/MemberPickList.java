package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * This is a list of Picks for a Member.
*/
public class MemberPickList {

	// For this Member...
	private int memberID = -1;

	// These Picks (not Pick IDs)
	private List<Pick> pickList = new ArrayList<Pick>();

	public int getMemberID() {
		return memberID;
	}

	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}

	public List<Pick> getPickList() {
		return pickList;
	}

	public void setPickList(List<Pick> pickList) {
		this.pickList = pickList;
	}

}
