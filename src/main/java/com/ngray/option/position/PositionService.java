package com.ngray.option.position;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.position.IGPositionList;
import com.ngray.option.ig.position.IGPositionUpdate;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataListener;
import com.ngray.option.marketdata.MarketDataService;
import com.ngray.option.risk.Risk;
import com.ngray.option.risk.RiskListener;
import com.ngray.option.risk.RiskService;
import com.ngray.option.service.ServiceListener;
import com.ngray.option.ui.PositionRiskTableModel;

public class PositionService {
	
	private final String name; 
	
	private final Object positionsLock = new Object();
	
	private final Map<String, Position> positions;
	
	private final Object listenerLock = new Object();
	
	private final Map<String, List<PositionListener>> listeners = new HashMap<>();
	
	private final Map<FinancialInstrument, PositionListener> listenersByUnderlying = new HashMap<>();
	
	private final Session session;
	
	private final RiskService riskService;
	
	private final MarketDataService marketDataService;
	
	private final PositionUpdateService positionUpdateService;
	
	
	private final Map<Position, RiskListener> riskListeners;
	
	// a position may have more than one market data listener if its underlying is a derivative
	// need a separate lock here as we need to synchronize non-atomic operations
	private final Object marketDataListenerLock = new Object();
	private final Map<Position, Map<FinancialInstrument, MarketDataListener>> marketDataListeners;
	
	public PositionService(String name, Session session, RiskService riskService, MarketDataService marketDataService, PositionUpdateService positionUpdateService) {
		Log.getLogger().info("Creating PositionService " + name);
		this.name = name;
		this.session = session;
		this.positions = new HashMap<>();
		this.riskService = riskService;
		this.marketDataService = marketDataService;
		this.positionUpdateService = positionUpdateService;
		this.riskListeners = new ConcurrentHashMap<>();
		this.marketDataListeners = new HashMap<>();
	}
	
	public String getName() {
		return name;
		
	}
	
	public void initialize() throws SessionException {
		Log.getLogger().info("Initializing PositionService " + getName());
		IGPositionList positionList = session.getPositions();
		positionList.getPositions().forEach(igPos -> {
			try {
				Log.getLogger().info("PositionService " + getName() + ": adding position " + igPos.getPositionDetail().getDealId());
				positions.put(igPos.getPositionDetail().getDealId(), new Position(igPos));
			} catch (MissingReferenceDataException e) {
				Log.getLogger().error(e.getMessage(), e);
			}
		} );
		
		subscribeAllToMarketDataService(marketDataService);
		subscribeAllToRiskService(riskService);
		subscribeToPositionUpdateService(positionUpdateService);
	}

	public List<Position> getPositions() {
		synchronized(positionsLock) {
			return Collections.unmodifiableList(positions.values().stream().collect(Collectors.toList()));
		}
	}
	
	public List<Position> getPositions(FinancialInstrument underlying) {
		synchronized(positionsLock) {
			return positions.values().stream().filter(position -> underlying.equals(position.getUnderlying())).collect(Collectors.toList());
		}
	}
	
	public Set<FinancialInstrument> getUnderlyings() {
		Set<FinancialInstrument> underlyings = new HashSet<>();
		synchronized(positionsLock) {
			positions.forEach((id, position) -> underlyings.add(position.getUnderlying()));
		}
		return underlyings;
	}
	
	protected void createDeleteOrUpdatePosition(IGPositionUpdate positionUpdate) {		
		if (positionUpdate == null) {
			Log.getLogger().error("Null position update received - ignoring");
			return;
		}
		
		Log.getLogger().info("Position update received " + positionUpdate.getDealId());
		
		switch (positionUpdate.getStatus()) {
			case "OPEN":
				openPosition(positionUpdate);
			break;
			case "UPDATED":
				updatePosition(positionUpdate);
			break;
			case "DELETED":
				deletePosition(positionUpdate);
			break;
			default:
				Log.getLogger().error("Unrecognised position update status: " + positionUpdate.getStatus());
		}
	}
	
