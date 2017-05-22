package com.ngray.option.risk;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.marketdata.MarketDataException;
import com.ngray.option.marketdata.MarketDataListener;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.model.ModelException;
import com.ngray.option.service.ServiceException;

/**
 * A service to manage multi-threaded access to a RiskCache
 * @author nigelgray
 *
 */
public class RiskService {
	
	private final RiskCache cache;
	
	private final Object riskListenerLock = new Object();
	
	private final Map<FinancialInstrument, List<RiskListener>> riskListeners;
	
	private final MarketDataService marketDataService;
	
	private final LocalDate valueDate;

	private Map<FinancialInstrument, MarketDataListener> marketDataListeners;

	/**
	 * Create a Risk data service with the specified name
	 * @param name
	 */
	public RiskService(String name, MarketDataService marketDataService, LocalDate valueDate) {
		Log.getLogger().info("Constructing RiskService: " + name);
		this.cache = new RiskCache(name);
		this.riskListeners = new HashMap<>();
		this.marketDataService = marketDataService;
		this.valueDate = valueDate;
		this.marketDataListeners = new HashMap<>();
	}
	
	/**
	 * Return the name of this service
	 * @return
	 */
	public String getName() {
		return cache.getName();
	}
	
	/**
	 * Add a listener for risk on the specified instrument
	 * @param instrument
	 * @param riskListener
	 * @return
	 */
	public RiskListener addRiskListener(FinancialInstrument instrument, RiskListener riskListener) {
		Log.getLogger().info("RiskService " + getName() + ": adding subscription for " + instrument);
		if (instrument == null || riskListener == null) return null;
		
		synchronized(riskListenerLock) {	
			if (!riskListeners.containsKey(instrument)) {
				Log.getLogger().debug("RiskService " + getName() + ": creating new subscription list for instrument " + instrument);
				riskListeners.put(instrument, new ArrayList<>());
				
				MarketDataListener marketDataListener = 
						(MarketDataListener)marketDataService.addListener(instrument, new MarketDataListener() {

							@Override
							public void onUpdate(FinancialInstrument instrument, MarketData marketData) {
								publishRisk(instrument, calculateRisk(instrument, marketData));
							}
							
						});
				
				marketDataListeners.put(instrument, marketDataListener);
				
			}
			
			Log.getLogger().debug("RiskService " + getName() + ": adding subscription to list for " + instrument);
			riskListeners.get(instrument).add(riskListener);
		}
		
		return riskListener;
	}
	
	/**
	 * Remove a listener for risk on the specified instrument
	 * @param instrument
	 * @param riskListener
	 */
	public void removeRiskListener(FinancialInstrument instrument, RiskListener riskListener) {
		Log.getLogger().info("RiskService " + getName() + ": removing subscription for " + instrument);
		if(instrument == null || riskListener == null) return;
		
		synchronized(riskListenerLock) {
			if (riskListeners.containsKey(instrument)) {
				riskListeners.get(instrument).remove(riskListener);
			}
			
			// if we have no more risk subscriptions, we no longer need the market data
			if (riskListeners.get(instrument).isEmpty()) {
				marketDataService.removeListener(instrument, marketDataListeners.remove(instrument));
				riskListeners.remove(instrument);
			}
		}
	}
	
	/**
	 * Notify all listeners of a risk update on the given instrument
	 * @param instrument
	 */
	protected void notifyRiskListeners(FinancialInstrument instrument) {
		Log.getLogger().info("RiskService " + getName() + ": notifying subscriptions to " + instrument);
		if (instrument == null) return;
		
		List<RiskListener> listenersCopy = null;
		synchronized(riskListenerLock) {
			if (riskListeners.containsKey(instrument)) {
				listenersCopy = new ArrayList<>(riskListeners.get(instrument));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onRiskUpdate(instrument, cache.getRisk(instrument)));
		}
	}
	
	/**
	 * Publish risk for the given instrument - the service will notify all registered listeners for
	 * that piece of risk.
	 * @param instrument
	 * @param risk
	 */
	public void publishRisk(FinancialInstrument instrument, Risk risk) {
		Log.getLogger().info("RiskService " + getName() + ": publish: " + instrument + risk);
		if (instrument == null || risk == null) return;
		cache.insertRisk(instrument, risk);
		notifyRiskListeners(instrument);
	}
		
	protected Risk calculateRisk(FinancialInstrument instrument, MarketData marketData) {	
		Log.getLogger().info("RiskService " + getName() + ": calculating risk for using " + marketData);
		try {
			Map<FinancialInstrument, MarketData> map = new HashMap<>();
			map.put(instrument, marketData);
			
			if (instrument instanceof EuropeanOption) {
				EuropeanOption option = (EuropeanOption) instrument;
				MarketData underlyingPrice = marketDataService.getData(option.getUnderlying()); 
				map.put(option.getUnderlying(), underlyingPrice);
			}
		
			MarketDataCollection marketDataCollection = new MarketDataCollection(map);
			return instrument.getModel().calculateRisk(instrument, marketDataCollection, valueDate);
		} catch (ModelException | ServiceException e) {
			Log.getLogger().error("RiskService " + getName() + ": " + e.getMessage(), e);
			return new Risk();
		}
	}
	
	/**
	 * Shutdown the service
	 */
	public void shutdown() {
		Log.getLogger().info("RiskService " + getName() + " shutdown");
		synchronized(riskListenerLock) {
			marketDataListeners.clear();
			riskListeners.clear();
		}
	}
}
