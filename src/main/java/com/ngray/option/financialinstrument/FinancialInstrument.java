package com.ngray.option.financialinstrument;

import com.ngray.option.model.Model;

public abstract class FinancialInstrument {

	/**
	 * The unique id for the financial instrument
	 */
	private final String identifier;
	
	/**
	 * Constructor
	 * @param identifier
	 */
	public FinancialInstrument(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Return the unique identifier for the financial instrument
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Return a Model object that can be used to value the financial instrument
	 * @return
	 */
	public abstract Model getModel();
	
}
