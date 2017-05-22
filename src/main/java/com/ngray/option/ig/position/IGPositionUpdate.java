package com.ngray.option.ig.position;

import com.google.gson.Gson;

public class IGPositionUpdate {

	private boolean guaranteedStop;
	private double stopLevel;
	private double limitLevel;
	private double trailingStep;
	private double trailingStopDistance;
	private String currency;
	private String expiry;
	private String dealIdOrigin;
	private String dealIdStatus;
	private String dealId;
	private String dealReference;
	private String direction;
	private String epic;
	private double level;
	private String status;
	private double size;
	private String channel;
	private String timestamp;
	
	public IGPositionUpdate() {
		
	}
	
	public static IGPositionUpdate fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPositionUpdate.class);
	}
	
	@Override
	public String toString() {
		return "PositionUpdate: " + getDealId() + " Status: " + getStatus();
	}

	public String getDealId() {
		return dealId;
	}

	public boolean isGuaranteedStop() {
		return guaranteedStop;
	}

	public double getStopLevel() {
		return stopLevel;
	}

	public double getLimitLevel() {
		return limitLevel;
	}

	public double getTrailingStep() {
		return trailingStep;
	}

	public double getTrailingStopDistance() {
		return trailingStopDistance;
	}

	public String getCurrency() {
		return currency;
	}

	public String getExpiry() {
		return expiry;
	}
	
	public String getDealIdOrigin() {
		return dealIdOrigin;
	}

	public String getDealIdStatus() {
		return dealIdStatus;
	}

	public String getDealReference() {
		return dealReference;
	}

	public String getDirection() {
		return direction;
	}

	public String getEpic() {
		return epic;
	}

	public double getLevel() {
		return level;
	}

	public String getStatus() {
		return status;
	}

	public double getSize() {
		return size;
	}

	public String getChannel() {
		return channel;
	}

	public String getTimestamp() {
		return timestamp;
	}

}
