package com.ngray.option.marketdata;

import java.util.Random;

import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.marketdata.MarketData.Type;

public class DummyMarketDataSource implements MarketDataPublisher, Runnable {

	private final String name;
	private final MarketDataService service;
	private final FinancialInstrument instrument;
	private volatile boolean stop;
	
	public DummyMarketDataSource(String name, FinancialInstrument instrument, MarketDataService service) {
		this.name = name;
		this.service = service;
		this.instrument = instrument;
		stop = false;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void run() {
		if (service == null) return;
		
		int count = 10;
		while (!stop && count > 0) {
			double value = new Random().nextDouble();
			MarketData marketData = new MarketData(instrument.getIdentifier(), value, Type.PRICE);
			publish(instrument, marketData);
			--count;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void shutdown() {
		stop = true;
	}

	@Override
	public void publish(FinancialInstrument instrument, MarketData marketData) {
		service.publishData(instrument, marketData);			
	}

}
