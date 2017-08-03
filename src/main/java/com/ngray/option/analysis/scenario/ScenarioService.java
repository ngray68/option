package com.ngray.option.analysis.scenario;

import com.ngray.option.service.Service;
import com.ngray.option.service.ServiceDataSource;

public class ScenarioService extends Service<String, Scenario> {

	/**
	 * Create a ScenarioService object
	 * @param name
	 * @param dataSource
	 */
	public ScenarioService(String name, ServiceDataSource<String, Scenario> dataSource) {
		super(name, dataSource);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Add the scenario to the service, with the given key
	 * @param key
	 * @param scenario
	 */
	public void addScenario(String key, Scenario scenario) {
		publishData(key, scenario);
	}
	
	/**
	 * Remove the scenario from the service and remove any listeners for it
	 * @param key
	 */
	public void removeScenario(String key) {
		unpublishData(key);
	}
}
