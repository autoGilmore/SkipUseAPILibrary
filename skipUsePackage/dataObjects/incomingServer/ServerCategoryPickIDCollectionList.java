package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import java.util.ArrayList;

import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberPickIDCollection;

/* 
* The API server response containing a List of CategoryPickIDCollection.
*/
public class ServerCategoryPickIDCollectionList extends ServerResponse {

	// JSON object name
	public static final String NAME = "clientCategoryCollectionList";

	private List<CategoryMemberPickIDCollection> categoryPickIDCollectionList = new ArrayList<CategoryMemberPickIDCollection>();

	public List<CategoryMemberPickIDCollection> getCategoryPickIDCollectionList() {
		return categoryPickIDCollectionList;
	}

	public void addCategoryPickIDCollection(CategoryMemberPickIDCollection clientCategoryCollection) {
		if (!categoryPickIDCollectionList.contains(clientCategoryCollection))
			this.categoryPickIDCollectionList.add(clientCategoryCollection);
	}

	// Expected server JSON object name (don't rename)
	public void setClientCategoryCollectionList(
			List<CategoryMemberPickIDCollection> clientCategoryCollectionList) {
		this.categoryPickIDCollectionList = clientCategoryCollectionList;
	}
}
