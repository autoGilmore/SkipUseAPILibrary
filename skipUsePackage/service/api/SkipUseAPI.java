package com.autogilmore.throwback.skipUsePackage.service.api;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerCategoryPickIDCollectionList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberCategoryList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerMemberMap;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickIDCollection;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerPickList;
import com.autogilmore.throwback.skipUsePackage.dataObjects.incomingServer.ServerResponse;
import com.autogilmore.throwback.skipUsePackage.exception.SkipUseException;
import com.autogilmore.throwback.skipUsePackage.tokenData.SkipUseToken;
import com.autogilmore.throwback.skipUsePackage.tokenData.SkipUseTokenHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SkipUseAPI {
	public String api_url = "";

	public SkipUseTokenHelper tokenHelper = new SkipUseTokenHelper();

	// Store response from server.
	public ServerResponse serverResponseData = new ServerResponse();

	public ObjectMapper mapper = new ObjectMapper();
	{
		// Don't worry about property cases from server
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		// Don't fail if a property is not found on an incomming server object
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	// Constructor.
	//
	public SkipUseAPI(String skipUseAPIURL) {
		this.api_url = skipUseAPIURL;
	}

	// Get the last response from the server.
	//
	public ServerResponse getLastServerResponseData() {
		return serverResponseData;
	}

	// Checks if the SkipUserAPI server is up. It should respond back with a
	// message to this call.
	//
	public boolean checkServerConnection() throws SkipUseException {
		try {
			String result = getRestTemplate().getForObject(api_url + "/hello", String.class);
			if (result != null && true) {
				System.out.println(result);
				return true;
			}
		} catch (Exception e) {
			System.err.println("Failed to get an acknowledgement from the SkipUseApi server.");
		}
		return false;
	}

	// A proxyID is the server's reference ID for tracking user requests.
	// NOTE: a single proxyID per user is used. If the same user logs in at the
	// same time, the older proxyID will be removed.
	//
	public void initiateProxy() throws SkipUseException {
		serverResponseData = new ServerResponse();
		SkipUseToken sendingToken = tokenHelper.getInitiateToken();
		try {
			String responseJson = getRestTemplate().getForObject(
					api_url + "/skipusetoken/" + sendingToken.toString() + "/initiate",
					String.class);

			processStringResponse(responseJson);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, "GET /initiate");
		} catch (Exception e) {
			throw new SkipUseException(
					"Failed to initiate a proxy with the SkipUse server. " + e.getMessage());
		}
	}

	// Check to see if the currently stored proxyID is still valid.
	// A proxyID will time-out and no longer be valid. If it is not valid, API
	// calls will have SkipUse errors. Using the 'clearFollowUp' method (which
	// is just a simple non-data changing call) we can see if the server still
	// recognizes our proxy ID.
	//
	public boolean isProxyValid() {
		try {
			clearFollowUp();
			return true;
		} catch (SkipUseException e) {
			return false;
		}
	}

	// Helper to process GET requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example get collection: '/collection'
	// expectedObject: the expected 'incomingServer' object to be return from
	// the call.
	//
	public Object getAndProcess(String postFixUrl, String expectedObject) throws SkipUseException {
		String attempting = "GET " + postFixUrl;
		System.out.println(attempting);
		String url = buildURLAddProxyIDSkipUseToken(postFixUrl, expectedObject);
		try {
			String returnJSON = getRestTemplate().getForObject(url, String.class);
			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, attempting);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// Helper to process POST requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example skip a Pick: '/skip'
	// data: the data object to serialize sending to the server
	// expectedObject: the expected 'incomingServer' object to be return from
	// the call.
	//
	public Object postAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		String attempting = "POST " + postFixUrl;
		System.out.println(attempting);
		String url = buildURLAddProxyIDSkipUseToken(postFixUrl, expectedObject);
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

			ResponseEntity<String> responseEntity = getRestTemplate().exchange(url, HttpMethod.POST,
					entity, String.class);
			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, attempting);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// TODO: Not a PATCH, should be a PUT.

	// Helper to process PATCH requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example edit category: '"/memberid/" +
	// memberID + "/category"'
	// data: the data object to serialize sending to the server
	// expectedObject: the expected 'incomingServer' object to be return from
	// the call. Usually 'ServerResponse.NAME'
	//
	public Object patchAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		String attempting = "PATCH " + postFixUrl;
		System.out.println(attempting);
		String url = buildURLAddProxyIDSkipUseToken(postFixUrl, expectedObject);
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

			ResponseEntity<String> responseEntity = getRestTemplate_requestFactory().exchange(url,
					HttpMethod.PATCH, entity, String.class);

			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, attempting);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// Helper to process DELETE requests to the SkipUseAPI server.
	// postFixUrl: the / API call. Example '/memberid/" + memberID +
	// "/category"'
	// data: the data object to serialize sending to the server
	// expectedObject: the expected 'incomingServer' object to be return from
	// the call. Usually 'ServerResponse.NAME'
	//
	public Object deleteAndProcess(String postFixUrl, Object data, String expectedObject)
			throws SkipUseException {
		String attempting = "DELETE " + postFixUrl;
		System.out.println(attempting);
		String url = buildURLAddProxyIDSkipUseToken(postFixUrl, expectedObject);
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

			ResponseEntity<String> responseEntity = getRestTemplate().exchange(url,
					HttpMethod.DELETE, entity, String.class);
			returnJSON = responseEntity.getBody();

			processStringResponse(returnJSON);

			if (expectedObject.equals(ServerResponse.NAME))
				return returnJSON;

			return convertJSONToServerObject(returnJSON, expectedObject);

		} catch (HttpClientErrorException e) {
			handleHttpClientError(e, attempting);
		}

		throw new SkipUseException("There was a problem calling the API: " + postFixUrl
				+ " . To get a: " + expectedObject);
	}

	// Helper to add the ProxyID and SkipUseToken to the URL.
	//
	private String buildURLAddProxyIDSkipUseToken(String postFixUrl, String expectedObject)
			throws SkipUseException {
		String url = api_url;

		if (getLastServerResponseData().getProxyID().isEmpty() && !expectedObject.isEmpty())
			throw new SkipUseException(
					"Attempting an API call with no proxyID set. Maybe log-in first?");

		if (!getLastServerResponseData().getProxyID().isEmpty())
			url += "/proxyid/" + getLastServerResponseData().getProxyID();

		url += "/skipusetoken/" + tokenHelper.getSkipUseToken().toString() + postFixUrl;

		return url;
	}

	// Create an 'incomingServer' object from JSON.
	// The 'incomingServer' object has a .NAME field that is found in the JSON
	// to indicate which server object to de-serialize..
	// Returns the object or throws an error if fails.
	//
	private Object convertJSONToServerObject(String json, String serverObjectName)
			throws SkipUseException {
		if (serverObjectName == null || json == null)
			throw new SkipUseException(
					"Invalid parameters for convertJSONToServerObject operation");

		try {
			if (serverObjectName.equals(ServerPickIDCollection.NAME)
					&& json.contains(ServerPickIDCollection.NAME)) {
				return mapper.readValue(json.toString(), ServerPickIDCollection.class);
			} else if (serverObjectName.equals(ServerCategoryPickIDCollectionList.NAME)
					&& json.contains(ServerCategoryPickIDCollectionList.NAME)) {
				return mapper.readValue(json.toString(), ServerCategoryPickIDCollectionList.class);
			} else if (serverObjectName.equals(ServerPickList.NAME)
					&& json.contains(ServerPickList.NAME)) {
				return mapper.readValue(json.toString(), ServerPickList.class);
			} else if (serverObjectName.equals(ServerMemberCategoryList.NAME)
					&& json.contains(ServerMemberCategoryList.NAME)) {
				return mapper.readValue(json.toString(), ServerMemberCategoryList.class);
			} else if (serverObjectName.equals(ServerMemberMap.NAME)
					&& json.contains(ServerMemberMap.NAME)) {
				return mapper.readValue(json.toString(), ServerMemberMap.class);
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
				// a known error response. we need to process the token if
				// needed and the error should have an error message that we can
				// use.
				processStringResponse(errorPayload);
			} else {
				if (errorPayload.isEmpty()) {
					// this error is most likely a proxy ID which is no longer
					// valid. We can check the proxy ID to be sure.
					if (isProxyValid() == false) {
						errorPayload = "Proxy was no longer valid on API call: " + onAPICall
								+ " Clearing out stored proxy ID.";
						serverResponseData.setProxyID("");
					} else {
						// some unknown error.
						errorPayload = "An error occurred with no message on API call: "
								+ onAPICall;
					}
				} else if (errorPayload.contains("status\":404"))
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

	// Process the server response by collecting known elements contained in
	// the standard server response.
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
						clearFollowUp();
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

	private void clearFollowUp() throws SkipUseException {
		getAndProcess("/clearFollowUp", ServerResponse.NAME);
	}

	private RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	// Work-around for RestTemplate PATCH exchange request bug.
	private RestTemplate getRestTemplate_requestFactory() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		return new RestTemplate(requestFactory);
	}

}
