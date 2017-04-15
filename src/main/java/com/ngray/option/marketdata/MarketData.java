package com.ngray.option.marketdata;

public class MarketData {
	
	public enum Type {
		PRICE,
		VOLATILITY
	};
	
	private final double value;
	private final Type   type;
	
	public MarketData(double value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "MarketData: Value=" + value + "\tType=" + type;
	}

	public double getValue() {
		return value;
	}

	public Type getType() {
		return type;
	}
}
