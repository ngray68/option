package com.ngray.option.position;

import com.ngray.option.Log;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.position.IGPosition;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.risk.Risk;
import com.ngray.option.risk.RiskListener;
import com.ngray.option.risk.RiskService;

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
	
	private double positionPnL;
	
	private Risk positionRisk;
	
	private RiskListener riskListener = null;
	
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
		this.instrument = FinancialInstrument.fromIGMarket(igPosition.getMarket());
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
		
		// initialize PnL and risk to NaN
		this.positionPnL = Double.NaN;
		this.positionRisk = new Risk();
	}
	
	/**
	 * Update the position risk given the risk on a single contract supplied
	 * @param riskOnPositionOfSizeOne
	 */
	public void updatePositionRiskAndPnL(Risk riskOnPositionOfSizeOne) {
		Log.getLogger().info("Position: " + getId() + " updating risk");
		Log.getLogger().debug("Previous risk:\n" + positionRisk);
		Log.getLogger().debug("Previous PnL: " + positionPnL);
		positionRisk = riskOnPositionOfSizeOne.multiply(getPositionSize());
		positionPnL = positionRisk.getValue() - getOpen() * getPositionSize();
		Log.getLogger().debug("New risk:\n" + positionRisk);
		Log.getLogger().debug("New PnL:" + positionPnL);
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
	 * Get the risk for this position
	 * @return
	 */
	public Risk getPositionRisk() {
		return positionRisk;
	}
	
	/**
	 * Subscribe to the supplied risk service to receive risk updates for this position
	 * @param riskService
	 */
	public void subscribeToRiskService(RiskService riskService) {
		Log.getLogger().info("Position: " + getId() + " subcribing to RiskService " + riskService.getName());
		riskListener = 
			riskService.addRiskListener(getInstrument(), new RiskListener() {

				@Override
				public void onRiskUpdate(FinancialInstrument instrument, Risk risk) {
					if (!instrument.equals(getInstrument())) {
						Log.getLogger().warn("RiskListener::onRiskUpdate called with instrument: " + 
					                          instrument + ", expected: " + getInstrument() + ", update ignored");
						return;
					}
					
					updatePositionRiskAndPnL(risk);
				}	
			});
		
	}

	/**
	 * Unsubscribe from the supplied risk service - the position will no longer receive risk updates
	 * @param riskService
	 */
	public void unsubscribeFromRiskService(RiskService riskService) {
		Log.getLogger().info("Position: " + getId() + " unsubcribing from RiskService " + riskService.getName());
		riskService.removeRiskListener(getInstrument(), riskListener);
	}
 }
