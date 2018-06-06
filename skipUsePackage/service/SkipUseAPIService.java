package com.autogilmore.throwback.skipUsePackage.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.autogilmore.throwback.skipUsePackage.dataObjects.CategoryPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickIDList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.MemberPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PatchName;
import com.autogilmore.throwback.skipUsePackage.dataObjects.Pick;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.PickQuery;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerCategoryPickIDCollectionList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
import com.autogilmore.throwback.skipUsePackage.enums.SkipUsePass;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.tokenData.SkipUseToken;
import com.autogilmore.throwback.skipUsePackage.tokenData.SkipUseTokenHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/* 
 * A service for handling the API SkipUse server calls. 
 * Intended to be used by the SkipUseManager, or other code, that maintains the SkipUseToken sync with the server.
*/
public class SkipUseAPIService {
	private String API_URL = "";
	private HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	private RestTemplate restTemplate = new RestTemplate(requestFactory);
	private SkipUseTokenHelper tokenHelper = new SkipUseTokenHelper();

	// Store response from server
	private ServerResponse serverResponseData = new ServerResponse();

	private ObjectMapper mapper = new ObjectMapper();
	{
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}

	// Constructor
	//
	public SkipUseAPIService(String skipUseApiUrl) {
		this.API_URL = skipUseApiUrl;
	}

	// Checks if the SkipUserAPI server is up. It should respond back with a
	// message to this call.
	//
	public boolean checkServerConnection() throws SkipUseException {
		try {
			String result = restTemplate.getForObject(API_URL + "/hello", String.class);
			if (result != null && true) {
				System.out.println(result);
				return true;
			}
		} catch (Exception e) {
			throw new SkipUseException(
					"Fail to get an acknowledgement from the SkipUseApi server.");
		}
		return false;
	}

	// Calling this will initiates a proxy and or log in to the service.
	//
	public void login(String email, String password) throws SkipUseException {
		if (getLastServerResponseData().getProxyID().isEmpty())
			initiateProxy();

		if (isLoggedIn() == false) {
			JsonNode rootNode = mapper.createObjectNode();
			((ObjectNode) rootNode).put("email", email);
			((ObjectNode) rootNode).put("password", password);
			// validationCode is used for user sign-up and profile changes, not
			// implemented in this example
			// ((ObjectNode) rootNode).put("validationCode", "");

			postAndProcess("/login", rootNode, ServerResponse.NAME);
		}
	}

	public void logout() throws SkipUseException {
		try {
			postAndProcess("/logout", null, ServerResponse.NAME);
		} catch (SkipUseException e) {
			// Ignore removed token error from logout response
			if (!e.getMessage().contains("token was empty"))
				throw new SkipUseException(e.getMessage());
		}
	}

	// Considered to be logged-in when there is a ProxyID and an owner member ID
	// is present.
	//
	public boolean isLoggedIn() {
		if (serverResponseData.getMemberID() < 0) {
			return false;
		} else if (!getLastServerResponseData().getProxyID().isEmpty()) {
			return true;
		}
		return false;
	}

	// Returns the logged in user/owner's member ID.
	// NOTE: the owner ID is not changeable and is recommended that you create
	// member ID for the owner as well, because IDs can be used publicly. Get
	// all
	// member IDs with the getMemberMap() method.
	//
	public int getMyMemberID() {
		return serverResponseData.getMemberID();
	}

	// Set the collection of Pick IDs used by all members.
	//
	public ServerPickIDCollection setPickIDCollection(PickIDCollection pickIDCollection)
			throws SkipUseException {
		return (ServerPickIDCollection) postAndProcess("/collection", pickIDCollection,
				ServerPickIDCollection.NAME);
	}

