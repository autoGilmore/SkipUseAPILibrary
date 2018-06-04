package com.autogilmore.throwback.skipUsePackage.dataObjects;

/*
 * This is a Member's list of Categories for a Collection of Pick IDs.
*/
public class CategoryPickIDCollection {

	// For this member's Categories...
	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	// Use these Pick IDs...
	private PickIDCollection pickIDCollection = new PickIDCollection();

	public CategoryPickIDCollection() {
	}

	public PickIDCollection getPickIDCollection() {
		return pickIDCollection;
	}

	public void setPickIDCollection(PickIDCollection pickIDCollection) {
		this.pickIDCollection = pickIDCollection;
	}

	public MemberCategoryList getMemberCategoryList() {
		return memberCategoryList;
	}

	public void setMemberCategoryList(MemberCategoryList memberCategoryList) {
		this.memberCategoryList = memberCategoryList;
	}
}
