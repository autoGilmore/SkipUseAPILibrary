package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import java.util.ArrayList;

import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryPickIDCollection;

/* 
* The API server response containing a List of CategoryPickIDCollection.
*/
public class ServerCategoryPickIDCollectionList extends ServerResponse {
	// JSON object name
	public static final String NAME = "clientCategoryCollectionList";

	private List<CategoryPickIDCollection> categoryPickIDCollectionList = new ArrayList<CategoryPickIDCollection>();

	public List<CategoryPickIDCollection> getCategoryPickIDCollectionList() {
		return categoryPickIDCollectionList;
	}

	public void addCategoryPickIDCollection(CategoryPickIDCollection clientCategoryCollection) {
		if (!categoryPickIDCollectionList.contains(clientCategoryCollection))
			this.categoryPickIDCollectionList.add(clientCategoryCollection);
	}

	// Expected server JSON object name (don't rename)
	public void setClientCategoryCollectionList(
			List<CategoryPickIDCollection> clientCategoryCollectionList) {
		this.categoryPickIDCollectionList = clientCategoryCollectionList;
	}
}