	// A long value of remaining Nibbles for the logged in account.
	//
	public long getRemainingDataNibbles() {
		try {
			return Long.valueOf(serverResponseData.getRemainingNibbles());
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	// Gets a Pick ID Collection
	// NOTE: depending on your subscription level or current version, this may
	// be limited to one collection per account.
	//
	public ServerPickIDCollection getServerPickIDCollection() throws SkipUseException {
		return (ServerPickIDCollection) getAndProcess("/collection", ServerPickIDCollection.NAME);
	}

	// Sets the query for getting Picks from the Pick ID collection.
	// See the PickQuery class for required and optional settings.
	//
	public ServerPickList setPickQuery(PickQuery pickQuery) throws SkipUseException {
		return (ServerPickList) postAndProcess("/pick", pickQuery, ServerPickList.NAME);
	}

	// Get a Pick list from the server. A 'Pick' is a 'Pick ID' that contains
	// more information on that ID.
	// NOTE: a PickQuery must be set first or else it will return a default
	// search.
	//
	public ServerPickList getServerPickList() throws SkipUseException {
		return (ServerPickList) getAndProcess("/pick", ServerPickList.NAME);
	}

	// Get all Picks for a member.
	//
	public ServerPickList getAllServerPickListByMemberID(int memberID) throws SkipUseException {
		// get the maximum number of Picks...
		PickQuery pickQuery = new PickQuery(5000);
		// include all recently offered Picks...
		pickQuery.setExcludeRecentPicks(false);
		// include Picks marked as Stop Using...
		pickQuery.setIncludeStopUsing(true);
		// return back Picks that may have not been used yet...
		pickQuery.setGetMorePicksIfShort(true);
		// for this member...
		pickQuery.addToMemberIDList(memberID);
		// un-comment below to add the category data too. This query could take
		// longer and might cost more to use.
		// pickQuery.setIncludeCategories(true);
		return setPickQuery(pickQuery);
	}

	// Search for a Pick for a member by Pick ID.
	//
	public ServerPickList _getPickByMemberIDAndPickID(int memberID, String pickID)
			throws SkipUseException {
		PickQuery pickQuery = new PickQuery();
		// for this member...
		pickQuery.addToMemberIDList(memberID);
		// for this pick ID...
		// NOTE: see this setter function for how the query will be altered.
		pickQuery.setPickID(pickID);
		// NOTE: Comment out below to not add the category data. This query
		// takes
		// longer and might cost more to use.
		pickQuery.setIncludeCategories(true);
		return setPickQuery(pickQuery);
	}

	// Update a member Pick.
	// NOTE: updating a list of Picks for a member could be performed too.
	//
	public void updatePickByMemberID(int memberID, Pick pick) throws SkipUseException {
		MemberPickList memberPickList = new MemberPickList();
		memberPickList.setMemberID(memberID);
		List<Pick> pickList = new ArrayList<>();
		pickList.add(pick);
		memberPickList.setPickList(pickList);
		patchAndProcess("/pick", memberPickList, ServerResponse.NAME);
	}

	// Get the last ServerResponse data.
	//
	public ServerResponse getLastServerResponseData() {
		return serverResponseData;
	}

	// Skip, Use or Pass Pick IDs by members.
	// See the API documentation for what each version does to a Pick ID.
	//
	public void skipUsePassPick(SkipUsePass skipUsePass, MemberPickIDList memberPickIDList)
			throws SkipUseException {
		postAndProcess("/" + skipUsePass.toString().toLowerCase(), memberPickIDList,
				ServerResponse.NAME);
	}

	// Add members to an account.
	//
	public ServerMemberMap addMemberList(MemberList memberList) throws SkipUseException {
		return (ServerMemberMap) postAndProcess("/member", memberList, ServerMemberMap.NAME);
	}

	// Get names and ID of the current members.
	//
	public ServerMemberMap getMemberMap() throws SkipUseException {
		return (ServerMemberMap) getAndProcess("/member", ServerMemberMap.NAME);
	}

	// Update a member's name by their ID and current name.
	//
	public ServerMemberMap updateMemberNameByMemberID(int memberID, String before, String after)
			throws SkipUseException {
		PatchName patchName = new PatchName(before, after);
		return (ServerMemberMap) patchAndProcess("/member/" + memberID, patchName,
				ServerMemberMap.NAME);
	}

	// Delete a member by ID.
	//
	public void deleteMemberByID(int memberID) throws SkipUseException {
		deleteAndProcess("/member/" + memberID, null, ServerResponse.NAME);
	}

	// Member's can have a category to use/mark on Pick IDs.
	//
	public ServerMemberCategoryList createCategoryByMemberID(int memberID, String categoryName)
			throws SkipUseException {
		List<String> categoryNameList = new ArrayList<>();
		categoryNameList.add(categoryName);
		MemberCategoryList memberCategoryList = new MemberCategoryList(memberID, categoryNameList);
		return createCategoryByMemberID(memberCategoryList);
	}

	// Create member categories by list.
	//
	public ServerMemberCategoryList createCategoryByMemberID(MemberCategoryList categoryNameList)
			throws SkipUseException {
		if (categoryNameList.getMemberID() <= 0 || categoryNameList.getCategoryList().isEmpty())
			throw new SkipUseException(
					"There is a problem with a parameter for creating a Category for a Member.");

		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) postAndProcess("/category",
				categoryNameList, ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Returns a List of categories created by a member.
	//
	public ServerMemberCategoryList getCategoryListByMemberID(int memberID)
			throws SkipUseException {
		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) getAndProcess(
				"/memberid/" + memberID + "/category", ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Updates a category name for a member.
	//
	public ServerMemberCategoryList updateCategoryNameByMemberID(int memberID,
			String oldCategoryName, String newCategoryName) throws SkipUseException {
		PatchName patchName = new PatchName(oldCategoryName, newCategoryName);
		ServerMemberCategoryList memberCategoryList = new ServerMemberCategoryList();
		memberCategoryList = (ServerMemberCategoryList) patchAndProcess(
				"/memberid/" + memberID + "/category", patchName, ServerMemberCategoryList.NAME);
		return memberCategoryList;
	}

	// Delete a category List for a member.
	//
	public void deleteCategoryListByMemberCategoryList(MemberCategoryList memberCategoryList)
			throws SkipUseException {
		deleteAndProcess("/memberid/" + memberCategoryList.getMemberID() + "/category",
				memberCategoryList, ServerResponse.NAME);
	}

	// Mark Pick IDs with categories by a member.
	//
	public void markCategoryPickIDCollection(CategoryPickIDCollection categoryPickIDCollection)
			throws SkipUseException {
		postAndProcess("/mark", categoryPickIDCollection, ServerResponse.NAME);
	}

	// Un-mark Pick IDs with categories by a member.
	//
	public void unmarkCategoryPickIDCollection(CategoryPickIDCollection categoryPickIDCollection)
			throws SkipUseException {
		postAndProcess("/unmark", categoryPickIDCollection, ServerResponse.NAME);
	}

	// Request a test error message from the server.
	//
	public void errorTest() {
		try {
			postAndProcess("/errortest", null, ServerPickIDCollection.NAME);
		} catch (SkipUseException e) {
			// ignore
			e.printStackTrace();
		}
	}

	// A proxyID is the server's reference ID for tracking user requests.
	// NOTE: a single proxyID per user is used. If another proxyID is logged by
	// the same user, the old proxyID will be removed.
	//
	private void initiateProxy() throws SkipUseException {
		serverResponseData = new ServerResponse();
		SkipUseToken sendingToken = tokenHelper.getInitiateToken();
		try {
			String responseJson = "";
			RestTemplate getRestTemplate = new RestTemplate();
			responseJson = getRestTemplate.getForObject(
					API_URL + "/skipusetoken/" + sendingToken.toString() + "/initiate",
					String.class);

			processStringResponse(responseJson);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, "/initiate");
		} catch (Exception e) {
			throw new SkipUseException(
					"Failed to initiate a proxy with the SkipUse server. " + e.getMessage());
		}
	}

	// Helper to add the ProxyID and SkipUseToken to the URL.
	//
	private String buildURL(String postFixUrl, String expectedObject) throws SkipUseException {
		String url = API_URL;

		if (getLastServerResponseData().getProxyID().isEmpty() && !expectedObject.isEmpty())
			throw new SkipUseException(
					"Attempting an API call with no proxyID set. Maybe log-in first?");

		if (!getLastServerResponseData().getProxyID().isEmpty())
			url += "/proxyid/" + getLastServerResponseData().getProxyID();

		url += "/skipusetoken/" + tokenHelper.getSkipUseToken().toString() + postFixUrl;

		return url;
	}

	// Helper to process GET requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example '/collection'
	// expectedObject: the expected 'Server' object to be return from the call.
	//
	private Object getAndProcess(String postFixUrl, String expectedObject) throws SkipUseException {
		System.out.println("GET " + postFixUrl);
		String url = buildURL(postFixUrl, expectedObject);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			String returnJSON = restTemplate.getForObject(url, String.class);
			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			// Return the expected server object if present
			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, postFixUrl);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// Helper to process POST requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example '/skip'
	// data: the data object to serialize sending to the server
	// expectedObject: the expected 'Server' object to be return from the call.
	//
	private Object postAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		System.out.println("POST " + postFixUrl);
		String url = buildURL(postFixUrl, expectedObject);
		String returnJSON = "";
		try {
			HttpEntity<String> entity;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			String json;
			if (data == null) {
				json = "";
			} else {
				try {
					json = mapper.writeValueAsString(data);
				} catch (JsonProcessingException e) {
					throw new SkipUseException("Problem serializing object. " + e.getMessage());
				}
			}

			entity = new HttpEntity<String>(json.toString(), headers);

			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
					entity, String.class);
			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			// Return the expected server object if present
			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, postFixUrl);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	private Object patchAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		System.out.println("PATCH " + postFixUrl);
		String url = buildURL(postFixUrl, expectedObject);
		String returnJSON = "";
		try {
			HttpEntity<String> entity;
			HttpHeaders headers = new HttpHeaders();
			MediaType mediaType = new MediaType("application", "merge-patch+json");
			headers.setContentType(mediaType);

			String json;
			if (data == null) {
				json = "";
			} else {
				try {
					json = mapper.writeValueAsString(data);
				} catch (JsonProcessingException e) {
					throw new SkipUseException("Problem serializing object. " + e.getMessage());
				}
			}

			entity = new HttpEntity<String>(json.toString(), headers);

			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PATCH,
					entity, String.class);
			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			// return the expected server object if present
			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, postFixUrl);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	private Object deleteAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		System.out.println("DELETE " + postFixUrl);
		String url = buildURL(postFixUrl, expectedObject);
		String returnJSON = "";
		try {
			HttpEntity<String> entity;
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			String json;
			if (data == null) {
				json = "";
			} else {
				try {
					json = mapper.writeValueAsString(data);
				} catch (JsonProcessingException e) {
					throw new SkipUseException("Problem serializing object. " + e.getMessage());
				}
			}

			entity = new HttpEntity<String>(json.toString(), headers);

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			RestTemplate restTemplate = new RestTemplate(requestFactory);

			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE,
					entity, String.class);
			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			// return the expected server object if present
			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, postFixUrl);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// Creates a 'Server' object from JSON.
	// A 'Server' object has a .NAME field that is found in the JSON.
	// returns the object or throws an error if fails.
	//
	private Object convertJSONToServerObject(String json, String serverObjectName)
			throws SkipUseException {
		if (serverObjectName == null || json == null)
			throw new SkipUseException(
					"Invalid parameters for convertJSONToServerObject operation");

		try {
			if (serverObjectName.equals(ServerPickIDCollection.NAME)
					&& json.contains(ServerPickIDCollection.NAME)) {
				ServerPickIDCollection serverCollection;
				serverCollection = mapper.readValue(json.toString(), ServerPickIDCollection.class);
				return serverCollection;
			} else if (serverObjectName.equals(ServerCategoryPickIDCollectionList.NAME)
					&& json.contains(ServerCategoryPickIDCollectionList.NAME)) {
				ServerCategoryPickIDCollectionList serverCategoryPickIDCollectionList;
				serverCategoryPickIDCollectionList = mapper.readValue(json.toString(),
						ServerCategoryPickIDCollectionList.class);
				return serverCategoryPickIDCollectionList;
			} else if (serverObjectName.equals(ServerPickList.NAME)
					&& json.contains(ServerPickList.NAME)) {
				ServerPickList serverPickList;
				serverPickList = mapper.readValue(json.toString(), ServerPickList.class);
				return serverPickList;
			} else if (serverObjectName.equals(ServerMemberCategoryList.NAME)
					&& json.contains(ServerMemberCategoryList.NAME)) {
				ServerMemberCategoryList serverMemberCategoryList;
				serverMemberCategoryList = mapper.readValue(json.toString(),
						ServerMemberCategoryList.class);
				return serverMemberCategoryList;
			} else if (serverObjectName.equals(ServerMemberMap.NAME)
					&& json.contains(ServerMemberMap.NAME)) {
				ServerMemberMap serverMemberMap;
				serverMemberMap = mapper.readValue(json.toString(), ServerMemberMap.class);
				return serverMemberMap;
			} else {
				throw new SkipUseException("Could not convert the expected object: "
						+ serverObjectName + " or the incoming JSON name changed. Was: " + json);
			}
		} catch (IOException e) {
			throw new SkipUseException(e.getMessage());
		}
	}

