package com.ngray.option.marketdata;

import java.util.Collections;
import java.util.Map;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;

public class MarketDataCollection {
	
	private final Map<FinancialInstrument, MarketData> prices;
	
	public MarketDataCollection(Map<FinancialInstrument, MarketData> prices) {
		this.prices = Collections.unmodifiableMap(prices);
	}
	
	public MarketData getMarketData(FinancialInstrument instrument) throws MarketDataException {
		if (!prices.containsKey(instrument)) {
			throw new MarketDataException("MarketData object doesn't have a price for instrument " + instrument);
		}
		
		return prices.get(instrument);
	}
}
