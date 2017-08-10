package com.ngray.option.ig.price;

import com.google.gson.Gson;

/**
 * Class representing a price - includes bid, ask and last traded
 * @author nigelgray
 *
 */
public final class IGPrice {

	private double bid;
	private double ask;
	private double lastTraded;
	
	public IGPrice() {
		bid = Double.NaN;
		ask = Double.NaN;
		lastTraded = Double.NaN;
	}
	
	public IGPrice(double bid, double ask, double lastTraded) {
		this.bid = bid;
		this.ask = ask;
		this.lastTraded = lastTraded;
	}
	
	public static IGPrice fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPrice.class);
	}
		
	public String asJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public double getBid() {
		return bid;
	}
	
	public double getAsk() {
		return ask;
	}
	
	public double getLastTraded() {
		return lastTraded;
	}
	
	public double getMid() {
		return (ask + bid)/2.0;
	}
}
