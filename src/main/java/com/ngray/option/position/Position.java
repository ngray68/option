package com.ngray.option.position;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
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
	
	private double positionSize;
	
	private final double open;
	
	private double latest;
		
	private double positionPnL;
	
	private Risk positionRisk;
	
	private final IGPosition igPosition;
	
	private LocalTime timestamp;
	
	/**
	 * Construct a position object from the supplied IGPosition
	 * @param igPosition
	 */
	public Position(IGPosition igPosition) throws MissingReferenceDataException {
		this.igPosition = igPosition;
		this.id = igPosition.getPositionDetail().getDealId();
		String direction = igPosition.getPositionDetail().getDirection();
		if (direction.equals("BUY")) {
			this.positionSize = igPosition.getPositionDetail().getDealSize();
		} else {
			this.positionSize = -igPosition.getPositionDetail().getDealSize();
		}
		this.open = igPosition.getPositionDetail().getOpenLevel();
		this.latest = Double.NaN;
		this.instrument = FinancialInstrument.fromIGMarket(igPosition.getMarket());
		
		// initialize PnL and risk
		initializePnL();
		initializeRisk();
		
		this.timestamp = LocalTime.now();
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
		this.timestamp = LocalTime.now();
	}
	
	private void initializeRisk() {
		if (igPosition != null) {
			Log.getLogger().info("Position: " + getId() + " initializing risk...");
			double bid = igPosition.getMarket().getBid();
			double offer = igPosition.getMarket().getOffer();
			Map<FinancialInstrument, MarketData> map = new HashMap<>();
			map.put(instrument, new MarketData(instrument.getIdentifier(), bid, offer, Type.PRICE));
			MarketDataCollection marketDataColl = new MarketDataCollection(map);
		
			if (instrument instanceof EuropeanOption) {
				EuropeanOption option = (EuropeanOption) instrument;
				double underlyingBid = option.getUnderlying().getIGMarket().getBid();
				double underlyingOffer = option.getUnderlying().getIGMarket().getOffer();
				MarketData underlyingPrice = new MarketData(option.getUnderlying().getIdentifier(), underlyingBid, underlyingOffer, Type.PRICE);
 				map.put(option.getUnderlying(), underlyingPrice );
			}
		
			try {
				Risk riskOnOneContract = instrument.getModel().calculateRisk(instrument, marketDataColl, LocalDate.now());
				positionRisk = riskOnOneContract.multiply(getPositionSize());
				Log.getLogger().debug(positionRisk);
			} catch (ModelException e) {
				Log.getLogger().error(e.getMessage(), true);
			}		
		} 
	}
	
	private void initializePnL() {
		Log.getLogger().info("Position: " + getId() + " initializing PnL...");
		if (igPosition != null && positionSize > 0) {
			Log.getLogger().debug("Long position - Using bid price " + igPosition.getMarket().getBid());
			latest =  igPosition.getMarket().getBid();
			positionPnL = (latest - getOpen()) * getPositionSize();
			
		} else if (igPosition != null && positionSize < 0) {
			Log.getLogger().debug("Long position - Using offer price " + igPosition.getMarket().getBid());
			latest = igPosition.getMarket().getOffer();
			positionPnL = (latest - getOpen()) * getPositionSize();
		}
		Log.getLogger().debug(getPositionDetails());
	}
	
	/**
	 * Update the position risk given the risk on a single contract supplied
	 * @param riskOnPositionOfSizeOne
	 */
	public void updatePositionRisk(Risk riskOnPositionOfSizeOne) {
		Log.getLogger().info("Position: " + getId() + " updating risk...");
		positionRisk = riskOnPositionOfSizeOne.multiply(getPositionSize());
		Log.getLogger().debug(positionRisk);
		setTimestamp(LocalTime.now());
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
		setTimestamp(LocalTime.now());
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
	 * Get the financial instrument for this position
	 * @return
	 */
	public FinancialInstrument getInstrument() {
		return instrument;
	}
	
	/**
	 * Get the financial instrument which is the underlying of the position's instrument
	 * Will return the same as getInstrument for if the instrument is a delta-one product, or the underlying for
	 * derivatives
	 * @return
	 */
	public FinancialInstrument getUnderlying() {
		return instrument.getUnderlying();
	}

	/**
	 * Get the position size - short positions are negative
	 * @return
	 */
	public double getPositionSize() {
		return positionSize;
	}
	
	/**
	 * Set the position size the supplied value and scale risk and pnl accordingly
	 * @param positionSize
	 */
	public void amendPositionSize(double positionSize) {
		double prevPositionSize = this.positionSize;
		this.positionSize = positionSize;	
		positionRisk = positionRisk.multiply(Math.abs(positionSize/prevPositionSize));
		positionPnL = positionPnL * Math.abs(positionSize/prevPositionSize);
		setTimestamp(LocalTime.now());
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

	public double getUnderlyingLatest() {
		return positionRisk.getUnderlyingPrice();
	}


	public LocalTime getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(LocalTime timestamp) {
		this.timestamp = timestamp;
	}


	public Position copy() throws MissingReferenceDataException {
		if (getIgPosition() != null) {
			return new Position(getIgPosition());
		} else {
			return new Position(getId(), getInstrument(), getPositionSize(), getOpen());
		}
				
	}

	
}
