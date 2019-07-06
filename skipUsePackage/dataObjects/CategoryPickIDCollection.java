package com.autogilmore.throwback.skipUsePackage.dataObjects;

/*
 * This is a member's list of categories for a collection of Pick IDs.
*/
public class CategoryPickIDCollection {

	// For this member's categories...
	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	// Use these Pick IDs from this member's Pick ID collection...
	private MemberPickIDCollection pickIDCollection = new MemberPickIDCollection();

	public CategoryPickIDCollection() {
	}

	public MemberPickIDCollection getPickIDCollection() {
		return pickIDCollection;
	}

	public void setPickIDCollection(MemberPickIDCollection pickIDCollection) {
		this.pickIDCollection = pickIDCollection;
	}

	public MemberCategoryList getMemberCategoryList() {
		return memberCategoryList;
	}

	public void setMemberCategoryList(MemberCategoryList memberCategoryList) {
		this.memberCategoryList = memberCategoryList;
	}
}
