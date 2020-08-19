package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PickIDCountAdvanceList {
    private List<PickIDCountAdvance> pickIDCountAdvanceList = new ArrayList<>();

    private boolean updateASAP = false;

    public List<PickIDCountAdvance> getPickIDCountAdvanceList() {
	return pickIDCountAdvanceList;
    }

    public void addCountAdvance(PickIDCountAdvance pickIDCountAdvance) {
	if (pickIDCountAdvance != null)
	    pickIDCountAdvanceList.add(pickIDCountAdvance);
    }

    public List<PickIDCountAdvance> getAllCollectionID(long collectionID) {
	List<PickIDCountAdvance> returnList = new ArrayList<>();
	for (PickIDCountAdvance pickIDUpdate : pickIDCountAdvanceList) {
	    if (pickIDUpdate.getMemberCollectionID() == collectionID) {
		returnList.add(pickIDUpdate);
	    }
	}
	return returnList;
    }

    public int getSize() {
	return pickIDCountAdvanceList.size();
    }

    @JsonIgnore
    public List<Long> getAllCollectionIDs() {
	Set<Long> collectionIDSet = new HashSet<Long>();
	pickIDCountAdvanceList.forEach(u -> collectionIDSet.add(u.getMemberCollectionID()));
	return collectionIDSet.stream().collect(Collectors.toList());
    }

    public boolean isUpdateASAP() {
	return updateASAP;
    }

    public void setUpdateASAP(boolean updateASAP) {
	this.updateASAP = updateASAP;
    }
}
