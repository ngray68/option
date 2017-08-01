package com.ngray.option.analysis.scenario;

public class ScenarioDefinition {
	
	public enum Type {
		UNDERLYING,
		IMPLIED_VOL
	};
	
	private Type type;
	
	private double increment;
	
	private double baseValue;
	
	private double range;
	
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
		this.setType(type);
		this.setIncrement(increment);
		this.setBaseValue(baseValue);
		this.setRange(range);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public double getIncrement() {
		return increment;
	}

	public void setIncrement(double increment) {
		this.increment = increment;
	}

	public double getBaseValue() {
		return baseValue;
	}

	public void setBaseValue(double baseValue) {
		this.baseValue = baseValue;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
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
		if (Double.compare(range, 0.0) <= 0) {
			throw new InvalidScenarioDefinitionException("Scenario range must be greater than zero");
		}
		
		if (Double.compare(increment, 0.0) <= 0) {
			throw new InvalidScenarioDefinitionException("Scenario increment must be greater than zero");
		}
		
		if (Double.compare(increment, range/2.0) > 0) {
			throw new InvalidScenarioDefinitionException("Scenario increment must not be greater than half the range");
		}	
	}
}
