package com.ngray.option.ig.position;

import com.google.gson.Gson;

public class IGPositionDetail {
	
	private double contractSize;
	private boolean controlledRisk;
	private String createdDate;
	private String currency;
	private String dealId;
	private double dealSize;
	private String direction;
	private double limitLevel;
	private double openLevel;
	private double stopLevel;
	private double trailingStep;
	private double trailingStopDistance;
	
	public IGPositionDetail() {
		
	}
	
	public static IGPositionDetail fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPositionDetail.class);	
	}

	public double getContractSize() {
		return contractSize;
	}

	public boolean isControlledRisk() {
		return controlledRisk;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public String getCurrency() {
		return currency;
	}

	public String getDealId() {
		return dealId;
	}

	public double getDealSize() {
		return dealSize;
	}

	public String getDirection() {
		return direction;
	}

	public double getLimitLevel() {
		return limitLevel;
	}

	public double getOpenLevel() {
		return openLevel;
	}

	public double getStopLevel() {
		return stopLevel;
	}

	public double getTrailingStep() {
		return trailingStep;
	}

	public double getTrailingStopDistance() {
		return trailingStopDistance;
	}
	

}