	protected void updatePosition(IGPositionUpdate positionUpdate) {
		if (positionUpdate == null) return;
		
		Log.getLogger().info("PositionService " + getName() + ": updating position " + positionUpdate.getDealId());
		
		String dealId = positionUpdate.getDealId();
		Position existingPosition = null;
		synchronized(positionsLock) {
			if (!positions.containsKey(dealId)) {
				Log.getLogger().error("PositionService " + getName() + ": position to be updated is not present in the cache: id " + dealId);
				return;
			}	
			existingPosition = positions.get(dealId);
		}
		
		// null check should be unnecessary as we have called containsKey above, but just in case we
		// have a null entry...
		if (existingPosition != null) {
			if (positionUpdate.getDirection().equals("BUY")) {
				existingPosition.amendPositionSize(positionUpdate.getSize());
			} else {
				existingPosition.amendPositionSize(-positionUpdate.getSize());
			}
			notifyUpdatePositionListeners(existingPosition);
		}
	}

	protected void deletePosition(IGPositionUpdate positionUpdate) {
		if (positionUpdate == null) return;
		
		Log.getLogger().info("PositionService " + getName() + ": deleting position " + positionUpdate.getDealId());
		
		String dealId = positionUpdate.getDealId();
		Position deletedPosition = null;
		synchronized(positionsLock) {
			if (!positions.containsKey(dealId)) {
				Log.getLogger().error("PositionService " + getName() + ": position to be deleted is not present in the cache: id " + dealId);
				return;
			}
			
			deletedPosition = positions.remove(dealId);
		}
		
		// null check not strictly necessary, but just in case we have a null entry...
		if (deletedPosition != null) {
			notifyDeletePositionListeners(deletedPosition);
			unsubscribeFromMarketDataService(deletedPosition, marketDataService);
			unsubscribeFromRiskService(deletedPosition, riskService);
			
			PositionListener positionListener = getListener(deletedPosition.getUnderlying());
			if (positionListener != null) {
				removeListener(deletedPosition, positionListener);
			} 
		}
	}
	
	protected void openPosition(IGPositionUpdate positionUpdate) {
		if (positionUpdate == null) return;
		
		Log.getLogger().info("PositionService " + getName() + ": creating position " + positionUpdate.getDealId());
		
		String dealId = positionUpdate.getDealId();
		Position newPosition;
		try {
			newPosition = new Position(session.getPosition(dealId));
			synchronized(positionsLock) {
				positions.put(dealId, newPosition);
			}
			
			PositionListener positionListener = getListener(newPosition.getUnderlying());
			if (positionListener != null) {
				addListener(newPosition, positionListener);
			} else {
				/*	this doesn't work properly	- postpone to next check-in
				// create a new JFrame to show the new position and add it as a listener to this service
				// TODO - refactor this and the equivalent code in risk engine
				List<Position> newPositions = new ArrayList<>();
				newPositions.add(newPosition);
				PositionRiskTableModel model = new PositionRiskTableModel(newPositions);
				JTable table = new JTable(model);
				JScrollPane pane = new JScrollPane(table);
				JFrame frame = new JFrame();
				frame.add(pane);
				frame.pack();
				frame.setTitle(newPosition.getUnderlying().getName());
				
				EventQueue.invokeLater(()-> {
					try {
						frame.setVisible(true);
					} catch (HeadlessException e) {
						Log.getLogger().error(e.getMessage(), e);
					}
				});
				
				addListener(newPosition.getUnderlying(), model);
				*/
			}
			
			subscribeToMarketDataService(newPosition, marketDataService);
			subscribeToRiskService(newPosition, riskService);	
			notifyOpenPositionListeners(newPosition);
		} catch (MissingReferenceDataException | SessionException e) {
			Log.getLogger().error("PositionService " + getName() + ": create position failed for dealId " + positionUpdate.getDealId(), e);
		}		
	}
	
	protected void subscribeToRiskService(Position position, RiskService riskService) {
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
		
		// keep track so we can unsubscribe when the position closes
		riskListeners.put(position, riskListener);
	}
	
	protected void subscribeToMarketDataService(Position position, MarketDataService marketDataService) {
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
		
		// keep track so we can unsubscribe when the position closes
		synchronized(marketDataListenerLock) {
			if (!marketDataListeners.containsKey(position)) {
				marketDataListeners.put(position, new HashMap<>());
			}
			
			marketDataListeners.get(position).put(position.getInstrument(), marketDataListener);
		}
		
		if (position.getInstrument() instanceof EuropeanOption) {
			// we add a listener for the underlying price too so its available for risk calculations
			// this listener's purpose is to force the market data service to subscribe to the underlying price
			FinancialInstrument underlying = ((EuropeanOption)position.getInstrument()).getUnderlying();
			MarketDataListener listener = marketDataService.addListener(underlying, new MarketDataListener() {

				@Override
				public void onMarketDataUpdate(FinancialInstrument instrument, MarketData marketData) {
					if (!instrument.equals(underlying)) {
						Log.getLogger().warn("MarketDataListener::onMarketDataUpdate called with instrument: " + 
					                          instrument + ", expected: " + underlying + ", update ignored");
						return;
					}
				}	
			});
			
			synchronized(marketDataListenerLock) {
				marketDataListeners.get(position).put(position.getUnderlying(), listener);
			}
		}
	}
	
