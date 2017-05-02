package com.ngray.option.ig.market;

import com.google.gson.Gson;
import com.ngray.option.ig.Session;
import com.ngray.option.ig.SessionException;
import com.ngray.option.ig.rest.RestAPIGet;
import com.ngray.option.ig.rest.RestAPIResponse;

/**
 * The class Market encapsulates the properties of a market
 * eg. the FTSE 100 index.
 * Properties include the name, expiry, type etc of the instrument
 * last bid, offer and so on.
 * The only methods provided are getters for each attribute
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

	// Getter methods for all attributes
	public double getBid() {
		return bid;
	}

	public double getOffer() {
		return offer;
	}
	
	public int getDelayTime() {
		return delayTime;
	}

	public String getEpic() {
		return epic;
	}

	public String getExpiry() {
		return expiry;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public String getInstrumentType() {
		return instrumentType;
	}

	public int getLotSize() {
		return lotSize;
	}

	public String getMarketStatus() {
		return marketStatus;
	}

	public double getNetChange() {
		return netChange;
	}

	public String getUpdateTimeUTC() {
		return updateTimeUTC;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public boolean isStreamingPricesAvailable() {
		return streamingPricesAvailable;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public double getPercentageChange() {
		return percentageChange;
	}

	public boolean isOtcTradeable() {
		return otcTradeable;
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
