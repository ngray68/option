package com.ngray.option.analysis.scenario;

import java.time.LocalDate;
import java.util.List;

import com.ngray.option.position.Position;

public interface Scenario {
	
	/**
	 * Evaluate the scenario and return the results in a ScenarioResult object
	 * @return
	 */
	public ScenarioResult evaluate();
	
	/**
	 * Get the name of this scenario
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the scenario definition object which defines the perturbations
	 * to be applied in this scenario
	 * @return
	 */
	public ScenarioDefinition getDefinition();
	
	/**
	 * Get the value date used to evaluate the scenario
	 * @return
	 */
	public LocalDate getValueDate();
	
	/**
	 * Get the positions whose risk will be calculated using
	 * the perturbations defined by the scenario
	 * @return
	 */
	public List<Position> getBasePositions();
	
	/**
	 * Get the scenario result object previously calculated by evaluate()
	 * @return
	 */
	public ScenarioResult getScenarioResult();

}
