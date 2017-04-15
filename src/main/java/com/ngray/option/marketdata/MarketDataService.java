package com.ngray.option.marketdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Create a Market data service with the specified name
	 * @param name
	 */
	public MarketDataService(String name) {
		Log.getLogger().info("Constructing MarketDataService: " + name);
		cache = new MarketDataCache(name);
		listeners = new HashMap<>();
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
		Log.getLogger().info("Adding subscription for " + instrument);
		if (instrument == null || listener == null) return null;
		
		synchronized(listenerLock) {	
			if (!listeners.containsKey(instrument)) {
				Log.getLogger().debug("Creating new subscription list for Instrument " + instrument);
				listeners.put(instrument, new ArrayList<>());
			}
			
			Log.getLogger().debug("Adding subscription to list for for " + instrument);
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
		Log.getLogger().info("Removing subscription for " + instrument);
		if(instrument == null || listener == null) return;
		
		synchronized(listenerLock) {
			if (listeners.containsKey(instrument)) {
				listeners.get(instrument).remove(listener);
			}
		}
	}
	
	/**
	 * Notify all listeners for a given instrument that an update to market data has occurred.
	 * @param instrument
	 */
	public void notifyListeners(FinancialInstrument instrument) {
		Log.getLogger().info("Notifying subscriptions to " + instrument);
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
		Log.getLogger().info("Publish: " + instrument + "\t" + marketData);
		cache.insertMarketData(instrument, marketData);
		notifyListeners(instrument);
	}
	
	public int getListenerCount(FinancialInstrument instrument) {
		Log.getLogger().info("Getting subscription count for " + instrument);
		if (instrument == null) return 0;
		
		List<MarketDataListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(instrument)) {
				listenersCopy = new ArrayList<>(listeners.get(instrument));
			}
		}
		
		return listenersCopy == null ? 0 : listenersCopy.size();
		
	}
	
	
	

}
