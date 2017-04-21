package com.ngray.option.ig;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;

/**
 * Instances of this class represent the response body returned from an IG login request.
 * Objects of this type are effectively immutable - fields are non-final due to the method of consruction
 * but cannot be changed through the class interface
 * @author nigelgray
 *
 */
public final class SessionInfo {

	private AccountInfo accountInfo;
	private String accountType;
	private List<AccountDetails> accounts;
	
	private String clientId;
	private String currencyIsoCode;
	private String currencySymbol;
	private String currentAccountId;
	private boolean dealingEnabled;
	private boolean hasActiveDemoAccounts;
	private boolean hasActiveLiveAccounts;
	private String lightstreamerEndpoint;
	private String reroutingEnvironment;
	private double timezoneOffset;
	private boolean trailingStopsEnabled;
	
	/**
	 * Private constructor - this object can only be constructed from Json
	 */
	private SessionInfo() {
	}
	
	public static SessionInfo fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, SessionInfo.class);
	}

	public AccountInfo getAccountInfo() {
		return accountInfo;
	}

	public String getAccountType() {
		return accountType;
	}

	public List<AccountDetails> getAccounts() {
		return Collections.unmodifiableList(accounts);
	}

	public String getClientId() {
		return clientId;
	}

	public String getCurrencyIsoCode() {
		return currencyIsoCode;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public String getCurrentAccountId() {
		return currentAccountId;
	}

	public boolean isDealingEnabled() {
		return dealingEnabled;
	}

	public boolean isHasActiveDemoAccounts() {
		return hasActiveDemoAccounts;
	}

	public boolean isHasActiveLiveAccounts() {
		return hasActiveLiveAccounts;
	}

	public String getLightStreamerEndpoint() {
		return lightstreamerEndpoint;
	}

	public String getReroutingEnvironment() {
		return reroutingEnvironment;
	}

	public double getTimezoneOffset() {
		return timezoneOffset;
	}

	public boolean isTrailingStopsEnabled() {
		return trailingStopsEnabled;
	}

}
