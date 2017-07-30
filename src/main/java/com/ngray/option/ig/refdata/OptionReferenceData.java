package com.ngray.option.ig.refdata;

import java.time.LocalDate;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;

/*
 * IG's market objects don't contain enough detail to determine underlyings, strikes, exact expiry dates etc.
 * In this case, I'm creating a static data reference map to look these up. These objects are the entries in the map
 */
public class OptionReferenceData {
	
	public enum Attribute {
		
		OptionEpic,
		UnderlyingEpic,
		Strike,
		Expiry,
		CallOrPut,
		DividendYield,
		RiskFreeRate;
	/*	
		private final String name;
		
		private Attribute(String name) {
			this.name = name;
		}*/
	}
	private final String optionName;
	private final Security underlying;
	private final String underlyingEpic;
	private final double strike;
	private final LocalDate expiryDate;
	private final EuropeanOption.Type callOrPut;
	private final double dividendYield;
	private final double riskFreeRate;
	
	public OptionReferenceData(String optionName, Security underlying, double strike, LocalDate expiryDate, EuropeanOption.Type callOrPut, double dividendYield, double riskFreeRate) {
	  this.optionName = optionName;  
	  this.underlying = underlying;
	  
	  // If we have an IGMarket, use that to determine the epic
	  // If we don't then use the identifier
	  if (underlying.getIGMarket() != null) {
		  this.underlyingEpic = underlying.getIGMarket().getEpic();
	  } else {
		  this.underlyingEpic = underlying.getIdentifier();
	  }
	  this.strike = strike;
	  this.expiryDate = expiryDate;
	  this.callOrPut = callOrPut;  
	  
	  /* This is a bit of hack - ideally we would have separate data sources for these parameters, with indirections to them from
	   * the option reference data so that we could take account of changes in option value due to changes in the discount rate and
	   * dividend yield - but this is a sort-of-okish approximation in a world of fairly static interest rates and low div yields
	   */
	  this.dividendYield = dividendYield;
	  this.riskFreeRate = riskFreeRate;
	}

	public OptionReferenceData(String optionName, String underlyingEpic, double strike, LocalDate expiryDate,
			EuropeanOption.Type callOrPut, double dividendYield, double riskFreeRate) {
		  this.optionName = optionName;  
		  this.underlying = null;;
		  this.underlyingEpic = underlyingEpic;
		  this.strike = strike;
		  this.expiryDate = expiryDate;
		  this.callOrPut = callOrPut; 
		  this.dividendYield = dividendYield;
		  this.riskFreeRate = riskFreeRate;
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

	public double getDividendYield() {
		return dividendYield;
	}

	public double getRiskFreeRate() {
		return riskFreeRate;
	}

	public String getUnderlyingEpic() {
		return underlyingEpic;
	}
}
