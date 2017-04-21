package com.ngray.option.ig;

import com.google.gson.Gson;

/**
 * Class represents the Account details returned in the response body of an IG login
 * Objects of this type are effectively immutable - fields are non-final due to the method of consruction
 * but cannot be changed through the class interface
 * @author nigelgray
 *
 */
public final class AccountDetails {
	
	private String accountId;
	private String accountName;
	private String accountType;
	private boolean preferred;
	
	/**
	 * Private constructor - this object can only be constructed from Json
	 */
	private AccountDetails() {
	}
	
	public static AccountDetails fromJson(String json) {
		return new Gson().fromJson(json, AccountDetails.class);
	}

	public String getAccountId() {
		return accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getAccountType() {
		return accountType;
	}

	public boolean isPreferred() {
		return preferred;
	}
}
