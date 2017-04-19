package com.ngray.option.marketdata;

import com.ngray.option.financialinstrument.FinancialInstrument;

public interface MarketDataSource {
	
	public void publishMarketData(FinancialInstrument instrument, MarketData marketData);

}
