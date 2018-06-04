package com.autogilmore.throwback.skipUsePackage.dataObjects;

import java.util.ArrayList;
import java.util.List;

/* 
 * This is a list of Member names.
*/
public class MemberList {

	private List<String> memberNameList = new ArrayList<String>();

	public List<String> getMemberNameList() {
		return memberNameList;
	}

	public void addMemberName(String memberName) {
		if (memberName != null && !memberName.isEmpty()
				&& !this.memberNameList.contains(memberName))
			this.memberNameList.add(memberName);
	}
}
