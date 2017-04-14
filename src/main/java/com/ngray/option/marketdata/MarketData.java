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

	public double getValue() {
		return value;
	}

	public Type getType() {
		return type;
	}
}
