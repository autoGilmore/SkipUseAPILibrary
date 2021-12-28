package com.autogilmore.throwback.skipUsePackage.dataObjects;

/*
 * This is a member's list of categories for a collection of Pick IDs.
*/
public class CategoryMemberCollection {

	// For this member's categories...
	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	// Use these Pick IDs from this member's Pick ID collection...
	private MemberCollection memberCollection = new MemberCollection();

	public CategoryMemberCollection() {
	}

	public MemberCollection getMemberCollection() {
		return memberCollection;
	}

	public void setMemberCollection(MemberCollection memberCollection) {
		this.memberCollection = memberCollection;
	}

	public MemberCategoryList getMemberCategoryList() {
		return memberCategoryList;
	}

	public void setMemberCategoryList(MemberCategoryList memberCategoryList) {
		this.memberCategoryList = memberCategoryList;
	}
}
