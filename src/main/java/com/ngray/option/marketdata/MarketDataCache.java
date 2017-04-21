package com.ngray.option.marketdata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;

/**
 * A local cache for market data. We expect multiple threads to access
 * this cache, hence the implementation is ConcurrentHashMap
 * @author nigelgray
 *
 */
public class MarketDataCache {

	/**
	 * The name for this cache - useful if we require multiple caches in 
	 * the same process eg. EOD and LIVE
	 */
	private final String name;
	
	/**
	 * The map storing the market data
	 */
	private final Map<FinancialInstrument, MarketData> cache;
	
	public MarketDataCache(String name) {
		Log.getLogger().info("Constructing MarketDataCache: " + name);
		this.name = name;
		this.cache = new ConcurrentHashMap<>();
	}
	
	/**
	 * Get the name of this cache
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Insert the specified market data in the cache, with key instrument
	 * @param instrument
	 * @param marketData
	 */
	public void insertMarketData(FinancialInstrument instrument, MarketData marketData) {
		Log.getLogger().info("MarketDataCache " + getName() + ": insert: " + marketData);
		cache.put(instrument, marketData);
	}
	
	/**
	 * Get the cached market data for the specified instrument. Will return null if the
	 * entry is not present.
	 * @param instrument
	 * @return
	 */
	public MarketData getMarketData(FinancialInstrument instrument) {
		Log.getLogger().info("MarketDataCache " +getName() + ": get " + instrument);
		return cache.get(instrument);
	}
}
