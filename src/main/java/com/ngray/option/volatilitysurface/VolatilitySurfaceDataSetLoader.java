package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;

public interface VolatilitySurfaceDataSetLoader {
	
	public Map<Security, MarketData> getUnderlyingPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition);
	public Map<Security, Set<EuropeanOption>> getOptions(LocalDate valueDate, VolatilitySurfaceDefinition definition);
	public Map<EuropeanOption, MarketData> getOptionPrices(LocalDate valueDate, VolatilitySurfaceDefinition definition);

}
