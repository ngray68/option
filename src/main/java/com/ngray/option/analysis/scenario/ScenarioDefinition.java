package com.ngray.option.analysis.scenario;

import com.ngray.option.financialinstrument.FinancialInstrument;

public class ScenarioDefinition {
	
	public enum Type {
		UNDERLYING,
		IMPLIED_VOL
	};
	
	private FinancialInstrument instrument;
	
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
	public ScenarioDefinition(FinancialInstrument instrument, Type type, double increment, double baseValue, double range) {
		this.setInstrument(instrument);
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
	 */
	public double[] getValues() {
		if (values != null) return values;
		
		double min = baseValue - range/2.0;
		double max = baseValue + range/2.0;
		
		int numValues = (int)(range/increment) + 1;
		values = new double[numValues];
		double value = min;
		for (int i = 0; i < numValues; ++i) {
			values[i] = value;
			value+=increment;
			//if (Double.compare(value, max) > 0) break;
		}
		
		return values;
	}

	public FinancialInstrument getInstrument() {
		return instrument;
	}

	public void setInstrument(FinancialInstrument instrument) {
		this.instrument = instrument;
	}
	
	public boolean validate() {
		if (instrument == null) {
			return false;
		}
		
		if (Double.isNaN(baseValue)) {
			return false;
		}
		
		if (Double.compare(range, 0.0) <= 0) {
			return false;
		}
		
		if (Double.compare(increment, 0.0) <= 0) {
			return false;
		}
		
		if (Double.compare(increment, range/2.0) > 0) {
			return false;
		}	
		
		return true;
	}

	public ScenarioDefinition copy() {
		return new ScenarioDefinition(getInstrument(), getType(), getIncrement(), getBaseValue(), getRange());
	}
}
