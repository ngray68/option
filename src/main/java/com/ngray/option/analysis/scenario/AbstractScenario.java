package com.ngray.option.analysis.scenario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.position.Position;

public abstract class AbstractScenario implements Scenario {

	/**
	 * The name of this scenario
	 */
	protected final String name;
	
	/**
	 * The base positions used to calculate the scenarios
	 */
	protected final List<Position> basePositions;
	
	/**
	 * This defines the parameters of the scenario
	 */
	protected final ScenarioDefinition scenarioDefinition;
	
	/**
	 * The value date for the scenario
	 */
	protected final LocalDate valueDate;
	
	/**
	 * The result of evaluating the scenario
	 */
	protected ScenarioResult scenarioResult;
	
	/**
	 * synchronize access to the modifiable elements of the Scenario - updates to positions might come from different threads
	 * than risk/pnl updates
	 */
	protected Object lock = new Object();
	
	public AbstractScenario(ScenarioDefinition definition, LocalDate valueDate) {
		
		if (definition.getInstrument().getIGMarket() != null) {
			this.name = definition.getInstrument().getName() + " " +
					    definition.getInstrument().getIGMarket().getExpiry() + " " + 
					    definition.getType() + " " +
					    definition.getBaseValue() + "-" + definition.getIncrement() + "-" + definition.getRange();
		} else {
			this.name = definition.getInstrument().getIdentifier() + " " + 
						definition.getType() + " " +
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
		synchronized(lock) {
			return Collections.unmodifiableList(new ArrayList<>(basePositions));
		}
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
	public void addBasePosition(Position position) {
		synchronized(lock) {
			if (basePositions.contains(position)) return;
			basePositions.add(position);
		}
	}

	@Override
	public void removeBasePosition(Position position) {
		synchronized(lock) {
			basePositions.remove(position);
		}
	}

	@Override
	public ScenarioResult getScenarioResult() {
		synchronized(lock) {
			return scenarioResult;
		}
	}

	protected double getSpread(Position position) {
		// if the position is long, we take off the spread when calculating values,, if short, we add the spread
		double spread = position.getIgPosition().getMarket().getOffer() - position.getIgPosition().getMarket().getBid();
		return position.getPositionSize() >= 0 ? -spread : spread;
	}
}
