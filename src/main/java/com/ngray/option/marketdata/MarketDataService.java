package com.ngray.option.marketdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;

/**
 * A service to manage multi-threaded access to a MarketDataCache
 * @author nigelgray
 *
 */
public class MarketDataService {
	
	/**
	 * The cache underlying this service
	 */
	private final MarketDataCache cache;
	
	/**
	 * Lock object for listener access
	 */
	private final Object listenerLock = new Object();
	
	/**
	 * Listeners for updates on financial instruments
	 */
	private final Map<FinancialInstrument, List<MarketDataListener>> listeners;
	
	/**
	 * Market data sources
	 */
	private final Map<FinancialInstrument, MarketDataPublisher> sources;
	
	/**
	 * Thread pool for market data source
	 */
	private final ExecutorService executor;
	
	/**
	 * Create a Market data service with the specified name
	 * @param name
	 */
	public MarketDataService(String name) {
		Log.getLogger().info("MarketDataService: Constructing service: " + name);
		cache = new MarketDataCache(name);
		listeners = new HashMap<>();
		sources = new HashMap<>();
		executor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Get the name of this service
	 * @return
	 */
	public String getName() {
		return cache.getName();
	}
	
	/**
	 * Add the specified listener as a subscription to the specified instrument
	 * @param instrument
	 * @param listener
	 * @return
	 */
	public MarketDataListener addListener(FinancialInstrument instrument, MarketDataListener listener) {
		Log.getLogger().info("MarketDataService: " + getName() + "\tAdding subscription for " + instrument);
		if (instrument == null || listener == null) return null;
		
		synchronized(listenerLock) {	
			if (!listeners.containsKey(instrument)) {
				Log.getLogger().debug("MarketDataService: " + getName() + "\tCreating new subscription list for instrument " + instrument);
				listeners.put(instrument, new ArrayList<>());
				
				// we also add a market data source for the instrument here, and start the source running
				sources.put(instrument,  new DummyMarketDataSource(instrument.getIdentifier(), instrument, this));
				executor.execute((Runnable)sources.get(instrument));
			}
			
			Log.getLogger().debug("MarketDataService: " + getName() + "\tAdding subscription to list for for " + instrument);
			listeners.get(instrument).add(listener);
		}
		
		return listener;
	}
	
	/**
	 * Remove the specified listener's subscription to the specified instrument
	 * @param instrument
	 * @param listener
	 */
	public void removeListener(FinancialInstrument instrument, MarketDataListener listener) {
		Log.getLogger().info("MarketDataService: " + getName() + "\tRemoving subscription for " + instrument);
		if(instrument == null || listener == null) return;
		
		synchronized(listenerLock) {
			if (listeners.containsKey(instrument)) {
				listeners.get(instrument).remove(listener);
			}
			
			// if we have no more subscriptions, we no longer need the market data
			// TODO - stop the market data source threads when we don't need them
			if (listeners.get(instrument).isEmpty()) {
				Log.getLogger().debug("MarketDataService: " + getName() + "\tRemoving market data source for instrument " + instrument);
				((DummyMarketDataSource)sources.remove(instrument)).shutdown();
				listeners.remove(instrument);
			}
		}
	}
	
	/**
	 * Notify all listeners for a given instrument that an update to market data has occurred.
	 * @param instrument
	 */
	protected void notifyListeners(FinancialInstrument instrument) {
		Log.getLogger().info("MarketDataService: " + getName() + "\tNotifying subscriptions to " + instrument);
		if (instrument == null) return;
		
		List<MarketDataListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(instrument)) {
				listenersCopy = new ArrayList<>(listeners.get(instrument));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onMarketDataUpdate(instrument, cache.getMarketData(instrument)));
		}
	}
	
	/**
	 * Publish market data for the given instrument - the service will notify all registered listeners for
	 * that piece of market data.
	 * @param instrument
	 * @param marketData
	 */
	public void publishMarketData(FinancialInstrument instrument, MarketData marketData) {
		Log.getLogger().info("MarketDataService: " + getName() + "\tPublish: " + instrument + "\t" + marketData);
		if (instrument == null || marketData == null) return;
		cache.insertMarketData(instrument, marketData);
		notifyListeners(instrument);
	}
	
	public int getListenerCount(FinancialInstrument instrument) {
		Log.getLogger().info("MarketDataService: " + getName() + "\tGetting subscription count for " + instrument);
		if (instrument == null) return 0;
		
		List<MarketDataListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(instrument)) {
				listenersCopy = new ArrayList<>(listeners.get(instrument));
			}
		}
		
		return listenersCopy == null ? 0 : listenersCopy.size();
		
	}
	
	public MarketData getMarketData(FinancialInstrument instrument) throws MarketDataException {
		MarketData marketData = cache.getMarketData(instrument);
		if (marketData == null) throw new MarketDataException("MarketDataService: " + getName() + "\tNo entry in cache for " + instrument);
		return marketData;
	}
}
