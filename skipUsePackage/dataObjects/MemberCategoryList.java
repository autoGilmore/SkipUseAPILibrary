package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * This is a list of Categories for a Member.
*/
public class MemberCategoryList {

	// For this member...
	private long memberID = 0;

	// Use these Categories.
	private List<String> categoryList = new ArrayList<String>();

	public MemberCategoryList(long memberID, List<String> categoryList) {
		this.memberID = memberID;
		this.categoryList = categoryList;
	}

	public MemberCategoryList() {
	}

	public long getMemberID() {
		return memberID;
	}

	public void setMemberID(long memberID) {
		this.memberID = memberID;
	}

	public List<String> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<String> categoryList) {
		this.categoryList = categoryList;
	}

	public void addCategory(String category) {
		if (category != null && !categoryList.contains(category))
			this.categoryList.add(category);
	}

}
