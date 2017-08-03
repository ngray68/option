package com.ngray.option.analysis.scenario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.financialinstrument.EuropeanOption;
import com.ngray.option.financialinstrument.FinancialInstrument;
import com.ngray.option.ig.refdata.MissingReferenceDataException;
import com.ngray.option.marketdata.MarketData;
import com.ngray.option.marketdata.MarketDataCollection;
import com.ngray.option.model.ModelException;
import com.ngray.option.marketdata.MarketData.Type;
import com.ngray.option.position.Position;
import com.ngray.option.risk.Risk;

/**
 * Calculates the effect on risk and P&L of changes in the underlying price
 *for all live positions on that underlying
 * @author nigelgray
 *
 */
public class UnderlyingPriceScenario implements Scenario {
	
	/**
	 * The name of this scenario
	 */
	private final String name;
	
	/**
	 * The base positions used to calculate the scenarios
	 */
	private final List<Position> basePositions;
	
	/**
	 * This defines the parameters of the scenario
	 */
	private final ScenarioDefinition scenarioDefinition;
	
	/**
	 * The value date for the scenario
	 */
	private final LocalDate valueDate;
	
	/**
	 * The result of evaluating the scenario
	 */
	private ScenarioResult scenarioResult;
	

	public UnderlyingPriceScenario(ScenarioDefinition definition, LocalDate valueDate) {
		
		if (definition.getInstrument().getIGMarket() != null) {
			this.name = definition.getInstrument().getName() + " " +
					    definition.getInstrument().getIGMarket().getExpiry() + " " +
					    definition.getBaseValue() + "-" + definition.getIncrement() + "-" + definition.getRange();
		} else {
			this.name = definition.getInstrument().getIdentifier() + " " + 
					    definition.getBaseValue() + "-" + definition.getIncrement() + "-" + definition.getRange();
		}
		Log.getLogger().info("Creating scenario " + name);
		this.scenarioDefinition = definition;
		this.basePositions = new ArrayList<>(RiskEngine.getPositionService().getPositions(scenarioDefinition.getInstrument()));
		this.valueDate = valueDate;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public List<Position> getBasePositions() {
		return Collections.unmodifiableList(basePositions);
	}

	@Override
	public ScenarioDefinition getDefinition() {
		return scenarioDefinition;
	}
	
	@Override
	public LocalDate getValueDate() {
		return valueDate;
	}
	
	@Override
	public ScenarioResult evaluate() {
		scenarioResult = new ScenarioResult();
		basePositions.forEach(
				basePosition -> scenarioResult.addPerturbedPositions(basePosition, getPerturbedPositions(basePosition))
			);
		
		return scenarioResult;
	}

	@Override
	public ScenarioResult getScenarioResult() {
		return scenarioResult;
	}
	
	private List<Position> getPerturbedPositions(Position basePosition) {
		List<MarketDataCollection> perturbedMarketData = getPerturbedMarketData(basePosition);
		List<Position> perturbedPositions = new ArrayList<>();
		perturbedMarketData.forEach(
			 (marketDataCollection) -> {
				 try {
					Position position = basePosition.copy();
					FinancialInstrument instrument = position.getInstrument();
					Risk risk = instrument.getModel().calculateRisk(instrument, marketDataCollection, valueDate);
					position.updatePositionRisk(risk);
					double spreadAdjustedValue = risk.getValue() + getSpread(position)/2.0;
					position.updatePositionPnL(new MarketData(instrument.getIdentifier(), spreadAdjustedValue, Type.PRICE));
					perturbedPositions.add(position);
				} catch (MissingReferenceDataException | ModelException e) {
					Log.getLogger().error(e.getMessage(), e);
				}			 
			 } 	
			);
		return perturbedPositions;
	}
	
	private List<MarketDataCollection> getPerturbedMarketData(Position basePosition) {
		List<MarketDataCollection> marketDataCollectionList = new ArrayList<>();
		
		double constantImpliedVolatility = basePosition.getPositionRisk().getImpliedVolatility();
		double[] underlyingPrices = scenarioDefinition.getValues();
		for (double underlyingPrice : underlyingPrices) {
			Map<FinancialInstrument, MarketData> map = new HashMap<>();
			FinancialInstrument instrument = scenarioDefinition.getInstrument();
			map.put(instrument, new MarketData(instrument.getIdentifier(), underlyingPrice, Type.PRICE));
			if (basePosition.getInstrument() instanceof EuropeanOption) {
				map.put(basePosition.getInstrument(), new MarketData(basePosition.getInstrument().getIdentifier(), constantImpliedVolatility, Type.VOLATILITY));
			}
			marketDataCollectionList.add(new MarketDataCollection(map));
		}
		
		return marketDataCollectionList;	
	}
	
	private double getSpread(Position position) {
		// if the position is long, we take off the spread when calculating values,, if short, we add the spread
		double spread = position.getIgPosition().getMarket().getOffer() - position.getIgPosition().getMarket().getBid();
		return position.getPositionSize() >= 0 ? -spread : spread;
	}
}
