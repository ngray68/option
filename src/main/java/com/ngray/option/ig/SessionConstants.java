package com.ngray.option.ig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public final class SessionConstants {
	
	private static final String PROTOCOL = "https://";
	private static final String DEMO_GATEWAY = "demo-api.ig.com/gateway/deal";
	private static final String LIVE_GATEWAY = "api.ig.com/gateway/deal";

	private static final String[][] HEADERS = {
			{ "Accept", 		"application/json; charset=UTF-8" },
			{ "Content-Type", 	"application/json; charset=UTF-8" },
			{ "VERSION" , 		"1" },
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
	
	public static String[][] getHeaders() {
		return HEADERS.clone();
	}
	
	public static List<Header> getHeaderList() {
		final List<Header> headers = new ArrayList<>();
		for (String[] header : HEADERS) {
			headers.add(new BasicHeader(header[0], header[1]));
		}
		return headers;
	}
}
