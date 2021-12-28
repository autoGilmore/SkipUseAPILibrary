package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* 
 * This is a list of member IDs that reference a member's collection of Pick IDs.
*/
public class MemberListPickIDList extends MemberCollection {

    // From Pick IDs from a member's collection of Pick IDs...

    // Use these member IDs...
    private List<Long> memberIDList = new ArrayList<Long>();

    public MemberListPickIDList(MemberCollection memberMemberCollection) {
	setCollectionName(memberMemberCollection.getCollectionName());
	setMemberID(memberMemberCollection.getMemberID());
	setPickIDList(memberMemberCollection.getPickIDList());
	setSplitCSV(memberMemberCollection.isSplitCSV());
    }

    public MemberListPickIDList(MemberCollection memberMemberCollection, List<Long> memberIDList) {
	setCollectionName(memberMemberCollection.getCollectionName());
	setMemberID(memberMemberCollection.getMemberID());
	setPickIDList(memberMemberCollection.getPickIDList());
	setSplitCSV(memberMemberCollection.isSplitCSV());
	setMemberIDList(memberIDList);
    }

    public List<Long> getMemberIDList() {
	return memberIDList;
    }

    public void setMemberIDList(List<Long> memberIDList) {
	if (memberIDList != null) {
	    memberIDList.removeAll(Collections.singleton(null));
	    for (long memberID : memberIDList)
		addMemberID(memberID);
	}
    }

    public void addMemberID(long memberID) {
	if (memberID > 0 && !this.memberIDList.contains(memberID))
	    this.memberIDList.add(memberID);
    }
}
