package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * This is a list of Categories for a Member.
*/
public class MemberCategoryList {

	// For this member...
	private int memberID = -1;

	// Use these Categories.
	private List<String> categoryList = new ArrayList<>();

	public MemberCategoryList(int memberID, List<String> categoryList) {
		this.memberID = memberID;
		this.categoryList = categoryList;
	}

	public MemberCategoryList() {
	}

	public int getMemberID() {
		return memberID;
	}

	public void setMemberID(int memberID) {
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
