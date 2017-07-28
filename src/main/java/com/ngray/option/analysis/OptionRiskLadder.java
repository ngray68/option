package com.ngray.option.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.risk.Risk;
import com.ngray.option.risk.RiskListener;
import com.ngray.option.risk.RiskService;

/**
 * This class publishes live risk and implied volatility for a collection
 * of option contracts supplied. The class OptionRiskLadderView is used
 * to show this risk, enabling users to see at a glance how implied volatility
 * and risk behaves across strikes
 * @author nigelgray
 *
 */
public class OptionRiskLadder implements RiskListener {

	private final Map<EuropeanOption, Risk> riskMap;
	private final RiskService riskService;
	
	// OptionRiskLadder is itself a risk service
	private final Object listenerLock = new Object();
	private final List<RiskListener> listeners = new ArrayList<>();
	
	public OptionRiskLadder(List<EuropeanOption> options) {
		riskMap = new ConcurrentHashMap<>();
		
		options.forEach(
				(option) -> { riskMap.put(option, new Risk()); }
			);
		
		riskService = new RiskService("OptionRiskLadder", RiskEngine.getMarketDataService(), LocalDate.now());
		subscribeAllToRiskService();
		subscribeUnderlyingsToMarketDataService();
	}
	
	private void subscribeUnderlyingsToMarketDataService() {
		Log.getLogger().info("OptionRiskLadder: subscribing to underlyings to MarketDataService...");
		Set<Security> underlyings = riskMap.keySet().stream().map(option -> option.getUnderlying()).collect(Collectors.toSet());
		underlyings.forEach(underlying -> RiskEngine.getMarketDataService().addListener(underlying, (instrument, marketData) -> {}));		
	}

	@Override
	public void onRiskUpdate(FinancialInstrument instrument, Risk risk) {
		if (instrument == null) {
			Log.getLogger().warn("OptionRiskLadder::onRiskUpdate called for null financial instrument - ignoring");			
		}
		if (!(instrument instanceof EuropeanOption)) {
			Log.getLogger().warn("OptionRiskLadder::onRiskUpdate called for non-option financial instrument - ignoring");
		}
		EuropeanOption option = (EuropeanOption)instrument;
		riskMap.put(option, risk);
		notifyListeners(option);
	}
	
	public RiskListener addListener(RiskListener riskListener) {
		synchronized(listenerLock) {
			listeners.add(riskListener);
		}
		return riskListener;
	}
	
	public void removeListener(RiskListener riskListener) {
		synchronized(listenerLock) {
			listeners.remove(riskListener);
		}
	}
	
	public Map<EuropeanOption, Risk> getRiskMap() {
		return Collections.unmodifiableSortedMap(new TreeMap<>(riskMap));
	}
	
	protected void notifyListeners(EuropeanOption option) {
		Log.getLogger().info("OptionRiskLadder: notifying subscriptions to " + option);
		if (option == null) return;
		
		List<RiskListener> listenersCopy = null;
		synchronized(listenerLock) {
				listenersCopy = new ArrayList<>(listeners);
			}
	
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onRiskUpdate(option, riskMap.get(option)));;
		}	
	}

	private void subscribeAllToRiskService() {
		Log.getLogger().info("OptionRiskLadder: subscribing to RiskService...");
		riskMap.forEach((option, risk) -> subscribeToRiskService(option));
	}

	private void subscribeToRiskService(EuropeanOption option) {
		Log.getLogger().info("OptionRiskLadder: subscribing to RiskService for " + option);
		riskService.addRiskListener(option, this);
	}
	
	private void unsubscribeAll() {
		Log.getLogger().info("OptionRiskLadder: unsubscribing from RiskService...");
		riskMap.forEach((option, risk) -> unsubscribe(option));
	}
	
	private void unsubscribe(EuropeanOption option) {
		Log.getLogger().info("OptionRiskLadder: subscribing to RiskService for " + option);
		riskService.removeRiskListener(option, this);
	}	
}
