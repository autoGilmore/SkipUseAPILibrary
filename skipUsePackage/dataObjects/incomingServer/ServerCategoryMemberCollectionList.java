package com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer;

import java.util.ArrayList;
import java.util.List;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryMemberCollection;

/* 
* The API server response containing a List of CategoryMemberCollection.
*/
public class ServerCategoryMemberCollectionList extends ServerResponse {

	// JSON object name
	public static final String NAME = "clientCategoryMemberCollectionList";

	private List<CategoryMemberCollection> categoryMemberCollectionList = new ArrayList<CategoryMemberCollection>();

	public List<CategoryMemberCollection> getCategoryMemberCollectionList() {
		return categoryMemberCollectionList;
	}

	public void addCategoryMemberCollection(CategoryMemberCollection clientCategoryCollection) {
		if (!categoryMemberCollectionList.contains(clientCategoryCollection))
			this.categoryMemberCollectionList.add(clientCategoryCollection);
	}

	// Expected server JSON object name (don't rename)
	public void setClientCategoryCollectionList(
			List<CategoryMemberCollection> clientCategoryCollectionList) {
		this.categoryMemberCollectionList = clientCategoryCollectionList;
	}
}
