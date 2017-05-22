package com.ngray.option.marketdata.test;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.service.ServiceDataSource;
import com.ngray.option.service.ServiceListener;
import com.ngray.option.marketdata.MarketDataPublisher;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.marketdata.MarketDataListener;

public class TestMarketDataService {
	
	private MarketDataService service = new MarketDataService("Test", null);
	
	public class TestMarketDataSource implements Runnable, MarketDataPublisher {
		
		private final String name;
		private MarketDataService service = null;
		
		public TestMarketDataSource(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void connectToMarketDataService(MarketDataService service) {
			this.service = service;
		}

		@Override
		public void run() {
			if (service == null) return;
			
			FinancialInstrument instrument = new Security(name);
			int count = 1000;
			while (count > 0) {
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

		@Override
		public void publish(FinancialInstrument instrument, MarketData marketData) {
			service.publishData(instrument, marketData);			
		}
	}
	
	@Test
	public void testAddListener() throws InterruptedException {
		
		Log.getLogger().info("Testing AddListener");
		ExecutorService executor = Executors.newFixedThreadPool(20);
		// set up a random sources publishing data to the service
		TestMarketDataSource source  = new TestMarketDataSource("1");
		source.connectToMarketDataService(service);
		executor.execute(source);
		
		FinancialInstrument security = new Security("1");
		int listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 0);
		service.addListener(security, 
				(instrument, marketData) -> { Log.getLogger().info("Listener 1 - Received: " + instrument + "\t" + marketData); } 
				);
		
		listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 1);
		
		service.addListener(security, 
				(instrument, marketData) -> { Log.getLogger().info("Listener 2 - Received: " + instrument + "\t" + marketData); } 
				);
		
		listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 2);
		
		executor.shutdown();
		executor.awaitTermination(60000, TimeUnit.MILLISECONDS);
	}
		
	

	@Test
	public void testRemoveListener() throws InterruptedException {
		Log.getLogger().info("Testing AddListener");
		ExecutorService executor = Executors.newFixedThreadPool(20);
		// set up a random sources publishing data to the service
		TestMarketDataSource source  = new TestMarketDataSource("1");
		source.connectToMarketDataService(service);
		executor.execute(source);
		
		FinancialInstrument security = new Security("1");
		int listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 0);
		ServiceListener<FinancialInstrument, MarketData> listener1 = service.addListener(security, 
				(instrument, marketData) -> { Log.getLogger().info("Listener 1 - Received: " + instrument + "\t" + marketData); } 
				);
		
		ServiceListener<FinancialInstrument, MarketData> listener2 = service.addListener(security, 
				(instrument, marketData) -> { Log.getLogger().info("Listener 2 - Received: " + instrument + "\t" + marketData); } 
				);
		
		listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 2);
		
		// hang around for 5 seconds so the listeners can receive some messages
		Thread.sleep(5000);
		
		// now remove them
		service.removeListener(security, listener1);
		listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 1);
		
		service.removeListener(security, listener2);
		listenerCount = service.getListeners(security).size();
		assertTrue(listenerCount == 0);

		executor.shutdown();
		executor.awaitTermination(60000, TimeUnit.MILLISECONDS);
	}
/*
	@Test
	public void testNotifyListeners() {
		fail("Not yet implemented");
	}

	@Test
	public void testPublishMarketData() {
		fail("Not yet implemented");
	}
*/
}
