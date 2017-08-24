package com.ngray.option.ig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.ngray.option.Log;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.position.IGPosition;
import com.ngray.option.ig.position.IGPositionList;
import com.ngray.option.ig.rest.RestAPIDelete;
import com.ngray.option.ig.rest.RestAPIGet;
import com.ngray.option.ig.rest.RestAPIPost;
import com.ngray.option.ig.rest.RestAPIResponse;


/**
 * Session class
 * This class is the main class of the AutoTrader application
 * @author nigelgray
 *
 */
public final class Session {
		
	private final List<Header> sessionHeaders;
	private final SessionInfo sessionInfo;
	private final boolean isLive;
	
	/**
	 * Create a new Session with the supplied Session headers
	 * The session headers will include keys identifying the session
	 * which must be supplied with every subsequent REST action after login,
	 * and must also include the basic headers required to exceute a loging eg api key
	 * @param sessionHeaders
	 * @param isLive
	 */
	private Session(List<Header> sessionHeaders, SessionInfo sessionInfo, boolean isLive) {
		this.sessionHeaders  = new ArrayList<>(sessionHeaders);
		this.sessionInfo = sessionInfo;
		this.isLive = isLive;
	}
	
	/**
	 * Create a new bare session - this is only used to login
	 * @param isLive
	 */
	private Session(String apiKey, boolean isLive) {
		this.isLive = isLive;
		this.sessionHeaders  = new ArrayList<>(SessionConstants.getHeaderList());
		this.sessionHeaders.add(new BasicHeader("X-IG-API-KEY", apiKey));
		this.sessionInfo = null;
	}
	
	/**
	 * Static method login
	 * Returns a valid Session object if login is successful, throws a SessionException if not
	 * @param username
	 * @param password
	 * @param encrypted (true if the password supplied is encrypted)
	 * @param isLive    (true if the required session is live, false if a demo session is required)
	 * @return a Session
	 * @throws SessionException
	 */
	public static Session login(SessionLoginDetails loginDetails, boolean isLive) throws SessionException {
		
		Log.getLogger().info("Logging in to" + (isLive ? " live account" : " demo account"));
		
		RestAPIPost post = new RestAPIPost("/session", loginDetails.asJson());
		Session session = new Session(loginDetails.getApiKey(), isLive);
		RestAPIResponse response = post.execute(session);
		List<Header> headers = new ArrayList<>(session.getSessionHeaders());
		headers.addAll(response.getHeaders());
		SessionInfo sessionInfo = SessionInfo.fromJson(response.getResponseBodyAsJson());	
		return new Session(headers, sessionInfo, isLive);
	}
	
	/**
	 * Logout from the current session
	 * @throws SessionException
	 */
	public void logout() throws SessionException{
		
		Log.getLogger().info("Logging out of session");
		RestAPIDelete logout = new RestAPIDelete("/session");
		logout.execute(this);
	}
	
	/**
	 * Return a list of the headers needed to submit requests using this session
	 * @return
	 */
	public List<Header> getSessionHeaders() {
		return Collections.unmodifiableList(sessionHeaders);
	}
	
	/**
	 * Return a modified copy of headers parameter with version 3 instead of version  1
	 * @param headers
	 * @return
	 */
	public List<Header> getSessionHeadersVersion3(List<Header> headers) {
		List<Header> modifiedHeaders = headers.stream().filter(header -> !header.getName().equals("VERSION")).collect(Collectors.toList());
		modifiedHeaders.add(new BasicHeader("VERSION", "3"));
		return modifiedHeaders;
	}
	
	/**
	 * Return the CST. Empty string if not present
	 * @param name
	 * @return
	 */
	public String getClientSecurityToken() {
		String result = null;
		for (Header header : sessionHeaders) {
			if (header.getName().equals(SessionConstants.CST)) {
				result = header.getValue();
			}
		}
		return result;
	}
	
	/**
	 * Return the CST. Empty string if not present
	 * @param name
	 * @return
	 */
	public String getXSecurityToken() {
		String result = null;
		for (Header header : sessionHeaders) {
			if (header.getName().equals(SessionConstants.X_SECURITY_TOKEN)) {
				result = header.getValue();
			}
		}
		return result;
	}
	
	/**
	 * Return the session info object for this session
	 * @return
	 */
	public SessionInfo getSessionInfo() {
		return sessionInfo;
	}
	
	/**
	 * Return true if the Session connects to a live account
	 * false if connects to a demo account
	 * @return
	 */
	public boolean getIsLive() {
		return isLive;
	}
	
	/**
	 * Return a list of all the open positions for the active account
	 * @throws SessionException 
	 */
	public IGPositionList getPositions() throws SessionException {
		RestAPIGet get = new RestAPIGet("/positions");
		RestAPIResponse response = get.execute(this);
		String json = response.getResponseBodyAsJson();
		return IGPositionList.fromJson(json);
	}

	/**
	 * Return the open position specified by the dealId
	 * @param dealId
	 * @return
	 * @throws SessionException
	 */
	public IGPosition getPosition(String dealId) throws SessionException {
		RestAPIGet get = new RestAPIGet("/positions/" + dealId);
		RestAPIResponse response = get.execute(this);
		String json = response.getResponseBodyAsJson();
		return IGPosition.fromJson(json);
	}
}
