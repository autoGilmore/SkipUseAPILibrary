package com.autogilmore.throwback.skipUsePackage.dataObjects;

public class Profile {

	// Optional value to change the Owner's account name.
	private String ownerName = "";

	// Optional value to change the account's email address.
	private String email = "";

	// Optional value to change the account's password.
	// NOTE: you cannot change both email and password at the same time.
	private String password = "";

	public Profile() {
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String name) {
		if (name != null)
			this.ownerName = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email != null)
			this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (password != null)
			this.password = password;
	}
}