	// Helper to handle errors from requests.
	// Process the SkipUseToken if a proxyID is found.
	//
	private void handleHttpClientError(HttpClientErrorException e, String onAPICall)
			throws SkipUseException {
		String errorPayload = e.getResponseBodyAsString();
		if (errorPayload != null) {
			if (errorPayload.contains("proxyID")) {
				processStringResponse(errorPayload);
			} else {
				if (errorPayload.isEmpty())
					errorPayload = "An error occurred with no message on API call: " + onAPICall;
				if (errorPayload.contains("status\":404"))
					errorPayload = "404 not found. Check the API path for API call: " + onAPICall
							+ " " + errorPayload;
				throw new SkipUseException(errorPayload);
			}
		} else {
			throw new SkipUseException("HttpClientError>>> " + e.getMessage());
		}
	}

	// Helper to process the SkipUseToken if a proxyID is found.
	//
	private void processStringResponse(String json) throws SkipUseException {
		try {
			ServerResponse skipUseResponse = new ServerResponse();
			ObjectMapper tempMapper = new ObjectMapper();
			tempMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			skipUseResponse = tempMapper.readValue(json, ServerResponse.class);
			processResponse(skipUseResponse);
		} catch (IOException e) {
			throw new SkipUseException(e.getMessage());
		}
	}

