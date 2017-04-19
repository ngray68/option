package com.ngray.option.marketdata;

import com.ngray.option.financialinstrument.FinancialInstrument;

public interface MarketDataListener {

	public void onMarketDataUpdate(FinancialInstrument instrument, MarketData marketData);
}
