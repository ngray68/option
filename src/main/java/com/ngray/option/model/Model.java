package com.ngray.option.model;

import java.time.LocalDate;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.risk.Risk;

public interface Model {
	
	public Risk calculateRisk(FinancialInstrument instrument, MarketDataCollection marketData, LocalDate valueDate) throws ModelException;

}
