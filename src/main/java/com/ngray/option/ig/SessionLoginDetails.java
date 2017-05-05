package com.ngray.option.ig;

import com.google.gson.Gson;

public class SessionLoginDetails {

	private String username;
	private String password;
	
	private String apiKey;
	private boolean encrypted;
	/*
	public SessionLoginDetails(String username, String password, boolean isEncrypted, String apiKey) {
		this.username = username;
		this.password = password;
		this.encrypted = isEncrypted;
		this.apiKey = apiKey;
	}*/
	
	public SessionLoginDetails() {
	}
	
	/**
	 * Return the login details (excluding apiKey) as a json body for an IG session request
	 * @return
	 */
	public String asJson() {
		return "{\"encryptedPassword\" : " + encrypted  + ", \"identifier\" : \"" + username + "\", \"password\"  :\"" + password + "\"}";
	}
	
	/**
	 * Return the apiKey
	 * @return
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	public static SessionLoginDetails fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, SessionLoginDetails.class);
	}
	
	

}
