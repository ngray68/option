package com.ngray.option.analysis.scenario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ngray.option.Log;
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
 * Calculates the effect on risk and P&L on changes in the underlying variables
 * eg. underlying price or implied volatility
 * @author nigelgray
 *
 */
public class Scenario {
	
	/**
	 * The name of this scenario
	 */
	private final String name;
	
	/**
	 * The base case position used to calculate the scenarios
	 */
	private final Position basePosition;
	
	/**
	 * This defines the parameters of the scenario
	 */
	private final ScenarioDefinition scenarioDefinition;
	
	/**
	 * The value date for the scenario
	 */
	private final LocalDate valueDate;
	
	/**
	 * The positions that would be in play for each point on the scenario
	 */
	private List<Position> scenarioPositions;
	

	public Scenario(String name, Position position, ScenarioDefinition definition, LocalDate valueDate) {
		Log.getLogger().info("Creating scenario " + name);
		this.name= name;
		this.basePosition = position;
		this.scenarioDefinition = definition;
		this.valueDate = valueDate;
	}

	public Position getBasePosition() {
		return basePosition;
	}

	public ScenarioDefinition getScenarioDefinition() {
		return scenarioDefinition;
	}
	
	public void evaluate() throws InvalidScenarioDefinitionException {
		Log.getLogger().info("Evaluating scenario " + name);
		switch (scenarioDefinition.getType()) {
		case UNDERLYING:
			scenarioPositions = evaluateUnderlyingScenario();
			break;
		case IMPLIED_VOL:
			evaluateImpliedVolScenario();
			break;
		default:
			break;
		}
	}

	public List<Position> getScenarioPositions() {
		return scenarioPositions == null ? new ArrayList<>() : Collections.unmodifiableList(scenarioPositions);
	}
	
	@Override
	public String toString() {
		String string = "Scenario " + name + " results\n";
		try {
			double[] values = scenarioDefinition.getValues();
			string += basePosition.getInstrument().getIdentifier() + "\n";
			string += "Underlying: " + "\t";
			for (double value : values) {
				string += value + "\t";
			}
			
			string += "\n";
			string += "Delta: " + "\t";
			for (Position position : scenarioPositions) {
				string += position.getPositionRisk().getDelta() + "\t";
			}
			
			string += "\n";
			string += "PnL: " + "\t";
			for (Position position : scenarioPositions) {
				string += position.getPositionPnL() + "\t";
			}
			string += "\n";
		} catch (InvalidScenarioDefinitionException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
		
		return string;
		
	}

	private void evaluateImpliedVolScenario() {
		// TODO
	}

	private List<Position> evaluateUnderlyingScenario() throws InvalidScenarioDefinitionException {	
		Log.getLogger().info("Evaluating underlying price change scenario " + name);
		FinancialInstrument instrument = basePosition.getInstrument();
		List<MarketDataCollection> marketDataCollectionList = getMarketDataCollectionForUnderlyingScenario();
		List<Position> positionList = new ArrayList<>();
		marketDataCollectionList.forEach(
			marketDataCollection -> {
				try {
					Position position = basePosition.copy();
					Risk risk = instrument.getModel().calculateRisk(instrument, marketDataCollection, valueDate);
					position.updatePositionRisk(risk);
					position.updatePositionPnL(new MarketData(instrument.getIdentifier(), risk.getValue(), Type.PRICE));
					positionList.add(position);
				} catch (ModelException | MissingReferenceDataException e) {
					Log.getLogger().error("Error evaluating scenario " + name, e);
				}
			}
		);
		return positionList;
	}
	
	private List<MarketDataCollection> getMarketDataCollectionForUnderlyingScenario() throws InvalidScenarioDefinitionException {
		// calculate risk and option price while holding implied vol constant
		double[] underlyingPriceValues = scenarioDefinition.getValues();
		
		List<MarketDataCollection> marketDataCollectionList = new ArrayList<MarketDataCollection>();
		
		FinancialInstrument instrument = basePosition.getInstrument();
		String id = instrument.getUnderlying().getIdentifier();
		double constantImpliedVolatility = basePosition.getPositionRisk().getImpliedVolatility();	
		
		Log.getLogger().info("Using constant implied volatility: "  + constantImpliedVolatility);
		//Log.getLogger().info("Using underlying prices: "  + underlyingPriceValues);
		
		for (double underlyingPrice : underlyingPriceValues) {
			Map<FinancialInstrument, MarketData> map = new HashMap<>();
			if (instrument instanceof EuropeanOption) {
				map.put(instrument, new MarketData(instrument.getIdentifier(),constantImpliedVolatility, Type.VOLATILITY));
			}
			
			map.put(instrument.getUnderlying(), new MarketData(id, underlyingPrice, Type.PRICE));
			marketDataCollectionList.add(new MarketDataCollection(map));
		}
		
		return marketDataCollectionList;		
	}	
}
