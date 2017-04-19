package com.ngray.option.ig.refdata;

import java.time.LocalDate;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;

/*
 * IG's market objects don't contain enough detail to determine underlyings, strikes, exact expiry dates etc.
 * In this case, I'm creating a static data reference map to look these up. These objects are the entries in the map
 */
public class OptionReferenceData {
	
	private final String optionName;
	private final Security underlying;
	private final double strike;
	private final LocalDate expiryDate;
	private final EuropeanOption.Type callOrPut;
	
	public OptionReferenceData(String optionName, String underlyingName, double strike, LocalDate expiryDate, EuropeanOption.Type callOrPut) {
	  this.optionName = optionName;
	  this.underlying= new Security(underlyingName);
	  this.strike = strike;
	  this.expiryDate = expiryDate;
	  this.callOrPut = callOrPut;  
	}

	public String getOptionName() {
		return optionName;
	}

	public Security getUnderlying() {
		return underlying;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public EuropeanOption.Type getCallOrPut() {
		return callOrPut;
	}

	public double getStrike() {
		return strike;
	}
}
