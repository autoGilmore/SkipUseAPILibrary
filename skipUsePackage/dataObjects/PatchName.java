package com.autogilmore.throwback.skipUsePackage.dataObjects;

/* 
 * This is an object to use for changing the name of another object.
*/
public class PatchName {
	String beforeName = "";
	String afterName = "";

	public PatchName(String before, String after) {
		setBeforeName(before);
		setAfterName(after);
	}

	public String getBeforeName() {
		return beforeName;
	}

	public void setBeforeName(String beforeName) {
		this.beforeName = beforeName;
	}

	public String getAfterName() {
		return afterName;
	}

	public void setAfterName(String afterName) {
		this.afterName = afterName;
	}
}
