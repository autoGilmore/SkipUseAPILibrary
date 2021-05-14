package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PickIDCountAdvance {

    // from this member's collection
    private long memberCollectionID = 0L;

    // for these members
    private Set<Long> memberIDSet = new HashSet<Long>();

    // these Pick IDs
    private Set<String> pickIDSet = new HashSet<String>();

    // advance the Pick ID count
    private int skipCount = 0;
    private int useCount = 0;

    public PickIDCountAdvance(long collectionID, long memberID, String pickID, int skipCnt, int useCnt) {
	this.memberCollectionID = collectionID;
	addToMemberIDSet(memberID);
	addToPickIDList(pickID);
	this.skipCount = skipCnt;
	this.useCount = useCnt;
    }

    public int getSkipCount() {
	return skipCount;
    }

    public int getUseCount() {
	return useCount;
    }

    public Set<Long> getMemberIDSet() {
	return memberIDSet;
    }

    public void setMemberIDSet(Set<Long> memberIDSet) {
	this.memberIDSet = memberIDSet;
    }

    public void addToMemberIDSetByList(List<Long> memberIDList) {
	if (memberIDList != null)
	    memberIDList.forEach(m -> addToMemberIDSet(m));
    }

    public long getMemberCollectionID() {
	return memberCollectionID;
    }

    public void addToMemberIDSet(long memberID) {
	memberIDSet.add(memberID);
    }

    public void addToPickIDList(String pickID) {
	pickIDSet.add(pickID.trim());
    }

    public Set<String> getPickIDSet() {
	return pickIDSet;
    }

    public void setPickIDSet(Set<String> pickIDSet) {
	this.pickIDSet = pickIDSet;
    }

    public void setMemberCollectionID(long memberCollectionID) {
	this.memberCollectionID = memberCollectionID;
    }

    @Override
    public boolean equals(Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof PickIDCountAdvance)) {
	    return false;
	}
	PickIDCountAdvance advance = (PickIDCountAdvance) o;
	return memberCollectionID == advance.getMemberCollectionID()
		&& Objects.equals(memberIDSet, advance.getMemberIDSet())
		&& Objects.equals(pickIDSet, advance.getPickIDSet());
    }

    @Override
    public int hashCode() {
	return Objects.hash(memberCollectionID, memberIDSet, pickIDSet);
    }

    @Override
    public String toString() {
	return "memberCollectionID: " + this.memberCollectionID + " memberIDSet:" + this.memberIDSet + " pickIDSet: "
		+ this.pickIDSet + " skipCount: " + this.skipCount + " useCount: " + this.useCount;
    }

}
