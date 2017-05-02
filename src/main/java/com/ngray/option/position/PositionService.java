package com.ngray.option.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.position.IGPositionList;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataListener;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.risk.Risk;
import com.ngray.option.risk.RiskListener;
import com.ngray.option.risk.RiskService;

public class PositionService {
	
	private final String name; 
	
	//private final PositionCache cache;
	private final List<Position> positions;
	
	private final Object listenerLock = new Object();
	
	private final Map<String, List<PositionListener>> listeners = new HashMap<>();
	
	public PositionService(String name) {
		Log.getLogger().info("Creating PositionService " + name);
		this.name = name;
		//this.cache = new PositionCache(name);
		this.positions = new ArrayList<>();
	}
	
	public String getName() {
		return name;
		
	}
	
	public void initialize(Session session) throws SessionException {
		Log.getLogger().info("Initializing PositionService " + getName());
		IGPositionList positionList = session.getPositions();
		positionList.getPositions().forEach(igPos -> {
			try {
				Log.getLogger().info("PositionService " + getName() + ": adding position " + igPos.getPositionDetail().getDealId());
				positions.add(new Position(igPos));
			} catch (MissingReferenceDataException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		} );
	}
	
	public List<Position> getPositions() {
		return Collections.unmodifiableList(positions);
	}
	
	public void subscribeToRiskService(Position position, RiskService riskService) {
		Log.getLogger().info("Position " + position.getId() + ": subcribing to RiskService " + riskService.getName());
		FinancialInstrument instrument = position.getInstrument();
		
		RiskListener riskListener = 
			riskService.addRiskListener(instrument, new RiskListener() {

				@Override
				public void onRiskUpdate(FinancialInstrument thisInstrument, Risk risk) {
					if (!thisInstrument.equals(instrument)) {
						Log.getLogger().warn("RiskListener::onRiskUpdate called with instrument: " + 
					                          thisInstrument + ", expected: " + instrument + ", update ignored");
						return;
					}
					
					position.updatePositionRisk(risk);
					notifyRiskUpdateListeners(position);
				}	
			});
	}
	
	public void subscribeToMarketDataService(Position position, MarketDataService marketDataService) {
		Log.getLogger().info("Position " + position.getId() + ": subcribing to MarketDataService " + marketDataService.getName());
		MarketDataListener marketDataListener = 
				marketDataService.addListener(position.getInstrument(), new MarketDataListener() {

				@Override
				public void onMarketDataUpdate(FinancialInstrument instrument, MarketData marketData) {
					if (!instrument.equals(position.getInstrument())) {
						Log.getLogger().warn("MarketDataListener::onMarketDataUpdate called with instrument: " + 
					                          instrument + ", expected: " + position.getInstrument() + ", update ignored");
						return;
					}
					
					position.updatePositionPnL(marketData);
					notifyPnLUpdateListeners(position);
				}	
			});
	}
	
	public void subscribeAllToRiskService(RiskService riskService) {
		Log.getLogger().info("Subscribing all positions to RiskService " + riskService.getName());
		positions.forEach(position -> subscribeToRiskService(position, riskService));
	}
	
	public void subscribeAllToMarketDataService(MarketDataService marketDataService) {
		Log.getLogger().info("Subscribing all positions to MarketDataService " + marketDataService.getName());
		positions.forEach(position -> subscribeToMarketDataService(position, marketDataService));
	}
	
	public PositionListener addListener(Position position, PositionListener listener) {
		Log.getLogger().info("PositionService " + getName() + ": adding subscription for " + position.getId());
		if (position == null || listener == null) return null;
		
		synchronized(listenerLock) {	
			if (!listeners.containsKey(position.getId())) {
				Log.getLogger().debug("PositionService " + getName() + ": creating new subscription list for position " + position.getId());
				listeners.put(position.getId(), new ArrayList<>());
			}
			
			Log.getLogger().debug("PositionService " + getName() + ": adding subscription to list for for " + position.getId());
			listeners.get(position.getId()).add(listener);
		}
		
		return listener;
	}
	
	public void removeListener(Position position, PositionListener listener) {
		Log.getLogger().info("PositionService " + getName() + ": removing subscription for " + position.getId());
		if(position == null || listener == null) return;
		
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listeners.get(position.getId()).remove(listener);
			}
			
			if (listeners.get(position.getId()).isEmpty()) {
				listeners.remove(position.getId());
			}
		}
	}
	
	protected void notifyPnLUpdateListeners(Position position) {
		Log.getLogger().info("PositionService " + getName() + ": notifying PnL subscriptions to " + position);
		if (position == null) return;
		
		List<PositionListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listenersCopy = new ArrayList<>(listeners.get(position.getId()));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onPositionPnLUpdate(position));
		}
	}
	
	protected void notifyRiskUpdateListeners(Position position) {
		Log.getLogger().info("PositionService " + getName() + ": notifying risk subscriptions to " + position);
		if (position == null) return;
		
		List<PositionListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listenersCopy = new ArrayList<>(listeners.get(position.getId()));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onPositionRiskUpdate(position));
		}
	}

}
