package com.ngray.option.ig;

public class SessionLoginDetails {

	private final String username;
	private final String password;
	private final boolean isEncrypted;
	
	public SessionLoginDetails(String username, String password, boolean isEncrypted) {
		this.username = username;
		this.password = password;
		this.isEncrypted = isEncrypted;
	}
	
	/**
	 * Return the login details as a json body for an IG session request
	 * @return
	 */
	public String asJson() {
		return "{\"encryptedPassword\" : " + isEncrypted  + ", \"identifier\" : \"" + username + "\", \"password\"  :\"" + password + "\"}";
	}
	
	

}
