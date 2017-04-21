package com.ngray.option.marketdata;

public class MarketData {
	
	public enum Type {
		PRICE,
		VOLATILITY
	};
	
	private final String identifier;
	private final double mid;
	private final double bid;
	private final double offer;
	private final Type   type;
	
	/**
	 * Construct a simple single price - no bid or offer
	 * @param id
	 * @param mid
	 * @param type
	 */
	public MarketData(String id, double mid, Type type) {
		this.identifier = id;
		this.mid = mid;
		this.bid = Double.NaN;
		this.offer = Double.NaN;
		this.type = type;
	}
	
	/**
	 * Construct a price from separate bid and offers
	 * @param id
	 * @param bid
	 * @param offer
	 * @param type
	 */
	public MarketData(String id, double bid, double offer, Type type) {
		this.identifier = id;
		this.mid = (bid + offer)/2;
		this.bid = bid;
		this.offer = offer;
		this.type = type;
	}
	
	@Override
	public String toString() {
		String result = "\n\nMarketData: " + getIdentifier();
		result+="\n===========================================";
		result+="\nBid:\t" + getBid();
		result+="\nOffer:\t" + getOffer();
		result+="\nMid:\t" + getMid();
		result+="\nType:\t" + getType();
		result+="\n===========================================\n\n";
		return result;
	}

	/**
	 * Get the identifier for this piece of market data
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Get the mid-price (bid + offer)/2
	 * @return
	 */
	public double getMid() {
		return mid;
	}
	
	/**
	 * Get the bid price
	 * @return
	 */
	public double getBid() {
		return bid;
	}

	/**
	 * Get the offer price
	 * @return
	 */
	public double getOffer() {
		return offer;
	}
	
	/**
	 * Get the type (price or vol)
	 * @return
	 */
	public Type getType() {
		return type;
	}
}
