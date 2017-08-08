package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;

public class VolatilitySurfaceDataSet {

	private final LocalDate valueDate;
	private final Map<Security, MarketData> underlyingPrices;
	private final Map<Security, Set<EuropeanOption>> options;
	private final Map<EuropeanOption, MarketData> optionPrices;
	private final VolatilitySurfaceDefinition definition;
	
	public VolatilitySurfaceDataSet(LocalDate valueDate, VolatilitySurfaceDefinition definition, VolatilitySurfaceDataSetLoader loader) {
		this.valueDate = valueDate;
		this.definition = definition;
		this.underlyingPrices = loader.getUnderlyingPrices(valueDate, definition);
		this.options = loader.getOptions(valueDate, definition);
		this.optionPrices = loader.getOptionPrices(valueDate, definition);
	}

	public LocalDate getValueDate() {
		return valueDate;
	}

	public VolatilitySurfaceDefinition getDefinition() {
		return definition;
	}
	
	public Set<Security> getUnderlyings() {
		return underlyingPrices.keySet();
	}
	
	public Set<EuropeanOption> getOptions(Security underlying) {
		return options.get(underlying);
	}
	
	public MarketData getUnderlyingPrice(Security underlying) {
		return underlyingPrices.get(underlying);
	}
	
	public MarketData getOptionPrice(EuropeanOption option) {
		return optionPrices.get(option);
	}
	
}
