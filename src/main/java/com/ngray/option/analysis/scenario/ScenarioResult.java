package com.ngray.option.analysis.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ngray.option.position.Position;

public class ScenarioResult {
	
	private final Map<Position, List<Position>> results;
	
	public ScenarioResult() {
		results = new HashMap<>();
	}
	
	public void addPerturbedPositions(Position base, List<Position> perturbed) {
		results.put(base, new ArrayList<>(perturbed));
	}
	
	public List<Position> getPerturbedPosition(Position base) {
		return results.get(base);
	}
	
	public Set<Position> getBasePositions() {
		return results.keySet();
	}
}
