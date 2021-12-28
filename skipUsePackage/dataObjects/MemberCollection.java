package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.sql.Timestamp;

import com.autogilmore.throwback.skipUsePackage.service.SkipUseProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * This is a Collection of PickID for a member.
*/
public class MemberCollection extends PickIDList {

    // The member's ID for their collection.
    // NOTE: 0 will use the Owner's Pick ID collection.
    private long memberID = 0;

    // The name for the collection.
    private String collectionName = SkipUseProperties.PICK_ID_COLLECTION_NAME;

    // Timestamp when the collection was last updated. Rounding by minutes.
    @JsonProperty("lastUpdated")
    private Timestamp _lastUpdated;

    public MemberCollection() {
    }

    public MemberCollection(long fromMemberIDCollection) {
	setMemberID(fromMemberIDCollection);
    }

    public long getMemberID() {
	return this.memberID;
    }

    public void setMemberID(long fromMemberIDCollection) {
	this.memberID = fromMemberIDCollection;
    }

    public String getCollectionName() {
	return collectionName;
    }

    public void setCollectionName(String collectionName) {
	if (collectionName != null)
	    this.collectionName = collectionName;
    }

    public Timestamp _getLastUpdated() {
	return _lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
	if (lastUpdated != null)
	    this._lastUpdated = lastUpdated;
    }

}