	protected void unsubscribeFromRiskService(Position deletedPosition, RiskService riskService) {
		Log.getLogger().info("PositionService " + getName() + ": unsubcribing from risk service for deleted position "  + deletedPosition.getId());
		riskService.removeRiskListener(deletedPosition.getInstrument(), riskListeners.get(deletedPosition));
		riskListeners.remove(deletedPosition);
	}

	protected void unsubscribeFromMarketDataService(Position deletedPosition, MarketDataService marketDataService) {
		Log.getLogger().info("PositionService " + getName() + ": unsubcribing from market data service for deleted position "  + deletedPosition.getId());
		synchronized(marketDataListenerLock) {
			Map<FinancialInstrument, MarketDataListener> listeners = marketDataListeners.get(deletedPosition);
			if (listeners != null) {
				listeners.keySet().forEach(
					(instrument) -> { marketDataService.removeListener(instrument, listeners.get(instrument)); }
						);
				marketDataListeners.remove(deletedPosition);
			}
		}
	}
	
	protected void subscribeAllToRiskService(RiskService riskService) {
		Log.getLogger().info("Subscribing all positions to RiskService " + riskService.getName());
		positions.forEach((id, position) -> subscribeToRiskService(position, riskService));
	}
	
	protected void subscribeAllToMarketDataService(MarketDataService marketDataService) {
		Log.getLogger().info("Subscribing all positions to MarketDataService " + marketDataService.getName());
		positions.forEach((id, position) -> subscribeToMarketDataService(position, marketDataService));
	}
	

	protected void subscribeToPositionUpdateService(PositionUpdateService positionUpdateService) {
		positionUpdateService.addListener(session.getSessionInfo().getCurrentAccountId(), new ServiceListener<String, IGPositionUpdate>() {

			@Override
			public void onUpdate(String accountId, IGPositionUpdate value) {
				createDeleteOrUpdatePosition(value);
			}});	
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

	protected void notifyUpdatePositionListeners(Position position) {
		Log.getLogger().info("PositionService " + getName() + ": notifying update position subscriptions to " + position);
		if (position == null) return;
		
		List<PositionListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listenersCopy = new ArrayList<>(listeners.get(position.getId()));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onUpdatePosition(position));
		}	
	}
	
	protected void notifyOpenPositionListeners(Position position) {
		Log.getLogger().info("PositionService " + getName() + ": notifying open position subscriptions to " + position);
		if (position == null) return;
		
		List<PositionListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listenersCopy = new ArrayList<>(listeners.get(position.getId()));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onOpenPosition(position));
		}
	}
	
	protected void notifyDeletePositionListeners(Position position) {
		Log.getLogger().info("PositionService " + getName() + ": notifying delete position subscriptions to " + position);
		if (position == null) return;
		
		List<PositionListener> listenersCopy = null;
		synchronized(listenerLock) {
			if (listeners.containsKey(position.getId())) {
				listenersCopy = new ArrayList<>(listeners.get(position.getId()));
			}
		}
		
		if (listenersCopy != null) {
			listenersCopy.forEach(listener -> listener.onDeletePosition(position));
		}
	}
	
	public void shutdown() {
		Log.getLogger().info("PositionService " + getName() + " shutdown");
		synchronized(listenerLock) {
			listeners.clear();
		}
	}

	public Session getSession() {
		return session;
	}
	
	public void addListener(FinancialInstrument underlying, PositionListener listener) {
		synchronized(listenerLock) {
			listenersByUnderlying.put(underlying, listener);
			getPositions(underlying).forEach(position -> addListener(position, listener));
		}
	}
	
	public PositionListener getListener(FinancialInstrument underlying) {
		synchronized(listenerLock) {
			return listenersByUnderlying.get(underlying);
		}
	}

	public RiskService getRiskService() {
		return riskService;
	}

	public MarketDataService getMarketDataService() {
		return marketDataService;
	}

	public PositionUpdateService getPositionUpdateService() {
		return positionUpdateService;
	}
}
