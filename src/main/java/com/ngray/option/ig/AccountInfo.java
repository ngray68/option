package com.ngray.option.ig;

import com.google.gson.Gson;

/**
 * Class representing the current account information returned in the response body of an IG login request
 * Objects of this type are effectively immutable - fields are non-final due to the method of consruction
 * but cannot be changed through the class interface
 * @author nigelgray
 *
 */
public final class AccountInfo {
	
	private double available;
	private double balance;
	private double deposit;
	private double profitLoss;
	
	/**
	 * Private constructor - this object can only be constructed from Json
	 */
	private AccountInfo() {
	}
	
	public static AccountInfo fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, AccountInfo.class);
	}

	public double getAvailable() {
		return available;
	}

	public double getBalance() {
		return balance;
	}

	public double getDeposit() {
		return deposit;
	}

	public double getProfitLoss() {
		return profitLoss;
	}
}
