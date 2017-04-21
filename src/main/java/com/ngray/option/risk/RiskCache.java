package com.ngray.option.risk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;

public class RiskCache {
	/**
	 * The name for this cache - useful if we require multiple caches in 
	 * the same process eg. EOD and LIVE
	 */
	private final String name;
	
	/**
	 * The map storing the market data
	 */
	private final Map<FinancialInstrument, Risk> cache;
	
	public RiskCache(String name) {
		Log.getLogger().info("Constructing RiskCache " + name);
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
	 * Insert the specified risk object in the cache, with key instrument
	 * @param instrument
	 * @param marketData
	 */
	public void insertRisk(FinancialInstrument instrument, Risk risk) {
		Log.getLogger().info("RiskCache " + getName() + ": insert " + instrument + risk);
		cache.put(instrument, risk);
	}
	
	/**
	 * Remove the specified instrument and its risk from the risk cache
	 * @param instrument
	 */
	public void removeRisk(FinancialInstrument instrument) {
		Log.getLogger().info("RiskCache " + getName() + ": remove: " + instrument);
		cache.remove(instrument);
	}
	
	/**
	 * Get the cached risk for the specified instrument. Will return null if the
	 * entry is not present.
	 * @param instrument
	 * @return
	 */
	public Risk getRisk(FinancialInstrument instrument) {
		Log.getLogger().info("RiskCache " + getName() + ": get " + instrument);
		return cache.get(instrument);
	}
}
