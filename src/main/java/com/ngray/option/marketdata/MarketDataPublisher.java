package com.ngray.option.marketdata;

import com.ngray.option.financialinstrument.FinancialInstrument;

public interface MarketDataPublisher {
	
	public void publishMarketData(FinancialInstrument instrument, MarketData marketData);

}
