package com.ngray.option.ig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.Header;
import com.ngray.option.Log;
import com.ngray.option.ig.SessionException;
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
	private final boolean isLive;
	
	/**
	 * Create a new Session with the supplied Session headers
	 * The session headers will include keys identifying the session
	 * which must be supplied with every subsequent REST action after login
	 * @param sessionHeaders
	 * @param isLive
	 */
	private Session(List<Header> sessionHeaders, boolean isLive) {
		this.sessionHeaders  = new ArrayList<>(SessionConstants.getHeaderList(isLive));
		this.sessionHeaders.addAll(sessionHeaders);
		this.isLive = isLive;
	}
	
	/**
	 * Create a new bare session - this is only used to login
	 * @param isLive
	 */
	private Session(boolean isLive) {
		this.isLive = isLive;
		this.sessionHeaders  = new ArrayList<>(SessionConstants.getHeaderList(isLive));
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
	public static Session login(String username, String password, boolean encrypted, boolean isLive) throws SessionException {
		
		Log.getLogger().info("Logging in as " + username + " to" + (isLive ? " live account" : " demo account"));
		SessionLoginDetails loginDetails = new SessionLoginDetails(username, password, encrypted);
		RestAPIPost post = new RestAPIPost("/session", loginDetails.asJson());
		Session session = new Session(isLive);
		RestAPIResponse response = post.execute(session);
		List<Header> headers = response.getHeaders();
		return new Session(headers, isLive);
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
}
