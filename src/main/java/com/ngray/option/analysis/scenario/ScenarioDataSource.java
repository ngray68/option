package com.ngray.option.analysis.scenario;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ngray.option.Log;
import com.ngray.option.RiskEngine;
import com.ngray.option.position.Position;
import com.ngray.option.position.PositionListener;
import com.ngray.option.position.PositionService;
import com.ngray.option.service.ServiceDataPublisher;
import com.ngray.option.service.ServiceDataSource;
import com.ngray.option.service.ServiceException;

/**
 * This class drives live updates to scenarios arising from changes in the scenario's
 * base positions.
 * @author nigelgray
 *
 */
public class ScenarioDataSource implements ServiceDataSource<String, Scenario> {

	private final String name;
	private final PositionService positionService;
	private final Map<String, PositionListener> subscriptions;

	public ScenarioDataSource(String name, PositionService positionService) {
		this.name = name;
		this.positionService = positionService;
		this.subscriptions = new ConcurrentHashMap<>();
	}

	@Override
	public void addSubscription(String key, ServiceDataPublisher<String, Scenario> publisher) {
		Log.getLogger().info("ScenarioDataSource: add subscription for " + key);
		try {
			if (subscriptions.containsKey(key)) {
				Log.getLogger().info("Already subscribed to data source for scenario " + key  + ": ignoring subscription request");
				return;
			}
			
			Scenario scenario = getScenarioService().getData(key);
			positionService.addListener(scenario.getDefinition().getInstrument(), new PositionListener() {

				@Override
				public void onPositionRiskUpdate(Position position) {
					scenario.evaluate();
					publisher.publish(key, scenario);
				}

				@Override
				public void onPositionPnLUpdate(Position position) {
					// not required
				}

				@Override
				public void onOpenPosition(Position position) {
					scenario.addBasePosition(position);
					scenario.evaluate();
					publisher.publish(key, scenario);
				}

				@Override
				public void onDeletePosition(Position position) {
					scenario.removeBasePosition(position);
					scenario.evaluate();
					publisher.publish(key, scenario);
					
				}

				@Override
				public void onUpdatePosition(Position position) {
					scenario.evaluate();
					publisher.publish(key, scenario);
				}
			});
		} catch (ServiceException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
		
	}

	@Override
	public void removeSubscription(String key) {
		Log.getLogger().info("ServiceDataSource" + name + ": removing subscription for scenario " + key);
		PositionListener listener = subscriptions.remove(key);
		if (listener != null) {
			removePositionListener(key, listener);
		}
	}

	@Override
	public void start() {
		// do nothing
	}

	@Override
	public void shutdown() {
		Log.getLogger().info("ServiceDataSource" + name + ": shutdown...");
		subscriptions.forEach((key, subscription) -> removePositionListener(key, subscription));
		subscriptions.clear();
	}
	
	public ScenarioService getScenarioService() {
		return RiskEngine.getScenarioService();
	}

	public String getName() {
		return name;
	}
	
	private void removePositionListener(String key, PositionListener listener) {
		try {
			Scenario scenario = getScenarioService().getData(key);
			positionService.removeListener(scenario.getDefinition().getInstrument(), listener);
		} catch (ServiceException e) {
			Log.getLogger().error(e.getMessage(), e);
		}
	}
}
