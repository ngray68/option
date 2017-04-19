package com.ngray.option.ig.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;

public class IGPositionList {

	private List<IGPosition> positions;
	
	public IGPositionList() {
		positions = new ArrayList<>();
	}
	
	public static IGPositionList fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, IGPositionList.class);	
	}
	
	public List<IGPosition> getPositions() {
		return Collections.unmodifiableList(positions);
	}

}
