package com.ngray.option.ig.position;

import com.google.gson.Gson;
import com.ngray.option.ig.market.Market;

public class IGPosition {
	
	private Market market;
	
	private IGPositionDetail position;
	
	public IGPosition() {
		
	}
	
	public static IGPosition fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPosition.class);	
	}
	
	public Market getMarket() {
		return market;
	}
	
	public IGPositionDetail getPositionDetail() {
		return position;
	}

}