	// Processes the server response by collecting known elements contained in
	// most server responses.
	//
	private void processResponse(ServerResponse skipUseResponse) throws SkipUseException {
		if (skipUseResponse != null) {
			// the logged in user's member ID
			serverResponseData.setMemberID(skipUseResponse.getMemberID());

			// the sever's proxyID reference
			if (skipUseResponse.getProxyID() != null)
				serverResponseData.setProxyID(skipUseResponse.getProxyID());

			// the logged in user's display name
			if (skipUseResponse.getMemberName() != null)
				serverResponseData.setMemberName(skipUseResponse.getMemberName());

			// the sever's additional message from response
			if (skipUseResponse.getMessage() != null) {
				serverResponseData.setMessage(skipUseResponse.getMessage());
				System.out.println(serverResponseData.getMessage());
			}

			// the user's remaining data nibbles
			if (skipUseResponse.getRemainingNibbles() != null)
				serverResponseData.setRemainingNibbles(skipUseResponse.getRemainingNibbles());

			// the status of the response
			if (skipUseResponse.getStatus() != null)
				serverResponseData.setStatus(skipUseResponse.getStatus());

			// process the SkipUseToken
			if (skipUseResponse.getSkipUseToken() != null) {
				if (!skipUseResponse.getSkipUseToken().isEmpty()) {
					serverResponseData.setSkipUseToken(skipUseResponse.getSkipUseToken());
					tokenHelper.processToken(skipUseResponse.getSkipUseToken());
					if (skipUseResponse.isConfirmRequired()) {
						getAndProcess("/clearFollowUp", ServerResponse.NAME);
					}
				} else {
					serverResponseData.setSkipUseToken("");
				}
			} else {
				throw new SkipUseException("The SkipUseToken was null. This should not happen.");
			}

			// throw error if error message is found
			if (skipUseResponse.getErrorMessage() != null
					&& !skipUseResponse.getErrorMessage().isEmpty()) {
				serverResponseData.setErrorMessage(skipUseResponse.getErrorMessage());
				if (!serverResponseData.getMessage().isEmpty())
					System.err.println(skipUseResponse.getMessage());
				throw new SkipUseException(serverResponseData.getErrorMessage());
			}
		}
	}
}
