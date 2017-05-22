package com.ngray.option.marketdata;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.service.Service;
import com.ngray.option.service.ServiceDataSource;
import com.ngray.option.service.ServiceListener;

public class MarketDataService extends Service<FinancialInstrument, MarketData> {

	public MarketDataService(String name, ServiceDataSource<FinancialInstrument, MarketData> dataSource) {
		super(name, dataSource);
	}
	
	@Override
	public ServiceListener<FinancialInstrument, MarketData> addListener(FinancialInstrument instrument, ServiceListener<FinancialInstrument, MarketData> serviceListener) {
		
		// we want to initialize the service with the initial value of the price for the financial instrument in question
		if (instrument.getIGMarket() != null) {
			MarketData initialPrice = new MarketData(instrument.getIdentifier(), instrument.getIGMarket().getBid(), instrument.getIGMarket().getOffer(), Type.PRICE);
			publishData(instrument, initialPrice);
		}
		
		return super.addListener(instrument, serviceListener);
	}

}
