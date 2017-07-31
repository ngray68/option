package com.ngray.option.analysis.scenario;

public class ScenarioDefinition {
	
	public enum Type {
		UNDERLYING,
		IMPLIED_VOL
	};
	
	private final Type type;
	
	private final double increment;
	
	private final double baseValue;
	
	private final double range;
	
	private double[] values;
	
	/**
	 * Create a ScenarioDefinition of the given type which increments/decrements
	 * the relevant variable across the given range.
	 * @param type
	 * @param increment
	 * @param baseValue
	 * @param range
	 */
	public ScenarioDefinition(Type type, double increment, double baseValue, double range) {
		this.type = type;
		this.increment = increment;
		this.baseValue = baseValue;
		this.range = range;
	}


	public Type getType() {
		return type;
	}


	public double getIncrement() {
		return increment;
	}


	public double getRange() {
		return range;
	}
	
	/**
	 * Return the scenario values
	 * @return
	 * @throws InvalidScenarioDefinitionException
	 */
	public double[] getValues() throws InvalidScenarioDefinitionException {
		if (values != null) return values;
		
		validate();
			
		double min = baseValue - range/2.0;
		double max = baseValue + range/2.0;
		
		int numValues = (int)(range/increment) + 1;
		values = new double[numValues];
		double value = min;
		for (int i = 0; i < numValues; ++i) {
			values[i] = value;
			value+=increment;
			if (value > max) break;
		}
		
		return values;
	}

	private void validate() throws InvalidScenarioDefinitionException {
		if (range <= 0) {
			throw new InvalidScenarioDefinitionException("Scenario range must be greater than zero");
		}
		
		if (increment <= 0) {
			throw new InvalidScenarioDefinitionException("Scenario increment must be greater than zero");
		}
		
		if (increment > range/2.0) {
			throw new InvalidScenarioDefinitionException("Scenario increment must not be greater than half the range");
		}	
	}
}
