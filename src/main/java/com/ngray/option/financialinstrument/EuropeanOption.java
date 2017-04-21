package com.ngray.option.financialinstrument;

import java.time.LocalDate;

import com.ngray.option.ig.market.Market;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.ig.refdata.OptionReferenceData;
import com.ngray.option.ig.refdata.OptionReferenceDataMap;
import com.ngray.option.model.EuropeanOptionModel;
import com.ngray.option.model.Model;

/**
 * This class represents a European option on a simple
 * underlying security such as a stock or future
 * @author nigelgray
 *
 */
public class EuropeanOption extends FinancialInstrument {

	private final Security underlying;
	private final double strike;
	private final LocalDate expiryDate;
	private final Type type;
	
	public enum Type {
		CALL,
		PUT
	};
	
	/**
	 * Construct a European option on the specified underlying of the given strike
	 * and type
	 * @param identifier
	 * @param underlying
	 * @param strike
	 * @param type
	 */
	public EuropeanOption(String identifier, Security underlying, double strike, LocalDate expiryDate, Type type) {
		super(identifier);
		this.underlying = underlying;
		this.strike = strike;
		this.expiryDate = expiryDate;
		this.type = type;
	}
	
	public EuropeanOption(Market market) throws MissingReferenceDataException {
		super(market.getInstrumentName(), market);
		OptionReferenceData refData = OptionReferenceDataMap.getOptionReferenceData(market.getInstrumentName());
		this.underlying = refData.getUnderlying();
		this.strike = refData.getStrike();
		this.expiryDate = refData.getExpiryDate();
		this.type = refData.getCallOrPut();		
	}
	
	// Overrides from Object
	@Override
	public String toString() {
		return getIdentifier();
	}
	
	@Override
	public boolean equals(Object rhs) {
		if (rhs == null) {
			return false;
		}
		
		if (this == rhs) {
			return true;
		}
		
		if (!(rhs instanceof EuropeanOption)) {
			return false;
		}
		
		EuropeanOption other = (EuropeanOption)rhs;
		boolean result = getIdentifier().equals(other.getIdentifier()) &&
				         (Double.compare(getStrike(), other.getStrike()) == 0) &&
				         getType() == other.getType() &&
				         getExpiryDate().equals(other.getExpiryDate());
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}
	
	// Overrides from Security
	@Override
	public Model getModel() {
		return new EuropeanOptionModel();
	}

	/**
	 * Return the option's underlying security
	 * @return
	 */
	public Security getUnderlying() {
		return underlying;
	}

	/**
	 * Return the option's strike price
	 * @return
	 */
	public double getStrike() {
		return strike;
	}

	/**
	 * Return the option's type (Put or Call)
	 * @return
	 */
	public Type getType() {
		return type;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}
}
