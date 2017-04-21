package com.ngray.option.ig;

import com.google.gson.Gson;

public class SessionLoginDetails {

	private String username;
	private String password;
	private boolean encrypted;
	private String apiKey;
	/*
	public SessionLoginDetails(String username, String password, boolean isEncrypted, String apiKey) {
		this.username = username;
		this.password = password;
		this.encrypted = isEncrypted;
		this.apiKey = apiKey;
	}*/
	
	public SessionLoginDetails() {
		this.username = "ngray68demo";
		this.password = "Stargate50";
		this.encrypted = false;
		this.apiKey = "4b17a7696dd2b7bb0ea793c3bd734c8f78b5b186";
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
		return new Gson().fromJson(json, SessionLoginDetails.class);
	}
	
	

}
