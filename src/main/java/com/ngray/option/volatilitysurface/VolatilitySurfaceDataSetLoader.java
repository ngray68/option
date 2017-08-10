package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.ngray.option.marketdata.MarketData;
import com.ngray.option.mongo.VolatilitySurfaceDefinition;

public interface VolatilitySurfaceDataSetLoader {
	
	public Map<String, MarketData> getUnderlyingPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition);
	public Map<String, Set<String>> getOptions(LocalDate valueDate, VolatilitySurfaceDefinition definition);
	public Map<String, MarketData> getOptionPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition);

}
