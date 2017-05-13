package com.ngray.option.financialinstrument;

import com.ngray.option.ig.market.Market;
import com.ngray.option.model.DeltaOneModel;
import com.ngray.option.model.Model;

public class Security extends FinancialInstrument {


	public Security(String identifier) {
		super(identifier);
	}
	
	public Security(Market market) {
		super(market.getInstrumentName(), market);
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
		
		if (!(rhs instanceof Security)) {
			return false;
		}
		
		Security other = (Security)rhs;
		return getIdentifier().equals(other.getIdentifier());
	}
	
	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public Model getModel() {
		return new DeltaOneModel();
	}

	@Override
	public FinancialInstrument getUnderlying() {
		return this;
	}
}
