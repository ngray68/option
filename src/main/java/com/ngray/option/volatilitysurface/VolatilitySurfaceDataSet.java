package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.mongo.Price;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;

public class VolatilitySurfaceDataSet {

	private final LocalDate valueDate;
	private final Map<String, Price> underlyingPrices;
	private final Map<String, Set<String>> options;
	private final Map<String, Price> optionPrices;
	private final VolatilitySurfaceDefinition definition;
	
	public VolatilitySurfaceDataSet(LocalDate valueDate, Map<String, Price> underlyingPrices,
			Map<String, Set<String>> optionEpics, Map<String, Price> optionPrices,
			VolatilitySurfaceDefinition definition) {
		this.valueDate = valueDate;
		this.definition = definition;
		this.underlyingPrices = new TreeMap<>(underlyingPrices);
		this.options = new TreeMap<>(optionEpics);
		this.optionPrices = new TreeMap<>(optionPrices);
		
	}

	public LocalDate getValueDate() {
		return valueDate;
	}

	public VolatilitySurfaceDefinition getDefinition() {
		return definition;
	}
	
	public Set<String> getUnderlyingIdentifiers() {
		return underlyingPrices.keySet();
	}
	
	public Set<String> getOptionIdentifiers(Security underlying) {
		return options.get(underlying);
	}
	
	public Price getUnderlyingPrice(Security underlying) {
		return underlyingPrices.get(underlying.getIdentifier());
	}
	
	public Price getOptionPrice(EuropeanOption option) {
		return optionPrices.get(option.getIdentifier());
	}
	
}
