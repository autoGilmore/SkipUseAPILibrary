package com.autogilmore.throwback.skipUsePackage.dataObjects;

/*
 * This is a member's list of categories for a collection of Pick IDs.
*/
public class CategoryMemberPickIDCollection {

	// For this member's categories...
	private MemberCategoryList memberCategoryList = new MemberCategoryList();

	// Use these Pick IDs from this member's Pick ID collection...
	private MemberPickIDCollection memberPickIDCollection = new MemberPickIDCollection();

	public CategoryMemberPickIDCollection() {
	}

	public MemberPickIDCollection getMemberPickIDCollection() {
		return memberPickIDCollection;
	}

	public void setMemberPickIDCollection(MemberPickIDCollection memberPickIDCollection) {
		this.memberPickIDCollection = memberPickIDCollection;
	}

	public MemberCategoryList getMemberCategoryList() {
		return memberCategoryList;
	}

	public void setMemberCategoryList(MemberCategoryList memberCategoryList) {
		this.memberCategoryList = memberCategoryList;
	}
}
