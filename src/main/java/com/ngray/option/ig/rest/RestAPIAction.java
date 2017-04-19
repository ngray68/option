package com.ngray.option.ig.rest;

import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.Session;

public interface RestAPIAction {

	/**
	 * Implement this method to execute the required action using the credentials
	 * in the supplied session. Throw a SessionException of the action fails
	 * Will throw NullPointerException if session is null
	 * @param session
	 * @return
	 * @throws SessionException, NullPointerException
	 */
	public RestAPIResponse execute(Session session) throws SessionException;
}
