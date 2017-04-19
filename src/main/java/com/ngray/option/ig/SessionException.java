package com.ngray.option.ig;

// We will not serialize instances of this class so its safe to suppress this warning
@SuppressWarnings("serial")
public class SessionException extends Exception {
	
	/**
	 * Constructor for SessionException
	 * @param message
	 */
	public SessionException(String message) {
		super(message);
	}
	
	/**
	 * Constructor wrapping another exception
	 * @param wrappedException
	 */
	public SessionException(Exception wrappedException) {
		super(wrappedException);
	}

}
