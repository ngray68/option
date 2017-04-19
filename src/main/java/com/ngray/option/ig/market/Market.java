package com.ngray.option.ig.market;

import com.google.gson.Gson;

/**
 * The class Market encapsulates the properties of a market
 * eg. the FTSE 100 index.
 * Properties include the name, expiry, type etc of the instrument
 * last bid, offer and so on.
 * The only methods provided are getters/setters for each attribute
 * and utility methods returning arrays of attribute names and values
 * for use in, for example, a TableModel, as well as fromJson and asJson
 * methods
 * @author nigelgray
 *
 */
public final class Market {
	
	/**
	 * Names of attributes to be used in eg. a TableModel
	 */
	private static final String[] attributeNames = new String[] {
		"Name",
		"Epic",
		"Type",
		"Expiry",
		"Bid",
		"Offer",
		"High",
		"Low",
		"Net Change",
		"% Change",
		"Update Time",
		"Delay Time",
		"Market Status",
		"OTC Tradeable",
		"Lot Size",
		"Scaling Factor",
		"Streaming Prices Available"
	};
	
	
	private double bid;		// The market's latest bid and offer prices
	private double offer;
	
	private int delayTime; 	// The price delay time in minutes
	private String epic;	// The instrument's "epic" identifier
	private String expiry;	// The instrument's expiry period
	
	private double high;	// Intra-day highest price
	private double low;		// Intra-day lowest price
	
	private String instrumentName; 
	
	private String instrumentType;
	private int lotSize;
	private String marketStatus;
	
	private double netChange;	// price net change
	
	private boolean otcTradeable;
	
	private double percentageChange; // price % change on the day
	
	private double scalingFactor;	
	
	private boolean streamingPricesAvailable;
	
	private String updateTime;
	private String updateTimeUTC;
	
	/**
	 * Empty constructor provided for use by Gson
	 */
	public Market() {
		
	}
	
	/**
	 * Construct a Market object from the Json string
	 * @param json
	 * @return
	 */
	public static Market fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Market.class);	
	}
	
	/**
	 * Return a Json string representing this object
	 * @return
	 */
	public String asJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	// Getter and Setter methods for all attributes
	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getOffer() {
		return offer;
	}

	public void setOffer(double offer) {
		this.offer = offer;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public String getEpic() {
		return epic;
	}

	public void setEpic(String epic) {
		this.epic = epic;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public String getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(String instrumentType) {
		this.instrumentType = instrumentType;
	}

	public int getLotSize() {
		return lotSize;
	}

	public void setLotSize(int lotSize) {
		this.lotSize = lotSize;
	}

	public String getMarketStatus() {
		return marketStatus;
	}

	public void setMarketStatus(String marketStatus) {
		this.marketStatus = marketStatus;
	}

	public double getNetChange() {
		return netChange;
	}

	public void setNetChange(double netChange) {
		this.netChange = netChange;
	}

	public String getUpdateTimeUTC() {
		return updateTimeUTC;
	}

	public void setUpdateTimeUTC(String updateTimeUTC) {
		this.updateTimeUTC = updateTimeUTC;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isStreamingPricesAvailable() {
		return streamingPricesAvailable;
	}

	public void setStreamingPricesAvailable(boolean streamingPricesAvailable) {
		this.streamingPricesAvailable = streamingPricesAvailable;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public double getPercentageChange() {
		return percentageChange;
	}

	public void setPercentageChange(double percentageChange) {
		this.percentageChange = percentageChange;
	}

	public boolean isOtcTradeable() {
		return otcTradeable;
	}

	public void setOtcTradeable(boolean otcTradeable) {
		this.otcTradeable = otcTradeable;
	}

	/**
	 * Return a string array of the attribute names
	 * @return
	 */
	public static String[] getAttributeNames() {
		return attributeNames;
	}

	/**
	 * Return this Market's attribute values as an Object array
	 * @return
	 */
	public Object[] getAttributeValues() {
		Object[] data = new Object[] {
				getInstrumentName(),
				getEpic(),
				getInstrumentType(),
				getExpiry(),
				getBid(),
				getOffer(),
				getHigh(),
				getLow(),
				getNetChange(),
				getPercentageChange(),
				getUpdateTimeUTC(),
				getDelayTime(),
				getMarketStatus(),
				isOtcTradeable(),
				getLotSize(),
				getScalingFactor(),
				isStreamingPricesAvailable()
				
		};
		return data;
	}
}
