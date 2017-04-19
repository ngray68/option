package com.ngray.option.ig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public final class SessionConstants {
	
	private static final String PROTOCOL = "https://";
	private static final String DEMO_GATEWAY = "demo-api.ig.com/gateway/deal";
	private static final String LIVE_GATEWAY = "api.ig.com/gateway/deal";
	private static final String DEMO_API_KEY = "dummykey"; // replace with real key locally
	private static final String LIVE_API_KEY = "dummykey"; // replace with real key locally
	
	// TODO: import api keys from user supplied config
	
	private static final String[][] DEMO_HEADERS = {
			{ "Accept", 		"application/json; charset=UTF-8" },
			{ "Content-Type", 	"application/json; charset=UTF-8" },
			{ "VERSION" , 		"1" },
			{ "X-IG-API-KEY", 	DEMO_API_KEY }
	};
	
	private static final String[][] LIVE_HEADERS = {
			{ "Accept", 		"application/json; charset=UTF-8" },
			{ "Content-Type", 	"application/json; charset=UTF-8" },
			{ "VERSION" , 		"1" },
			{ "X-IG-API-KEY", 	LIVE_API_KEY }
	};
	
	
	public static final String CST = "CST";
	public static final String X_SECURITY_TOKEN = "X-SECURITY-TOKEN";
	
	public static String getProtocol() {
		return PROTOCOL;
	}
	
	public static String getGateway(boolean isLive) {
		return isLive ? LIVE_GATEWAY : DEMO_GATEWAY;
	}
	
	public static String getSessionUrl(boolean isLive) {
		return isLive ? PROTOCOL + LIVE_GATEWAY + "/session" : PROTOCOL + DEMO_GATEWAY + "/session";
	}
	
	public static String[][] getHeaders(boolean isLive) {
		return isLive ? LIVE_HEADERS.clone() : DEMO_HEADERS.clone();
	}
	
	public static List<Header> getHeaderList(boolean isLive) {
		final List<Header> headers = new ArrayList<>();
		for (String[] header : isLive ? LIVE_HEADERS : DEMO_HEADERS) {
			headers.add(new BasicHeader(header[0], header[1]));
		}
		return headers;
	}

	public static String getAPIKey(boolean isLive) {
		return isLive ? LIVE_API_KEY : DEMO_API_KEY;
	}
	
	public static String getLoginMessageBody(String username, String password) {
		return "{\"identifier\" : \"" + username + "\", \"password\"  :\"" + password + "\"}";
	}
}
