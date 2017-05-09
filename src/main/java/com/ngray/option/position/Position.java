package com.ngray.option.position;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.financialinstrument.Security;
import com.ngray.option.ig.position.IGPosition;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.model.ModelException;
import com.ngray.option.risk.Risk;

/**
 * Models a trading position in a financial instrument
 * @author nigelgray
 *
 */
public class Position {
	
	private final String id;
	
	private final FinancialInstrument instrument;
	
	private final double positionSize;
	
	private final double open;
	
	private double latest;
		
	private double positionPnL;
	
	private Risk positionRisk;
	
	private final IGPosition igPosition;
	
	/**
	 * Construct a position object from the supplied IGPosition
	 * @param igPosition
	 */
	public Position(IGPosition igPosition) throws MissingReferenceDataException {
		this.igPosition = igPosition;
		this.id = igPosition.getPositionDetail().getDealId();
		this.positionSize = igPosition.getPositionDetail().getDealSize();
		this.open = igPosition.getPositionDetail().getOpenLevel();
		this.latest = Double.NaN;
		this.instrument = FinancialInstrument.fromIGMarket(igPosition.getMarket());
		
		// initialize PnL and risk to NaN
		initializePnL();
		initializeRisk();
	}
	
	/**
	 * Construct a position of size positionSize, in the instrument supplied at price open
	 * @param instrument
	 * @param positionSize
	 * @param open
	 */
	public Position(String id, FinancialInstrument instrument, double positionSize, double open) {
		this.igPosition = null;
		this.id = id;
		this.instrument = instrument;
		this.positionSize = positionSize;
		this.open = open;
		this.latest = Double.NaN;
		
		// initialize PnL and risk to NaN
		this.positionPnL = Double.NaN;
		this.positionRisk = new Risk();
	}
	
	private void initializeRisk() {
		if (igPosition != null) {
			double bid = igPosition.getMarket().getBid();
			double offer = igPosition.getMarket().getOffer();
			Map<FinancialInstrument, MarketData> map = new HashMap<>();
			map.put(instrument, new MarketData(instrument.getIdentifier(), bid, offer, Type.PRICE));
			MarketDataCollection marketDataColl = new MarketDataCollection(map);
		
			if (instrument instanceof EuropeanOption) {
				EuropeanOption option = (EuropeanOption) instrument;
				double underlyingBid = option.getUnderlying().getIGMarket().getBid();
				double underlyingOffer = option.getUnderlying().getIGMarket().getOffer();
 				map.put(option.getUnderlying(), new MarketData(option.getUnderlying().getIdentifier(), underlyingBid, underlyingOffer, Type.PRICE));
			}
		
			try {
				Risk riskOnOneContract = instrument.getModel().calculateRisk(instrument, marketDataColl, LocalDate.now());
				updatePositionRisk(riskOnOneContract);
			} catch (ModelException e) {
				Log.getLogger().error(e.getMessage(), true);
			}		
		} 
	}
	
	private void initializePnL() {
		if (igPosition != null && positionSize > 0) {
			latest =  igPosition.getMarket().getBid();
			positionPnL = (latest - getOpen()) * getPositionSize();
			
		} else if (igPosition != null && positionSize < 0) {
			latest = igPosition.getMarket().getOffer();
			positionPnL = (latest - getOpen()) * getPositionSize();
		}
	}
	
	/**
	 * Update the position risk given the risk on a single contract supplied
	 * @param riskOnPositionOfSizeOne
	 */
	public void updatePositionRisk(Risk riskOnPositionOfSizeOne) {
		Log.getLogger().info("Position: " + getId() + " updating risk...");
		positionRisk = riskOnPositionOfSizeOne.multiply(getPositionSize());
		Log.getLogger().debug(positionRisk);
	}
	
	/**
	 * Update the position PnL from the market data supplied
	 * @param marketData
	 */
	public void updatePositionPnL(MarketData marketData) {
		Log.getLogger().info("Position: " + getId() + " updating PnL...");
		
		latest = marketData.getMid();
		if (Double.compare(positionSize, 0.0) >= 0) {
			if (!Double.isNaN(marketData.getBid())) {
				Log.getLogger().debug("Long position - Using bid price " + marketData.getBid());
				latest = marketData.getBid();
			}
		} else {
			if (!Double.isNaN(marketData.getOffer())) {
				Log.getLogger().debug("Long position - Using offer price " + marketData.getOffer());
				latest = marketData.getOffer();
			}
		}
		
		positionPnL = (latest - getOpen()) * getPositionSize();
		Log.getLogger().debug(getPositionDetails());
	}
	
	@Override
	public String toString() {
		String result = getPositionDetails();
		result+=getPositionRisk();
		return result;
	}
	
	public String getPositionDetails() {
		String result = "\n\nPosition: " + getId();
		result+="\n===================================";
		result+="\nSecurity:\t" + getInstrument();
		result+="\nPosition:\t" + getPositionSize();
		result+="\nOpen:\t\t" + getOpen();
		result+="\nLatest:\t\t" + getLatest();
		result+="\nPnL:\t\t" + getPositionPnL();
		result+="\n===================================\n\n";
		return result;
	}
	
	/**
	 * Get the position ID
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the financial instrument underlying this position
	 * @return
	 */
	public FinancialInstrument getInstrument() {
		return instrument;
	}

	/**
	 * Get the position size - short positions are negative
	 * @return
	 */
	public double getPositionSize() {
		return positionSize;
	}

	/**
	 * Get the position PnL
	 * @return
	 */
	public double getPositionPnL() {
		return positionPnL;
	}

	/**
	 * Get the price at which the position was opened
	 * @return
	 */
	public double getOpen() {
		return open;
	}
	
	/**
	 * Return the latest price for the instrument underlying the position
	 * @return
	 */
	public double getLatest() {
		return latest;
	}

	/**
	 * Get the risk for this position
	 * @return
	 */
	public Risk getPositionRisk() {
		return positionRisk;
	}

	public IGPosition getIgPosition() {
		return igPosition;
	}
}
